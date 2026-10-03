package com.pictureorganizer.ui.renametemplate

data class RenameTemplateEditUiState(
    val editingId: String? = null,
    val name: String = "",
    val pattern: String = "{date}_{name}",
    val isDefault: Boolean = false,
    val isLoading: Boolean = false,
    val isBusy: Boolean = false,
    val errorMessageResId: Int? = null,
)

sealed interface RenameTemplateEditUiEvent {
    data class NameChanged(
        val value: String,
    ) : RenameTemplateEditUiEvent

    data class PatternChanged(
        val value: String,
    ) : RenameTemplateEditUiEvent

    data class DefaultChanged(
        val value: Boolean,
    ) : RenameTemplateEditUiEvent

    data object Save : RenameTemplateEditUiEvent
}

sealed interface RenameTemplateEditUiEffect {
    data object Saved : RenameTemplateEditUiEffect

    data object NotFound : RenameTemplateEditUiEffect
}
