package com.pictureorganizer.ui.renametemplate

import com.pictureorganizer.model.RenameTemplate

data class RenameTemplateManageUiState(
    val templates: List<RenameTemplate> = emptyList(),
    val confirmDeleteId: String? = null,
    val isBusy: Boolean = false,
)

sealed interface RenameTemplateManageUiEvent {
    data class SetDefault(
        val id: String,
    ) : RenameTemplateManageUiEvent

    data class RequestDelete(
        val id: String,
    ) : RenameTemplateManageUiEvent

    data object ConfirmDelete : RenameTemplateManageUiEvent

    data object CancelDelete : RenameTemplateManageUiEvent

    data object NotifySaved : RenameTemplateManageUiEvent
}

sealed interface RenameTemplateManageUiEffect {
    data class ShowMessage(
        val messageResId: Int,
    ) : RenameTemplateManageUiEffect

    data class ShowMessageText(
        val text: String,
    ) : RenameTemplateManageUiEffect
}
