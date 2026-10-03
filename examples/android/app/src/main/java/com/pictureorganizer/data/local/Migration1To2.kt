package com.pictureorganizer.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 =
    object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `tags` (
                    `id` TEXT NOT NULL,
                    `name` TEXT NOT NULL,
                    `sortOrder` INTEGER NOT NULL,
                    `createdAt` INTEGER NOT NULL,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent(),
            )
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS `index_tags_name` ON `tags` (`name`)",
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `tag_templates` (
                    `id` TEXT NOT NULL,
                    `name` TEXT NOT NULL,
                    `tagNamesJson` TEXT NOT NULL,
                    `isDefault` INTEGER NOT NULL,
                    `sortOrder` INTEGER NOT NULL,
                    `createdAt` INTEGER NOT NULL,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent(),
            )
        }
    }
