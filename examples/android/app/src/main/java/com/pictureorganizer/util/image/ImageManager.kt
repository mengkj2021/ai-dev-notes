package com.pictureorganizer.util.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import androidx.exifinterface.media.ExifInterface
import com.pictureorganizer.R
import com.pictureorganizer.model.ImageStatus
import com.pictureorganizer.util.file.AppFileManager
import java.io.FileOutputStream
import java.util.UUID

data class PreparedImage(
    val id: String,
    val filePath: String,
    val fileName: String,
    val width: Int,
    val height: Int,
    val compressed: Boolean,
)

class ImageManager(
    private val context: Context,
    private val fileManager: AppFileManager = AppFileManager(context),
) {
    fun prepareForImport(
        uri: Uri,
        compressEnabled: Boolean = true,
    ): PreparedImage {
        val id = UUID.randomUUID().toString()
        val bounds = decodeBounds(uri)
        val sizeBytes = querySizeBytes(uri)
        val needsCompress =
            compressEnabled &&
                (
                    sizeBytes > MAX_BYTES ||
                        bounds.width > MAX_LONG_EDGE ||
                        bounds.height > MAX_LONG_EDGE
                )

        val displayName = displayNameOf(uri)
        if (!needsCompress) {
            val ext = extensionFromDisplayName(displayName) ?: guessExtension(uri)
            val destName = allocateDestName(displayName, ext, id)
            val plainFile = fileManager.createDestFile(ImageStatus.Pending, destName)
            fileManager.copyFromUri(uri, plainFile)
            return PreparedImage(
                id = id,
                filePath = fileManager.relativePath(ImageStatus.Pending, destName),
                fileName = destName,
                width = bounds.width,
                height = bounds.height,
                compressed = false,
            )
        }

        val destName = allocateDestName(displayName, "jpg", id)
        val destFile = fileManager.createDestFile(ImageStatus.Pending, destName)
        compressToFile(uri, destFile, bounds)

        val outBounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(destFile.absolutePath, outBounds)
        return PreparedImage(
            id = id,
            filePath = fileManager.relativePath(ImageStatus.Pending, destName),
            fileName = destName,
            width = outBounds.outWidth,
            height = outBounds.outHeight,
            compressed = true,
        )
    }

    private fun allocateDestName(
        displayName: String,
        extension: String,
        id: String,
    ): String {
        val stem = sanitizeAndTruncateStem(stemFromDisplayName(displayName))
        val ext = extension.trim().lowercase().ifBlank { "jpg" }
        val shortId = id.replace("-", "").take(8)
        val candidate = "$stem.$ext"
        val dest = fileManager.createDestFile(ImageStatus.Pending, candidate)
        if (!dest.exists()) return candidate
        return "${stem}_$shortId.$ext"
    }

    private fun stemFromDisplayName(displayName: String): String {
        val trimmed = displayName.trim()
        if (trimmed.isEmpty()) return "image"
        val dot = trimmed.lastIndexOf('.')
        return if (dot > 0) trimmed.substring(0, dot) else trimmed
    }

    private fun extensionFromDisplayName(displayName: String): String? {
        val trimmed = displayName.trim()
        val dot = trimmed.lastIndexOf('.')
        if (dot <= 0 || dot == trimmed.lastIndex) return null
        val ext = trimmed.substring(dot + 1).lowercase()
        return ext.takeIf { it.matches(Regex("[a-z0-9]{1,8}")) }
    }

    private fun sanitizeAndTruncateStem(raw: String): String {
        val cleaned =
            buildString(raw.length) {
                for (ch in raw) {
                    when {
                        ch.isLetterOrDigit() -> append(ch)
                        ch in "._- " || ch.code > 127 -> append(if (ch == ' ') '_' else ch)
                        else -> append('_')
                    }
                }
            }.trim('_', '.', ' ')
                .ifBlank { "image" }
        return cleaned
            .take(MAX_STEM_CHARS)
            .trimEnd('_', '.', ' ')
            .ifBlank { "image" }
    }

    private fun decodeBounds(uri: Uri): Bounds {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input, null, options)
        }
        return Bounds(
            width = options.outWidth.coerceAtLeast(0),
            height = options.outHeight.coerceAtLeast(0),
        )
    }

    private fun querySizeBytes(uri: Uri): Long {
        context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { afd ->
            return afd.length.coerceAtLeast(0L)
        }
        return 0L
    }

    private fun compressToFile(
        uri: Uri,
        destFile: java.io.File,
        bounds: Bounds,
    ) {
        val sampleSize = calculateInSampleSize(bounds, MAX_LONG_EDGE)
        val options =
            BitmapFactory.Options().apply {
                inSampleSize = sampleSize
            }
        val bitmap =
            context.contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, options)
            } ?: error("无法解码图片")

        val scaled = scaleToLongEdge(bitmap, MAX_LONG_EDGE)
        if (scaled !== bitmap) {
            bitmap.recycle()
        }
        FileOutputStream(destFile).use { out ->
            scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
        }
        scaled.recycle()
    }

    private fun scaleToLongEdge(
        bitmap: Bitmap,
        maxLongEdge: Int,
    ): Bitmap {
        val longEdge = maxOf(bitmap.width, bitmap.height)
        if (longEdge <= maxLongEdge) return bitmap
        val scale = maxLongEdge.toFloat() / longEdge
        val w = (bitmap.width * scale).toInt().coerceAtLeast(1)
        val h = (bitmap.height * scale).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, w, h, true)
    }

    private fun calculateInSampleSize(
        bounds: Bounds,
        maxLongEdge: Int,
    ): Int {
        var sample = 1
        var longEdge = maxOf(bounds.width, bounds.height)
        while (longEdge / (sample * 2) >= maxLongEdge) {
            sample *= 2
        }
        return sample.coerceAtLeast(1)
    }

    private fun guessExtension(uri: Uri): String {
        val type = context.contentResolver.getType(uri) ?: return "jpg"
        return when {
            type.contains("png") -> "png"
            type.contains("webp") -> "webp"
            type.contains("jpeg") || type.contains("jpg") -> "jpg"
            else -> "jpg"
        }
    }

    fun readUserTagsFromUri(uri: Uri): List<String> =
        runCatching {
            context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { afd ->
                ImageTagMetadata.readUserTags(afd.fileDescriptor)
            } ?: emptyList()
        }.getOrDefault(emptyList())

    fun readDateTakenMillisFromUri(uri: Uri): Long? =
        runCatching {
            context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { afd ->
                val raw =
                    ExifInterface(afd.fileDescriptor)
                        .getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)
                ExifDateTaken.parseToMillis(raw)
            }
        }.getOrNull()

    fun displayNameOf(uri: Uri): String {
        val queried =
            runCatching {
                context.contentResolver
                    .query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                    ?.use { cursor ->
                        if (cursor.moveToFirst()) cursor.getString(0) else null
                    }
            }.getOrNull()
        return queried
            ?.takeIf { it.isNotBlank() }
            ?: uri.lastPathSegment?.substringAfterLast('/')?.takeIf { it.isNotBlank() }
            ?: context.getString(R.string.unnamed_image)
    }

    private data class Bounds(
        val width: Int,
        val height: Int,
    )

    companion object {
        const val MAX_BYTES = 2L * 1024L * 1024L
        const val MAX_LONG_EDGE = 1920
        const val JPEG_QUALITY = 85
        const val MAX_STEM_CHARS = 40
    }
}
