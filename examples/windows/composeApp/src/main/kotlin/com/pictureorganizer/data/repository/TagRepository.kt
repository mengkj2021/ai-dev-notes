package com.pictureorganizer.data.repository

import com.pictureorganizer.data.local.SharedCatalog
import com.pictureorganizer.data.local.TagRecord
import com.pictureorganizer.data.local.TagTemplateRecord
import com.pictureorganizer.model.ImageListItem
import com.pictureorganizer.model.Tag
import com.pictureorganizer.model.TagTemplate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class TagRepository(
    private val catalog: SharedCatalog,
) {
    fun observeTags(): Flow<List<Tag>> =
        catalog.state.map { file ->
            file.tags
                .sortedWith(compareBy({ it.sortOrder }, { it.name }))
                .map { it.toModel() }
        }

    fun observeTemplates(): Flow<List<TagTemplate>> =
        catalog.state.map { file ->
            file.tagTemplates
                .sortedWith(compareBy({ it.sortOrder }, { it.name }))
                .map { it.toModel() }
        }

    suspend fun getTags(): List<Tag> =
        catalog.withLock { file ->
            file to
                file.tags
                    .sortedWith(compareBy({ it.sortOrder }, { it.name }))
                    .map { it.toModel() }
        }

    suspend fun insertTag(name: String): Result<Tag> =
        catalog.withLock { file ->
            val trimmed = name.trim()
            when {
                trimmed.isEmpty() -> file to Result.failure(IllegalArgumentException("标签名不能为空"))
                ImageListItem.isReservedStatusName(trimmed) ->
                    file to Result.failure(IllegalArgumentException("不能使用状态保留名"))
                file.tags.any { it.name == trimmed } ->
                    file to Result.failure(IllegalArgumentException("标签已存在"))
                else -> {
                    val tag =
                        TagRecord(
                            id = UUID.randomUUID().toString(),
                            name = trimmed,
                            sortOrder = file.tags.size,
                            createdAt = System.currentTimeMillis(),
                        )
                    file.copy(tags = file.tags + tag) to Result.success(tag.toModel())
                }
            }
        }

    suspend fun updateTag(
        id: String,
        name: String,
    ): Result<Unit> =
        catalog.withLock { file ->
            val trimmed = name.trim()
            when {
                trimmed.isEmpty() -> file to Result.failure(IllegalArgumentException("标签名不能为空"))
                ImageListItem.isReservedStatusName(trimmed) ->
                    file to Result.failure(IllegalArgumentException("不能使用状态保留名"))
                file.tags.any { it.id != id && it.name == trimmed } ->
                    file to Result.failure(IllegalArgumentException("标签已存在"))
                else -> {
                    val next = file.tags.map { if (it.id == id) it.copy(name = trimmed) else it }
                    file.copy(tags = next) to Result.success(Unit)
                }
            }
        }

    suspend fun deleteTag(id: String) {
        catalog.update { it.copy(tags = it.tags.filterNot { tag -> tag.id == id }) }
    }

    suspend fun ensureTagNames(names: Collection<String>) {
        catalog.update { file ->
            var tags = file.tags.toMutableList()
            var changed = false
            names
                .map { it.trim() }
                .filter { it.isNotEmpty() && !ImageListItem.isReservedStatusName(it) }
                .distinct()
                .forEach { name ->
                    if (tags.none { it.name == name }) {
                        tags +=
                            TagRecord(
                                id = UUID.randomUUID().toString(),
                                name = name,
                                sortOrder = tags.size,
                                createdAt = System.currentTimeMillis(),
                            )
                        changed = true
                    }
                }
            if (changed) file.copy(tags = tags) else file
        }
    }

    suspend fun insertTemplate(
        name: String,
        tagNames: List<String>,
        isDefault: Boolean,
    ): Result<TagTemplate> =
        catalog.withLock { file ->
            val trimmed = name.trim()
            if (trimmed.isEmpty()) return@withLock file to Result.failure(IllegalArgumentException("模板名不能为空"))
            if (file.tagTemplates.any { it.name == trimmed }) {
                return@withLock file to Result.failure(IllegalArgumentException("模板名已存在"))
            }
            val cleaned =
                tagNames.map { it.trim() }.filter { it.isNotEmpty() && !ImageListItem.isReservedStatusName(it) }
            var templates = file.tagTemplates
            if (isDefault) templates = templates.map { it.copy(isDefault = false) }
            val record =
                TagTemplateRecord(
                    id = UUID.randomUUID().toString(),
                    name = trimmed,
                    tagNames = cleaned,
                    isDefault = isDefault,
                    sortOrder = templates.size,
                    createdAt = System.currentTimeMillis(),
                )
            file.copy(tagTemplates = templates + record) to Result.success(record.toModel())
        }

    suspend fun updateTemplate(
        id: String,
        name: String,
        tagNames: List<String>,
        isDefault: Boolean,
    ): Result<Unit> =
        catalog.withLock { file ->
            val trimmed = name.trim()
            if (trimmed.isEmpty()) return@withLock file to Result.failure(IllegalArgumentException("模板名不能为空"))
            if (file.tagTemplates.any { it.id != id && it.name == trimmed }) {
                return@withLock file to Result.failure(IllegalArgumentException("模板名已存在"))
            }
            val cleaned =
                tagNames.map { it.trim() }.filter { it.isNotEmpty() && !ImageListItem.isReservedStatusName(it) }
            var templates = file.tagTemplates
            if (isDefault) templates = templates.map { it.copy(isDefault = false) }
            templates =
                templates.map {
                    if (it.id == id) {
                        it.copy(name = trimmed, tagNames = cleaned, isDefault = isDefault)
                    } else {
                        it
                    }
                }
            file.copy(tagTemplates = templates) to Result.success(Unit)
        }

    suspend fun deleteTemplate(id: String) {
        catalog.update { it.copy(tagTemplates = it.tagTemplates.filterNot { t -> t.id == id }) }
    }

    suspend fun setDefaultTemplate(id: String) {
        catalog.update {
            it.copy(tagTemplates = it.tagTemplates.map { t -> t.copy(isDefault = t.id == id) })
        }
    }

    private fun TagRecord.toModel() = Tag(id, name, sortOrder, createdAt)

    private fun TagTemplateRecord.toModel() =
        TagTemplate(id, name, tagNames, isDefault, sortOrder, createdAt)
}
