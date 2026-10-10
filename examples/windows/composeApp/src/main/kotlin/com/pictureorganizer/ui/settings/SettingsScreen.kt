package com.pictureorganizer.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pictureorganizer.data.repository.UserPreferencesRepository
import com.pictureorganizer.util.list.ListPaging
import kotlinx.coroutines.launch

const val APP_VERSION = "0.1.0-dev"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    prefs: UserPreferencesRepository,
    onBack: () -> Unit,
    onOpenTutorial: () -> Unit,
    onOpenTagManage: () -> Unit,
    onOpenDefaultTags: () -> Unit,
    onOpenRenameTemplates: () -> Unit,
    onOpenExportManage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by prefs.prefs.collectAsState()
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
                    .widthIn(max = 720.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
        ) {
            ListItem(
                headlineContent = { Text("标签管理") },
                supportingContent = { Text("标签库与标签模板") },
                modifier = Modifier.clickable(onClick = onOpenTagManage),
            )
            ListItem(
                headlineContent = { Text("默认标签") },
                supportingContent = { Text("导入时自动合并") },
                modifier = Modifier.clickable(onClick = onOpenDefaultTags),
            )
            ListItem(
                headlineContent = { Text("重命名模板") },
                supportingContent = { Text("占位符规则") },
                modifier = Modifier.clickable(onClick = onOpenRenameTemplates),
            )
            ListItem(
                headlineContent = { Text("已打包文件") },
                supportingContent = { Text("exports 历史") },
                modifier = Modifier.clickable(onClick = onOpenExportManage),
            )
            ListItem(
                headlineContent = { Text("查看教程") },
                modifier = Modifier.clickable(onClick = onOpenTutorial),
            )
            ListItem(
                headlineContent = { Text("导入时压缩大图") },
                supportingContent = { Text(">2MB 或长边>1920 → JPEG≈85") },
                trailingContent = {
                    Switch(
                        checked = state.compressOnImport,
                        onCheckedChange = { enabled ->
                            scope.launch { prefs.setCompressOnImport(enabled) }
                        },
                    )
                },
            )
            ListItem(
                headlineContent = { Text("导入重名询问") },
                supportingContent = { Text("同 originalName 时询问跳过 / 仍导入") },
                trailingContent = {
                    Switch(
                        checked = state.importDuplicateAsk,
                        onCheckedChange = { enabled ->
                            scope.launch { prefs.setImportDuplicateAsk(enabled) }
                        },
                    )
                },
            )
            ListItem(
                headlineContent = { Text("列表分页") },
                supportingContent = { Text("桌面默认关闭；超大库可开启") },
                trailingContent = {
                    Switch(
                        checked = state.listPagingEnabled,
                        onCheckedChange = { enabled ->
                            scope.launch { prefs.setListPagingEnabled(enabled) }
                        },
                    )
                },
            )
            ListItem(
                headlineContent = { Text("每页数量：${state.listPageSize}") },
                supportingContent = { Text(ListPaging.PRESET_PAGE_SIZES.joinToString()) },
                trailingContent = {
                    androidx.compose.material3.TextButton(
                        onClick = {
                            val presets = ListPaging.PRESET_PAGE_SIZES
                            val idx = presets.indexOf(state.listPageSize).let { if (it < 0) 0 else (it + 1) % presets.size }
                            scope.launch { prefs.setListPageSize(presets[idx]) }
                        },
                    ) { Text("切换") }
                },
            )
            ListItem(
                headlineContent = { Text("版本") },
                supportingContent = { Text(APP_VERSION) },
            )
        }
    }
}
