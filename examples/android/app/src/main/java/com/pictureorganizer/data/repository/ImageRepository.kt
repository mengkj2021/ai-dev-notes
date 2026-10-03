package com.pictureorganizer.data.repository

import com.pictureorganizer.model.ImageListItem
import com.pictureorganizer.model.ImageStatus
import kotlinx.coroutines.flow.Flow

interface ImageRepository {
    fun observeItems(status: ImageStatus): Flow<List<ImageListItem>>

    fun observeItem(id: String): Flow<ImageListItem?>

    suspend fun getItems(status: ImageStatus): List<ImageListItem>

    suspend fun getItem(id: String): ImageListItem?

    suspend fun getStoredOriginalNames(): List<String>

    suspend fun moveItems(
        ids: Set<String>,
        from: ImageStatus,
        to: ImageStatus,
    )

    suspend fun deleteItems(ids: Set<String>)

    suspend fun insert(
        item: ImageListItem,
        filePath: String,
        fileName: String,
        importedAt: Long,
    )

    suspend fun migrateFlatPathsIfNeeded()

    suspend fun rename(
        id: String,
        newFileName: String,
    )

    suspend fun updateTags(
        id: String,
        tags: List<String>,
    ): Boolean

    suspend fun countImagesWithTag(tagName: String): Int

    suspend fun removeTagFromAllImages(tagName: String): Int

    suspend fun renameTagInAllImages(
        oldName: String,
        newName: String,
    ): Int
}
