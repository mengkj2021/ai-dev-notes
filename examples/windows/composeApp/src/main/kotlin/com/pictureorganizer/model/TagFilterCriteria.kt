package com.pictureorganizer.model

enum class ImageSort {
    ImportedAtDesc,
    NameAsc,
    DateTakenDesc,
}

data class TagFilterCriteria(
    val nameContains: String = "",
    val sort: ImageSort = ImageSort.ImportedAtDesc,
    val selectedTags: Set<String> = emptySet(),
    val includeUntagged: Boolean = false,
) {
    fun isActive(): Boolean =
        nameContains.isNotBlank() || selectedTags.isNotEmpty() || includeUntagged || sort != ImageSort.ImportedAtDesc

    fun apply(items: List<ImageListItem>): List<ImageListItem> {
        var list = items
        val q = nameContains.trim()
        if (q.isNotEmpty()) {
            list = list.filter { it.description.contains(q, ignoreCase = true) || it.filePath.contains(q, ignoreCase = true) }
        }
        if (includeUntagged || selectedTags.isNotEmpty()) {
            list =
                list.filter { item ->
                    val tags = ImageListItem.userTagsOf(item.tags)
                    (includeUntagged && tags.isEmpty()) || tags.any { it in selectedTags }
                }
        }
        return when (sort) {
            ImageSort.ImportedAtDesc -> list.sortedByDescending { it.importedAt }
            ImageSort.NameAsc -> list.sortedBy { it.description.lowercase() }
            ImageSort.DateTakenDesc -> list.sortedByDescending { it.dateTakenMillis ?: Long.MIN_VALUE }
        }
    }
}
