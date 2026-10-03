package com.pictureorganizer.util.image

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId

class ExifDateTakenTest {
    @Test
    fun parseToMillis_validExifString() {
        val raw = "2020:05:15 14:30:45"
        val expected =
            LocalDateTime
                .of(2020, 5, 15, 14, 30, 45)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        assertEquals(expected, ExifDateTaken.parseToMillis(raw))
    }

    @Test
    fun parseToMillis_nullOrBlank_returnsNull() {
        assertNull(ExifDateTaken.parseToMillis(null))
        assertNull(ExifDateTaken.parseToMillis(""))
        assertNull(ExifDateTaken.parseToMillis("   "))
    }

    @Test
    fun parseToMillis_invalidFormat_returnsNull() {
        assertNull(ExifDateTaken.parseToMillis("2020-05-15 14:30:45"))
        assertNull(ExifDateTaken.parseToMillis("not-a-date"))
        assertNull(ExifDateTaken.parseToMillis("2020:13:01 00:00:00"))
    }

    @Test
    fun parseToMillis_trimsWhitespace() {
        val raw = "  2019:01:02 03:04:05  "
        val expected =
            LocalDateTime
                .of(2019, 1, 2, 3, 4, 5)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        assertEquals(expected, ExifDateTaken.parseToMillis(raw))
    }

    @Test
    fun formatForDisplay_yyyyMmDdHhMm() {
        val millis =
            LocalDateTime
                .of(2020, 5, 15, 14, 30, 45)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        assertEquals("2020-05-15 14:30", ExifDateTaken.formatForDisplay(millis))
    }

    @Test
    fun formatToExif_roundTripsWithParse() {
        val millis =
            LocalDateTime
                .of(2020, 5, 15, 14, 30, 45)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        val raw = ExifDateTaken.formatToExif(millis)
        assertEquals("2020:05:15 14:30:45", raw)
        assertEquals(millis, ExifDateTaken.parseToMillis(raw))
    }
}
