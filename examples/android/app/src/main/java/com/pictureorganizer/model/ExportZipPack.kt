package com.pictureorganizer.model

fun List<ImageListItem>.excludingIds(excludedIds: Set<String>): List<ImageListItem> {
    if (excludedIds.isEmpty()) return this
    return filter { it.id !in excludedIds }
}

const val SINGLE_EXPORT_PACK_LABEL = "__single__"

fun buildSingleExportPack(matched: List<ImageListItem>): List<ImageListItem> = matched
