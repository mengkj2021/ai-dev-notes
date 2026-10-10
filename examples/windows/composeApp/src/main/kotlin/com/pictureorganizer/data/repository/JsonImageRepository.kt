package com.pictureorganizer.data.repository

import com.pictureorganizer.data.local.ImageRecord
import com.pictureorganizer.data.local.JsonImageStore
import com.pictureorganizer.data.local.toImageStatus
import com.pictureorganizer.data.local.toStorageKey
import com.pictureorganizer.model.ImageListItem
import com.pictureorganizer.model.ImageStatus
import com.pictureorganizer.util.file.AppFileManager
import com.pictureorganizer.util.image.ImageTagMetadata
import com.pictureorganizer.util.log.AppLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class JsonImageRepository(
    private val store: JsonImageStore = JsonImageStore(),
    private val files: AppFileManager = AppFileManager(),
) : ImageRepository {
    private val mutex = Mutex()
    private val records = MutableStateFlow(store.readAll())

    init {
        files.ensureAllDirs()
    }

    override fun observeItems(status: ImageStatus): Flow<List<ImageListItem>> =
        records.map { list ->
            list
                .filter { it.status.toImageStatus() == status }
                .sortedByDescending { it.importedAt }
                .map { it.toListItem() }
        }

    override fun observeItem(id: String): Flow<ImageListItem?> =
        records.map { list -> list.find { it.id == id }?.toListItem() }

    override suspend fun getItems(status: ImageStatus): List<ImageListItem> =
        mutex.withLock {
            records.value
                .filter { it.status.toImageStatus() == status }
                .sortedByDescending { it.importedAt }
                .map { it.toListItem() }
        }

    override suspend fun getItem(id: String): ImageListItem? =
        mutex.withLock { records.value.find { it.id == id }?.toListItem() }

    override suspend fun getAllItems(): List<ImageListItem> =
        mutex.withLock { records.value.map { it.toListItem() } }

    override suspend fun getStoredOriginalNames(): List<String> =
        mutex.withLock {
            records.value.mapNotNull { it.originalName?.takeIf { n -> n.isNotBlank() } }
        }

    override suspend fun countImagesWithTag(tagName: String): Int =
        mutex.withLock {
            ImageListItem.countImagesWithTag(records.value.map { it.tags }, tagName)
        }

    override suspend fun removeTagFromAllImages(tagName: String): Int {
        val target = tagName.trim()
        if (target.isEmpty()) return 0
        return withContext(Dispatchers.IO) {
            mutex.withLock {
                var count = 0
                val next =
                    records.value.map { record ->
                        if (target in record.tags) {
                            count++
                            val updated = record.copy(tags = ImageListItem.removeTagName(record.tags, target))
                            ImageTagMetadata.writeUserTags(files.absoluteFile(updated.filePath), updated.tags)
                            updated
                        } else {
                            record
                        }
                    }
                if (count > 0) persist(next)
                count
            }
        }
    }

    override suspend fun renameTagInAllImages(
        oldName: String,
        newName: String,
    ): Int {
        val old = oldName.trim()
        val new = newName.trim()
        if (old.isEmpty() || new.isEmpty() || old == new) return 0
        return withContext(Dispatchers.IO) {
            mutex.withLock {
                var count = 0
                val next =
                    records.value.map { record ->
                        if (old in record.tags) {
                            count++
                            val updated = record.copy(tags = ImageListItem.renameTagName(record.tags, old, new))
                            ImageTagMetadata.writeUserTags(files.absoluteFile(updated.filePath), updated.tags)
                            updated
                        } else {
                            record
                        }
                    }
                if (count > 0) persist(next)
                count
            }
        }
    }

    override suspend fun moveItems(
        ids: Set<String>,
        from: ImageStatus,
        to: ImageStatus,
    ) {
        if (ids.isEmpty() || from == to) return
        withContext(Dispatchers.IO) {
            mutex.withLock {
                val next =
                    records.value.mapNotNull { record ->
                        if (record.id !in ids || record.status.toImageStatus() != from) {
                            record
                        } else {
                            runCatching {
                                val newPath = files.moveToStatus(record.filePath, record.fileName, to)
                                record.copy(status = to.toStorageKey(), filePath = newPath)
                            }.getOrElse { err ->
                                AppLog.e("JsonImageRepository", "move failed ${record.id}", err)
                                record
                            }
                        }
                    }
                persist(next)
            }
        }
    }

    override suspend fun deleteItems(ids: Set<String>) {
        if (ids.isEmpty()) return
        withContext(Dispatchers.IO) {
            mutex.withLock {
                val keep = mutableListOf<ImageRecord>()
                records.value.forEach { record ->
                    if (record.id in ids) {
                        files.deleteRelative(record.filePath)
                    } else {
                        keep += record
                    }
                }
                persist(keep)
            }
        }
    }

    override suspend fun insert(
        item: ImageListItem,
        filePath: String,
        fileName: String,
        importedAt: Long,
    ) {
        insertAll(listOf(ImageInsert(item, filePath, fileName, importedAt)))
    }

    override suspend fun insertAll(entries: List<ImageInsert>) {
        if (entries.isEmpty()) return
        withContext(Dispatchers.IO) {
            mutex.withLock {
                val appended =
                    entries.map { entry ->
                        ImageRecord(
                            id = entry.item.id,
                            filePath = entry.filePath,
                            fileName = entry.fileName,
                            description = entry.item.description,
                            status = entry.item.status.toStorageKey(),
                            importedAt = entry.importedAt,
                            tags = ImageListItem.userTagsOf(entry.item.tags),
                            originalName = entry.item.originalName,
                            dateTakenMillis = entry.item.dateTakenMillis,
                        )
                    }
                persist(records.value + appended)
            }
        }
    }

    override suspend fun rename(
        id: String,
        newFileName: String,
    ) {
        withContext(Dispatchers.IO) {
            mutex.withLock {
                val next =
                    records.value.map { record ->
                        if (record.id != id) {
                            record
                        } else {
                            val newPath = files.renameInPlace(record.filePath, newFileName)
                            record.copy(
                                filePath = newPath,
                                fileName = newFileName,
                                description = newFileName.substringBeforeLast('.').ifBlank { newFileName },
                            )
                        }
                    }
                persist(next)
            }
        }
    }

    override suspend fun updateTags(
        id: String,
        tags: List<String>,
    ): Boolean {
        return withContext(Dispatchers.IO) {
            mutex.withLock {
                var changed = false
                val next =
                    records.value.map { record ->
                        if (record.id != id) {
                            record
                        } else {
                            changed = true
                            val cleaned = ImageListItem.userTagsOf(tags)
                            ImageTagMetadata.writeUserTags(files.absoluteFile(record.filePath), cleaned)
                            record.copy(tags = cleaned)
                        }
                    }
                if (changed) persist(next)
                changed
            }
        }
    }

    private fun persist(next: List<ImageRecord>) {
        store.writeAll(next)
        records.value = next.toMutableList()
    }

    private fun ImageRecord.toListItem(): ImageListItem =
        ImageListItem(
            id = id,
            description = description,
            tags = tags,
            status = status.toImageStatus(),
            filePath = filePath,
            importedAt = importedAt,
            originalName = originalName,
            dateTakenMillis = dateTakenMillis,
        )
}
