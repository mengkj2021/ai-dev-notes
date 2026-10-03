package com.pictureorganizer.ui.renametemplate

import org.junit.Assert.assertEquals
import org.junit.Test

class PatternTokenInsertTest {
    @Test
    fun insertAtCursor_emptySelection() {
        val result = insertAtSelection("{date}_", 7, 7, "{name}")
        assertEquals("{date}_{name}", result.text)
        assertEquals(13, result.cursor)
    }

    @Test
    fun insertAtStart() {
        val result = insertAtSelection("foo", 0, 0, "{tag}")
        assertEquals("{tag}foo", result.text)
        assertEquals(5, result.cursor)
    }

    @Test
    fun replaceSelection() {
        val result = insertAtSelection("abXYcd", 2, 4, "{date}")
        assertEquals("ab{date}cd", result.text)
        assertEquals(8, result.cursor)
    }

    @Test
    fun replaceSelection_reversedRange() {
        val result = insertAtSelection("abXYcd", 4, 2, "{name}")
        assertEquals("ab{name}cd", result.text)
        assertEquals(8, result.cursor)
    }

    @Test
    fun clampsOutOfBounds() {
        val result = insertAtSelection("hi", -1, 99, "{tag}")
        assertEquals("{tag}", result.text)
        assertEquals(5, result.cursor)
    }

    @Test
    fun tokensMatchLockedSet() {
        assertEquals(
            listOf(
                "{name}",
                "{date}",
                "{tag}",
                "{yyyy}",
                "{mm}",
                "{dd}",
                "{time}",
                "{tags}",
                "{n}",
            ),
            RenamePatternInsertTokens,
        )
    }
}
