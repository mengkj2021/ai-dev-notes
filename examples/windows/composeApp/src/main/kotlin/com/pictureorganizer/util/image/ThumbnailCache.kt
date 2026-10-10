package com.pictureorganizer.util.image

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Image as SkiaImage
import org.jetbrains.skia.Rect
import org.jetbrains.skia.SamplingMode
import org.jetbrains.skia.Surface
import java.io.File
import java.util.LinkedHashMap

object ThumbnailCache {
    private const val MAX_ENTRIES = 128
    private val lock = Any()
    private val map =
        object : LinkedHashMap<String, ImageBitmap>(MAX_ENTRIES, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, ImageBitmap>?): Boolean =
                size > MAX_ENTRIES
        }

    fun get(key: String): ImageBitmap? = synchronized(lock) { map[key] }

    fun put(
        key: String,
        bitmap: ImageBitmap,
    ) {
        synchronized(lock) { map[key] = bitmap }
    }

    fun cacheKey(file: File): String =
        "${file.absolutePath}|${file.lastModified()}|${file.length()}"

    fun decodeDownscaled(
        file: File,
        maxEdgePx: Int,
    ): ImageBitmap? {
        if (!file.exists() || !file.isFile) return null
        val bytes = file.readBytes()
        val encoded = SkiaImage.makeFromEncoded(bytes)
        val maxDim = maxOf(encoded.width, encoded.height).coerceAtLeast(1)
        if (maxDim <= maxEdgePx) {
            return encoded.toComposeImageBitmap()
        }
        val scale = maxEdgePx.toFloat() / maxDim
        val w = (encoded.width * scale).toInt().coerceAtLeast(1)
        val h = (encoded.height * scale).toInt().coerceAtLeast(1)
        val surface = Surface.makeRasterN32Premul(w, h)
        surface.canvas.drawImageRect(
            encoded,
            Rect.makeWH(encoded.width.toFloat(), encoded.height.toFloat()),
            Rect.makeWH(w.toFloat(), h.toFloat()),
            SamplingMode.LINEAR,
            paint = null,
            strict = true,
        )
        return surface.makeImageSnapshot().toComposeImageBitmap()
    }
}

@Composable
fun FileThumbnail(
    file: File,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    maxEdgePx: Int = 128,
) {
    val key = remember(file.absolutePath, file.lastModified(), file.length()) { ThumbnailCache.cacheKey(file) }
    var bitmap by remember(key) { mutableStateOf(ThumbnailCache.get(key)) }

    LaunchedEffect(key) {
        if (bitmap != null) return@LaunchedEffect
        val loaded =
            withContext(Dispatchers.IO) {
                runCatching { ThumbnailCache.decodeDownscaled(file, maxEdgePx) }.getOrNull()
            }
        if (loaded != null) {
            ThumbnailCache.put(key, loaded)
            bitmap = loaded
        }
    }

    if (bitmap != null) {
        Image(
            painter = BitmapPainter(bitmap!!),
            contentDescription = null,
            modifier = modifier.size(size),
            contentScale = ContentScale.Crop,
        )
    } else {
        Box(
            modifier = modifier.size(size),
            contentAlignment = Alignment.Center,
        ) {
            Text("图", style = MaterialTheme.typography.labelLarge)
        }
    }
}
