package com.pictureorganizer

import com.pictureorganizer.data.local.SharedCatalog
import com.pictureorganizer.data.repository.JsonImageRepository
import com.pictureorganizer.data.repository.RenameTemplateRepository
import com.pictureorganizer.data.repository.TagRepository
import com.pictureorganizer.data.repository.UserPreferencesRepository
import com.pictureorganizer.util.file.AppFileManager

class AppContainer {
    val files = AppFileManager()
    val catalog = SharedCatalog()
    val images = JsonImageRepository(files = files)
    val tags = TagRepository(catalog)
    val renameTemplates = RenameTemplateRepository(catalog)
    val prefs = UserPreferencesRepository()

    init {
        files.ensureAllDirs()
    }
}
