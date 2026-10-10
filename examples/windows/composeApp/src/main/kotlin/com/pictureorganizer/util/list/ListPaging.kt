package com.pictureorganizer.util.list

import kotlin.math.max

data class ListPagingResult<T>(
    val pageItems: List<T>,
    val pageIndex: Int,
    val totalPages: Int,
    val totalCount: Int,
    val pagingEnabled: Boolean,
)

object ListPaging {
    const val DEFAULT_PAGE_SIZE = 30
    const val MIN_PAGE_SIZE = 1
    const val MAX_PAGE_SIZE = 500

    val PRESET_PAGE_SIZES: List<Int> = listOf(20, 30, 50, 100)

    fun isValidPageSize(size: Int): Boolean = size in MIN_PAGE_SIZE..MAX_PAGE_SIZE

    fun normalizePageSize(size: Int): Int = if (isValidPageSize(size)) size else DEFAULT_PAGE_SIZE

    fun <T> slice(
        items: List<T>,
        pagingEnabled: Boolean,
        pageSize: Int,
        pageIndex: Int,
    ): ListPagingResult<T> {
        val totalCount = items.size
        if (!pagingEnabled) {
            return ListPagingResult(
                pageItems = items,
                pageIndex = 0,
                totalPages = 1,
                totalCount = totalCount,
                pagingEnabled = false,
            )
        }
        val size = normalizePageSize(pageSize)
        val totalPages = max(1, (totalCount + size - 1) / size)
        val index = pageIndex.coerceIn(0, totalPages - 1)
        val pageItems = items.drop(index * size).take(size)
        return ListPagingResult(
            pageItems = pageItems,
            pageIndex = index,
            totalPages = totalPages,
            totalCount = totalCount,
            pagingEnabled = true,
        )
    }
}
