package com.pictureorganizer.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExportZipPackTest {
    private fun item(
        id: String,
        tags: List<String> = emptyList(),
        description: String = "$id.jpg",
    ) = ImageListItem(
        id = id,
        description = description,
        tags = tags,
        status = ImageStatus.Confirmed,
        filePath = "confirmed/$id.jpg",
    )

    @Test
    fun excludingIds_emptySet_returnsSameList() {
        val items = listOf(item("a", listOf("旅行")), item("b"))
        assertEquals(items, items.excludingIds(emptySet()))
    }

    @Test
    fun excludingIds_removesMatchingIds_keepsOrder() {
        val a = item("a", listOf("旅行"))
        val b = item("b")
        val c = item("c", listOf("家人"))
        assertEquals(listOf(a, c), listOf(a, b, c).excludingIds(setOf("b")))
    }

    @Test
    fun excludingIds_unknownId_noChange() {
        val items = listOf(item("a"))
        assertEquals(items, items.excludingIds(setOf("missing")))
    }

    @Test
    fun buildSingleExportPack_emptyMatched_returnsEmpty() {
        assertTrue(buildSingleExportPack(emptyList()).isEmpty())
    }

    @Test
    fun buildSingleExportPack_returnsAllMatchedInOrder() {
        val tagged = item("t", listOf("旅行"))
        val untagged = item("u")
        val both = item("both", listOf("旅行", "家人"))
        val items = listOf(tagged, untagged, both)
        assertEquals(items, buildSingleExportPack(items))
    }

    @Test
    fun excludeThenSinglePack_removedFromExport() {
        val both = item("both", listOf("旅行", "家人"))
        val keep = item("keep", listOf("旅行"))
        val remaining = listOf(both, keep).excludingIds(setOf("both"))
        assertEquals(listOf(keep), buildSingleExportPack(remaining))
    }

    @Test
    fun excludeAll_yieldsEmptyPack() {
        val items = listOf(item("a", listOf("旅行")), item("b"))
        val remaining = items.excludingIds(setOf("a", "b"))
        assertTrue(remaining.isEmpty())
        assertTrue(buildSingleExportPack(remaining).isEmpty())
    }
}
