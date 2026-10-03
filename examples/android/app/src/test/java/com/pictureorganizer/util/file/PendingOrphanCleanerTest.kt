package com.pictureorganizer.util.file

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PendingOrphanCleanerTest {
    @Test
    fun orphanFileNames_keepsKnownPendingFiles() {
        val orphans =
            PendingOrphanCleaner.orphanFileNames(
                pendingFileNames = listOf("a.jpg", "b.jpg"),
                knownRelativePaths = listOf("pending/a.jpg", "confirmed/c.jpg"),
            )
        assertEquals(listOf("b.jpg"), orphans)
    }

    @Test
    fun orphanFileNames_emptyPending_yieldsEmpty() {
        assertTrue(
            PendingOrphanCleaner
                .orphanFileNames(
                    pendingFileNames = emptyList(),
                    knownRelativePaths = listOf("pending/a.jpg"),
                ).isEmpty(),
        )
    }

    @Test
    fun orphanFileNames_allKnown_yieldsEmpty() {
        assertTrue(
            PendingOrphanCleaner
                .orphanFileNames(
                    pendingFileNames = listOf("a.jpg", "b.png"),
                    knownRelativePaths = listOf("pending/a.jpg", "pending/b.png"),
                ).isEmpty(),
        )
    }

    @Test
    fun orphanFileNames_ignoresBlankAndNestedNames() {
        val orphans =
            PendingOrphanCleaner.orphanFileNames(
                pendingFileNames = listOf("", "ok.jpg", "sub/x.jpg"),
                knownRelativePaths = listOf("pending/"),
            )
        assertEquals(listOf("ok.jpg"), orphans)
    }

    @Test
    fun orphanFileNames_ignoresNonPendingDbPaths() {
        val orphans =
            PendingOrphanCleaner.orphanFileNames(
                pendingFileNames = listOf("x.jpg"),
                knownRelativePaths = listOf("confirmed/x.jpg", "no_modify/x.jpg"),
            )
        assertEquals(listOf("x.jpg"), orphans)
    }
}
