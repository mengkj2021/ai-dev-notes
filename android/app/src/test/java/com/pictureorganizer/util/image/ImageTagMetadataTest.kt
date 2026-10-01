package com.pictureorganizer.util.image

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ImageTagMetadataTest {
    @Test
    fun mergeImportTags_union_preservesDefaultOrder_thenExifExtras() {
        val merged =
            ImageTagMetadata.mergeImportTags(
                defaultTags = listOf("默认A", "默认B"),
                exifTags = listOf("旅行", "默认A", "家人"),
            )
        assertEquals(listOf("默认A", "默认B", "旅行", "家人"), merged)
    }

    @Test
    fun mergeImportTags_stripsStatusReservedWords() {
        val merged =
            ImageTagMetadata.mergeImportTags(
                defaultTags = listOf("待归档", "待处理", "风景"),
                exifTags = listOf("已归档", "已确认", "工作", "回收站"),
            )
        assertEquals(listOf("风景", "工作"), merged)
    }

    @Test
    fun mergeImportTags_trimsAndDropsBlanks() {
        val merged =
            ImageTagMetadata.mergeImportTags(
                defaultTags = listOf("  家居  ", ""),
                exifTags = listOf("  ", "旅行"),
            )
        assertEquals(listOf("家居", "旅行"), merged)
    }

    @Test
    fun mergeImportTags_bothEmpty_yieldsEmpty() {
        assertTrue(ImageTagMetadata.mergeImportTags(emptyList(), emptyList()).isEmpty())
    }

    @Test
    fun mergeImportTags_onlyExif_works() {
        assertEquals(
            listOf("A", "B"),
            ImageTagMetadata.mergeImportTags(emptyList(), listOf("A", "B")),
        )
    }

    @Test
    fun parseUserCommentJson_plainArray() {
        assertEquals(listOf("旅行", "家人"), ImageTagMetadata.parseUserCommentJson("""["旅行","家人"]"""))
    }

    @Test
    fun parseUserCommentJson_stripsAsciiCharsetPrefixText() {
        assertEquals(
            listOf("A", "B"),
            ImageTagMetadata.parseUserCommentJson("ASCII\u0000\u0000\u0000[\"A\",\"B\"]"),
        )
    }

    @Test
    fun parseUserCommentJson_stripsUnicodeKeywordOnlyAndFindsArray() {
        assertEquals(
            listOf("旅行"),
            ImageTagMetadata.parseUserCommentJson("UNICODE[\"旅行\"]"),
        )
    }

    @Test
    fun parseUserCommentJson_handlesUnicodeEscapes() {
        assertEquals(
            listOf("旅行"),
            ImageTagMetadata.parseUserCommentJson("""["\u65c5\u884c"]"""),
        )
    }

    @Test
    fun parseUserCommentJson_emptyOrGarbage_yieldsEmpty() {
        assertTrue(ImageTagMetadata.parseUserCommentJson("").isEmpty())
        assertTrue(ImageTagMetadata.parseUserCommentJson("UNICODE").isEmpty())
        assertTrue(ImageTagMetadata.parseUserCommentJson("not-json").isEmpty())
    }

    @Test
    fun decodeUserCommentBytes_asciiPrefix() {
        val payload = """["家","庭"]""".toByteArray(Charsets.UTF_8)
        val bytes = byteArrayOf(0x41, 0x53, 0x43, 0x49, 0x49, 0, 0, 0) + payload
        assertEquals(listOf("家", "庭"), ImageTagMetadata.parseUserCommentJson(ImageTagMetadata.decodeUserCommentBytes(bytes)))
    }

    @Test
    fun decodeUserCommentBytes_unicodeUtf16Be() {
        val json = """["旅行"]"""
        val payload = json.toByteArray(Charsets.UTF_16BE)
        val header =
            byteArrayOf(
                'U'.code.toByte(),
                'N'.code.toByte(),
                'I'.code.toByte(),
                'C'.code.toByte(),
                'O'.code.toByte(),
                'D'.code.toByte(),
                'E'.code.toByte(),
                0,
            )
        val decoded = ImageTagMetadata.decodeUserCommentBytes(header + payload)
        assertEquals(listOf("旅行"), ImageTagMetadata.parseUserCommentJson(decoded))
    }

    @Test
    fun decodeUserCommentBytes_rawUtf8WithoutPrefix() {
        val bytes = """["A","B"]""".toByteArray(Charsets.UTF_8)
        assertEquals(
            listOf("A", "B"),
            ImageTagMetadata.parseUserCommentJson(ImageTagMetadata.decodeUserCommentBytes(bytes)),
        )
    }

    @Test
    fun asciiSafeJsonArray_escapesNonAscii() {
        val json = ImageTagMetadata.asciiSafeJsonArray(listOf("旅行", "AB"))
        assertTrue(json.all { it.code < 128 })
        assertEquals(listOf("旅行", "AB"), ImageTagMetadata.parseUserCommentJson(json))
    }
}
