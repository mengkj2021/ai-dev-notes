package com.pictureorganizer.ui.main

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pictureorganizer.R
import com.pictureorganizer.model.TagFilterCriteria
import com.pictureorganizer.ui.common.LogScreenLifecycle
import com.pictureorganizer.ui.common.showSnackbarReplacing
import com.pictureorganizer.ui.main.tab.ImageListTab
import com.pictureorganizer.util.log.AppLog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onNavigateToImport: () -> Unit = {},
    onNavigateToDetail: (String) -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToFilter: (TagFilterCriteria) -> Unit = {},
    onNavigateToExportZip: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    LogScreenLifecycle("Main")
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val untaggedLabel = stringResource(R.string.filter_untagged)
    var pendingEmptyTrashConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(
        state.selectedTab,
        state.totalCount,
        state.pageItems.size,
        state.isFilterActive,
        state.filter,
    ) {
        AppLog.d(
            "Main",
            "tab=${state.selectedTab} pageItems=${state.pageItems.size} " +
                "total=${state.totalCount} filterActive=${state.isFilterActive} " +
                "filter=${state.filter.briefForLog()}",
        )
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is MainUiEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbarReplacing(
                        message = context.getString(effect.messageResId),
                    )
                }
                MainUiEffect.NavigateToImport -> onNavigateToImport()
            }
        }
    }

    val emptyMessage =
        when (state.selectedTab) {
            MainTab.Pending -> stringResource(R.string.empty_pending)
            MainTab.Confirmed -> stringResource(R.string.empty_confirmed)
            MainTab.NoModify -> stringResource(R.string.empty_no_modify)
        }

    val filterSummaryLabels =
        buildList {
            addAll(state.selectedTagNames.sorted())
            if (state.includeUntagged) add(untaggedLabel)
            val q = state.nameContains.trim()
            if (q.isNotEmpty()) add(stringResource(R.string.filter_name_contains) + ": $q")
            if (state.filter.hasDateTakenRange) {
                val unset = stringResource(R.string.filter_date_taken_unset)
                state.filter.dateTakenRangeLabel(unset)?.let { range ->
                    add(stringResource(R.string.filter_date_taken_summary, range))
                }
            }
            if (state.filter.hasImportedAtRange) {
                val unset = stringResource(R.string.filter_date_taken_unset)
                state.filter.importedAtRangeLabel(unset)?.let { range ->
                    add(stringResource(R.string.filter_imported_at_summary, range))
                }
            }
        }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets.safeDrawing.exclude(WindowInsets.ime),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings_title),
                        )
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = state.selectedTab == MainTab.Pending,
                    onClick = { viewModel.onEvent(MainUiEvent.SelectTab(MainTab.Pending)) },
                    icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                    label = { Text(stringResource(R.string.tab_pending)) },
                )
                NavigationBarItem(
                    selected = state.selectedTab == MainTab.Confirmed,
                    onClick = { viewModel.onEvent(MainUiEvent.SelectTab(MainTab.Confirmed)) },
                    icon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
                    label = { Text(stringResource(R.string.tab_confirmed)) },
                )
                NavigationBarItem(
                    selected = state.selectedTab == MainTab.NoModify,
                    onClick = { viewModel.onEvent(MainUiEvent.SelectTab(MainTab.NoModify)) },
                    icon = { Icon(Icons.Default.Close, contentDescription = null) },
                    label = { Text(stringResource(R.string.tab_no_modify)) },
                )
            }
        },
    ) { innerPadding ->
        ImageListTab(
            currentTab = state.selectedTab,
            items = state.pageItems,
            emptyMessage = emptyMessage,
            isEditMode = state.isEditMode,
            selectedIds = state.selectedIds,
            isFilterActive = state.isFilterActive,
            filterSummaryLabels = filterSummaryLabels,
            renameTemplates = state.renameTemplates,
            isBatchRenaming = state.isBatchRenaming,
            canEmptyTrash = state.trashItemCount > 0,
            pagingEnabled = state.pagingEnabled,
            pageIndex = state.pageIndex,
            totalPages = state.totalPages,
            canGoPrev = state.canGoPrev,
            canGoNext = state.canGoNext,
            onEditClick = { viewModel.onEvent(MainUiEvent.ToggleEditMode) },
            onSelectAllClick = { viewModel.onEvent(MainUiEvent.SelectAll) },
            onMoveTo = { viewModel.onEvent(MainUiEvent.MoveSelectedTo(it)) },
            onApplyRenameTemplate = {
                viewModel.onEvent(MainUiEvent.ApplyRenameTemplate(it))
            },
            onImportClick = { viewModel.onEvent(MainUiEvent.ImportImages) },
            onDeleteClick = { viewModel.onEvent(MainUiEvent.DeleteSelected) },
            onEmptyTrashClick = { pendingEmptyTrashConfirm = true },
            onToggleSelect = { viewModel.onEvent(MainUiEvent.ToggleSelect(it)) },
            onFilterClick = {
                onNavigateToFilter(state.filter)
            },
            onClearFilter = { viewModel.onEvent(MainUiEvent.ClearTagFilter) },
            onExportClick = onNavigateToExportZip,
            onPrevPage = { viewModel.onEvent(MainUiEvent.PrevPage) },
            onNextPage = { viewModel.onEvent(MainUiEvent.NextPage) },
            onItemClick = onNavigateToDetail,
            modifier = Modifier.padding(innerPadding),
        )
    }

    if (pendingEmptyTrashConfirm) {
        AlertDialog(
            onDismissRequest = { pendingEmptyTrashConfirm = false },
            title = { Text(stringResource(R.string.action_empty_trash)) },
            text = { Text(stringResource(R.string.confirm_empty_trash)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingEmptyTrashConfirm = false
                        viewModel.onEvent(MainUiEvent.EmptyTrash)
                    },
                ) {
                    Text(stringResource(R.string.action_empty_trash))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingEmptyTrashConfirm = false }) {
                    Text(stringResource(R.string.detail_tag_cancel))
                }
            },
        )
    }

    if (state.renameFailures.isNotEmpty() && !state.isBatchRenaming) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(MainUiEvent.DismissRenameFailures) },
            title = {
                Text(
                    stringResource(
                        R.string.batch_rename_failures_title,
                        state.renameFailures.size,
                    ),
                )
            },
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                    items(state.renameFailures, key = { it.id }) { item ->
                        ListItem(
                            headlineContent = { Text(item.displayName) },
                            supportingContent = {
                                Text(
                                    buildString {
                                        item.reasonResId?.let { resId ->
                                            append(stringResource(resId))
                                        }
                                        item.reasonDetail?.let { detail ->
                                            if (isNotEmpty()) append('\n')
                                            append(detail)
                                        }
                                    },
                                )
                            },
                            trailingContent = {
                                TextButton(
                                    onClick = {
                                        viewModel.onEvent(MainUiEvent.RetryRenameFailure(item.id))
                                    },
                                ) {
                                    Text(stringResource(R.string.import_failure_retry))
                                }
                            },
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.onEvent(MainUiEvent.DismissRenameFailures) }) {
                    Text(stringResource(R.string.import_failure_done))
                }
            },
            dismissButton = {
                if (state.renameFailures.size > 1) {
                    Row {
                        TextButton(
                            onClick = { viewModel.onEvent(MainUiEvent.RetryAllRenameFailures) },
                        ) {
                            Text(stringResource(R.string.import_failure_retry_all))
                        }
                    }
                }
            },
        )
    }
}

private fun TagFilterCriteria.briefForLog(): String =
    "tags=${selectedTagNames.size} untagged=$includeUntagged " +
        "q=${nameContains.isNotBlank()} sort=$sort " +
        "dateTaken=$hasDateTakenRange importedAt=$hasImportedAtRange active=$isActive"
