package com.pictureorganizer.ui.imagedetail

import com.pictureorganizer.model.ImageListItem
import com.pictureorganizer.model.ImageStatus
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** S9：只按同状态全量判定是否回一览（不再看筛选可见数）。 */
class ShouldReturnAfterRemovalTest {
    private fun item(id: String) =
        ImageListItem(
            id = id,
            description = id,
            status = ImageStatus.Pending,
        )

    @Test
    fun aloneInStatus_returnsTrue() {
        assertTrue(shouldReturnAfterRemoval(listOf(item("a")), "a"))
    }

    @Test
    fun othersRemainInFullStatus_returnsFalse() {
        val siblings = listOf(item("a"), item("b"), item("c"))
        assertFalse(shouldReturnAfterRemoval(siblings, "a"))
    }

    @Test
    fun emptySiblings_returnsTrue() {
        assertTrue(shouldReturnAfterRemoval(emptyList(), "a"))
    }
}
