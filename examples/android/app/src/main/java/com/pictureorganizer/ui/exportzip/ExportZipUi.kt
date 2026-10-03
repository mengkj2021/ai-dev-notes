package com.pictureorganizer.ui.exportzip

import com.pictureorganizer.model.ImageListItem
import com.pictureorganizer.model.TagFilterCriteria

data class ExportZipUiState(
    val filter: TagFilterCriteria = TagFilterCriteria(),
    val confirmedCount: Int = 0,
    val matchedCount: Int = 0,
    val previewItems: List<ImageListItem> = emptyList(),
    val isExporting: Boolean = false,
    val progressCurrent: Int = 0,
    val progressTotal: Int = 0,
    val results: List<ExportZipResult> = emptyList(),
    val errorMessage: String? = null,
    val plannedPacks: List<ExportZipPackDraft> = emptyList(),
) {
    val isFilterActive: Boolean get() = filter.isActive
}

data class ExportZipPackDraft(
    val label: String,
    val stem: String,
)

data class ExportZipResult(
    val label: String,
    val fileName: String,
    val absolutePath: String,
    val entryCount: Int,
)

sealed interface ExportZipUiEvent {
    data class SetFilter(
        val filter: TagFilterCriteria,
    ) : ExportZipUiEvent

    data object ClearFilter : ExportZipUiEvent

    data object StartExport : ExportZipUiEvent

    data class ExcludeImage(
        val imageId: String,
    ) : ExportZipUiEvent

    data class ZipStemChanged(
        val label: String,
        val stem: String,
    ) : ExportZipUiEvent

    data object ClearError : ExportZipUiEvent
}

sealed interface ExportZipUiEffect {
    data class ShareFile(
        val absolutePath: String,
        val fileName: String,
    ) : ExportZipUiEffect

    data class ShowMessage(
        val messageResId: Int,
    ) : ExportZipUiEffect

    data class ShowMessageText(
        val text: String,
    ) : ExportZipUiEffect
}
