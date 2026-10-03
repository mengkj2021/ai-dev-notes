package com.pictureorganizer.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.pictureorganizer.data.local.converter.Converters
import com.pictureorganizer.data.local.dao.ImageDao
import com.pictureorganizer.data.local.dao.RenameTemplateDao
import com.pictureorganizer.data.local.dao.TagDao
import com.pictureorganizer.data.local.dao.TagTemplateDao
import com.pictureorganizer.data.local.entity.ImageEntity
import com.pictureorganizer.data.local.entity.RenameTemplateEntity
import com.pictureorganizer.data.local.entity.TagEntity
import com.pictureorganizer.data.local.entity.TagTemplateEntity

@Database(
    entities = [
        ImageEntity::class,
        TagEntity::class,
        TagTemplateEntity::class,
        RenameTemplateEntity::class,
    ],
    version = 5,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun imageDao(): ImageDao

    abstract fun tagDao(): TagDao

    abstract fun tagTemplateDao(): TagTemplateDao

    abstract fun renameTemplateDao(): RenameTemplateDao

    companion object {
        private const val DATABASE_NAME = "picture_organizer.db"

        fun build(context: Context): AppDatabase =
            Room
                .databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME,
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                .build()
    }
}
