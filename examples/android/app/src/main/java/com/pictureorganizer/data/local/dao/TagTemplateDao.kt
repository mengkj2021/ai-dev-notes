package com.pictureorganizer.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pictureorganizer.data.local.entity.TagTemplateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TagTemplateDao {
    @Query("SELECT * FROM tag_templates ORDER BY sortOrder ASC, name ASC")
    fun observeAll(): Flow<List<TagTemplateEntity>>

    @Query("SELECT * FROM tag_templates ORDER BY sortOrder ASC, name ASC")
    suspend fun getAll(): List<TagTemplateEntity>

    @Query("SELECT * FROM tag_templates WHERE id = :id")
    suspend fun findById(id: String): TagTemplateEntity?

    @Query("SELECT * FROM tag_templates WHERE name = :name LIMIT 1")
    suspend fun findByName(name: String): TagTemplateEntity?

    @Query("SELECT * FROM tag_templates WHERE isDefault = 1 LIMIT 1")
    suspend fun findDefault(): TagTemplateEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: TagTemplateEntity)

    @Update
    suspend fun update(entity: TagTemplateEntity)

    @Query("UPDATE tag_templates SET isDefault = 0 WHERE isDefault = 1")
    suspend fun clearDefaults()

    @Query("DELETE FROM tag_templates WHERE id = :id")
    suspend fun deleteById(id: String)
}
