package com.pictureorganizer.ui.exportmanage

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pictureorganizer.util.file.AppFileManager
import com.pictureorganizer.util.file.DesktopDialogs
import com.pictureorganizer.util.file.ExportZipNames
import java.io.File

data class ExportedZipItem(
    val fileName: String,
    val absolutePath: String,
    val sizeBytes: Long,
    val lastModifiedMillis: Long,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportManageScreen(
    files: AppFileManager,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val exportsDir = remember { files.exportsDir() }
    var items by remember { mutableStateOf(listExported(exportsDir)) }
    var message by remember { mutableStateOf<String?>(null) }
    var renameTarget by remember { mutableStateOf<ExportedZipItem?>(null) }
    var renameStem by remember { mutableStateOf("") }
    var deleteTarget by remember { mutableStateOf<ExportedZipItem?>(null) }

    fun refresh() {
        items = listExported(exportsDir)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("已打包文件") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    TextButton(onClick = { DesktopDialogs.revealInExplorer(exportsDir) }) {
                        Text("打开目录")
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            message?.let { Text(it, modifier = Modifier.padding(12.dp)) }
            if (items.isEmpty()) {
                Text("暂无 zip", modifier = Modifier.padding(16.dp))
            } else {
                LazyColumn {
                    items(items, key = { it.absolutePath }) { item ->
                        ListItem(
                            headlineContent = { Text(item.fileName) },
                            supportingContent = {
                                Text("${item.sizeBytes / 1024} KB · 单击在资源管理器中显示")
                            },
                            trailingContent = {
                                androidx.compose.foundation.layout.Row {
                                    TextButton(
                                        onClick = {
                                            renameTarget = item
                                            renameStem = ExportZipNames.normalizeZipStem(item.fileName)
                                        },
                                    ) { Text("改名") }
                                    TextButton(onClick = { deleteTarget = item }) { Text("删除") }
                                }
                            },
                            modifier =
                                Modifier.clickable {
                                    DesktopDialogs.revealInExplorer(File(item.absolutePath))
                                },
                        )
                    }
                }
            }
        }
    }

    renameTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            title = { Text("重命名 zip") },
            text = {
                OutlinedTextField(
                    value = renameStem,
                    onValueChange = { renameStem = it },
                    label = { Text("主文件名") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        when (
                            val plan =
                                ExportZipNames.planZipRename(
                                    target.fileName,
                                    renameStem,
                                    items.map { it.fileName }.toSet(),
                                )
                        ) {
                            ExportZipNames.ZipRenamePlan.Unchanged -> renameTarget = null
                            is ExportZipNames.ZipRenamePlan.Invalid -> message = plan.message
                            ExportZipNames.ZipRenamePlan.Collision -> message = "已存在同名文件"
                            is ExportZipNames.ZipRenamePlan.Ready -> {
                                val src = File(target.absolutePath)
                                val dest = File(exportsDir, plan.newFileName)
                                if (src.renameTo(dest)) {
                                    message = "已改名"
                                    renameTarget = null
                                    refresh()
                                } else {
                                    message = "改名失败"
                                }
                            }
                        }
                    },
                ) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { renameTarget = null }) { Text("取消") }
            },
        )
    }

    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("删除？") },
            text = { Text(target.fileName) },
            confirmButton = {
                TextButton(
                    onClick = {
                        File(target.absolutePath).delete()
                        deleteTarget = null
                        refresh()
                        message = "已删除"
                    },
                ) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("取消") }
            },
        )
    }
}

private fun listExported(dir: File): List<ExportedZipItem> =
    dir
        .listFiles { f -> f.isFile && f.name.lowercase().endsWith(".zip") }
        ?.map {
            ExportedZipItem(
                fileName = it.name,
                absolutePath = it.absolutePath,
                sizeBytes = it.length(),
                lastModifiedMillis = it.lastModified(),
            )
        }?.sortedByDescending { it.lastModifiedMillis }
        .orEmpty()
