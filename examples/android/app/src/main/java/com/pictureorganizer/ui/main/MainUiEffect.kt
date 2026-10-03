package com.pictureorganizer.ui.main

sealed interface MainUiEffect {
    data class ShowSnackbar(
        val messageResId: Int,
    ) : MainUiEffect

    data object NavigateToImport : MainUiEffect
}
