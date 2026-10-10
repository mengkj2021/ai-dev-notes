package com.pictureorganizer.util.image

import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.exif.ExifSubIFDDirectory
import com.pictureorganizer.model.ImageListItem
import com.pictureorganizer.util.log.AppLog
import org.apache.commons.imaging.Imaging
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.Charset

object ImageTagMetadata {
    private val ASCII_PREFIX =
        byteArrayOf(0x41, 0x53, 0x43, 0x49, 0x49, 0, 0, 0) // "ASCII\0\0\0"
    private val UNICODE_PREFIX =
        byteArrayOf(0x55, 0x4E, 0x49, 0x43, 0x4F, 0x44, 0x45, 0) // "UNICODE\0"
    private val JIS_PREFIX =
        byteArrayOf(0x4A, 0x49, 0x53, 0, 0, 0, 0, 0) // "JIS\0\0\0\0"
    private val UNDEFINED_PREFIX = ByteArray(8)

    fun isJpeg(file: File): Boolean {
        val name = file.name.lowercase()
        return name.endsWith(".jpg") || name.endsWith(".jpeg")
    }

    fun writeUserTags(
        file: File,
        tags: List<String>,
    ): Boolean = writeImportMetadata(file, tags, dateTakenMillis = null, writeTagsAlways = true)

    fun writeImportMetadata(
        file: File,
        tags: List<String>,
        dateTakenMillis: Long?,
        writeTagsAlways: Boolean = false,
    ): Boolean {
        if (!file.exists() || !file.isFile || !isJpeg(file)) return false
        return runCatching {
            val userTags = ImageListItem.userTagsOf(tags)
            val writeTags = writeTagsAlways || userTags.isNotEmpty()
            val writeDate = dateTakenMillis != null
            if (!writeTags && !writeDate) return false

            val outputSet = readOutputSet(file) ?: TiffOutputSet()
            val exifDir = outputSet.getOrCreateExifDirectory()
            if (writeTags) {
                exifDir.removeField(ExifTagConstants.EXIF_TAG_USER_COMMENT)
                // TagInfoGpsText：库内会按 Exif 文本格式编码；写入 JSON 数组字符串
                exifDir.add(ExifTagConstants.EXIF_TAG_USER_COMMENT, asciiSafeJsonArray(userTags))
            }
            if (writeDate) {
                val millis = dateTakenMillis
                if (millis != null) {
                    exifDir.removeField(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL)
                    exifDir.add(
                        ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL,
                        ExifDateTaken.formatToExif(millis),
                    )
                }
            }
            rewriteExif(file, outputSet)
            true
        }.onFailure { AppLog.e("Exif", "write failed ${file.name}", it) }
            .getOrDefault(false)
    }

