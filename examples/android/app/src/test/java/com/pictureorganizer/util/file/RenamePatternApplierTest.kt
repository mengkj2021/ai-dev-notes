package com.pictureorganizer.util.file

import com.pictureorganizer.model.ImageListItem
import com.pictureorganizer.model.ImageStatus
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class RenamePatternApplierTest {
    private val zone: ZoneId = ZoneId.systemDefault()

    private fun item(
        date: String = "2026-01-15",
        dateTakenMillis: Long? = null,
        tags: List<String> = emptyList(),
        description: String = "photo.jpg",
        importedAt: Long = 0L,
    ) = ImageListItem(
        id = "id",
        date = date,
        description = description,
        tags = tags,
        status = ImageStatus.Pending,
        filePath = "pending/$description",
        importedAt = importedAt,
        dateTakenMillis = dateTakenMillis,
    )

    private fun millisOn(
        epochDay: Long,
        hour: Int = 12,
        minute: Int = 0,
        second: Int = 0,
    ): Long =
        LocalDate
            .ofEpochDay(epochDay)
            .atTime(LocalTime.of(hour, minute, second))
            .atZone(zone)
            .toInstant()
            .toEpochMilli()

    @Test
    fun apply_replacesNameDateTag_andKeepsExtension() {
        val result =
            RenamePatternApplier.apply(
                pattern = "{name}_{date}_{tag}",
                fileName = "photo.JPG",
                dateYmd = "20260903",
                userTags = listOf("旅行", "家人"),
            )
        assertEquals("photo_20260903_旅行.JPG", result)
    }

    @Test
    fun apply_emptyPattern_fallsBackToBaseName() {
        val result =
            RenamePatternApplier.apply(
                pattern = "   ",
                fileName = "a.png",
                dateYmd = "20260101",
                userTags = emptyList(),
            )
        assertEquals("a.png", result)
    }

    @Test
    fun apply_noExtension_returnsStemOnly() {
        val result =
            RenamePatternApplier.apply(
                pattern = "{tag}-{name}",
                fileName = "noext",
                dateYmd = "20260101",
                userTags = listOf("风景"),
            )
        assertEquals("风景-noext", result)
    }

    @Test
    fun apply_emptyUserTags_tagPlaceholderBecomesEmpty() {
        val result =
            RenamePatternApplier.apply(
                pattern = "{name}{tag}",
                fileName = "x.jpg",
                dateYmd = "20260101",
                userTags = emptyList(),
            )
        assertEquals("x.jpg", result)
    }

    @Test
    fun apply_usesFirstUserTagOnly() {
        val result =
            RenamePatternApplier.apply(
                pattern = "{tag}",
                fileName = "f.webp",
                dateYmd = "20260101",
                userTags = listOf("一", "二"),
            )
        assertEquals("一.webp", result)
    }

    @Test
    fun applyForItem_prefersDateTakenOverImportDate() {
        val takenDay = LocalDate.of(2020, 5, 4)
        val item =
            item(
                date = "2026-01-15",
                dateTakenMillis = millisOn(takenDay.toEpochDay()),
            )
        val result =
            RenamePatternApplier.applyForItem(
                pattern = "{date}_{name}",
                item = item,
                currentFileName = "photo.jpg",
            )
        assertEquals("20200504_photo.jpg", result)
    }

    @Test
    fun applyForItem_fallsBackToImportDateWhenNoDateTaken() {
        val item = item(date = "2026-01-15", dateTakenMillis = null)
        val result =
            RenamePatternApplier.applyForItem(
                pattern = "{date}_{name}",
                item = item,
                currentFileName = "photo.jpg",
            )
        assertEquals("20260115_photo.jpg", result)
    }

    @Test
    fun applyForItem_dateOnly_withDateTaken_notBlankStem() {
        val takenDay = LocalDate.of(2019, 12, 31)
        val item =
            item(
                date = "2026-09-05",
                dateTakenMillis = millisOn(takenDay.toEpochDay()),
            )
        val result =
            RenamePatternApplier.applyForItem(
                pattern = "{date}",
                item = item,
                currentFileName = "x.png",
            )
        assertEquals("20191231.png", result)
    }

    @Test
    fun apply_yyyyMmDd_fromDateYmd() {
        val result =
            RenamePatternApplier.apply(
                pattern = "{yyyy}-{mm}-{dd}_{name}",
                fileName = "photo.jpg",
                dateYmd = "20200504",
                userTags = emptyList(),
            )
        assertEquals("2020-05-04_photo.jpg", result)
    }

    @Test
    fun applyForItem_yyyyMmDd_sameSourceAsDate() {
        val takenDay = LocalDate.of(2020, 5, 4)
        val item =
            item(
                date = "2026-01-15",
                dateTakenMillis = millisOn(takenDay.toEpochDay()),
            )
        val result =
            RenamePatternApplier.applyForItem(
                pattern = "{date}_{yyyy}{mm}{dd}",
                item = item,
                currentFileName = "photo.jpg",
            )
        assertEquals("20200504_20200504.jpg", result)
    }

    @Test
    fun applyForItem_time_prefersDateTakenClock() {
        val takenDay = LocalDate.of(2020, 5, 4)
        val item =
            item(
                date = "2026-01-15",
                dateTakenMillis = millisOn(takenDay.toEpochDay(), hour = 9, minute = 8, second = 7),
                importedAt = millisOn(LocalDate.of(2026, 1, 15).toEpochDay(), hour = 1, minute = 2, second = 3),
            )
        val result =
            RenamePatternApplier.applyForItem(
                pattern = "{time}_{name}",
                item = item,
                currentFileName = "photo.jpg",
            )
        assertEquals("090807_photo.jpg", result)
    }

    @Test
    fun applyForItem_time_fallsBackToImportedAt() {
        val imported =
            millisOn(LocalDate.of(2026, 1, 15).toEpochDay(), hour = 18, minute = 30, second = 45)
        val item = item(date = "2026-01-15", dateTakenMillis = null, importedAt = imported)
        val result =
            RenamePatternApplier.applyForItem(
                pattern = "{time}",
                item = item,
                currentFileName = "x.png",
            )
        assertEquals("183045.png", result)
    }

    @Test
    fun apply_tags_joinsAll_andDoesNotClashWithTag() {
        val all =
            RenamePatternApplier.apply(
                pattern = "{tags}",
                fileName = "f.webp",
                dateYmd = "20260101",
                userTags = listOf("旅行", "家人"),
            )
        val first =
            RenamePatternApplier.apply(
                pattern = "{tag}",
                fileName = "f.webp",
                dateYmd = "20260101",
                userTags = listOf("旅行", "家人"),
            )
        val mixed =
            RenamePatternApplier.apply(
                pattern = "{tag}-{tags}",
                fileName = "f.webp",
                dateYmd = "20260101",
                userTags = listOf("旅行", "家人"),
            )
        assertEquals("旅行_家人.webp", all)
        assertEquals("旅行.webp", first)
        assertEquals("旅行-旅行_家人.webp", mixed)
    }

    @Test
    fun apply_emptyUserTags_tagsPlaceholderBecomesEmpty() {
        val result =
            RenamePatternApplier.apply(
                pattern = "{name}{tags}",
                fileName = "x.jpg",
                dateYmd = "20260101",
                userTags = emptyList(),
            )
        assertEquals("x.jpg", result)
    }

    @Test
    fun apply_sequence_doesNotBreakNameToken() {
        val result =
            RenamePatternApplier.apply(
                pattern = "{name}_{n}",
                fileName = "photo.jpg",
                dateYmd = "20260101",
                userTags = emptyList(),
                sequence = 12,
            )
        assertEquals("photo_12.jpg", result)
    }

    @Test
    fun applyForItem_sequenceDefaultsToOne() {
        val result =
            RenamePatternApplier.applyForItem(
                pattern = "{n}_{name}",
                item = item(),
                currentFileName = "photo.jpg",
            )
        assertEquals("1_photo.jpg", result)
    }
}
