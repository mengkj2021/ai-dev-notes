package com.pictureorganizer.data.mapper

import com.pictureorganizer.data.local.converter.Converters
import com.pictureorganizer.data.local.entity.RenameTemplateEntity
import com.pictureorganizer.data.local.entity.TagEntity
import com.pictureorganizer.data.local.entity.TagTemplateEntity
import com.pictureorganizer.model.RenameTemplate
import com.pictureorganizer.model.Tag
import com.pictureorganizer.model.TagTemplate

private val converters = Converters()

fun TagEntity.toModel(): Tag =
    Tag(
        id = id,
        name = name,
        sortOrder = sortOrder,
        createdAt = createdAt,
    )

fun Tag.toEntity(): TagEntity =
    TagEntity(
        id = id,
        name = name.trim(),
        sortOrder = sortOrder,
        createdAt = createdAt,
    )

fun TagTemplateEntity.toModel(): TagTemplate =
    TagTemplate(
        id = id,
        name = name,
        tagNames = converters.fromTagsJson(tagNamesJson),
        isDefault = isDefault,
        sortOrder = sortOrder,
        createdAt = createdAt,
    )

fun TagTemplate.toEntity(): TagTemplateEntity =
    TagTemplateEntity(
        id = id,
        name = name.trim(),
        tagNamesJson = converters.toTagsJson(tagNames.map { it.trim() }.filter { it.isNotEmpty() }),
        isDefault = isDefault,
        sortOrder = sortOrder,
        createdAt = createdAt,
    )

fun RenameTemplateEntity.toModel(): RenameTemplate =
    RenameTemplate(
        id = id,
        name = name,
        pattern = pattern,
        isDefault = isDefault,
        sortOrder = sortOrder,
        createdAt = createdAt,
    )

fun RenameTemplate.toEntity(): RenameTemplateEntity =
    RenameTemplateEntity(
        id = id,
        name = name.trim(),
        pattern = pattern.trim(),
        isDefault = isDefault,
        sortOrder = sortOrder,
        createdAt = createdAt,
    )
