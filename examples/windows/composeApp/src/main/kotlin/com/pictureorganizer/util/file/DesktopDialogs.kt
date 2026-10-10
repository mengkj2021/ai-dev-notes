package com.pictureorganizer.util.file

import java.awt.Component
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

object DesktopDialogs {
    private val imageFilter =
        FileNameExtensionFilter(
            "图片 (jpg, png, webp, gif, bmp)",
            "jpg",
            "jpeg",
            "png",
            "webp",
            "gif",
            "bmp",
        )

    fun pickImageFiles(parent: Component?): List<File> {
        val chooser =
            JFileChooser().apply {
                isMultiSelectionEnabled = true
                dialogTitle = "选择图片"
                fileFilter = imageFilter
                addChoosableFileFilter(imageFilter)
            }
        val result = chooser.showOpenDialog(parent)
        if (result != JFileChooser.APPROVE_OPTION) return emptyList()
        return chooser.selectedFiles?.filter { it.isFile }.orEmpty()
    }

    fun pickSaveZip(
        parent: Component?,
        suggestedName: String,
        initialDirectory: File? = null,
    ): File? {
        val chooser =
            JFileChooser().apply {
                dialogTitle = "另存为 zip"
                fileFilter = FileNameExtensionFilter("ZIP 压缩包", "zip")
                selectedFile = File(suggestedName)
                if (initialDirectory != null && initialDirectory.isDirectory) {
                    currentDirectory = initialDirectory
                }
            }
        val result = chooser.showSaveDialog(parent)
        if (result != JFileChooser.APPROVE_OPTION) return null
        var file = chooser.selectedFile ?: return null
        if (!file.name.lowercase().endsWith(".zip")) {
            file = File(file.parentFile, "${file.name}.zip")
        }
        return file
    }

    fun revealInExplorer(file: File) {
        if (!java.awt.Desktop.isDesktopSupported()) return
        val desktop = java.awt.Desktop.getDesktop()
        runCatching {
            if (file.isDirectory) {
                desktop.open(file)
            } else if (file.exists()) {
                // Windows: select file in Explorer when possible
                if (System.getProperty("os.name").orEmpty().contains("Windows", ignoreCase = true)) {
                    Runtime.getRuntime().exec(arrayOf("explorer.exe", "/select,", file.absolutePath))
                } else {
                    desktop.open(file.parentFile ?: file)
                }
            }
        }
    }
}
