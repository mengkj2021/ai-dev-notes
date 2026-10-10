@file:OptIn(ExperimentalComposeUiApi::class)

package com.pictureorganizer.ui.importimages

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragData
import androidx.compose.ui.draganddrop.dragData
import androidx.compose.ui.unit.dp
import com.pictureorganizer.data.repository.ImageInsert
import com.pictureorganizer.data.repository.ImageRepository
import com.pictureorganizer.data.repository.TagRepository
import com.pictureorganizer.data.repository.UserPreferencesRepository
import com.pictureorganizer.model.ImageListItem
import com.pictureorganizer.model.ImageStatus
import com.pictureorganizer.util.file.AppFileManager
import com.pictureorganizer.util.file.DesktopDialogs
import com.pictureorganizer.util.image.ImageCompressor
import com.pictureorganizer.util.image.ImageTagMetadata
import com.pictureorganizer.util.log.AppLog
import java.awt.Window
import java.io.File
import java.net.URI
import java.util.UUID
import kotlin.coroutines.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

private val IMAGE_EXT = setOf("jpg", "jpeg", "png", "webp", "gif", "bmp")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun ImportScreen(
    images: ImageRepository,
    tags: TagRepository,
    prefs: UserPreferencesRepository,
    files: AppFileManager,
    awtWindow: Window,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var busy by remember { mutableStateOf(false) }
    var logs by remember { mutableStateOf(listOf<String>()) }
    var pendingDup by remember { mutableStateOf<DupAsk?>(null) }
    var dragOver by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun startImport(selected: List<File>) {
        if (selected.isEmpty() || busy) return
        scope.launch {
            busy = true
            val ask = prefs.isImportDuplicateAsk()
            val compress = prefs.isCompressOnImport()
            val defaultTags = prefs.getDefaultTagNames().toList()
            val knownOriginals = images.getStoredOriginalNames().toMutableSet()
            val lines = mutableListOf<String>()
            val pendingInserts = mutableListOf<ImageInsert>()
            val tagsToEnsure = linkedSetOf<String>()
            var ok = 0

            for (source in selected) {
                val name = source.name
                try {
                    if (!source.isFile) {
                        lines += "跳过（不是文件）: $name"
                        continue
                    }
                    if (source.extension.lowercase() !in IMAGE_EXT) {
                        lines += "跳过（非图片）: $name"
                        continue
                    }
                    if (ask && name in knownOriginals) {
                        val decision =
                            suspendCancellableCoroutine { cont ->
                                pendingDup =
                                    DupAsk(name) { action ->
                                        pendingDup = null
                                        cont.resume(action)
                                    }
                            }
                        if (decision == DupAction.Skip) {
                            lines += "跳过重名: $name"
                            continue
                        }
                    }
                    val exifTags =
                        withContext(Dispatchers.IO) { ImageTagMetadata.readUserTags(source) }
                    val dateTaken =
                        withContext(Dispatchers.IO) { ImageTagMetadata.readDateTakenMillis(source) }
                    val prepared =
                        withContext(Dispatchers.IO) {
                            ImageCompressor.prepareForImport(
                                source = source,
                                destDir = files.dirFor(ImageStatus.Pending),
                                preferredName = name.replace(Regex("""[\\/:*?"<>|]"""), "_"),
                                compressEnabled = compress,
                            )
                        }
                    val now = System.currentTimeMillis()
                    val id = UUID.randomUUID().toString()
                    val relative = files.relativePath(ImageStatus.Pending, prepared.fileName)
                    val mergedTags = ImageTagMetadata.mergeImportTags(defaultTags, exifTags)
                    pendingInserts +=
                        ImageInsert(
                            item =
                                ImageListItem(
                                    id = id,
                                    description = prepared.fileName.substringBeforeLast('.').ifBlank { prepared.fileName },
                                    status = ImageStatus.Pending,
                                    filePath = relative,
                                    importedAt = now,
                                    originalName = name,
                                    tags = mergedTags,
                                    dateTakenMillis = dateTaken,
                                ),
                            filePath = relative,
                            fileName = prepared.fileName,
                            importedAt = now,
                        )
                    withContext(Dispatchers.IO) {
                        ImageTagMetadata.writeImportMetadata(
                            file = prepared.destFile,
                            tags = mergedTags,
                            dateTakenMillis = dateTaken,
                        )
                    }
                    tagsToEnsure += mergedTags
                    knownOriginals += name
                    ok++
                    lines +=
                        buildString {
                            append("已导入: $name → ${prepared.fileName}")
                            if (prepared.compressed) append("（已压缩）")
                            if (exifTags.isNotEmpty()) append(" Exif标签=${exifTags.joinToString()}")
                            if (dateTaken != null) append(" 拍摄日已读")
                            if (mergedTags.isNotEmpty()) append(" → ${mergedTags.joinToString()}")
                        }
                } catch (t: Throwable) {
                    AppLog.e("Import", "fail $name", t)
                    lines += "失败: $name (${t.message})"
                }
            }

            if (pendingInserts.isNotEmpty()) {
                images.insertAll(pendingInserts)
            }
            if (tagsToEnsure.isNotEmpty()) {
                tags.ensureTagNames(tagsToEnsure)
            }
            lines += "完成：成功 $ok / ${selected.size}"
            logs = lines
            busy = false
        }
    }

    val busyNow by rememberUpdatedState(busy)
    val onDropFiles by rememberUpdatedState { dropped: List<File> -> startImport(dropped) }
    val dropTarget =
        remember {
            object : DragAndDropTarget {
                override fun onStarted(event: DragAndDropEvent) {
                    dragOver = true
                }

                override fun onEntered(event: DragAndDropEvent) {
                    dragOver = true
                }

                override fun onExited(event: DragAndDropEvent) {
                    dragOver = false
                }

                override fun onEnded(event: DragAndDropEvent) {
                    dragOver = false
                }

                override fun onDrop(event: DragAndDropEvent): Boolean {
                    dragOver = false
                    if (busyNow) return false
                    val dropped = filesFromDragData(event.dragData())
                    if (dropped.isEmpty()) return false
                    onDropFiles(dropped)
                    return true
                }
            }
        }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("导入图片") },
                navigationIcon = {
                    IconButton(onClick = onBack, enabled = !busy) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .widthIn(max = 720.dp)
                    .dragAndDropTarget(
                        shouldStartDragAndDrop = { true },
                        target = dropTarget,
                    ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Text("从本机选择图片，或把文件拖入下方区域。复制到待归档（可压缩、合并默认标签、重名询问）。")
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp)
                        .border(
                            BorderStroke(
                                1.dp,
                                if (dragOver) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outline
                                },
                            ),
                            shape = MaterialTheme.shapes.medium,
                        )
                        .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    if (dragOver) "松开以导入…" else "拖放图片到此处",
                    style = MaterialTheme.typography.titleMedium,
                )
                Button(
                    onClick = {
                        val picked = DesktopDialogs.pickImageFiles(awtWindow)
                        startImport(picked)
                    },
                    enabled = !busy,
                ) {
                    Text(if (busy) "导入中…" else "选择文件…")
                }
            }
            if (busy) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(logs) { line -> Text(line) }
            }
        }
    }

    pendingDup?.let { ask ->
        AlertDialog(
            onDismissRequest = { ask.respond(DupAction.Skip) },
            title = { Text("发现同名来源") },
            text = { Text("已有 originalName「${ask.name}」。跳过或仍导入为新文件？") },
            confirmButton = {
                TextButton(onClick = { ask.respond(DupAction.ImportAnyway) }) { Text("仍导入") }
            },
            dismissButton = {
                TextButton(onClick = { ask.respond(DupAction.Skip) }) { Text("跳过") }
            },
        )
    }
}

private fun filesFromDragData(data: DragData): List<File> {
    val list = data as? DragData.FilesList ?: return emptyList()
    return list.readFiles().mapNotNull { uriString ->
        runCatching {
            when {
                uriString.startsWith("file:") -> File(URI(uriString))
                else -> File(uriString)
            }.takeIf { it.isFile }
        }.getOrNull()
    }
}

private enum class DupAction { Skip, ImportAnyway }

private data class DupAsk(
    val name: String,
    val respond: (DupAction) -> Unit,
)
