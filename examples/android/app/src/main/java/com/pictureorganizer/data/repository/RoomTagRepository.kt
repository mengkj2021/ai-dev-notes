package com.pictureorganizer.data.repository

import com.pictureorganizer.data.local.dao.TagDao
import com.pictureorganizer.data.local.dao.TagTemplateDao
import com.pictureorganizer.data.mapper.toEntity
import com.pictureorganizer.data.mapper.toModel
import com.pictureorganizer.model.ImageListItem
import com.pictureorganizer.model.Tag
import com.pictureorganizer.model.TagTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

class RoomTagRepository(
    private val tagDao: TagDao,
    private val tagTemplateDao: TagTemplateDao,
) : TagRepository {
    override fun observeTags(): Flow<List<Tag>> = tagDao.observeAll().map { list -> list.map { it.toModel() } }

    override fun observeTemplates(): Flow<List<TagTemplate>> = tagTemplateDao.observeAll().map { list -> list.map { it.toModel() } }

    override suspend fun getTags(): List<Tag> = tagDao.getAll().map { it.toModel() }

    override suspend fun getTemplates(): List<TagTemplate> = tagTemplateDao.getAll().map { it.toModel() }

    override suspend fun getTag(id: String): Tag? = tagDao.findById(id)?.toModel()

    override suspend fun getTemplate(id: String): TagTemplate? = tagTemplateDao.findById(id)?.toModel()

    override suspend fun insertTag(tag: Tag) =
        withContext(Dispatchers.IO) {
            val name = tag.name.trim()
            requireValidTagName(name)
            require(tagDao.findByName(name) == null) { "标签已存在: $name" }
            val entity =
                tag
                    .copy(
                        id = tag.id.ifBlank { UUID.randomUUID().toString() },
                        name = name,
                        createdAt = tag.createdAt.takeIf { it > 0 } ?: System.currentTimeMillis(),
                    ).toEntity()
            tagDao.insert(entity)
        }

    override suspend fun updateTag(tag: Tag) =
        withContext(Dispatchers.IO) {
            val name = tag.name.trim()
            requireValidTagName(name)
            val existing = tagDao.findById(tag.id) ?: error("标签不存在")
            val conflict = tagDao.findByName(name)
            require(conflict == null || conflict.id == tag.id) { "标签已存在: $name" }
            tagDao.update(
                existing.copy(
                    name = name,
                    sortOrder = tag.sortOrder,
                ),
            )
        }

    private fun requireValidTagName(name: String) {
        require(name.isNotEmpty()) { "标签名不能为空" }
        require(!ImageListItem.isReservedStatusName(name)) { "标签名为状态保留字: $name" }
    }

    override suspend fun deleteTag(id: String) =
        withContext(Dispatchers.IO) {
            tagDao.deleteById(id)
        }

    override suspend fun insertTemplate(template: TagTemplate) =
        withContext(Dispatchers.IO) {
            val name = template.name.trim()
            require(name.isNotEmpty()) { "模板名不能为空" }
            require(tagTemplateDao.findByName(name) == null) { "模板已存在: $name" }
            val id = template.id.ifBlank { UUID.randomUUID().toString() }
            val createdAt = template.createdAt.takeIf { it > 0 } ?: System.currentTimeMillis()
            if (template.isDefault) {
                tagTemplateDao.clearDefaults()
            }
            tagTemplateDao.insert(
                template.copy(id = id, name = name, createdAt = createdAt).toEntity(),
            )
        }

    override suspend fun updateTemplate(template: TagTemplate) =
        withContext(Dispatchers.IO) {
            val name = template.name.trim()
            require(name.isNotEmpty()) { "模板名不能为空" }
            tagTemplateDao.findById(template.id) ?: error("模板不存在")
            val conflict = tagTemplateDao.findByName(name)
            require(conflict == null || conflict.id == template.id) { "模板已存在: $name" }
            if (template.isDefault) {
                tagTemplateDao.clearDefaults()
            }
            tagTemplateDao.update(template.copy(name = name).toEntity())
        }

    override suspend fun deleteTemplate(id: String) =
        withContext(Dispatchers.IO) {
            tagTemplateDao.deleteById(id)
        }

    override suspend fun setDefaultTemplate(id: String) =
        withContext(Dispatchers.IO) {
            val entity = tagTemplateDao.findById(id) ?: error("模板不存在")
            tagTemplateDao.clearDefaults()
            tagTemplateDao.update(entity.copy(isDefault = true))
        }
}
