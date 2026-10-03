package com.pictureorganizer.ui.renametemplate

data class TextInsertResult(
    val text: String,
    val cursor: Int,
)

fun insertAtSelection(
    text: String,
    selectionStart: Int,
    selectionEnd: Int,
    token: String,
): TextInsertResult {
    val start = minOf(selectionStart, selectionEnd).coerceIn(0, text.length)
    val end = maxOf(selectionStart, selectionEnd).coerceIn(0, text.length)
    val newText = text.replaceRange(start, end, token)
    return TextInsertResult(text = newText, cursor = start + token.length)
}

val RenamePatternInsertTokens: List<String> =
    listOf("{name}", "{date}", "{tag}", "{yyyy}", "{mm}", "{dd}", "{time}", "{tags}", "{n}")
