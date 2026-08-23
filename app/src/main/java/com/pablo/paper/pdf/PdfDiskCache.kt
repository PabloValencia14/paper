package com.pablo.paper.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Fast, thread-safe Disk Cache for pre-rendered PDF page bitmaps.
 * Stores distant and background pages as compressed WebP files on fast internal storage.
 */
class PdfDiskCache(private val context: Context) {

    private val cacheDir: File by lazy {
        File(context.cacheDir, "pdf_bitmap_cache").apply {
            if (!exists()) mkdirs()
        }
    }

    private fun getFile(documentId: String, pageIndex: Int): File {
        val safeDocId = documentId.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        return File(cacheDir, "${safeDocId}_p${pageIndex}.webp")
    }

    fun hasPage(documentId: String, pageIndex: Int): Boolean {
        val file = getFile(documentId, pageIndex)
        return file.exists() && file.length() > 0
    }

    suspend fun getPageBitmap(documentId: String, pageIndex: Int): Bitmap? = withContext(Dispatchers.IO) {
        val file = getFile(documentId, pageIndex)
        if (!file.exists() || file.length() <= 0) return@withContext null

        try {
            BitmapFactory.decodeFile(file.absolutePath)
        } catch (e: Throwable) {
            e.printStackTrace()
            null
        }
    }

    suspend fun savePageBitmap(documentId: String, pageIndex: Int, bitmap: Bitmap) = withContext(Dispatchers.IO) {
        if (bitmap.isRecycled) return@withContext
        val file = getFile(documentId, pageIndex)

        try {
            val tmpFile = File(cacheDir, "${file.name}.tmp")
            FileOutputStream(tmpFile).use { out ->
                // Compress as lossless/near-lossless WebP (fastest decode speed, smallest disk footprint)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, 90, out)
                } else {
                    @Suppress("DEPRECATION")
                    bitmap.compress(Bitmap.CompressFormat.WEBP, 90, out)
                }
                out.flush()
            }
            if (tmpFile.exists()) {
                tmpFile.renameTo(file)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    fun clearDocument(documentId: String) {
        val safeDocId = documentId.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val files = cacheDir.listFiles { _, name -> name.startsWith("${safeDocId}_p") }
        files?.forEach { it.delete() }
    }

    fun clearAll() {
        cacheDir.listFiles()?.forEach { it.delete() }
    }
}
