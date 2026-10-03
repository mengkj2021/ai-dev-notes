package com.pictureorganizer.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TagTemplateApplyTest {
    @Test
    fun overwrite_keepsOnlyTemplateTags_orderPreserved() {
        assertEquals(
            listOf("B1", "B2"),
            tagsFromTagTemplateOverwrite(listOf("B1", "B2")),
        )
    }

    @Test
    fun overwrite_trimsAndDropsEmptyAndReserved() {
        assertEquals(
            listOf("旅行", "家人"),
            tagsFromTagTemplateOverwrite(
                listOf(" 旅行 ", "", "待归档", "Unfiled", "家人", "   "),
            ),
        )
    }

    @Test
    fun overwrite_dedupesPreservingFirstOrder() {
        assertEquals(
            listOf("A", "B"),
            tagsFromTagTemplateOverwrite(listOf("A", "B", "A", " B ")),
        )
    }

    @Test
    fun overwrite_emptyTemplate_yieldsEmpty() {
        assertTrue(tagsFromTagTemplateOverwrite(emptyList()).isEmpty())
        assertTrue(
            tagsFromTagTemplateOverwrite(listOf("待归档", "Filed", "待处理", "Confirmed", "  ")).isEmpty(),
        )
    }

    @Test
    fun overwrite_ignoresPriorUserTags_semantics() {
        val afterA = tagsFromTagTemplateOverwrite(listOf("A1", "手改"))
        val afterB = tagsFromTagTemplateOverwrite(listOf("B1"))
        assertEquals(listOf("A1", "手改"), afterA)
        assertEquals(listOf("B1"), afterB)
    }
}
