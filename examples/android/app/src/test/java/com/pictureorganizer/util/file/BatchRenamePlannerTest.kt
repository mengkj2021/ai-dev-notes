package com.pictureorganizer.util.file

import com.pictureorganizer.model.ImageListItem
import com.pictureorganizer.model.ImageStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BatchRenamePlannerTest {
    private fun item(
        id: String,
        fileName: String,
        date: String = "2026-09-05",
        tags: List<String> = emptyList(),
        dateTakenMillis: Long? = null,
    ) = ImageListItem(
        id = id,
        date = date,
        description = fileName,
        tags = tags,
        status = ImageStatus.Pending,
        filePath = "pending/$fileName",
        dateTakenMillis = dateTakenMillis,
    )

    @Test
    fun plan_expandsPerItem_andSkipsUnchanged() {
        val plan =
            BatchRenamePlanner.plan(
                pattern = "{name}_{date}",
                items =
                    listOf(
                        item("a", "photo.jpg"),
                        item("b", "keep.jpg"),
                    ),
            )

        val skipPlan =
            BatchRenamePlanner.plan(
                pattern = "{name}",
                items = listOf(item("b", "keep.jpg")),
            )
        assertEquals(2, plan.renames.size)
        assertEquals("photo_20260905.jpg", plan.renames[0].targetFileName)
        assertEquals("keep_20260905.jpg", plan.renames[1].targetFileName)
        assertTrue(skipPlan.renames.isEmpty())
        assertTrue(skipPlan.collisions.isEmpty())
    }

    @Test
    fun plan_batchCollision_keepsFirst_marksRest() {
        val plan =
            BatchRenamePlanner.plan(
                pattern = "same_{date}",
                items =
                    listOf(
                        item("a", "one.jpg"),
                        item("b", "two.jpg"),
                        item("c", "three.jpg"),
                    ),
            )
        assertEquals(1, plan.renames.size)
        assertEquals("a", plan.renames[0].id)
        assertEquals("same_20260905.jpg", plan.renames[0].targetFileName)
        assertEquals(2, plan.collisions.size)
        assertEquals(listOf("b", "c"), plan.collisions.map { it.id })
        assertTrue(plan.collisions.all { it.targetFileName == "same_20260905.jpg" })
    }

    @Test
    fun plan_usesTagAndKeepsExtension() {
        val plan =
            BatchRenamePlanner.plan(
                pattern = "{tag}_{name}",
                items = listOf(item("a", "shot.PNG", tags = listOf("旅行", "家人"))),
            )
        assertEquals("旅行_shot.PNG", plan.renames.single().targetFileName)
    }

    @Test
    fun plan_assignsOneBasedSequence() {
        val plan =
            BatchRenamePlanner.plan(
                pattern = "same_{n}",
                items =
                    listOf(
                        item("a", "one.jpg"),
                        item("b", "two.jpg"),
                        item("c", "three.jpg"),
                    ),
            )
        assertEquals(
            listOf("same_1.jpg", "same_2.jpg", "same_3.jpg"),
            plan.renames.map { it.targetFileName },
        )
        assertTrue(plan.collisions.isEmpty())
    }

    @Test
    fun fileNameOf_prefersPathSegment() {
        val item =
            ImageListItem(
                id = "x",
                description = "old-label",
                status = ImageStatus.Pending,
                filePath = "confirmed/real.jpg",
            )
        assertEquals("real.jpg", BatchRenamePlanner.fileNameOf(item))
    }
}
