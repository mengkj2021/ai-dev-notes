package com.pictureorganizer.util.file

import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ZipExporter {
    fun zipFiles(
        destZip: File,
        entries: List<Pair<String, File>>,
    ): Int {
        destZip.parentFile?.mkdirs()
        if (destZip.exists()) destZip.delete()
        var count = 0
        ZipOutputStream(FileOutputStream(destZip)).use { zos ->
            val usedNames = mutableSetOf<String>()
            entries.forEach { (preferredName, file) ->
                if (!file.isFile) return@forEach
                val entryName = uniqueName(preferredName, usedNames)
                usedNames.add(entryName)
                zos.putNextEntry(ZipEntry(entryName))
                BufferedInputStream(FileInputStream(file)).use { input ->
                    input.copyTo(zos)
                }
                zos.closeEntry()
                count++
            }
        }
        return count
    }

    fun sanitizeFileToken(raw: String): String {
        val cleaned =
            raw
                .trim()
                .replace(Regex("[\\\\/:*?\"<>|]"), "_")
                .replace(Regex("\\s+"), "_")
        return cleaned.ifBlank { "unnamed" }.take(40)
    }

    private fun uniqueName(
        preferred: String,
        used: Set<String>,
    ): String {
        if (preferred !in used) return preferred
        val base = preferred.substringBeforeLast('.', preferred)
        val ext = if (preferred.contains('.')) "." + preferred.substringAfterLast('.') else ""
        var i = 2
        while (true) {
            val candidate = "${base}_$i$ext"
            if (candidate !in used) return candidate
            i++
        }
    }
}
