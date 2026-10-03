package com.pictureorganizer.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TagNameSaveFailureTest {
    @Test
    fun classify_empty() {
        assertEquals(TagNameSaveFailure.Empty, classifyTagNameSaveFailure("", false))
        assertEquals(TagNameSaveFailure.Empty, classifyTagNameSaveFailure("   ".trim(), false))
    }

    @Test
    fun classify_reserved_before_duplicate() {
        assertEquals(
            TagNameSaveFailure.Reserved,
            classifyTagNameSaveFailure("待归档", nameTakenByOther = true),
        )
        assertEquals(
            TagNameSaveFailure.Reserved,
            classifyTagNameSaveFailure("Unfiled", nameTakenByOther = false),
        )
        assertEquals(
            TagNameSaveFailure.Reserved,
            classifyTagNameSaveFailure("待处理", nameTakenByOther = true),
        )
        assertEquals(
            TagNameSaveFailure.Reserved,
            classifyTagNameSaveFailure("Pending", nameTakenByOther = false),
        )
    }

    @Test
    fun classify_duplicate() {
        assertEquals(
            TagNameSaveFailure.Duplicate,
            classifyTagNameSaveFailure("旅行", nameTakenByOther = true),
        )
    }

    @Test
    fun classify_ok() {
        assertNull(classifyTagNameSaveFailure("旅行", nameTakenByOther = false))
    }

    @Test
    fun classifyTemplate_empty() {
        assertEquals(TagNameSaveFailure.Empty, classifyTemplateNameSaveFailure("", false))
        assertEquals(TagNameSaveFailure.Empty, classifyTemplateNameSaveFailure("   ".trim(), false))
    }

    @Test
    fun classifyTemplate_duplicate() {
        assertEquals(
            TagNameSaveFailure.Duplicate,
            classifyTemplateNameSaveFailure("出行", nameTakenByOther = true),
        )
    }

    @Test
    fun classifyTemplate_reserved_allowed() {
        assertNull(classifyTemplateNameSaveFailure("待归档", nameTakenByOther = false))
    }

    @Test
    fun classifyTemplate_ok() {
        assertNull(classifyTemplateNameSaveFailure("出行", nameTakenByOther = false))
    }
}
