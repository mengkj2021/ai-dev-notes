package com.pictureorganizer.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TagFilterCriteriaTagsParamTest {
    @Test
    fun encodeParse_roundTrip_plainTags() {
        val criteria =
            TagFilterCriteria(selectedTagNames = setOf("旅行", "家人"))
        val parsed = TagFilterCriteria.parseTagsParam(criteria.encodeTagsParam())
        assertEquals(setOf("旅行", "家人"), parsed)
    }

    @Test
    fun encodeParse_roundTrip_tagContainingComma() {
        val criteria =
            TagFilterCriteria(selectedTagNames = setOf("a,b", "c"))
        val encoded = criteria.encodeTagsParam()
        assertTrue(encoded.contains(TagFilterCriteria.TAGS_PARAM_SEPARATOR))
        assertEquals(setOf("a,b", "c"), TagFilterCriteria.parseTagsParam(encoded))
    }

    @Test
    fun parseTagsParam_blank_isEmpty() {
        assertTrue(TagFilterCriteria.parseTagsParam(null).isEmpty())
        assertTrue(TagFilterCriteria.parseTagsParam("").isEmpty())
        assertTrue(TagFilterCriteria.parseTagsParam("   ").isEmpty())
    }

    @Test
    fun parseTagsParam_legacyCommaSeparated_stillWorks() {
        assertEquals(
            setOf("旅行", "家人"),
            TagFilterCriteria.parseTagsParam("旅行,家人"),
        )
    }
}
