package com.pictureorganizer.data.repository

import com.pictureorganizer.model.RenameTemplate
import kotlinx.coroutines.flow.Flow

interface RenameTemplateRepository {
    fun observeAll(): Flow<List<RenameTemplate>>

    suspend fun getAll(): List<RenameTemplate>

    suspend fun getById(id: String): RenameTemplate?

    suspend fun getDefault(): RenameTemplate?

    suspend fun insert(template: RenameTemplate)

    suspend fun update(template: RenameTemplate)

    suspend fun delete(id: String)

    suspend fun setDefault(id: String)
}
