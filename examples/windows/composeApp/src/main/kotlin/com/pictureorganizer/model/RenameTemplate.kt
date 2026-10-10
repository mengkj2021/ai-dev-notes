package com.pictureorganizer.model

data class RenameTemplate(
    val id: String,
    val name: String,
    val pattern: String,
    val isDefault: Boolean = false,
    val sortOrder: Int = 0,
    val createdAt: Long = 0L,
)
