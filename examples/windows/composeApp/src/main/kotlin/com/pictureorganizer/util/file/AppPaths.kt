package com.pictureorganizer.util.file

import java.io.File

object AppPaths {
    private const val APP_DIR_NAME = "PictureOrganizer"

    fun appRoot(): File {
        val localAppData =
            System.getenv("LOCALAPPDATA")
                ?: (System.getProperty("user.home") + File.separator + "AppData" + File.separator + "Local")
        val root = File(localAppData, APP_DIR_NAME)
        if (!root.exists()) root.mkdirs()
        return root
    }
}
