package com.pictureorganizer.ui.imagedetail

import com.pictureorganizer.model.ImageListItem
import com.pictureorganizer.model.ImageStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NextSiblingAfterRemovalTest {
    private fun item(id: String) =
        ImageListItem(
            id = id,
            description = id,
            status = ImageStatus.Pending,
        )

    @Test
    fun alone_returnsNull() {
        assertNull(nextSiblingIdAfterRemoval(listOf(item("a")), "a"))
    }

    @Test
    fun removeFirst_selectsOldSecond() {
        val siblings = listOf(item("a"), item("b"), item("c"))
        assertEquals("b", nextSiblingIdAfterRemoval(siblings, "a"))
    }

    @Test
    fun removeMiddle_selectsOldNext() {
        val siblings = listOf(item("a"), item("b"), item("c"))
        assertEquals("c", nextSiblingIdAfterRemoval(siblings, "b"))
    }

    @Test
    fun removeLast_selectsPrevious() {
        val siblings = listOf(item("a"), item("b"), item("c"))
        assertEquals("b", nextSiblingIdAfterRemoval(siblings, "c"))
    }

    @Test
    fun unknownId_selectsFirstRemaining() {
        val siblings = listOf(item("a"), item("b"))
        assertEquals("a", nextSiblingIdAfterRemoval(siblings, "x"))
    }
}
