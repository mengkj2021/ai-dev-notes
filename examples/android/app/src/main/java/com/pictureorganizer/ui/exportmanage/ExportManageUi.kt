package com.pictureorganizer.ui.exportmanage

data class ExportedZipItem(
    val fileName: String,
    val absolutePath: String,
    val sizeBytes: Long,
    val lastModifiedMillis: Long,
)

data class ExportManageUiState(
    val files: List<ExportedZipItem> = emptyList(),
    val isLoading: Boolean = true,
    val confirmDeletePath: String? = null,
    val renamePath: String? = null,
    val renameStemDraft: String = "",
    val isBusy: Boolean = false,
)

sealed interface ExportManageUiEvent {
    data object Refresh : ExportManageUiEvent

    data class Share(
        val absolutePath: String,
    ) : ExportManageUiEvent

    data class RequestDelete(
        val absolutePath: String,
    ) : ExportManageUiEvent

    data object ConfirmDelete : ExportManageUiEvent

    data object CancelDelete : ExportManageUiEvent

    data class RequestRename(
        val absolutePath: String,
    ) : ExportManageUiEvent

    data class RenameDraftChanged(
        val stem: String,
    ) : ExportManageUiEvent

    data object ConfirmRename : ExportManageUiEvent

    data object CancelRename : ExportManageUiEvent
}

sealed interface ExportManageUiEffect {
    data class ShareFile(
        val absolutePath: String,
        val fileName: String,
    ) : ExportManageUiEffect

    data class ShowMessage(
        val messageResId: Int,
    ) : ExportManageUiEffect
}
