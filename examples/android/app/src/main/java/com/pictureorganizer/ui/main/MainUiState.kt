package com.pictureorganizer.ui.main

import com.pictureorganizer.model.ImageListItem
import com.pictureorganizer.model.ImageListSort
import com.pictureorganizer.model.RenameTemplate
import com.pictureorganizer.model.TagFilterCriteria

data class BatchRenameFailure(
    val id: String,
    val displayName: String,
    val reasonResId: Int? = null,
    val reasonDetail: String? = null,
)

data class MainUiState(
    val selectedTab: MainTab = MainTab.Pending,
    val isEditMode: Boolean = false,
    val selectedIds: Set<String> = emptySet(),
    val availableTags: List<String> = emptyList(),
    val filter: TagFilterCriteria = TagFilterCriteria(),
    val pageItems: List<ImageListItem> = emptyList(),
    val pageIndex: Int = 0,
    val totalCount: Int = 0,
    val totalPages: Int = 1,
    val pagingEnabled: Boolean = true,
    val renameTemplates: List<RenameTemplate> = emptyList(),
    val renameFailures: List<BatchRenameFailure> = emptyList(),
    val isBatchRenaming: Boolean = false,
    val trashItemCount: Int = 0,
) {
    val selectedTagNames: Set<String> get() = filter.selectedTagNames
    val includeUntagged: Boolean get() = filter.includeUntagged
    val nameContains: String get() = filter.nameContains
    val sort: ImageListSort get() = filter.sort

    val isFilterActive: Boolean
        get() = filter.isActive

    val canGoPrev: Boolean get() = pagingEnabled && pageIndex > 0
    val canGoNext: Boolean get() = pagingEnabled && pageIndex < totalPages - 1
}
