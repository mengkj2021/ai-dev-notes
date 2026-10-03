package com.pictureorganizer.ui.filter

import com.pictureorganizer.model.ImageListSort

data class FilterUiState(
    val availableTags: List<String> = emptyList(),
    val selectedTagNames: Set<String> = emptySet(),
    val includeUntagged: Boolean = false,
    val nameContains: String = "",
    val sort: ImageListSort = ImageListSort.ImportedAtDesc,
    val dateTakenFromEpochDay: Long? = null,
    val dateTakenToEpochDay: Long? = null,
    val importedAtFromEpochDay: Long? = null,
    val importedAtToEpochDay: Long? = null,
)
