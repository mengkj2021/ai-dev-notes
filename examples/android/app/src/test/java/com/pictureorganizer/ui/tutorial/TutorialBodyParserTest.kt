package com.pictureorganizer.ui.tutorial

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TutorialBodyParserTest {
    @Test
    fun blank_yieldsEmpty() {
        assertTrue(parseTutorialBody("").isEmpty())
        assertTrue(parseTutorialBody("  \n\n  ").isEmpty())
    }

    @Test
    fun singleParagraph() {
        val blocks = parseTutorialBody("本应用用来在本地整理图片，核心就三步。")
        assertEquals(
            listOf(TutorialBodyBlock.Paragraph("本应用用来在本地整理图片，核心就三步。")),
            blocks,
        )
    }

    @Test
    fun introThenNumberedItems_blankSeparated() {
        val raw =
            """
            本应用用来在本地整理图片，核心就三步。

            1. 导入
            从相册或文件导入。

            2. 打标签 / 重命名
            在详情或列表里操作。
            """.trimIndent()
        assertEquals(
            listOf(
                TutorialBodyBlock.Paragraph("本应用用来在本地整理图片，核心就三步。"),
                TutorialBodyBlock.NumberedItem("1", "导入", "从相册或文件导入。"),
                TutorialBodyBlock.NumberedItem("2", "打标签 / 重命名", "在详情或列表里操作。"),
            ),
            parseTutorialBody(raw),
        )
    }

    @Test
    fun numberedItems_withoutBlankBetween() {
        val raw =
            """
            1. 导入
            从相册导入。
            2. 导出
            按标签打包。
            """.trimIndent()
        assertEquals(
            listOf(
                TutorialBodyBlock.NumberedItem("1", "导入", "从相册导入。"),
                TutorialBodyBlock.NumberedItem("2", "导出", "按标签打包。"),
            ),
            parseTutorialBody(raw),
        )
    }

    @Test
    fun numberedTitleOnly_emptyBody() {
        val blocks = parseTutorialBody("1. 三状态管理")
        assertEquals(
            listOf(TutorialBodyBlock.NumberedItem("1", "三状态管理", "")),
            blocks,
        )
    }

    @Test
    fun numberedItem_multilineBody() {
        val raw =
            """
            1. 操作只影响副本
            整理、重命名、删除只作用于副本。
            压缩也只处理副本。
            """.trimIndent()
        assertEquals(
            listOf(
                TutorialBodyBlock.NumberedItem(
                    "1",
                    "操作只影响副本",
                    "整理、重命名、删除只作用于副本。\n压缩也只处理副本。",
                ),
            ),
            parseTutorialBody(raw),
        )
    }

    @Test
    fun crlf_normalized() {
        val raw = "A few things to know first.\r\n\r\n1. On-device only\r\nThe app stays local."
        assertEquals(
            listOf(
                TutorialBodyBlock.Paragraph("A few things to know first."),
                TutorialBodyBlock.NumberedItem("1", "On-device only", "The app stays local."),
            ),
            parseTutorialBody(raw),
        )
    }
}
