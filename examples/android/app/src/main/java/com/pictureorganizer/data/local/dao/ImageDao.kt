package com.pictureorganizer.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pictureorganizer.data.local.entity.ImageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ImageDao {
    @Query("SELECT * FROM images WHERE status = :status ORDER BY importedAt DESC")
    fun observeByStatus(status: String): Flow<List<ImageEntity>>

    @Query("SELECT * FROM images WHERE status = :status ORDER BY importedAt DESC")
    suspend fun getByStatus(status: String): List<ImageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ImageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<ImageEntity>)

    @Query("DELETE FROM images WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)

    @Query("SELECT * FROM images WHERE id = :id")
    suspend fun findById(id: String): ImageEntity?

    @Query("SELECT * FROM images WHERE id = :id")
    fun observeById(id: String): Flow<ImageEntity?>

    @Query("SELECT * FROM images")
    suspend fun getAll(): List<ImageEntity>

    @Query("SELECT originalName FROM images WHERE originalName IS NOT NULL AND originalName != ''")
    suspend fun getStoredOriginalNames(): List<String>
}
