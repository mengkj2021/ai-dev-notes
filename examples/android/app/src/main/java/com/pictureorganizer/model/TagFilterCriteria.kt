package com.pictureorganizer.model

import java.time.LocalDate

enum class ImageListSort {
    ImportedAtDesc,

    ImportedAtAsc,

    NameAsc,

    DateTakenDesc,

    DateTakenAsc,
    ;

    companion object {
        fun fromParam(raw: String?): ImageListSort = entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: ImportedAtDesc
    }
}

data class TagFilterCriteria(
    val selectedTagNames: Set<String> = emptySet(),
    val includeUntagged: Boolean = false,
    val nameContains: String = "",
    val sort: ImageListSort = ImageListSort.ImportedAtDesc,
    val dateTakenFromEpochDay: Long? = null,
    val dateTakenToEpochDay: Long? = null,
    val importedAtFromEpochDay: Long? = null,
    val importedAtToEpochDay: Long? = null,
) {
    val isActive: Boolean
        get() =
            selectedTagNames.isNotEmpty() ||
                includeUntagged ||
                nameContains.isNotBlank() ||
                dateTakenFromEpochDay != null ||
                dateTakenToEpochDay != null ||
                importedAtFromEpochDay != null ||
                importedAtToEpochDay != null

    val hasDateTakenRange: Boolean
        get() = dateTakenFromEpochDay != null || dateTakenToEpochDay != null

    val hasImportedAtRange: Boolean
        get() = importedAtFromEpochDay != null || importedAtToEpochDay != null

    fun summaryTagNames(): List<String> = selectedTagNames.sorted()

    fun encodeTagsParam(): String = selectedTagNames.joinToString(TAGS_PARAM_SEPARATOR)

    fun encodeEpochDayParam(value: Long?): String = value?.toString().orEmpty()

    fun dateTakenRangeLabel(unsetLabel: String): String? {
        if (!hasDateTakenRange) return null
        val fromText = dateTakenFromEpochDay?.let { LocalDate.ofEpochDay(it).toString() } ?: unsetLabel
        val toText = dateTakenToEpochDay?.let { LocalDate.ofEpochDay(it).toString() } ?: unsetLabel
        return "$fromText ~ $toText"
    }

    fun importedAtRangeLabel(unsetLabel: String): String? {
        if (!hasImportedAtRange) return null
        val fromText = importedAtFromEpochDay?.let { LocalDate.ofEpochDay(it).toString() } ?: unsetLabel
        val toText = importedAtToEpochDay?.let { LocalDate.ofEpochDay(it).toString() } ?: unsetLabel
        return "$fromText ~ $toText"
    }

    companion object {
        const val TAGS_PARAM_SEPARATOR: String = "\u001F"

        fun parseTagsParam(raw: String?): Set<String> {
            if (raw.isNullOrBlank()) return emptySet()
            val delimiter =
                if (raw.contains(TAGS_PARAM_SEPARATOR)) {
                    TAGS_PARAM_SEPARATOR
                } else {
                    ","
                }
            return raw
                .split(delimiter)
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .toSet()
        }

        fun parseEpochDayParam(raw: String?): Long? = raw?.takeIf { it.isNotBlank() }?.toLongOrNull()
    }
}
