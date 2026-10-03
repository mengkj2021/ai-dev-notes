package com.pictureorganizer.util.file

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExportZipNamesTest {
    @Test
    fun defaultSingleZipStem_usesDateOnly() {
        assertEquals(
            "export_20260911",
            ExportZipNames.defaultSingleZipStem("20260911"),
        )
    }

    @Test
    fun defaultZipStem_usesSanitizedLabelAndDate() {
        assertEquals(
            "export_旅行_20260911",
            ExportZipNames.defaultZipStem("旅行", "20260911"),
        )
    }

    @Test
    fun defaultZipStem_replacesIllegalCharsInLabel() {
        assertEquals(
            "export_a_b_20260911",
            ExportZipNames.defaultZipStem("a/b", "20260911"),
        )
    }

    @Test
    fun defaultZipStem_blankLabel_usesUnnamed() {
        assertEquals(
            "export_unnamed_20260911",
            ExportZipNames.defaultZipStem("   ", "20260911"),
        )
    }

    @Test
    fun normalizeZipStem_trimsAndStripsZipSuffix() {
        assertEquals("vacation", ExportZipNames.normalizeZipStem("  vacation.ZIP  "))
        assertEquals("foo", ExportZipNames.normalizeZipStem("foo"))
    }

    @Test
    fun validateZipStem_blank_isEmpty() {
        assertEquals(ZipStemError.Empty, ExportZipNames.validateZipStem("   "))
        assertEquals(ZipStemError.Empty, ExportZipNames.validateZipStem(".zip"))
    }

    @Test
    fun validateZipStem_illegalChars() {
        assertEquals(ZipStemError.IllegalChars, ExportZipNames.validateZipStem("a/b"))
        assertEquals(ZipStemError.IllegalChars, ExportZipNames.validateZipStem("a:b"))
        assertEquals(ZipStemError.IllegalChars, ExportZipNames.validateZipStem("a*b"))
    }

    @Test
    fun validateZipStem_allowsSpacesAndChinese() {
        assertNull(ExportZipNames.validateZipStem("旅行 2026"))
    }

    @Test
    fun uniqueZipFileName_noCollision_appendsZip() {
        assertEquals("foo.zip", ExportZipNames.uniqueZipFileName("foo", emptySet()))
    }

    @Test
    fun uniqueZipFileName_existing_appendsNumericSuffix() {
        val existing = setOf("foo.zip", "foo_2.zip")
        assertEquals("foo_3.zip", ExportZipNames.uniqueZipFileName("foo", existing))
    }

    @Test
    fun planZipFileNames_ready_usesCustomStems() {
        val plan =
            ExportZipNames.planZipFileNames(
                packs = listOf("旅行" to "trip", "家人" to "family"),
                existingFileNames = emptySet(),
            )
        val ready = plan as ZipNamePlan.Ready
        assertEquals("trip.zip", ready.fileNamesByLabel["旅行"])
        assertEquals("family.zip", ready.fileNamesByLabel["家人"])
    }

    @Test
    fun planZipFileNames_invalidStem_returnsFirstError() {
        val plan =
            ExportZipNames.planZipFileNames(
                packs = listOf("旅行" to "ok", "家人" to "bad:name"),
                existingFileNames = emptySet(),
            )
        val invalid = plan as ZipNamePlan.InvalidStem
        assertEquals("家人", invalid.label)
        assertEquals(ZipStemError.IllegalChars, invalid.error)
    }

    @Test
    fun planZipFileNames_duplicateStems_rejected() {
        val plan =
            ExportZipNames.planZipFileNames(
                packs = listOf("旅行" to "same", "家人" to " same.zip "),
                existingFileNames = emptySet(),
            )
        assertTrue(plan is ZipNamePlan.DuplicateStems)
    }

    @Test
    fun planZipFileNames_existingFile_getsSuffix_withoutChangingOtherPack() {
        val plan =
            ExportZipNames.planZipFileNames(
                packs = listOf("旅行" to "trip", "家人" to "family"),
                existingFileNames = setOf("trip.zip"),
            )
        val ready = plan as ZipNamePlan.Ready
        assertEquals("trip_2.zip", ready.fileNamesByLabel["旅行"])
        assertEquals("family.zip", ready.fileNamesByLabel["家人"])
    }

    @Test
    fun planZipFileNames_sequentialExistingCollision() {
        val plan =
            ExportZipNames.planZipFileNames(
                packs = listOf("A" to "pack", "B" to "pack_2"),
                existingFileNames = setOf("pack.zip"),
            )
        val ready = plan as ZipNamePlan.Ready
        assertEquals("pack_2.zip", ready.fileNamesByLabel["A"])
        assertEquals("pack_2_2.zip", ready.fileNamesByLabel["B"])
    }

    @Test
    fun planZipRename_ready() {
        val plan =
            ExportZipNames.planZipRename(
                currentFileName = "old.zip",
                rawStem = "new",
                existingFileNames = setOf("old.zip", "other.zip"),
            )
        assertEquals(ZipRenamePlan.Ready("new.zip"), plan)
    }

    @Test
    fun planZipRename_stripsZipSuffixAndTrims() {
        val plan =
            ExportZipNames.planZipRename(
                currentFileName = "old.zip",
                rawStem = "  trip.ZIP  ",
                existingFileNames = setOf("old.zip"),
            )
        assertEquals(ZipRenamePlan.Ready("trip.zip"), plan)
    }

    @Test
    fun planZipRename_unchanged_whenSameStem() {
        val plan =
            ExportZipNames.planZipRename(
                currentFileName = "trip.zip",
                rawStem = " trip.zip ",
                existingFileNames = setOf("trip.zip"),
            )
        assertTrue(plan is ZipRenamePlan.Unchanged)
    }

    @Test
    fun planZipRename_invalidEmpty() {
        val plan =
            ExportZipNames.planZipRename(
                currentFileName = "old.zip",
                rawStem = "  .zip ",
                existingFileNames = setOf("old.zip"),
            )
        assertEquals(ZipRenamePlan.Invalid(ZipStemError.Empty), plan)
    }

    @Test
    fun planZipRename_invalidIllegalChars() {
        val plan =
            ExportZipNames.planZipRename(
                currentFileName = "old.zip",
                rawStem = "a/b",
                existingFileNames = setOf("old.zip"),
            )
        assertEquals(ZipRenamePlan.Invalid(ZipStemError.IllegalChars), plan)
    }

    @Test
    fun planZipRename_collision_rejectsWithoutSuffix() {
        val plan =
            ExportZipNames.planZipRename(
                currentFileName = "old.zip",
                rawStem = "taken",
                existingFileNames = setOf("old.zip", "taken.zip"),
            )
        assertTrue(plan is ZipRenamePlan.Collision)
    }
}
