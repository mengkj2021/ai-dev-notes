package com.pictureorganizer.data.repository

import com.pictureorganizer.data.local.RenameTemplateRecord
import com.pictureorganizer.data.local.SharedCatalog
import com.pictureorganizer.model.RenameTemplate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class RenameTemplateRepository(
    private val catalog: SharedCatalog,
) {
    fun observeAll(): Flow<List<RenameTemplate>> =
        catalog.state.map { file ->
            file.renameTemplates
                .sortedWith(compareBy({ it.sortOrder }, { it.name }))
                .map { it.toModel() }
        }

    suspend fun getDefault(): RenameTemplate? =
        catalog.withLock { file ->
            file to (
                file.renameTemplates.find { it.isDefault }?.toModel()
                    ?: file.renameTemplates.minByOrNull { it.sortOrder }?.toModel()
            )
        }

    suspend fun getById(id: String): RenameTemplate? =
        catalog.withLock { file ->
            file to file.renameTemplates.find { it.id == id }?.toModel()
        }

    suspend fun insert(
        name: String,
        pattern: String,
        isDefault: Boolean,
    ): Result<RenameTemplate> =
        catalog.withLock { file ->
            val n = name.trim()
            val p = pattern.trim()
            if (n.isEmpty() || p.isEmpty()) {
                return@withLock file to Result.failure(IllegalArgumentException("名称与规则不能为空"))
            }
            var list = file.renameTemplates
            if (isDefault) list = list.map { it.copy(isDefault = false) }
            val record =
                RenameTemplateRecord(
                    id = UUID.randomUUID().toString(),
                    name = n,
                    pattern = p,
                    isDefault = isDefault,
                    sortOrder = list.size,
                    createdAt = System.currentTimeMillis(),
                )
            file.copy(renameTemplates = list + record) to Result.success(record.toModel())
        }

    suspend fun update(
        id: String,
        name: String,
        pattern: String,
        isDefault: Boolean,
    ): Result<Unit> =
        catalog.withLock { file ->
            val n = name.trim()
            val p = pattern.trim()
            if (n.isEmpty() || p.isEmpty()) {
                return@withLock file to Result.failure(IllegalArgumentException("名称与规则不能为空"))
            }
            var list = file.renameTemplates
            if (isDefault) list = list.map { it.copy(isDefault = false) }
            list =
                list.map {
                    if (it.id == id) it.copy(name = n, pattern = p, isDefault = isDefault) else it
                }
            file.copy(renameTemplates = list) to Result.success(Unit)
        }

    suspend fun delete(id: String) {
        catalog.update { it.copy(renameTemplates = it.renameTemplates.filterNot { t -> t.id == id }) }
    }

    suspend fun setDefault(id: String) {
        catalog.update {
            it.copy(renameTemplates = it.renameTemplates.map { t -> t.copy(isDefault = t.id == id) })
        }
    }

    private fun RenameTemplateRecord.toModel() =
        RenameTemplate(id, name, pattern, isDefault, sortOrder, createdAt)
}
