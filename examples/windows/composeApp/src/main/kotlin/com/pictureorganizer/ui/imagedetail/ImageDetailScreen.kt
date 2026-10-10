package com.pictureorganizer.ui.imagedetail

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.pictureorganizer.data.repository.ImageRepository
import com.pictureorganizer.data.repository.RenameTemplateRepository
import com.pictureorganizer.data.repository.TagRepository
import com.pictureorganizer.model.ImageStatus
import com.pictureorganizer.model.tagsFromTagTemplateOverwrite
import com.pictureorganizer.util.file.AppFileManager
import com.pictureorganizer.util.file.DesktopDialogs
import com.pictureorganizer.util.file.RenamePatternApplier
import com.pictureorganizer.util.image.ExifDateTaken
import com.pictureorganizer.util.image.ThumbnailCache
import com.pictureorganizer.util.log.AppLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ImageDetailScreen(
    imageId: String,
    images: ImageRepository,
    tags: TagRepository,
    renameTemplates: RenameTemplateRepository,
    files: AppFileManager,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val item by images.observeItem(imageId).collectAsState(initial = null)
    val tagLibrary by tags.observeTags().collectAsState(initial = emptyList())
    val tagTemplates by tags.observeTemplates().collectAsState(initial = emptyList())
    val renameList by renameTemplates.observeAll().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var renameDraft by remember(item?.filePath) {
        mutableStateOf(item?.filePath?.substringAfterLast('/').orEmpty())
    }
    var selectedTags by remember(item?.id, item?.tags) { mutableStateOf(item?.tags?.toSet().orEmpty()) }
    var message by remember { mutableStateOf<String?>(null) }
    var confirmDelete by remember { mutableStateOf(false) }
    val file = remember(item?.filePath) { item?.filePath?.let { files.absoluteFile(it) } }
    var preview by remember(file?.absolutePath, file?.lastModified(), file?.length()) {
        mutableStateOf<ImageBitmap?>(null)
    }
    LaunchedEffect(file?.absolutePath, file?.lastModified(), file?.length()) {
        val f = file
        preview =
            if (f == null) {
                null
            } else {
                withContext(Dispatchers.IO) {
                    runCatching { ThumbnailCache.decodeDownscaled(f, maxEdgePx = 1600) }.getOrNull()
                }
            }
    }
    val scroll = rememberScrollState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(item?.description ?: "详情") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (file != null) {
                        TextButton(onClick = { DesktopDialogs.revealInExplorer(file) }) {
                            Text("在资源管理器中显示")
                        }
                    }
                },
            )
        },
    ) { padding ->
        val current = item
        if (current == null) {
            Text("图片不存在或已删除", modifier = Modifier.padding(padding).padding(16.dp))
            return@Scaffold
        }

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .widthIn(max = 960.dp)
                    .verticalScroll(scroll),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (preview != null) {
                Image(
                    painter = BitmapPainter(preview!!),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().heightIn(max = 560.dp),
                    contentScale = ContentScale.Fit,
                )
            } else {
                Text("无法预览：${file?.absolutePath.orEmpty()}")
            }

            Text("状态：${current.status.displayName()}")
            Text("路径：${current.filePath}")
            current.dateTakenMillis?.let {
                Text("拍摄日：${ExifDateTaken.formatForDisplay(it)}")
            }

            OutlinedTextField(
                value = renameDraft,
                onValueChange = { renameDraft = it },
                label = { Text("文件名（含扩展名）") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Button(
                onClick = {
                    scope.launch {
                        try {
                            images.rename(current.id, renameDraft.trim())
                            message = "已重命名"
                        } catch (t: Throwable) {
                            AppLog.e("Detail", "rename", t)
                            message = t.message ?: "重命名失败"
                        }
                    }
                },
            ) { Text("保存文件名") }

            Text("套用重命名模板")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                renameList.forEach { template ->
                    AssistChip(
                        onClick = {
                            val currentName = current.filePath.substringAfterLast('/')
                            val next =
                                RenamePatternApplier.applyForItem(
                                    pattern = template.pattern,
                                    item = current,
                                    currentFileName = currentName,
                                )
                            renameDraft = next
                            scope.launch {
                                runCatching { images.rename(current.id, next) }
                                    .onSuccess { message = "已套用 ${template.name}" }
                                    .onFailure { message = it.message }
                            }
                        },
                        label = {
                            Text(if (template.isDefault) "${template.name}★" else template.name)
                        },
                    )
                }
            }

            Text("标签（点选库内标签）")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                tagLibrary.forEach { tag ->
                    FilterChip(
                        selected = tag.name in selectedTags,
                        onClick = {
                            selectedTags =
                                if (tag.name in selectedTags) selectedTags - tag.name else selectedTags + tag.name
                        },
                        label = { Text(tag.name) },
                    )
                }
            }
            Text("标签模板（覆盖）")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                tagTemplates.forEach { template ->
                    AssistChip(
                        onClick = {
                            selectedTags = tagsFromTagTemplateOverwrite(template.tagNames).toSet()
                        },
                        label = { Text(template.name) },
                    )
                }
            }
            Button(
                onClick = {
                    scope.launch {
                        images.updateTags(current.id, selectedTags.toList())
                        tags.ensureTagNames(selectedTags)
                        message = "已更新标签"
                    }
                },
            ) { Text("保存标签") }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                when (current.status) {
                    ImageStatus.Pending -> {
                        TextButton(
                            onClick = {
                                scope.launch {
                                    images.moveItems(setOf(current.id), ImageStatus.Pending, ImageStatus.Confirmed)
                                }
                            },
                        ) { Text("移到已归档") }
                        TextButton(
                            onClick = {
                                scope.launch {
                                    images.moveItems(setOf(current.id), ImageStatus.Pending, ImageStatus.NoModify)
                                }
                            },
                        ) { Text("移到回收站") }
                    }
                    ImageStatus.Confirmed -> {
                        TextButton(
                            onClick = {
                                scope.launch {
                                    images.moveItems(setOf(current.id), ImageStatus.Confirmed, ImageStatus.Pending)
                                }
                            },
                        ) { Text("移回待归档") }
                        TextButton(
                            onClick = {
                                scope.launch {
                                    images.moveItems(setOf(current.id), ImageStatus.Confirmed, ImageStatus.NoModify)
                                }
                            },
                        ) { Text("移到回收站") }
                    }
                    ImageStatus.NoModify -> {
                        TextButton(
                            onClick = {
                                scope.launch {
                                    images.moveItems(setOf(current.id), ImageStatus.NoModify, ImageStatus.Pending)
                                }
                            },
                        ) { Text("恢复待归档") }
                        TextButton(onClick = { confirmDelete = true }) { Text("彻底删除") }
                    }
                }
            }

            message?.let { Text(it) }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("彻底删除") },
            text = { Text("将永久删除此图片，无法恢复。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmDelete = false
                        val id = item?.id ?: return@TextButton
                        scope.launch {
                            images.deleteItems(setOf(id))
                            onBack()
                        }
                    },
                ) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("取消") }
            },
        )
    }
}
