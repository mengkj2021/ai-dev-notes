package com.pictureorganizer.model

data class Tag(
    val id: String,
    val name: String,
    val sortOrder: Int = 0,
    val createdAt: Long = 0L,
)
