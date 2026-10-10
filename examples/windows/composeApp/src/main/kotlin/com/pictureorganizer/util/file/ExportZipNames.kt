package com.pictureorganizer.util.file

import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object ExportZipNames {
    private val illegalChars = charArrayOf('\\', '/', ':', '*', '?', '"', '<', '>', '|')

    fun defaultSingleZipStem(dateToken: String = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)): String =
        "export_$dateToken"

    fun normalizeZipStem(raw: String): String {
        var stem = raw.trim()
        if (stem.endsWith(".zip", ignoreCase = true)) {
            stem = stem.dropLast(4).trimEnd()
        }
        return stem
    }

    fun validateZipStem(raw: String): String? {
        val stem = normalizeZipStem(raw)
        if (stem.isEmpty()) return "文件名不能为空"
        if (illegalChars.any { it in stem }) return "文件名包含非法字符"
        return null
    }

    fun uniqueZipFileName(
        stem: String,
        exportsDir: File,
    ): String {
        var name = "$stem.zip"
        var i = 2
        while (File(exportsDir, name).exists()) {
            name = "${stem}_$i.zip"
            i++
        }
        return name
    }

    sealed interface ZipRenamePlan {
        data class Ready(
            val newFileName: String,
        ) : ZipRenamePlan

        data class Invalid(
            val message: String,
        ) : ZipRenamePlan

        data object Collision : ZipRenamePlan

        data object Unchanged : ZipRenamePlan
    }

    fun planZipRename(
        currentFileName: String,
        rawStem: String,
        existingFileNames: Set<String>,
    ): ZipRenamePlan {
        val err = validateZipStem(rawStem)
        if (err != null) return ZipRenamePlan.Invalid(err)
        val stem = normalizeZipStem(rawStem)
        val newName = "$stem.zip"
        if (newName.equals(currentFileName, ignoreCase = true)) return ZipRenamePlan.Unchanged
        if (existingFileNames.any { it.equals(newName, ignoreCase = true) }) return ZipRenamePlan.Collision
        return ZipRenamePlan.Ready(newName)
    }
}
