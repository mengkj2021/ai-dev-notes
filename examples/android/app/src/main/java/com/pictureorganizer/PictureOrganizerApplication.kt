package com.pictureorganizer

import android.app.Application
import com.pictureorganizer.data.local.AppDatabase
import com.pictureorganizer.data.repository.RoomImageRepository
import com.pictureorganizer.data.repository.RoomRenameTemplateRepository
import com.pictureorganizer.data.repository.RoomTagRepository
import com.pictureorganizer.data.repository.UserPreferencesRepository
import com.pictureorganizer.util.file.AppFileManager
import com.pictureorganizer.util.log.AppLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PictureOrganizerApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database: AppDatabase by lazy { AppDatabase.build(this) }

    val fileManager: AppFileManager by lazy { AppFileManager(this) }

    val imageRepository: RoomImageRepository by lazy {
        RoomImageRepository(database.imageDao(), fileManager)
    }

    val tagRepository: RoomTagRepository by lazy {
        RoomTagRepository(database.tagDao(), database.tagTemplateDao())
    }

    val renameTemplateRepository: RoomRenameTemplateRepository by lazy {
        RoomRenameTemplateRepository(database.renameTemplateDao())
    }

    val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(this)
    }

    override fun onCreate() {
        super.onCreate()
        AppLog.i("App", "PictureOrganizerApplication onCreate")
        fileManager.ensureAllDirs()
        applicationScope.launch {
            imageRepository.migrateFlatPathsIfNeeded()
        }
    }
}
