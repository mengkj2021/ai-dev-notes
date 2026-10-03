package com.pictureorganizer.data.repository

import com.pictureorganizer.data.local.dao.RenameTemplateDao
import com.pictureorganizer.data.mapper.toEntity
import com.pictureorganizer.data.mapper.toModel
import com.pictureorganizer.model.RenameTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

class RoomRenameTemplateRepository(
    private val dao: RenameTemplateDao,
) : RenameTemplateRepository {
    override fun observeAll(): Flow<List<RenameTemplate>> = dao.observeAll().map { list -> list.map { it.toModel() } }

    override suspend fun getAll(): List<RenameTemplate> = dao.getAll().map { it.toModel() }

    override suspend fun getById(id: String): RenameTemplate? = dao.findById(id)?.toModel()

    override suspend fun getDefault(): RenameTemplate? = dao.findDefault()?.toModel()

    override suspend fun insert(template: RenameTemplate) =
        withContext(Dispatchers.IO) {
            val name = template.name.trim()
            val pattern = template.pattern.trim()
            require(name.isNotEmpty()) { "名称不能为空" }
            require(pattern.isNotEmpty()) { "命名规则不能为空" }
            val id = template.id.ifBlank { UUID.randomUUID().toString() }
            val createdAt = template.createdAt.takeIf { it > 0 } ?: System.currentTimeMillis()
            if (template.isDefault) dao.clearDefaults()
            dao.insert(
                template.copy(id = id, name = name, pattern = pattern, createdAt = createdAt).toEntity(),
            )
        }

    override suspend fun update(template: RenameTemplate) =
        withContext(Dispatchers.IO) {
            val name = template.name.trim()
            val pattern = template.pattern.trim()
            require(name.isNotEmpty()) { "名称不能为空" }
            require(pattern.isNotEmpty()) { "命名规则不能为空" }
            dao.findById(template.id) ?: error("模板不存在")
            if (template.isDefault) dao.clearDefaults()
            dao.update(template.copy(name = name, pattern = pattern).toEntity())
        }

    override suspend fun delete(id: String) =
        withContext(Dispatchers.IO) {
            dao.deleteById(id)
        }

    override suspend fun setDefault(id: String) =
        withContext(Dispatchers.IO) {
            val entity = dao.findById(id) ?: error("模板不存在")
            dao.clearDefaults()
            dao.update(entity.copy(isDefault = true))
        }
}
