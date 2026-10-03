package com.pictureorganizer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "images")
data class ImageEntity(
    @PrimaryKey val id: String,
    val filePath: String,
    val fileName: String,
    val description: String,
    val status: String,
    val importedAt: Long,
    val tagsJson: String,
    val originalName: String? = null,
    val dateTakenMillis: Long? = null,
)
