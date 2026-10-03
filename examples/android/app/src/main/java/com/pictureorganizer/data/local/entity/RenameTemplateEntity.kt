package com.pictureorganizer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rename_templates")
data class RenameTemplateEntity(
    @PrimaryKey val id: String,
    val name: String,
    val pattern: String,
    val isDefault: Boolean,
    val sortOrder: Int,
    val createdAt: Long,
)
