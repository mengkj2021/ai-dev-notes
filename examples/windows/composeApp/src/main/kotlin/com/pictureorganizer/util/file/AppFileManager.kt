package com.pictureorganizer.util.file

import com.pictureorganizer.model.ImageStatus
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class AppFileManager(
    private val root: File = AppPaths.appRoot(),
) {
    fun imagesRoot(): File {
        val dir = File(root, IMAGES_DIR_NAME)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun dirFor(status: ImageStatus): File {
        val dir = File(imagesRoot(), status.toDirName())
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun ensureAllDirs() {
        ImageStatus.entries.forEach { dirFor(it) }
        exportsDir()
    }

    fun exportsDir(): File {
        val dir = File(root, EXPORTS_DIR_NAME)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun createDestFile(
        status: ImageStatus,
        fileName: String,
    ): File = File(dirFor(status), fileName)

    fun absoluteFile(relativePath: String): File = File(imagesRoot(), relativePath)

    fun relativePath(
        status: ImageStatus,
        fileName: String,
    ): String = "${status.toDirName()}/$fileName"

    fun copyFromFile(
        source: File,
        destFile: File,
    ) {
        destFile.parentFile?.mkdirs()
        Files.copy(source.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
    }

    fun moveToStatus(
        currentRelativePath: String,
        fileName: String,
        to: ImageStatus,
    ): String {
        val source = absoluteFile(currentRelativePath)
        val dest = createDestFile(to, fileName)
        if (source.absolutePath == dest.absolutePath) {
            return relativePath(to, fileName)
        }
        dest.parentFile?.mkdirs()
        if (dest.exists()) dest.delete()
        check(source.exists()) { "源文件不存在: $currentRelativePath" }
        val moved = source.renameTo(dest)
        if (!moved) {
            source.copyTo(dest, overwrite = true)
            source.delete()
        }
        return relativePath(to, fileName)
    }

    fun deleteRelative(relativePath: String) {
        val file = absoluteFile(relativePath)
        if (file.exists()) file.delete()
    }

    fun renameInPlace(
        currentRelativePath: String,
        newFileName: String,
    ): String {
        val trimmed = newFileName.trim()
        require(trimmed.isNotEmpty()) { "文件名不能为空" }
        require(!trimmed.contains('/') && !trimmed.contains('\\')) { "文件名不能包含路径分隔符" }
        require(ILLEGAL_NAME_CHARS.none { it in trimmed }) { "文件名包含非法字符" }

        val source = absoluteFile(currentRelativePath)
        require(source.exists()) { "源文件不存在" }

        val parentRel = currentRelativePath.substringBeforeLast('/', missingDelimiterValue = "")
        val newRelative = if (parentRel.isEmpty()) trimmed else "$parentRel/$trimmed"
        val dest = absoluteFile(newRelative)
        if (source.absolutePath == dest.absolutePath) return newRelative
        require(!dest.exists()) { "同目录已存在同名文件" }

        dest.parentFile?.mkdirs()
        val moved = source.renameTo(dest)
        if (!moved) {
            source.copyTo(dest, overwrite = false)
            if (!source.delete()) {
                dest.delete()
                error("重命名失败")
            }
        }
        return newRelative
    }

    companion object {
        private val ILLEGAL_NAME_CHARS = charArrayOf(':', '*', '?', '"', '<', '>', '|')
        const val IMAGES_DIR_NAME = "images"
        const val EXPORTS_DIR_NAME = "exports"
        const val DIR_PENDING = "pending"
        const val DIR_CONFIRMED = "confirmed"
        const val DIR_NO_MODIFY = "no_modify"
    }
}

fun ImageStatus.toDirName(): String =
    when (this) {
        ImageStatus.Pending -> AppFileManager.DIR_PENDING
        ImageStatus.Confirmed -> AppFileManager.DIR_CONFIRMED
        ImageStatus.NoModify -> AppFileManager.DIR_NO_MODIFY
    }
