package com.pictureorganizer.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pictureorganizer.data.local.entity.RenameTemplateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RenameTemplateDao {
    @Query("SELECT * FROM rename_templates ORDER BY sortOrder ASC, name ASC")
    fun observeAll(): Flow<List<RenameTemplateEntity>>

    @Query("SELECT * FROM rename_templates ORDER BY sortOrder ASC, name ASC")
    suspend fun getAll(): List<RenameTemplateEntity>

    @Query("SELECT * FROM rename_templates WHERE id = :id")
    suspend fun findById(id: String): RenameTemplateEntity?

    @Query("SELECT * FROM rename_templates WHERE isDefault = 1 LIMIT 1")
    suspend fun findDefault(): RenameTemplateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: RenameTemplateEntity)

    @Update
    suspend fun update(entity: RenameTemplateEntity)

    @Query("UPDATE rename_templates SET isDefault = 0 WHERE isDefault = 1")
    suspend fun clearDefaults()

    @Query("DELETE FROM rename_templates WHERE id = :id")
    suspend fun deleteById(id: String)
}
