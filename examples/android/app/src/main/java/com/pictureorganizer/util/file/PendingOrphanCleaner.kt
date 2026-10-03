package com.pictureorganizer.util.file

object PendingOrphanCleaner {
    private const val PENDING_PREFIX = "${AppFileManager.DIR_PENDING}/"

    fun orphanFileNames(
        pendingFileNames: Collection<String>,
        knownRelativePaths: Collection<String>,
    ): List<String> {
        val knownInPending =
            knownRelativePaths
                .asSequence()
                .filter { it.startsWith(PENDING_PREFIX) }
                .map { it.removePrefix(PENDING_PREFIX) }
                .filter { it.isNotEmpty() && '/' !in it }
                .toHashSet()
        return pendingFileNames.filter { name ->
            name.isNotEmpty() && '/' !in name && name !in knownInPending
        }
    }
}
