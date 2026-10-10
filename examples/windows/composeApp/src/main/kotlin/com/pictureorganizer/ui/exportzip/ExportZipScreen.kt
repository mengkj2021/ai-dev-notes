package com.pictureorganizer.ui.exportzip

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pictureorganizer.data.repository.ImageRepository
import com.pictureorganizer.model.ImageStatus
import com.pictureorganizer.util.file.AppFileManager
import com.pictureorganizer.util.file.DesktopDialogs
import com.pictureorganizer.util.file.ExportZipNames
import com.pictureorganizer.util.file.ZipExporter
import com.pictureorganizer.util.log.AppLog
import java.awt.Window
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportZipScreen(
    images: ImageRepository,
    files: AppFileManager,
    awtWindow: Window,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var stem by remember { mutableStateOf(ExportZipNames.defaultSingleZipStem()) }
    var message by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var lastZip by remember { mutableStateOf<File?>(null) }
    val scope = rememberCoroutineScope()

    fun runExport(dest: File) {
        scope.launch {
            busy = true
            message =
                withContext(Dispatchers.IO) {
                    try {
                        val items = images.getItems(ImageStatus.Confirmed)
                        if (items.isEmpty()) return@withContext "没有已归档图片可打包"
                        dest.parentFile?.mkdirs()
                        val entries =
                            items.map { item ->
                                val name = item.filePath.substringAfterLast('/')
                                name to files.absoluteFile(item.filePath)
                            }
                        val count = ZipExporter.zipFiles(dest, entries)
                        lastZip = dest
                        DesktopDialogs.revealInExplorer(dest)
                        "已生成 ${dest.name}（$count 张）\n${dest.absolutePath}"
                    } catch (t: Throwable) {
                        AppLog.e("ExportZip", "fail", t)
                        t.message ?: "打包失败"
                    }
                }
            busy = false
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("打包导出") },
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
                    .widthIn(max = 720.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("将「已归档」图片打成单个 zip。可写入应用 exports，或另存到任意位置。")
            OutlinedTextField(
                value = stem,
                onValueChange = { stem = it },
                label = { Text("zip 主文件名") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                supportingText = { Text("默认 export_{日期}.zip；应用目录冲突自动 _2") },
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    enabled = !busy,
                    onClick = {
                        val err = ExportZipNames.validateZipStem(stem)
                        if (err != null) {
                            message = err
                            return@Button
                        }
                        val exports = files.exportsDir()
                        val fileName =
                            ExportZipNames.uniqueZipFileName(
                                ExportZipNames.normalizeZipStem(stem),
                                exports,
                            )
                        runExport(File(exports, fileName))
                    },
                ) {
                    Text(if (busy) "打包中…" else "打包到 exports")
                }
                OutlinedButton(
                    enabled = !busy,
                    onClick = {
                        val err = ExportZipNames.validateZipStem(stem)
                        if (err != null) {
                            message = err
                            return@OutlinedButton
                        }
                        val suggested =
                            ExportZipNames.normalizeZipStem(stem).let { "$it.zip" }
                        val chosen =
                            DesktopDialogs.pickSaveZip(
                                parent = awtWindow,
                                suggestedName = suggested,
                                initialDirectory = files.exportsDir(),
                            ) ?: return@OutlinedButton
                        runExport(chosen)
                    },
                ) {
                    Text("另存为…")
                }
                lastZip?.let { zip ->
                    OutlinedButton(
                        enabled = !busy,
                        onClick = { DesktopDialogs.revealInExplorer(zip) },
                    ) {
                        Text("在资源管理器中显示")
                    }
                }
            }
            message?.let { Text(it) }
        }
    }
}
