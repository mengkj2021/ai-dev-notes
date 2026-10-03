package com.pictureorganizer.ui.main.tab

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pictureorganizer.model.ImageListItem
import com.pictureorganizer.model.ImageStatus
import com.pictureorganizer.model.RenameTemplate
import com.pictureorganizer.ui.main.MainTab

@Composable
fun ImageListTab(
    currentTab: MainTab,
    items: List<ImageListItem>,
    emptyMessage: String,
    isEditMode: Boolean,
    selectedIds: Set<String>,
    isFilterActive: Boolean,
    filterSummaryLabels: List<String>,
    renameTemplates: List<RenameTemplate>,
    isBatchRenaming: Boolean,
    canEmptyTrash: Boolean,
    pagingEnabled: Boolean,
    pageIndex: Int,
    totalPages: Int,
    canGoPrev: Boolean,
    canGoNext: Boolean,
    onEditClick: () -> Unit,
    onSelectAllClick: () -> Unit,
    onMoveTo: (ImageStatus) -> Unit,
    onApplyRenameTemplate: (String) -> Unit,
    onImportClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onEmptyTrashClick: () -> Unit,
    onToggleSelect: (String) -> Unit,
    onFilterClick: () -> Unit,
    onClearFilter: () -> Unit,
    onExportClick: () -> Unit,
    onPrevPage: () -> Unit,
    onNextPage: () -> Unit,
    onItemClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        TabActionBar(
            currentTab = currentTab,
            isEditMode = isEditMode,
            hasSelection = selectedIds.isNotEmpty(),
            isFilterActive = isFilterActive,
            filterSummaryLabels = filterSummaryLabels,
            renameTemplates = renameTemplates,
            isBatchRenaming = isBatchRenaming,
            canEmptyTrash = canEmptyTrash,
            onEditClick = onEditClick,
            onSelectAllClick = onSelectAllClick,
            onMoveTo = onMoveTo,
            onApplyRenameTemplate = onApplyRenameTemplate,
            onImportClick = onImportClick,
            onDeleteClick = onDeleteClick,
            onEmptyTrashClick = onEmptyTrashClick,
            onExportClick = onExportClick,
            onFilterClick = onFilterClick,
            onClearFilter = onClearFilter,
        )

        if (items.isEmpty()) {
            EmptyListPlaceholder(
                emptyMessage = emptyMessage,
                modifier = Modifier.weight(1f),
            )
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(items, key = { it.id }) { item ->
                    ImageListItemRow(
                        item = item,
                        isEditMode = isEditMode,
                        isSelected = item.id in selectedIds,
                        onToggleSelect = { onToggleSelect(item.id) },
                        onClick = { onItemClick(item.id) },
                    )
                }
            }
        }

        if (pagingEnabled) {
            ListPageBar(
                pageIndex = pageIndex,
                totalPages = totalPages,
                canGoPrev = canGoPrev,
                canGoNext = canGoNext,
                onPrevClick = onPrevPage,
                onNextClick = onNextPage,
            )
        }
    }
}
