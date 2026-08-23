package com.pablo.paper.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.min

class NativePdfEngine(
    private val context: Context,
    private val cache: PdfBitmapCache = PdfBitmapCache()
) : PdfEngine {

    private var pfd: ParcelFileDescriptor? = null
    private var renderer: PdfRenderer? = null
    private var documentIdentifier: String = ""
    private val renderLock = Mutex()
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private var prefetchJob: Job? = null
    private var reusableRegionBitmap: Bitmap? = null

    companion object {
        private const val MAX_RENDER_WIDTH = 3840
        private const val MAX_RENDER_HEIGHT = 4320
    }

    override suspend fun open(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        close()
        try {
            documentIdentifier = uri.toString().hashCode().toString()
            val descriptor = if (uri.scheme == "file") {
                val path = uri.path ?: return@withContext false
                ParcelFileDescriptor.open(File(path), ParcelFileDescriptor.MODE_READ_ONLY)
            } else {
                context.contentResolver.openFileDescriptor(uri, "r") ?: return@withContext false
            }
            pfd = descriptor
            renderer = PdfRenderer(descriptor)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            close()
            false
        }
    }

    override fun getPageCount(): Int {
        return renderer?.pageCount ?: 0
    }

    override fun getPageSize(pageIndex: Int): PageSize? {
        val currentRenderer = renderer ?: return null
        if (pageIndex < 0 || pageIndex >= currentRenderer.pageCount) return null

        return try {
            val page = currentRenderer.openPage(pageIndex)
            val size = PageSize(page.width, page.height)
            page.close()
            size
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun renderPage(pageIndex: Int, targetWidth: Int, targetHeight: Int): Bitmap? = withContext(Dispatchers.IO) {
        val currentRenderer = renderer ?: return@withContext null
        if (pageIndex < 0 || pageIndex >= currentRenderer.pageCount) return@withContext null
        if (targetWidth <= 0 || targetHeight <= 0) return@withContext null

        // Constrain dimensions to prevent OutOfMemoryError
        var renderW = targetWidth
        var renderH = targetHeight

        val scaleX = MAX_RENDER_WIDTH.toFloat() / targetWidth.toFloat()
        val scaleY = MAX_RENDER_HEIGHT.toFloat() / targetHeight.toFloat()
        val minScale = min(1.0f, min(scaleX, scaleY))

        if (minScale < 1.0f) {
            renderW = (targetWidth * minScale).toInt().coerceAtLeast(100)
            renderH = (targetHeight * minScale).toInt().coerceAtLeast(100)
        }

        val cacheKey = PdfBitmapCache.buildKey(documentIdentifier, pageIndex, renderW, renderH)

        // 1. Check Fast Memory LRU Cache (0.001 ms)
        val memoryBitmap = cache.get(cacheKey)
        if (memoryBitmap != null && !memoryBitmap.isRecycled) {
            return@withContext memoryBitmap
        }

        // 2. Render vector from the native engine.
        // Disk-caching rendered pages is deliberately avoided: encoding every page while the user
        // reads wastes CPU, flash writes and battery, and a cache without dimensions can return a
        // bitmap rendered for a different zoom level.
        renderLock.withLock {
            val secondCheck = cache.get(cacheKey)
            if (secondCheck != null && !secondCheck.isRecycled) return@withContext secondCheck

            try {
                val page = currentRenderer.openPage(pageIndex)
                val bitmap = Bitmap.createBitmap(renderW, renderH, Bitmap.Config.ARGB_8888)
                bitmap.eraseColor(Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()

                cache.put(cacheKey, bitmap)

                bitmap
            } catch (oom: OutOfMemoryError) {
                oom.printStackTrace()
                cache.clear()
                try {
                    val halfW = (renderW / 2).coerceAtLeast(100)
                    val halfH = (renderH / 2).coerceAtLeast(100)
                    val page = currentRenderer.openPage(pageIndex)
                    val fallbackBitmap = Bitmap.createBitmap(halfW, halfH, Bitmap.Config.RGB_565)
                    fallbackBitmap.eraseColor(Color.WHITE)
                    page.render(fallbackBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()
                    cache.put(cacheKey, fallbackBitmap)
                    fallbackBitmap
                } catch (e2: Throwable) {
                    e2.printStackTrace()
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    override suspend fun renderPageRegion(
        pageIndex: Int,
        bitmapWidth: Int,
        bitmapHeight: Int,
        transform: android.graphics.Matrix
    ): Bitmap? = withContext(Dispatchers.IO) {
        val currentRenderer = renderer ?: return@withContext null
        if (pageIndex < 0 || pageIndex >= currentRenderer.pageCount) return@withContext null
        if (bitmapWidth <= 0 || bitmapHeight <= 0) return@withContext null

        val renderW = bitmapWidth.coerceIn(100, MAX_RENDER_WIDTH)
        val renderH = bitmapHeight.coerceIn(100, MAX_RENDER_HEIGHT)

        renderLock.withLock {
            try {
                val page = currentRenderer.openPage(pageIndex)
                var bitmap = reusableRegionBitmap
                if (bitmap == null || bitmap.isRecycled || bitmap.width != renderW || bitmap.height != renderH) {
                    try {
                        bitmap?.recycle()
                    } catch (e: Exception) {}
                    bitmap = Bitmap.createBitmap(renderW, renderH, Bitmap.Config.ARGB_8888)
                    reusableRegionBitmap = bitmap
                }
                bitmap.eraseColor(Color.WHITE)
                page.render(bitmap, null, transform, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()
                bitmap
            } catch (oom: OutOfMemoryError) {
                oom.printStackTrace()
                cache.clear()
                try {
                    val page = currentRenderer.openPage(pageIndex)
                    val fallbackBitmap = Bitmap.createBitmap(renderW, renderH, Bitmap.Config.RGB_565)
                    fallbackBitmap.eraseColor(Color.WHITE)
                    page.render(fallbackBitmap, null, transform, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()
                    fallbackBitmap
                } catch (e2: Throwable) {
                    e2.printStackTrace()
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    /**
     * Prefetch only the immediately adjacent pages. Rendering a whole PDF in the background is
     * counterproductive on battery-powered devices: most of those pages are never viewed and it
     * competes with gesture and ink rendering for the same native PDF renderer.
     */
    override fun prefetchPages(currentPageIndex: Int, targetWidth: Int, targetHeight: Int) {
        if (renderer == null || targetWidth <= 0 || targetHeight <= 0) return
        val count = getPageCount()

        prefetchJob?.cancel()
        prefetchJob = scope.launch {
            // Preserve the next/previous-page fast path without pre-rendering unseen content.
            val nearPages = listOf(
                currentPageIndex + 1,
                currentPageIndex - 1
            ).filter { it in 0 until count }

            for (page in nearPages) {
                if (!isActive) return@launch
                val key = PdfBitmapCache.buildKey(documentIdentifier, page, targetWidth, targetHeight)
                if (cache.get(key) == null) {
                    renderPage(page, targetWidth, targetHeight)
                }
                kotlinx.coroutines.yield()
            }

        }
    }

    override fun close() {
        prefetchJob?.cancel()
        try {
            renderer?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        renderer = null

        try {
            pfd?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        pfd = null
    }
}
