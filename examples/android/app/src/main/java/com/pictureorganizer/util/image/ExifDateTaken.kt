package com.pictureorganizer.util.image

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object ExifDateTaken {
    private val EXIF_FORMAT: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss")

    private val DISPLAY_FORMAT: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    fun parseToMillis(raw: String?): Long? {
        val text = raw?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        return runCatching {
            LocalDateTime
                .parse(text, EXIF_FORMAT)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        }.getOrNull()
    }

    fun formatForDisplay(millis: Long): String =
        DISPLAY_FORMAT.format(
            java.time.Instant
                .ofEpochMilli(millis)
                .atZone(ZoneId.systemDefault()),
        )

    fun formatToExif(millis: Long): String =
        EXIF_FORMAT.format(
            java.time.Instant
                .ofEpochMilli(millis)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime(),
        )
}
