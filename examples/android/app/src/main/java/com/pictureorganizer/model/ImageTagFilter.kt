package com.pictureorganizer.model

import java.time.Instant
import java.time.ZoneId

fun ImageListItem.matchesTagFilter(
    selectedTagNames: Set<String>,
    includeUntagged: Boolean,
    nameContains: String = "",
): Boolean {
    val query = nameContains.trim()
    if (query.isNotEmpty()) {
        val haystack = listOf(description, filePath.substringAfterLast('/')).joinToString("\u0000")
        if (!haystack.contains(query, ignoreCase = true)) return false
    }
    if (selectedTagNames.isEmpty() && !includeUntagged) return true
    val user = ImageListItem.userTagsOf(tags)
    val hitTag = selectedTagNames.any { it in user }
    val hitUntagged = includeUntagged && user.isEmpty()
    return hitTag || hitUntagged
}

fun ImageListItem.matchesFilter(criteria: TagFilterCriteria): Boolean {
    if (!matchesTagFilter(
            selectedTagNames = criteria.selectedTagNames,
            includeUntagged = criteria.includeUntagged,
            nameContains = criteria.nameContains,
        )
    ) {
        return false
    }
    if (!matchesDateTakenRange(
            fromEpochDay = criteria.dateTakenFromEpochDay,
            toEpochDay = criteria.dateTakenToEpochDay,
        )
    ) {
        return false
    }
    return matchesImportedAtRange(
        fromEpochDay = criteria.importedAtFromEpochDay,
        toEpochDay = criteria.importedAtToEpochDay,
    )
}

fun ImageListItem.matchesDateTakenRange(
    fromEpochDay: Long?,
    toEpochDay: Long?,
    zoneId: ZoneId = ZoneId.systemDefault(),
): Boolean {
    if (fromEpochDay == null && toEpochDay == null) return true
    val millis = dateTakenMillis ?: return false
    val day =
        Instant
            .ofEpochMilli(millis)
            .atZone(zoneId)
            .toLocalDate()
            .toEpochDay()
    if (fromEpochDay != null && day < fromEpochDay) return false
    if (toEpochDay != null && day > toEpochDay) return false
    return true
}

fun ImageListItem.matchesImportedAtRange(
    fromEpochDay: Long?,
    toEpochDay: Long?,
    zoneId: ZoneId = ZoneId.systemDefault(),
): Boolean {
    if (fromEpochDay == null && toEpochDay == null) return true
    val day =
        Instant
            .ofEpochMilli(importedAt)
            .atZone(zoneId)
            .toLocalDate()
            .toEpochDay()
    if (fromEpochDay != null && day < fromEpochDay) return false
    if (toEpochDay != null && day > toEpochDay) return false
    return true
}

fun List<ImageListItem>.sortedByFilter(sort: ImageListSort): List<ImageListItem> =
    when (sort) {
        ImageListSort.ImportedAtDesc -> sortedByDescending { it.importedAt }
        ImageListSort.ImportedAtAsc -> sortedBy { it.importedAt }
        ImageListSort.NameAsc ->
            sortedWith(
                compareBy(String.CASE_INSENSITIVE_ORDER) {
                    it.description.ifBlank { it.filePath.substringAfterLast('/') }
                },
            )
        ImageListSort.DateTakenDesc ->
            sortedWith(
                compareBy<ImageListItem> { it.dateTakenMillis == null }
                    .thenByDescending { it.dateTakenMillis ?: Long.MIN_VALUE },
            )
        ImageListSort.DateTakenAsc ->
            sortedWith(
                compareBy<ImageListItem> { it.dateTakenMillis == null }
                    .thenBy { it.dateTakenMillis ?: Long.MAX_VALUE },
            )
    }
