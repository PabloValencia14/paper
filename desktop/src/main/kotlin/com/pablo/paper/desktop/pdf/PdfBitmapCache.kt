package com.pablo.paper.desktop.pdf

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import java.awt.image.BufferedImage
import java.util.LinkedHashMap

class PdfBitmapCache(
    private val maxMemoryBytes: Long = Runtime.getRuntime().maxMemory() / 4
) {
    private val cache = object : LinkedHashMap<String, CachedBitmap>(64, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, CachedBitmap>?): Boolean {
            return currentBytes > maxMemoryBytes
        }
    }

    private var currentBytes: Long = 0L

    data class CachedBitmap(
        val bufferedImage: BufferedImage,
        val composeBitmap: ImageBitmap,
        val byteSize: Long
    )

    companion object {
        fun buildKey(docId: String, pageIndex: Int, width: Int, height: Int, rotation: Int): String {
            return "$docId-$pageIndex-${width}x${height}-r$rotation"
        }
    }

    @Synchronized
    fun get(key: String): ImageBitmap? {
        val entry = cache[key] ?: return null
        return entry.composeBitmap
    }

    @Synchronized
    fun getBufferedImage(key: String): BufferedImage? {
        return cache[key]?.bufferedImage
    }

    @Synchronized
    fun put(key: String, image: BufferedImage): ImageBitmap {
        val bytes = (image.width * image.height * 4L).coerceAtLeast(1024L)
        val composeBitmap = image.toComposeImageBitmap()
        val entry = CachedBitmap(image, composeBitmap, bytes)
        
        val old = cache.put(key, entry)
        if (old != null) {
            currentBytes -= old.byteSize
        }
        currentBytes += bytes

        // Evict if over budget
        while (currentBytes > maxMemoryBytes && cache.isNotEmpty()) {
            val firstKey = cache.keys.iterator().next()
            val removed = cache.remove(firstKey)
            if (removed != null) {
                currentBytes -= removed.byteSize
            }
        }

        return composeBitmap
    }

    @Synchronized
    fun clear() {
        cache.clear()
        currentBytes = 0L
    }
}