    fun readUserTags(file: File): List<String> {
        if (!file.exists() || !file.isFile) return emptyList()
        return runCatching {
            val metadata = ImageMetadataReader.readMetadata(file)
            val dir = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory::class.java) ?: return emptyList()
            val raw =
                dir.getString(ExifSubIFDDirectory.TAG_USER_COMMENT)
                    ?: dir.getByteArray(ExifSubIFDDirectory.TAG_USER_COMMENT)?.let { decodeUserCommentBytes(it) }
                    ?: return emptyList()
            parseUserCommentJson(raw)
        }.getOrDefault(emptyList())
    }

    fun readDateTakenMillis(file: File): Long? {
        if (!file.exists() || !file.isFile) return null
        return runCatching {
            val metadata = ImageMetadataReader.readMetadata(file)
            val dir = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory::class.java) ?: return null
            val date = dir.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL) ?: return null
            date.time
        }.getOrElse {
            runCatching {
                val metadata = ImageMetadataReader.readMetadata(file)
                val dir = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory::class.java) ?: return null
                ExifDateTaken.parseToMillis(dir.getString(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL))
            }.getOrNull()
        }
    }

    fun mergeImportTags(
        defaultTags: List<String>,
        exifTags: List<String>,
    ): List<String> {
        val cleaned =
            (defaultTags + exifTags)
                .map { it.trim() }
                .filter { it.isNotEmpty() }
        return ImageListItem.userTagsOf(cleaned).distinct()
    }

    fun asciiSafeJsonArray(tags: List<String>): String =
        tags.joinToString(prefix = "[", postfix = "]", separator = ",") { tag ->
            "\"${escapeJsonString(tag)}\""
        }

    fun parseUserCommentJson(raw: String): List<String> {
        val payload = extractJsonArrayPayload(raw) ?: return emptyList()
        return runCatching {
            ImageListItem.userTagsOf(parseJsonStringArray(payload))
        }.getOrDefault(emptyList())
    }

    fun decodeUserCommentBytes(bytes: ByteArray): String {
        if (bytes.isEmpty()) return ""
        return when {
            startsWith(bytes, ASCII_PREFIX) ->
                String(bytes, ASCII_PREFIX.size, bytes.size - ASCII_PREFIX.size, Charsets.UTF_8)

            startsWith(bytes, UNICODE_PREFIX) ->
                decodeUtf16Comment(bytes, UNICODE_PREFIX.size)

            startsWith(bytes, JIS_PREFIX) ->
                String(bytes, JIS_PREFIX.size, bytes.size - JIS_PREFIX.size, charsetOrUtf8("JIS"))

            startsWith(bytes, UNDEFINED_PREFIX) ->
                String(bytes, UNDEFINED_PREFIX.size, bytes.size - UNDEFINED_PREFIX.size, Charsets.UTF_8)

            else -> String(bytes, Charsets.UTF_8)
        }.trimEnd('\u0000')
    }

    private fun readOutputSet(file: File): TiffOutputSet? {
        val metadata = Imaging.getMetadata(file) as? JpegImageMetadata ?: return null
        val exif: TiffImageMetadata = metadata.exif ?: return null
        return exif.outputSet
    }

    private fun rewriteExif(
        file: File,
        outputSet: TiffOutputSet,
    ) {
        val temp = File(file.parentFile, "${file.name}.exif.tmp")
        try {
            BufferedOutputStream(FileOutputStream(temp)).use { os ->
                ExifRewriter().updateExifMetadataLossless(file, os, outputSet)
            }
            if (!temp.renameTo(file)) {
                temp.copyTo(file, overwrite = true)
                temp.delete()
            }
        } catch (t: Throwable) {
            temp.delete()
            throw t
        }
    }

    private fun extractJsonArrayPayload(raw: String): String? {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return null
        val start = trimmed.indexOf('[')
        val end = trimmed.lastIndexOf(']')
        if (start < 0 || end <= start) return null
        return trimmed.substring(start, end + 1)
    }

    private fun parseJsonStringArray(payload: String): List<String> {
        val body = payload.trim()
        require(body.startsWith('[') && body.endsWith(']')) { "not an array" }
        val inner = body.substring(1, body.length - 1).trim()
        if (inner.isEmpty()) return emptyList()
        val out = mutableListOf<String>()
        var i = 0
        while (i < inner.length) {
            while (i < inner.length && (inner[i].isWhitespace() || inner[i] == ',')) i++
            if (i >= inner.length) break
            require(inner[i] == '"') { "expected string" }
            i++
            val sb = StringBuilder()
            while (i < inner.length) {
                val ch = inner[i++]
                when (ch) {
                    '"' -> break
                    '\\' -> {
                        require(i < inner.length) { "bad escape" }
                        when (val esc = inner[i++]) {
                            '"', '\\', '/' -> sb.append(esc)
                            'b' -> sb.append('\b')
                            'f' -> sb.append('\u000C')
                            'n' -> sb.append('\n')
                            'r' -> sb.append('\r')
                            't' -> sb.append('\t')
                            'u' -> {
                                require(i + 4 <= inner.length) { "bad unicode" }
                                val hex = inner.substring(i, i + 4)
                                sb.append(hex.toInt(16).toChar())
                                i += 4
                            }
                            else -> error("bad escape")
                        }
                    }
                    else -> sb.append(ch)
                }
            }
            val tag = sb.toString().trim()
            if (tag.isNotEmpty()) out.add(tag)
        }
        return out
    }

    private fun escapeJsonString(value: String): String =
        buildString(value.length * 2) {
            for (ch in value) {
                when {
                    ch == '\\' || ch == '"' -> append('\\').append(ch)
                    ch == '\b' -> append("\\b")
                    ch == '\u000C' -> append("\\f")
                    ch == '\n' -> append("\\n")
                    ch == '\r' -> append("\\r")
                    ch == '\t' -> append("\\t")
                    ch.code < 0x20 -> append("\\u").append("%04x".format(ch.code))
                    ch.code < 128 -> append(ch)
                    else -> append("\\u").append("%04x".format(ch.code))
                }
            }
        }

    private fun decodeUtf16Comment(
        bytes: ByteArray,
        offset: Int,
    ): String {
        val remaining = bytes.size - offset
        if (remaining <= 0) return ""
        val hasBom =
            remaining >= 2 &&
                (
                    (bytes[offset] == 0xFE.toByte() && bytes[offset + 1] == 0xFF.toByte()) ||
                        (bytes[offset] == 0xFF.toByte() && bytes[offset + 1] == 0xFE.toByte())
                )
        val charset =
            when {
                hasBom && bytes[offset] == 0xFF.toByte() -> Charsets.UTF_16LE
                hasBom -> Charsets.UTF_16BE
                else -> Charsets.UTF_16BE
            }
        return String(bytes, offset, remaining, charset).trimEnd('\u0000')
    }

    private fun startsWith(
        bytes: ByteArray,
        prefix: ByteArray,
    ): Boolean {
        if (bytes.size < prefix.size) return false
        for (i in prefix.indices) {
            if (bytes[i] != prefix[i]) return false
        }
        return true
    }

    private fun charsetOrUtf8(name: String): Charset = runCatching { Charset.forName(name) }.getOrDefault(Charsets.UTF_8)
}
