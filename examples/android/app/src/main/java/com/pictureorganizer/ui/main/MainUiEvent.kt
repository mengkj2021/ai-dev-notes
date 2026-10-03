package com.pictureorganizer.ui.main

import com.pictureorganizer.model.ImageStatus
import com.pictureorganizer.model.TagFilterCriteria

sealed interface MainUiEvent {
    data class SelectTab(
        val tab: MainTab,
    ) : MainUiEvent

    data object ToggleEditMode : MainUiEvent

    data object SelectAll : MainUiEvent

    data class ToggleSelect(
        val id: String,
    ) : MainUiEvent

    data class MoveSelectedTo(
        val status: ImageStatus,
    ) : MainUiEvent

    data class ApplyRenameTemplate(
        val templateId: String,
    ) : MainUiEvent

    data class RetryRenameFailure(
        val id: String,
    ) : MainUiEvent

    data object RetryAllRenameFailures : MainUiEvent

    data object DismissRenameFailures : MainUiEvent

    data object ImportImages : MainUiEvent

    data object DeleteSelected : MainUiEvent

    data object EmptyTrash : MainUiEvent

    data class SetTagFilter(
        val criteria: TagFilterCriteria,
        val targetTab: MainTab? = null,
    ) : MainUiEvent

    data object ClearTagFilter : MainUiEvent

    data object PrevPage : MainUiEvent

    data object NextPage : MainUiEvent
}
