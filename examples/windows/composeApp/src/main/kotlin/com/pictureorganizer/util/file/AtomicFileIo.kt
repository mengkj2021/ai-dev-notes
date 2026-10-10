package com.pictureorganizer.util.file

import com.pictureorganizer.util.log.AppLog
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/** 先写 `.tmp` 再原子替换，避免崩溃截断主文件。 */
fun File.writeTextAtomically(text: String) {
    parentFile?.mkdirs()
    val tmp = File(parentFile, "$name.tmp")
    tmp.writeText(text)
    try {
        Files.move(
            tmp.toPath(),
            toPath(),
            StandardCopyOption.REPLACE_EXISTING,
            StandardCopyOption.ATOMIC_MOVE,
        )
    } catch (_: java.nio.file.AtomicMoveNotSupportedException) {
        Files.move(tmp.toPath(), toPath(), StandardCopyOption.REPLACE_EXISTING)
    } finally {
        if (tmp.exists()) tmp.delete()
    }
}

inline fun <T> decodeJsonOrDefault(
    file: File,
    tag: String,
    default: () -> T,
    decode: (String) -> T,
): T {
    if (!file.exists()) return default()
    return runCatching { decode(file.readText()) }.getOrElse { err ->
        AppLog.e(tag, "corrupt or unreadable: ${file.absolutePath}", err)
        default()
    }
}
