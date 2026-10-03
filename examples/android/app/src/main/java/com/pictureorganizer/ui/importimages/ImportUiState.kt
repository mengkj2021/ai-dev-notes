package com.pictureorganizer.ui.importimages

import android.net.Uri

data class FailedImportItem(
    val uri: Uri,
    val displayName: String,
    val errorKind: ImportErrorKind,
    val errorDetail: String?,
)

enum class ImportErrorKind(
    val messageResId: Int,
) {
    ReadFailed(com.pictureorganizer.R.string.import_error_read),
    DecodeFailed(com.pictureorganizer.R.string.import_error_decode),
    Unknown(com.pictureorganizer.R.string.import_error_unknown),
}

data class DuplicatePrompt(
    val originalName: String,
)

enum class DuplicateAskDecision {
    Skip,
    Import,
    SkipAskingRest,
}

data class ImportUiState(
    val isImporting: Boolean = false,
    val current: Int = 0,
    val total: Int = 0,
    val compressEnabled: Boolean = true,
    val failedItems: List<FailedImportItem> = emptyList(),
    val duplicatePrompt: DuplicatePrompt? = null,
)
