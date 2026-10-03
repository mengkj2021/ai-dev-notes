package com.pictureorganizer.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class ImageTagFilterTest {
    private val zone: ZoneId = ZoneId.systemDefault()

    private fun item(
        description: String = "desc.jpg",
        tags: List<String> = emptyList(),
        filePath: String = "pending/desc.jpg",
        importedAt: Long = 0L,
        dateTakenMillis: Long? = null,
    ) = ImageListItem(
        id = description,
        description = description,
        tags = tags,
        status = ImageStatus.Pending,
        filePath = filePath,
        importedAt = importedAt,
        dateTakenMillis = dateTakenMillis,
    )

    private fun millisOn(
        epochDay: Long,
        hour: Int = 12,
    ): Long =
        LocalDate
            .ofEpochDay(epochDay)
            .atTime(LocalTime.of(hour, 0))
            .atZone(zone)
            .toInstant()
            .toEpochMilli()

    @Test
    fun matchesFilter_noCriteria_alwaysTrue() {
        assertTrue(item().matchesFilter(TagFilterCriteria()))
    }

    @Test
    fun matchesFilter_tagOr_hitsAnySelected() {
        val photo = item(tags = listOf("旅行"))
        assertTrue(
            photo.matchesFilter(
                TagFilterCriteria(selectedTagNames = setOf("旅行", "家人")),
            ),
        )
        assertFalse(
            photo.matchesFilter(
                TagFilterCriteria(selectedTagNames = setOf("家人")),
            ),
        )
    }

    @Test
    fun matchesFilter_includeUntagged_ignoresStatusWordsAsUserTags() {
        val onlyStatus = item(tags = listOf("待归档"))
        assertTrue(
            onlyStatus.matchesFilter(
                TagFilterCriteria(includeUntagged = true),
            ),
        )
        val withUser = item(tags = listOf("风景"))
        assertFalse(
            withUser.matchesFilter(
                TagFilterCriteria(includeUntagged = true),
            ),
        )
    }

    @Test
    fun matchesFilter_nameContains_caseInsensitive_onDescriptionOrFileName() {
        val photo = item(description = "Holiday.JPG", filePath = "pending/Holiday.JPG")
        assertTrue(
            photo.matchesFilter(TagFilterCriteria(nameContains = "holiday")),
        )
        assertFalse(
            photo.matchesFilter(TagFilterCriteria(nameContains = "winter")),
        )
        val byPath =
            item(description = "", filePath = "pending/WinterTrip.png")
        assertTrue(
            byPath.matchesFilter(TagFilterCriteria(nameContains = "trip")),
        )
    }

    @Test
    fun matchesFilter_blankNameContains_doesNotRestrict() {
        assertTrue(
            item(description = "x.jpg").matchesFilter(
                TagFilterCriteria(nameContains = "   "),
            ),
        )
    }

    @Test
    fun sortedByFilter_importedAtDescAsc_andNameAsc() {
        val a = item(description = "b.jpg", importedAt = 10)
        val b = item(description = "A.jpg", importedAt = 30)
        val c = item(description = "c.jpg", importedAt = 20)
        val list = listOf(a, b, c)

        assertEquals(
            listOf(b, c, a),
            list.sortedByFilter(ImageListSort.ImportedAtDesc),
        )
        assertEquals(
            listOf(a, c, b),
            list.sortedByFilter(ImageListSort.ImportedAtAsc),
        )
        assertEquals(
            listOf(b, a, c).map { it.description },
            list.sortedByFilter(ImageListSort.NameAsc).map { it.description },
        )
    }

    @Test
    fun sortedByFilter_dateTaken_nullsLast_bothDirections() {
        val older = item(description = "older.jpg", dateTakenMillis = millisOn(100))
        val newer = item(description = "newer.jpg", dateTakenMillis = millisOn(200))
        val unknown = item(description = "unknown.jpg", dateTakenMillis = null)
        val list = listOf(unknown, older, newer)

        assertEquals(
            listOf(newer, older, unknown).map { it.description },
            list.sortedByFilter(ImageListSort.DateTakenDesc).map { it.description },
        )
        assertEquals(
            listOf(older, newer, unknown).map { it.description },
            list.sortedByFilter(ImageListSort.DateTakenAsc).map { it.description },
        )
    }

    @Test
    fun matchesFilter_dateTakenRange_inclusive_nullMisses_partialBounds() {
        val day = 10_000L
        val onDay = item(description = "on.jpg", dateTakenMillis = millisOn(day))
        val before = item(description = "before.jpg", dateTakenMillis = millisOn(day - 1))
        val after = item(description = "after.jpg", dateTakenMillis = millisOn(day + 1))
        val unknown = item(description = "unknown.jpg", dateTakenMillis = null)

        val bothEnds =
            TagFilterCriteria(
                dateTakenFromEpochDay = day,
                dateTakenToEpochDay = day,
            )
        assertTrue(onDay.matchesFilter(bothEnds))
        assertFalse(before.matchesFilter(bothEnds))
        assertFalse(after.matchesFilter(bothEnds))
        assertFalse(unknown.matchesFilter(bothEnds))

        val fromOnly = TagFilterCriteria(dateTakenFromEpochDay = day)
        assertTrue(onDay.matchesFilter(fromOnly))
        assertTrue(after.matchesFilter(fromOnly))
        assertFalse(before.matchesFilter(fromOnly))
        assertFalse(unknown.matchesFilter(fromOnly))

        val toOnly = TagFilterCriteria(dateTakenToEpochDay = day)
        assertTrue(onDay.matchesFilter(toOnly))
        assertTrue(before.matchesFilter(toOnly))
        assertFalse(after.matchesFilter(toOnly))
        assertFalse(unknown.matchesFilter(toOnly))
    }

    @Test
    fun matchesFilter_dateTakenRange_combinesWithNameContains() {
        val day = 20_000L
        val hit =
            item(
                description = "trip.jpg",
                dateTakenMillis = millisOn(day),
            )
        val wrongName =
            item(
                description = "other.jpg",
                dateTakenMillis = millisOn(day),
            )
        val criteria =
            TagFilterCriteria(
                nameContains = "trip",
                dateTakenFromEpochDay = day,
                dateTakenToEpochDay = day,
            )
        assertTrue(hit.matchesFilter(criteria))
        assertFalse(wrongName.matchesFilter(criteria))
    }

    @Test
    fun isActive_includesDateTakenRange() {
        assertFalse(TagFilterCriteria().isActive)
        assertTrue(TagFilterCriteria(dateTakenFromEpochDay = 1L).isActive)
        assertTrue(TagFilterCriteria(dateTakenToEpochDay = 2L).isActive)
    }

    @Test
    fun matchesFilter_importedAtRange_inclusive_partialBounds() {
        val day = 30_000L
        val onDay = item(description = "on.jpg", importedAt = millisOn(day))
        val before = item(description = "before.jpg", importedAt = millisOn(day - 1))
        val after = item(description = "after.jpg", importedAt = millisOn(day + 1))

        val bothEnds =
            TagFilterCriteria(
                importedAtFromEpochDay = day,
                importedAtToEpochDay = day,
            )
        assertTrue(onDay.matchesFilter(bothEnds))
        assertFalse(before.matchesFilter(bothEnds))
        assertFalse(after.matchesFilter(bothEnds))

        val fromOnly = TagFilterCriteria(importedAtFromEpochDay = day)
        assertTrue(onDay.matchesFilter(fromOnly))
        assertTrue(after.matchesFilter(fromOnly))
        assertFalse(before.matchesFilter(fromOnly))

        val toOnly = TagFilterCriteria(importedAtToEpochDay = day)
        assertTrue(onDay.matchesFilter(toOnly))
        assertTrue(before.matchesFilter(toOnly))
        assertFalse(after.matchesFilter(toOnly))
    }

    @Test
    fun matchesFilter_importedAtAndDateTaken_areAnd() {
        val day = 40_000L
        val bothOk =
            item(
                description = "ok.jpg",
                importedAt = millisOn(day),
                dateTakenMillis = millisOn(day),
            )
        val wrongImport =
            item(
                description = "bad-import.jpg",
                importedAt = millisOn(day + 1),
                dateTakenMillis = millisOn(day),
            )
        val wrongTaken =
            item(
                description = "bad-taken.jpg",
                importedAt = millisOn(day),
                dateTakenMillis = millisOn(day + 1),
            )
        val criteria =
            TagFilterCriteria(
                importedAtFromEpochDay = day,
                importedAtToEpochDay = day,
                dateTakenFromEpochDay = day,
                dateTakenToEpochDay = day,
            )
        assertTrue(bothOk.matchesFilter(criteria))
        assertFalse(wrongImport.matchesFilter(criteria))
        assertFalse(wrongTaken.matchesFilter(criteria))
    }

    @Test
    fun isActive_includesImportedAtRange() {
        assertTrue(TagFilterCriteria(importedAtFromEpochDay = 1L).isActive)
        assertTrue(TagFilterCriteria(importedAtToEpochDay = 2L).isActive)
    }
}
