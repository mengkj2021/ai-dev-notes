package com.pictureorganizer.data.mapper

import com.pictureorganizer.data.local.converter.Converters
import com.pictureorganizer.data.local.entity.ImageEntity
import com.pictureorganizer.model.ImageListItem
import com.pictureorganizer.model.ImageStatus
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val converters = Converters()

private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

private val PLACEHOLDER_COLORS =
    longArrayOf(
        0xFFE3F2FD,
        0xFFF3E5F5,
        0xFFE8F5E9,
        0xFFFFF3E0,
        0xFFFCE4EC,
        0xFFE0F7FA,
        0xFFF1F8E9,
        0xFFFFEBEE,
        0xFFE8EAF6,
        0xFFFFF8E1,
        0xFFE0F2F1,
        0xFFFBE9E7,
    )

fun ImageEntity.toListItem(): ImageListItem {
    val statusEnum = status.toImageStatus()
    val tags = converters.fromTagsJson(tagsJson)
    return ImageListItem(
        id = id,
        date =
            dateFormatter.format(
                Instant.ofEpochMilli(importedAt).atZone(ZoneId.systemDefault()),
            ),
        description = description.ifBlank { fileName },
        tags = tags,
        placeholderColorArgb =
            PLACEHOLDER_COLORS[
                (id.hashCode().and(Int.MAX_VALUE)) % PLACEHOLDER_COLORS.size,
            ],
        status = statusEnum,
        filePath = filePath,
        importedAt = importedAt,
        originalName = originalName,
        dateTakenMillis = dateTakenMillis,
    )
}

fun ImageListItem.toEntity(
    filePath: String,
    fileName: String,
    importedAt: Long,
): ImageEntity =
    ImageEntity(
        id = id,
        filePath = filePath,
        fileName = fileName,
        description = description,
        status = status.name,
        importedAt = importedAt,
        tagsJson = converters.toTagsJson(tags),
        originalName = originalName,
        dateTakenMillis = dateTakenMillis,
    )

fun ImageStatus.toStorage(): String = name

fun String.toImageStatus(): ImageStatus = runCatching { ImageStatus.valueOf(this) }.getOrDefault(ImageStatus.Pending)
