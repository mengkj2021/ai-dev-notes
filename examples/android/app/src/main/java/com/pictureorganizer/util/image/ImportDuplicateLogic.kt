package com.pictureorganizer.util.image

object ImportDuplicateLogic {
    fun normalize(name: String): String = name.trim().lowercase()

    data class PartitionResult<T>(
        val autoImport: List<T>,
        val conflicts: List<T>,
    )

    fun <T> partition(
        candidates: List<T>,
        nameOf: (T) -> String,
        libraryNormalized: Set<String>,
    ): PartitionResult<T> {
        val known = libraryNormalized.toMutableSet()
        val auto = mutableListOf<T>()
        val conflicts = mutableListOf<T>()
        for (c in candidates) {
            val raw = nameOf(c)
            val key = normalize(raw)
            if (raw.isBlank() || key !in known) {
                auto += c
                if (raw.isNotBlank()) known += key
            } else {
                conflicts += c
            }
        }
        return PartitionResult(autoImport = auto, conflicts = conflicts)
    }
}
