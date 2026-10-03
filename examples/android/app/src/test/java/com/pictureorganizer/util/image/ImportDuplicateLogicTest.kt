package com.pictureorganizer.util.image

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ImportDuplicateLogicTest {
    @Test
    fun normalize_trimsAndLowercases() {
        assertEquals("img_001.jpg", ImportDuplicateLogic.normalize("  IMG_001.JPG  "))
    }

    @Test
    fun partition_caseInsensitive_libraryHit_goesToConflicts() {
        val result =
            ImportDuplicateLogic.partition(
                candidates = listOf("IMG_001.JPG", "other.png"),
                nameOf = { it },
                libraryNormalized = setOf("img_001.jpg"),
            )
        assertEquals(listOf("other.png"), result.autoImport)
        assertEquals(listOf("IMG_001.JPG"), result.conflicts)
    }

    @Test
    fun partition_batchInternalDuplicate_firstAuto_secondConflict() {
        val result =
            ImportDuplicateLogic.partition(
                candidates = listOf("a.jpg", "A.JPG", "b.png"),
                nameOf = { it },
                libraryNormalized = emptySet(),
            )
        assertEquals(listOf("a.jpg", "b.png"), result.autoImport)
        assertEquals(listOf("A.JPG"), result.conflicts)
    }

    @Test
    fun partition_blankName_neverConflicts_andDoesNotPolluteKnown() {
        val result =
            ImportDuplicateLogic.partition(
                candidates = listOf("  ", "x.jpg", ""),
                nameOf = { it },
                libraryNormalized = emptySet(),
            )
        assertEquals(listOf("  ", "x.jpg", ""), result.autoImport)
        assertTrue(result.conflicts.isEmpty())
    }

    @Test
    fun partition_emptyLibrary_allUnique_allAuto() {
        val result =
            ImportDuplicateLogic.partition(
                candidates = listOf("a.jpg", "b.jpg"),
                nameOf = { it },
                libraryNormalized = emptySet(),
            )
        assertEquals(listOf("a.jpg", "b.jpg"), result.autoImport)
        assertTrue(result.conflicts.isEmpty())
    }

    @Test
    fun partition_preservesOrderWithinBuckets() {
        val result =
            ImportDuplicateLogic.partition(
                candidates = listOf("dup.jpg", "ok1.jpg", "DUP.JPG", "ok2.jpg", "dup.jpg"),
                nameOf = { it },
                libraryNormalized = emptySet(),
            )
        assertEquals(listOf("dup.jpg", "ok1.jpg", "ok2.jpg"), result.autoImport)
        assertEquals(listOf("DUP.JPG", "dup.jpg"), result.conflicts)
    }
}
