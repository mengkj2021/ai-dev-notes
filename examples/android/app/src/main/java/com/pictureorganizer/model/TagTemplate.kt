package com.pictureorganizer.model

data class TagTemplate(
    val id: String,
    val name: String,
    val tagNames: List<String> = emptyList(),
    val isDefault: Boolean = false,
    val sortOrder: Int = 0,
    val createdAt: Long = 0L,
)
