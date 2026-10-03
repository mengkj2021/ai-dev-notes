package com.pictureorganizer.util.file

import android.content.Context
import android.net.Uri
import androidx.annotation.StringRes
import com.pictureorganizer.model.ImageStatus
import java.io.File
import java.io.FileOutputStream

class AppFileManager(
    private val context: Context,
) {
    fun getString(
        @StringRes resId: Int,
    ): String = context.getString(resId)

    fun getString(
        @StringRes resId: Int,
        vararg formatArgs: Any,
    ): String = context.getString(resId, *formatArgs)

    fun imagesRoot(): File {
        val dir = File(context.filesDir, IMAGES_DIR_NAME)
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
        val dir = File(context.filesDir, EXPORTS_DIR_NAME)
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

    fun copyFromUri(
        uri: Uri,
        destFile: File,
    ) {
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(destFile).use { output ->
                input.copyTo(output)
            }
        } ?: error("无法读取所选文件")
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
        if (!source.exists()) {
            error("源文件不存在: $currentRelativePath")
        }
        dest.parentFile?.mkdirs()
        if (dest.exists()) dest.delete()
        val moved = source.renameTo(dest)
        if (!moved) {
            source.copyTo(dest, overwrite = true)
            source.delete()
        }
        return relativePath(to, fileName)
    }

    fun deleteRelative(relativePath: String) {
        val file = absoluteFile(relativePath)
        if (file.exists()) {
            file.delete()
        }
    }

    fun cleanupPendingOrphans(knownRelativePaths: Collection<String>): Int {
        val dir = dirFor(ImageStatus.Pending)
        val names =
            dir
                .listFiles()
                ?.asSequence()
                ?.filter { it.isFile }
                ?.map { it.name }
                ?.toList()
                .orEmpty()
        val orphans = PendingOrphanCleaner.orphanFileNames(names, knownRelativePaths)
        var deleted = 0
        orphans.forEach { name ->
            val file = File(dir, name)
            if (file.exists() && file.delete()) {
                deleted++
            }
        }
        return deleted
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
        if (source.absolutePath == dest.absolutePath) {
            return newRelative
        }
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
