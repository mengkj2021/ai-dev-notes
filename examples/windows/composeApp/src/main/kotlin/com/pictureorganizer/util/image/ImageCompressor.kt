package com.pictureorganizer.util.image

import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam
import javax.imageio.stream.FileImageOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

object ImageCompressor {
    const val MAX_BYTES = 2L * 1024 * 1024
    const val MAX_LONG_EDGE = 1920
    const val JPEG_QUALITY = 0.85f

    data class Prepared(
        val destFile: File,
        val fileName: String,
        val compressed: Boolean,
    )

    fun prepareForImport(
        source: File,
        destDir: File,
        preferredName: String,
        compressEnabled: Boolean,
    ): Prepared {
        destDir.mkdirs()
        val needsCompress =
            compressEnabled &&
                (source.length() > MAX_BYTES || longEdge(source)?.let { it > MAX_LONG_EDGE } == true)

        return if (needsCompress) {
            val destName = ensureJpgName(uniqueName(destDir, preferredName.substringBeforeLast('.') + ".jpg"))
            val dest = File(destDir, destName)
            writeCompressedJpeg(source, dest)
            Prepared(dest, destName, compressed = true)
        } else {
            val destName = uniqueName(destDir, preferredName)
            val dest = File(destDir, destName)
            source.copyTo(dest, overwrite = true)
            Prepared(dest, destName, compressed = false)
        }
    }

    private fun longEdge(file: File): Int? =
        runCatching {
            val img = ImageIO.read(file) ?: return null
            max(img.width, img.height)
        }.getOrNull()

    private fun writeCompressedJpeg(
        source: File,
        dest: File,
    ) {
        val original = ImageIO.read(source) ?: error("无法解码图片")
        val scaled = scaleIfNeeded(original)
        val rgb =
            if (scaled.type == BufferedImage.TYPE_INT_RGB) {
                scaled
            } else {
                BufferedImage(scaled.width, scaled.height, BufferedImage.TYPE_INT_RGB).also { canvas ->
                    val g = canvas.createGraphics()
                    g.drawImage(scaled, 0, 0, null)
                    g.dispose()
                }
            }
        val writers = ImageIO.getImageWritersByFormatName("jpg")
        require(writers.hasNext()) { "无 JPEG writer" }
        val writer = writers.next()
        val param = writer.defaultWriteParam
        if (param.canWriteCompressed()) {
            param.compressionMode = ImageWriteParam.MODE_EXPLICIT
            param.compressionQuality = JPEG_QUALITY
        }
        FileImageOutputStream(dest).use { out ->
            writer.output = out
            writer.write(null, IIOImage(rgb, null, null), param)
        }
        writer.dispose()
    }

    private fun scaleIfNeeded(image: BufferedImage): BufferedImage {
        val longEdge = max(image.width, image.height)
        if (longEdge <= MAX_LONG_EDGE) return image
        val scale = MAX_LONG_EDGE.toDouble() / longEdge
        val w = (image.width * scale).roundToInt().coerceAtLeast(1)
        val h = (image.height * scale).roundToInt().coerceAtLeast(1)
        val scaled = image.getScaledInstance(w, h, Image.SCALE_SMOOTH)
        val out = BufferedImage(w, h, BufferedImage.TYPE_INT_RGB)
        val g = out.createGraphics()
        g.drawImage(scaled, 0, 0, null)
        g.dispose()
        return out
    }

    private fun ensureJpgName(name: String): String =
        if (name.lowercase().endsWith(".jpg") || name.lowercase().endsWith(".jpeg")) name else "$name.jpg"

    private fun uniqueName(
        dir: File,
        preferred: String,
    ): String {
        val safe = preferred.replace(Regex("""[\\/:*?"<>|]"""), "_")
        var candidate = safe
        var index = 2
        while (File(dir, candidate).exists()) {
            val stem = safe.substringBeforeLast('.', missingDelimiterValue = safe)
            val ext = safe.substringAfterLast('.', missingDelimiterValue = "")
            candidate = if (ext.isEmpty()) "${stem}_$index" else "${stem}_$index.$ext"
            index++
        }
        return candidate
    }
}
