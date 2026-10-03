package com.pictureorganizer.util.file

enum class ZipStemError {
    Empty,
    IllegalChars,
}

sealed interface ZipNamePlan {
    data class Ready(
        val fileNamesByLabel: Map<String, String>,
    ) : ZipNamePlan

    data class InvalidStem(
        val label: String,
        val error: ZipStemError,
    ) : ZipNamePlan

    data object DuplicateStems : ZipNamePlan
}

sealed interface ZipRenamePlan {
    data class Ready(
        val newFileName: String,
    ) : ZipRenamePlan

    data class Invalid(
        val error: ZipStemError,
    ) : ZipRenamePlan

    data object Collision : ZipRenamePlan

    data object Unchanged : ZipRenamePlan
}

object ExportZipNames {
    private val illegalChars = charArrayOf('\\', '/', ':', '*', '?', '"', '<', '>', '|')

    fun defaultZipStem(
        label: String,
        dateToken: String,
    ): String = "export_${ZipExporter.sanitizeFileToken(label)}_$dateToken"

    fun defaultSingleZipStem(dateToken: String): String = "export_$dateToken"

    fun normalizeZipStem(raw: String): String {
        var stem = raw.trim()
        if (stem.endsWith(".zip", ignoreCase = true)) {
            stem = stem.dropLast(4).trimEnd()
        }
        return stem
    }

    fun validateZipStem(raw: String): ZipStemError? {
        val stem = normalizeZipStem(raw)
        if (stem.isEmpty()) return ZipStemError.Empty
        if (illegalChars.any { it in stem }) return ZipStemError.IllegalChars
        return null
    }

    fun uniqueZipFileName(
        stem: String,
        existingFileNames: Set<String>,
    ): String {
        val zip = "$stem.zip"
        if (zip !in existingFileNames) return zip
        var i = 2
        while (true) {
            val candidate = "${stem}_$i.zip"
            if (candidate !in existingFileNames) return candidate
            i++
        }
    }

    fun planZipFileNames(
        packs: List<Pair<String, String>>,
        existingFileNames: Set<String>,
    ): ZipNamePlan {
        val normalized = mutableListOf<Pair<String, String>>()
        val seenStems = mutableSetOf<String>()
        for ((label, raw) in packs) {
            val stem = normalizeZipStem(raw)
            val error = validateZipStem(raw)
            if (error != null) {
                return ZipNamePlan.InvalidStem(label, error)
            }
            if (stem in seenStems) {
                return ZipNamePlan.DuplicateStems
            }
            seenStems.add(stem)
            normalized.add(label to stem)
        }
        val used = existingFileNames.toMutableSet()
        val out = linkedMapOf<String, String>()
        for ((label, stem) in normalized) {
            val fileName = uniqueZipFileName(stem, used)
            used.add(fileName)
            out[label] = fileName
        }
        return ZipNamePlan.Ready(out)
    }

    fun planZipRename(
        currentFileName: String,
        rawStem: String,
        existingFileNames: Set<String>,
    ): ZipRenamePlan {
        val error = validateZipStem(rawStem)
        if (error != null) return ZipRenamePlan.Invalid(error)
        val stem = normalizeZipStem(rawStem)
        val newFileName = "$stem.zip"
        if (stem == normalizeZipStem(currentFileName)) {
            return ZipRenamePlan.Unchanged
        }
        if (newFileName in existingFileNames) {
            return ZipRenamePlan.Collision
        }
        return ZipRenamePlan.Ready(newFileName)
    }
}
