package com.pictureorganizer.util.list

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ListPagingTest {
    @Test
    fun normalizePageSize_acceptsPresetsAndCustomInRange() {
        assertEquals(30, ListPaging.normalizePageSize(30))
        assertEquals(20, ListPaging.normalizePageSize(20))
        assertEquals(50, ListPaging.normalizePageSize(50))
        assertEquals(100, ListPaging.normalizePageSize(100))
        assertEquals(1, ListPaging.normalizePageSize(1))
        assertEquals(7, ListPaging.normalizePageSize(7))
        assertEquals(500, ListPaging.normalizePageSize(500))
        assertEquals(ListPaging.DEFAULT_PAGE_SIZE, ListPaging.normalizePageSize(0))
        assertEquals(ListPaging.DEFAULT_PAGE_SIZE, ListPaging.normalizePageSize(-1))
        assertEquals(ListPaging.DEFAULT_PAGE_SIZE, ListPaging.normalizePageSize(501))
    }

    @Test
    fun isValidPageSize_matchesMinMaxInclusive() {
        assertTrue(ListPaging.isValidPageSize(1))
        assertTrue(ListPaging.isValidPageSize(500))
        assertFalse(ListPaging.isValidPageSize(0))
        assertFalse(ListPaging.isValidPageSize(501))
    }

    @Test
    fun slice_whenPagingDisabled_returnsAllOnSinglePage() {
        val items = (1..45).toList()
        val result = ListPaging.slice(items, pagingEnabled = false, pageSize = 30, pageIndex = 2)
        assertEquals(items, result.pageItems)
        assertEquals(0, result.pageIndex)
        assertEquals(1, result.totalPages)
        assertEquals(45, result.totalCount)
        assertFalse(result.pagingEnabled)
    }

    @Test
    fun slice_whenPagingEnabled_slicesAndCoercesIndex() {
        val items = (1..65).toList()
        val mid = ListPaging.slice(items, pagingEnabled = true, pageSize = 30, pageIndex = 1)
        assertEquals((31..60).toList(), mid.pageItems)
        assertEquals(1, mid.pageIndex)
        assertEquals(3, mid.totalPages)
        assertTrue(mid.pagingEnabled)

        val overflow = ListPaging.slice(items, pagingEnabled = true, pageSize = 30, pageIndex = 99)
        assertEquals((61..65).toList(), overflow.pageItems)
        assertEquals(2, overflow.pageIndex)
        assertEquals(3, overflow.totalPages)
    }

    @Test
    fun slice_emptyList_stillOnePage() {
        val result = ListPaging.slice(emptyList<Int>(), pagingEnabled = true, pageSize = 20, pageIndex = 0)
        assertTrue(result.pageItems.isEmpty())
        assertEquals(0, result.pageIndex)
        assertEquals(1, result.totalPages)
        assertEquals(0, result.totalCount)
    }

    @Test
    fun slice_customPageSize_usesNormalizedValue() {
        val items = (1..40).toList()
        val result = ListPaging.slice(items, pagingEnabled = true, pageSize = 7, pageIndex = 0)
        assertEquals(items.take(7), result.pageItems)
        assertEquals(6, result.totalPages)
    }

    @Test
    fun slice_invalidPageSize_fallsBackToDefault() {
        val items = (1..40).toList()
        val result = ListPaging.slice(items, pagingEnabled = true, pageSize = 0, pageIndex = 0)
        assertEquals(items.take(30), result.pageItems)
        assertEquals(2, result.totalPages)
    }
}
