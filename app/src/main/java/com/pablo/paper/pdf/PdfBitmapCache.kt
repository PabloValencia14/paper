package com.pablo.paper.pdf

import android.graphics.Bitmap
import android.util.LruCache

/**
 * Thread-safe LRU cache for the current page and its immediate neighbours.
 *
 * A PDF page at tablet resolution can occupy tens of MiB. Keeping a quarter of the heap (and
 * requesting a large heap) makes the app look fast at first but causes GC pressure and eviction
 * of other processes on modest tablets. The bounded cache below keeps navigation responsive while
 * leaving headroom for Compose, ink, and OCR.
 */
class PdfBitmapCache(
    maxMemoryBytes: Int = (Runtime.getRuntime().maxMemory() / 8).toInt()
        .coerceIn(48 * 1024 * 1024, 96 * 1024 * 1024)
) {

    private val cache = object : LruCache<String, Bitmap>(maxMemoryBytes) {
        override fun sizeOf(key: String, value: Bitmap): Int {
            return value.byteCount
        }

        override fun entryRemoved(evicted: Boolean, key: String?, oldValue: Bitmap?, newValue: Bitmap?) {
            // Note: In modern Android, letting GC manage recycled bitmaps is preferred over manual recycle() to avoid race conditions.
        }
    }

    fun get(key: String): Bitmap? {
        synchronized(cache) {
            val bitmap = cache.get(key)
            if (bitmap != null && !bitmap.isRecycled) {
                return bitmap
            }
            return null
        }
    }

    fun put(key: String, bitmap: Bitmap) {
        synchronized(cache) {
            if (!bitmap.isRecycled) {
                cache.put(key, bitmap)
            }
        }
    }

    fun remove(key: String): Bitmap? {
        synchronized(cache) {
            return cache.remove(key)
        }
    }

    fun clear() {
        synchronized(cache) {
            cache.evictAll()
        }
    }

    companion object {
        fun buildKey(documentId: String, pageIndex: Int, width: Int, height: Int): String {
            return "${documentId}_p${pageIndex}_${width}x${height}"
        }
    }
}
