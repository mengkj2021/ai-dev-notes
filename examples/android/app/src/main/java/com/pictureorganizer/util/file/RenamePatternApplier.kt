package com.pictureorganizer.util.file

import com.pictureorganizer.model.ImageListItem
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object RenamePatternApplier {
    private val ymdFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    private val hmsFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HHmmss")

    fun apply(
        pattern: String,
        fileName: String,
        dateYmd: String,
        userTags: List<String>,
        timeHms: String = "000000",
        sequence: Int = 1,
    ): String {
        val base = fileName.substringBeforeLast('.', missingDelimiterValue = fileName)
        val ext = if (fileName.contains('.')) fileName.substringAfterLast('.') else ""
        val tag = userTags.firstOrNull().orEmpty()
        val tagsJoined = userTags.joinToString("_")
        val yyyy = dateYmd.take(4)
        val mm = dateYmd.drop(4).take(2)
        val dd = dateYmd.drop(6).take(2)
        val stem =
            pattern
                .replace("{name}", base)
                .replace("{date}", dateYmd)
                .replace("{yyyy}", yyyy)
                .replace("{mm}", mm)
                .replace("{dd}", dd)
                .replace("{time}", timeHms)
                .replace("{tags}", tagsJoined)
                .replace("{tag}", tag)
                .replace("{n}", sequence.toString())
                .trim()
                .ifEmpty { base }
        return if (ext.isNotEmpty()) "$stem.$ext" else stem
    }

    fun applyForItem(
        pattern: String,
        item: ImageListItem,
        currentFileName: String,
        sequence: Int = 1,
    ): String {
        val zone = ZoneId.systemDefault()
        val takenZoned =
            item.dateTakenMillis?.let { millis ->
                Instant.ofEpochMilli(millis).atZone(zone)
            }
        val dateYmd =
            takenZoned?.toLocalDate()?.format(ymdFormatter)
                ?: item.date.replace("-", "").ifBlank {
                    LocalDate.now(zone).format(ymdFormatter)
                }
        val timeHms =
            (takenZoned ?: Instant.ofEpochMilli(item.importedAt).atZone(zone))
                .toLocalTime()
                .format(hmsFormatter)
        return apply(
            pattern = pattern,
            fileName = currentFileName,
            dateYmd = dateYmd,
            userTags = ImageListItem.userTagsOf(item.tags),
            timeHms = timeHms,
            sequence = sequence,
        )
    }
}
