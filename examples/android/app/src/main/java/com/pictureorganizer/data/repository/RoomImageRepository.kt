package com.pictureorganizer.data.repository

import com.pictureorganizer.data.local.converter.Converters
import com.pictureorganizer.data.local.dao.ImageDao
import com.pictureorganizer.data.mapper.toEntity
import com.pictureorganizer.data.mapper.toListItem
import com.pictureorganizer.data.mapper.toStorage
import com.pictureorganizer.model.ImageListItem
import com.pictureorganizer.model.ImageStatus
import com.pictureorganizer.util.file.AppFileManager
import com.pictureorganizer.util.image.ImageTagMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class RoomImageRepository(
    private val imageDao: ImageDao,
    private val fileManager: AppFileManager,
) : ImageRepository {
    private val converters = Converters()

    override fun observeItems(status: ImageStatus): Flow<List<ImageListItem>> =
        imageDao.observeByStatus(status.toStorage()).map { entities ->
            entities.map { it.toListItem() }
        }

    override fun observeItem(id: String): Flow<ImageListItem?> = imageDao.observeById(id).map { it?.toListItem() }

    override suspend fun getItems(status: ImageStatus): List<ImageListItem> =
        imageDao.getByStatus(status.toStorage()).map { it.toListItem() }

    override suspend fun getItem(id: String): ImageListItem? = imageDao.findById(id)?.toListItem()

    override suspend fun getStoredOriginalNames(): List<String> =
        withContext(Dispatchers.IO) {
            imageDao.getStoredOriginalNames()
        }

    override suspend fun moveItems(
        ids: Set<String>,
        from: ImageStatus,
        to: ImageStatus,
    ) {
        if (ids.isEmpty() || from == to) return
        withContext(Dispatchers.IO) {
            for (id in ids) {
                val entity = imageDao.findById(id) ?: continue
                if (entity.status != from.toStorage()) continue
                val newPath =
                    runCatching {
                        fileManager.moveToStatus(entity.filePath, entity.fileName, to)
                    }.getOrElse {
                        fileManager.relativePath(to, entity.fileName)
                    }
                val updated = entity.toListItem().withStatus(to)
                imageDao.insert(
                    updated.toEntity(
                        filePath = newPath,
                        fileName = entity.fileName,
                        importedAt = entity.importedAt,
                    ),
                )
            }
        }
    }

    override suspend fun deleteItems(ids: Set<String>) {
        if (ids.isEmpty()) return
        withContext(Dispatchers.IO) {
            for (id in ids) {
                val entity = imageDao.findById(id) ?: continue
                fileManager.deleteRelative(entity.filePath)
            }
            imageDao.deleteByIds(ids.toList())
        }
    }

    override suspend fun insert(
        item: ImageListItem,
        filePath: String,
        fileName: String,
        importedAt: Long,
    ) {
        val withTags = item.copy(tags = ImageListItem.userTagsOf(item.tags), filePath = filePath)
        imageDao.insert(withTags.toEntity(filePath, fileName, importedAt))
    }

    override suspend fun rename(
        id: String,
        newFileName: String,
    ) {
        withContext(Dispatchers.IO) {
            val entity = imageDao.findById(id) ?: error("图片不存在")
            val resolvedName = resolveFileName(entity.fileName, newFileName)
            val newPath = fileManager.renameInPlace(entity.filePath, resolvedName)
            imageDao.insert(
                entity.copy(
                    filePath = newPath,
                    fileName = resolvedName,
                    description = resolvedName,
                ),
            )
        }
    }

    override suspend fun updateTags(
        id: String,
        tags: List<String>,
    ): Boolean =
        withContext(Dispatchers.IO) {
            val entity = imageDao.findById(id) ?: error("图片不存在")

            val normalized = ImageListItem.userTagsOf(tags)
            imageDao.insert(
                entity.copy(tagsJson = converters.toTagsJson(normalized)),
            )
            val file = fileManager.absoluteFile(entity.filePath)
            ImageTagMetadata.writeUserTags(file, normalized)
        }

    override suspend fun countImagesWithTag(tagName: String): Int =
        withContext(Dispatchers.IO) {
            val lists = imageDao.getAll().map { converters.fromTagsJson(it.tagsJson) }
            ImageListItem.countImagesWithTag(lists, tagName)
        }

    override suspend fun removeTagFromAllImages(tagName: String): Int =
        withContext(Dispatchers.IO) {
            val target = tagName.trim()
            if (target.isEmpty()) return@withContext 0
            var exifFailures = 0
            for (entity in imageDao.getAll()) {
                val tags = converters.fromTagsJson(entity.tagsJson)
                if (target !in tags) continue
                val next = ImageListItem.userTagsOf(ImageListItem.removeTagName(tags, target))
                imageDao.insert(entity.copy(tagsJson = converters.toTagsJson(next)))
                val file = fileManager.absoluteFile(entity.filePath)
                if (!ImageTagMetadata.writeUserTags(file, next)) {
                    exifFailures++
                }
            }
            exifFailures
        }

    override suspend fun renameTagInAllImages(
        oldName: String,
        newName: String,
    ): Int =
        withContext(Dispatchers.IO) {
            val old = oldName.trim()
            val new = newName.trim()
            if (old.isEmpty() || new.isEmpty() || old == new) return@withContext 0
            var exifFailures = 0
            for (entity in imageDao.getAll()) {
                val tags = converters.fromTagsJson(entity.tagsJson)
                if (old !in tags) continue
                val next = ImageListItem.userTagsOf(ImageListItem.renameTagName(tags, old, new))
                imageDao.insert(entity.copy(tagsJson = converters.toTagsJson(next)))
                val file = fileManager.absoluteFile(entity.filePath)
                if (!ImageTagMetadata.writeUserTags(file, next)) {
                    exifFailures++
                }
            }
            exifFailures
        }

    override suspend fun migrateFlatPathsIfNeeded() {
        withContext(Dispatchers.IO) {
            fileManager.ensureAllDirs()
            val all = imageDao.getAll()
            for (entity in all) {
                val existingTags = converters.fromTagsJson(entity.tagsJson)
                val cleanTags = ImageListItem.userTagsOf(existingTags)
                val cleanJson =
                    if (cleanTags == existingTags) {
                        entity.tagsJson
                    } else {
                        converters.toTagsJson(cleanTags)
                    }
                if (entity.filePath.contains('/')) {
                    if (cleanJson != entity.tagsJson) {
                        imageDao.insert(entity.copy(tagsJson = cleanJson))
                    }
                    continue
                }
                val status =
                    runCatching {
                        ImageStatus.valueOf(entity.status)
                    }.getOrDefault(ImageStatus.Pending)
                val flat = fileManager.absoluteFile(entity.filePath)
                val targetRelative = fileManager.relativePath(status, entity.fileName)
                val target = fileManager.absoluteFile(targetRelative)
                if (flat.exists() && flat.absolutePath != target.absolutePath) {
                    target.parentFile?.mkdirs()
                    if (!flat.renameTo(target)) {
                        flat.copyTo(target, overwrite = true)
                        flat.delete()
                    }
                }
                if (entity.filePath != targetRelative || cleanJson != entity.tagsJson) {
                    imageDao.insert(entity.copy(filePath = targetRelative, tagsJson = cleanJson))
                }
            }

            val root = fileManager.imagesRoot()
            root.listFiles()?.forEach { file ->
                if (!file.isFile) return@forEach
                val dest = fileManager.createDestFile(ImageStatus.Pending, file.name)
                if (dest.exists()) {
                    file.delete()
                } else {
                    val ok = file.renameTo(dest)
                    if (!ok) {
                        file.copyTo(dest, overwrite = false)
                        file.delete()
                    }
                }
            }
        }
    }

    private fun resolveFileName(
        currentFileName: String,
        requested: String,
    ): String {
        val trimmed = requested.trim()
        require(trimmed.isNotEmpty()) { "文件名不能为空" }
        val currentExt = currentFileName.substringAfterLast('.', missingDelimiterValue = "")
        val hasExt =
            trimmed.contains('.') &&
                trimmed.substringAfterLast('.').isNotEmpty() &&
                !trimmed.endsWith('.')
        return if (hasExt || currentExt.isEmpty()) {
            trimmed
        } else {
            "$trimmed.$currentExt"
        }
    }
}
