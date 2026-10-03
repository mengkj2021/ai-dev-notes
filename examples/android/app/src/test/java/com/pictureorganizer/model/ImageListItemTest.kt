package com.pictureorganizer.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ImageListItemTest {
    @Test
    fun userTagsOf_stripsStatusReservedWords() {
        val input = listOf("待归档", "旅行", "已归档", "家人", "回收站")
        assertEquals(listOf("旅行", "家人"), ImageListItem.userTagsOf(input))
    }

    @Test
    fun userTagsOf_empty_staysEmpty() {
        assertTrue(ImageListItem.userTagsOf(emptyList()).isEmpty())
    }

    @Test
    fun userTagsOf_onlyStatusTags_yieldsEmpty() {
        assertTrue(
            ImageListItem.userTagsOf(listOf("待归档", "已归档", "回收站")).isEmpty(),
        )
    }

    @Test
    fun statusTags_containsThreeReservedNames() {
        assertEquals(setOf("待归档", "已归档", "回收站"), ImageListItem.STATUS_TAGS)
    }

    @Test
    fun userTagsOf_stripsLegacyChineseKeys() {
        assertEquals(
            listOf("旅行", "家人"),
            ImageListItem.userTagsOf(listOf("待处理", "旅行", "已确认", "家人")),
        )
    }

    @Test
    fun userTagsOf_oldNoModifyWord_isNotReserved() {
        assertEquals(listOf("不修改"), ImageListItem.userTagsOf(listOf("不修改")))
    }

    @Test
    fun userTagsOf_doesNotStripLocalizedDisplayAliases() {
        assertEquals(
            listOf(
                "Pending",
                "未処理",
                "Confirmed",
                "確認済み",
                "Unfiled",
                "未分類",
                "Filed",
                "分類済み",
                "Trash",
                "ゴミ箱",
            ),
            ImageListItem.userTagsOf(
                listOf(
                    "Pending",
                    "未処理",
                    "Confirmed",
                    "確認済み",
                    "Unfiled",
                    "未分類",
                    "Filed",
                    "分類済み",
                    "Trash",
                    "ゴミ箱",
                ),
            ),
        )
    }

    @Test
    fun isReservedStatusName_blocksChineseKeysAndLocalizedAliases() {
        listOf(
            "待归档",
            "已归档",
            "待处理",
            "已确认",
            "回收站",
            "未分類",
            "分類済み",
            "未処理",
            "確認済み",
            "ゴミ箱",
            "Unfiled",
            "Filed",
            "Pending",
            "Confirmed",
            "Trash",
        ).forEach { name ->
            assertTrue("expected reserved: $name", ImageListItem.isReservedStatusName(name))
        }
        assertTrue(ImageListItem.isReservedStatusName("  Unfiled  "))
        assertFalse(ImageListItem.isReservedStatusName("旅行"))
        assertFalse(ImageListItem.isReservedStatusName("不修改"))
        assertFalse(ImageListItem.isReservedStatusName("unfiled"))
    }

    @Test
    fun withStatus_updatesStatus_andStripsStatusWordsFromTags() {
        val item =
            ImageListItem(
                id = "1",
                description = "a.jpg",
                tags = listOf("待归档", "风景"),
                status = ImageStatus.Pending,
            )
        val moved = item.withStatus(ImageStatus.Confirmed)
        assertEquals(ImageStatus.Confirmed, moved.status)
        assertEquals(listOf("风景"), moved.tags)
        assertFalse(moved.tags.any { it in ImageListItem.STATUS_TAGS })
    }

    @Test
    fun removeTagName_removesExactMatch_keepsOthers() {
        assertEquals(
            listOf("旅行", "家人"),
            ImageListItem.removeTagName(listOf("旅行", "工作", "家人"), "工作"),
        )
    }

    @Test
    fun removeTagName_missingName_unchanged() {
        val tags = listOf("旅行", "家人")
        assertEquals(tags, ImageListItem.removeTagName(tags, "工作"))
    }

    @Test
    fun removeTagName_trimsTarget_andDropsAllExactHits() {
        assertEquals(
            listOf("A"),
            ImageListItem.removeTagName(listOf("工作", "A", "工作"), "  工作  "),
        )
    }

    @Test
    fun removeTagName_blankTarget_unchanged() {
        val tags = listOf("旅行")
        assertEquals(tags, ImageListItem.removeTagName(tags, "  "))
    }

    @Test
    fun countImagesWithTag_countsExactElementMatch() {
        val images =
            listOf(
                listOf("旅行", "家人"),
                listOf("工作"),
                listOf("旅行"),
                emptyList(),
            )
        assertEquals(2, ImageListItem.countImagesWithTag(images, "旅行"))
        assertEquals(0, ImageListItem.countImagesWithTag(images, "不存在"))
    }

    @Test
    fun renameTagName_replacesExactMatch() {
        assertEquals(
            listOf("出游", "家人"),
            ImageListItem.renameTagName(listOf("旅行", "家人"), "旅行", "出游"),
        )
    }

    @Test
    fun renameTagName_newAlreadyPresent_dropsOldOnly() {
        assertEquals(
            listOf("旅行", "家人"),
            ImageListItem.renameTagName(listOf("旅行", "家人", "旧名"), "旧名", "旅行"),
        )
    }

    @Test
    fun renameTagName_missingOld_unchanged() {
        val tags = listOf("旅行", "家人")
        assertEquals(tags, ImageListItem.renameTagName(tags, "工作", "办公"))
    }

    @Test
    fun renameTagName_blankOrSame_unchanged() {
        val tags = listOf("旅行")
        assertEquals(tags, ImageListItem.renameTagName(tags, "旅行", "旅行"))
        assertEquals(tags, ImageListItem.renameTagName(tags, "  ", "新名"))
    }
}
