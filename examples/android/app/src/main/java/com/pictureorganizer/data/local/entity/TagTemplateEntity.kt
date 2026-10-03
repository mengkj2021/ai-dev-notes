package com.pictureorganizer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tag_templates")
data class TagTemplateEntity(
    @PrimaryKey val id: String,
    val name: String,
    val tagNamesJson: String,
    val isDefault: Boolean,
    val sortOrder: Int,
    val createdAt: Long,
)
