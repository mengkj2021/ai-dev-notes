package com.pictureorganizer.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isCtrlPressed
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.dp
import com.pictureorganizer.model.ImageStatus
import com.pictureorganizer.util.file.AppFileManager
import com.pictureorganizer.util.image.FileThumbnail

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    files: AppFileManager,
    onNavigateToImport: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToFilter: () -> Unit,
    onNavigateToExportZip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var confirmDelete by remember { mutableStateOf(false) }
    var confirmEmptyTrash by remember { mutableStateOf(false) }

    LaunchedEffect(state.statusMessage) {
        val msg = state.statusMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearStatus()
    }

    Row(modifier = modifier.fillMaxSize()) {
        NavigationRail(
            modifier = Modifier.fillMaxHeight(),
            header = {
                IconButton(onClick = onNavigateToImport) {
                    Icon(Icons.Default.Add, contentDescription = "导入")
                }
            },
        ) {
            MainTab.entries.forEach { tab ->
                NavigationRailItem(
                    selected = state.selectedTab == tab,
                    onClick = { viewModel.onTabSelected(tab) },
                    icon = { Text(tab.title().take(1)) },
                    label = { Text(tab.title()) },
                )
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            NavigationRailItem(
                selected = false,
                onClick = onNavigateToSettings,
                icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                label = { Text("设置") },
            )
        }

        Scaffold(
            modifier = Modifier.weight(1f),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            buildString {
                                append(state.selectedTab.title())
                                if (state.isFilterActive) append(" · 已筛选")
                                append("（${state.totalCount}）")
                            },
                        )
                    },
                    actions = {
                        if (state.selectedTab == MainTab.Confirmed) {
                            IconButton(onClick = onNavigateToExportZip) {
                                Icon(Icons.Default.Inventory, contentDescription = "打包")
                            }
                        }
                        IconButton(onClick = onNavigateToFilter) {
                            Icon(Icons.Default.FilterList, contentDescription = "筛选")
                        }
                        IconButton(onClick = viewModel::selectAllVisible) {
                            Icon(Icons.Default.SelectAll, contentDescription = "全选")
                        }
                        if (state.selectedTab == MainTab.NoModify && state.items.isNotEmpty()) {
                            TextButton(onClick = { confirmEmptyTrash = true }) { Text("清空") }
                        }
                    },
                )
            },
            bottomBar = {
                if (state.hasSelection || (state.pagingEnabled && state.totalPages > 1)) {
                    Column {
                        if (state.hasSelection) {
                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text("已选 ${state.selectedIds.size}", style = MaterialTheme.typography.bodyMedium)
                                TextButton(onClick = viewModel::clearSelection) { Text("取消") }
                                TextButton(onClick = viewModel::applyDefaultRenameTemplateToSelected) {
                                    Text("套用重命名")
                                }
                                when (state.selectedTab) {
                                    MainTab.Pending -> {
                                        TextButton(onClick = { viewModel.moveSelected(ImageStatus.Confirmed) }) {
                                            Text("移到已归档")
                                        }
                                        TextButton(onClick = { viewModel.moveSelected(ImageStatus.NoModify) }) {
                                            Text("移到回收站")
                                        }
                                    }
                                    MainTab.Confirmed -> {
                                        TextButton(onClick = { viewModel.moveSelected(ImageStatus.Pending) }) {
                                            Text("移回待归档")
                                        }
                                        TextButton(onClick = { viewModel.moveSelected(ImageStatus.NoModify) }) {
                                            Text("移到回收站")
                                        }
                                    }
                                    MainTab.NoModify -> {
                                        TextButton(onClick = { viewModel.moveSelected(ImageStatus.Pending) }) {
                                            Text("恢复待归档")
                                        }
                                        TextButton(onClick = { confirmDelete = true }) {
                                            Icon(Icons.Default.Delete, contentDescription = null)
                                            Text("彻底删除")
                                        }
                                    }
                                }
                            }
                        }
                        if (state.pagingEnabled && state.totalPages > 1) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                TextButton(
                                    enabled = state.pageIndex > 0,
                                    onClick = { viewModel.setPage(state.pageIndex - 1) },
                                ) { Text("上一页") }
                                Text("${state.pageIndex + 1} / ${state.totalPages}")
                                TextButton(
                                    enabled = state.pageIndex < state.totalPages - 1,
                                    onClick = { viewModel.setPage(state.pageIndex + 1) },
                                ) { Text("下一页") }
                            }
                        }
                    }
                }
            },
        ) { padding ->
            if (state.items.isEmpty()) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text =
                            when (state.selectedTab) {
                                MainTab.Pending -> "暂无待归档图片。左侧「+」导入，或把图片拖到导入页。"
                                MainTab.Confirmed -> "暂无已归档图片"
                                MainTab.NoModify -> "回收站为空"
                            },
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            } else {
                LazyColumn(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(padding),
                ) {
                    items(state.pageItems, key = { it.id }) { item ->
                        val selected = item.id in state.selectedIds
                        ListItem(
                            headlineContent = { Text(item.description) },
                            supportingContent = {
                                Text(
                                    buildString {
                                        append(item.filePath)
                                        if (item.tags.isNotEmpty()) {
                                            append(" · ")
                                            append(item.tags.joinToString())
                                        }
                                    },
                                )
                            },
                            leadingContent = {
                                FileThumbnail(files.absoluteFile(item.filePath))
                            },
                            trailingContent = {
                                if (state.hasSelection || state.editMode) {
                                    Checkbox(
                                        checked = selected,
                                        onCheckedChange = { viewModel.toggleSelection(item.id) },
                                    )
                                }
                            },
                            modifier =
                                Modifier.onPointerEvent(PointerEventType.Release) { event ->
                                    val change = event.changes.firstOrNull() ?: return@onPointerEvent
                                    if (change.pressed || !change.previousPressed) return@onPointerEvent
                                    val ctrl = event.keyboardModifiers.isCtrlPressed
                                    if (ctrl || state.hasSelection || state.editMode) {
                                        viewModel.toggleSelection(item.id)
                                    } else {
                                        onNavigateToDetail(item.id)
                                    }
                                },
                            colors =
                                ListItemDefaults.colors(
                                    containerColor =
                                        if (selected) {
                                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
                                        } else {
                                            MaterialTheme.colorScheme.surface
                                        },
                                ),
                        )
                    }
                }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("彻底删除") },
            text = { Text("将永久删除选中的 ${state.selectedIds.size} 项，无法恢复。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmDelete = false
                        viewModel.deleteSelected()
                    },
                ) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("取消") }
            },
        )
    }

    if (confirmEmptyTrash) {
        AlertDialog(
            onDismissRequest = { confirmEmptyTrash = false },
            title = { Text("清空回收站") },
            text = { Text("将永久删除回收站内全部图片，无法恢复。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmEmptyTrash = false
                        viewModel.emptyTrash()
                    },
                ) { Text("清空") }
            },
            dismissButton = {
                TextButton(onClick = { confirmEmptyTrash = false }) { Text("取消") }
            },
        )
    }
}
