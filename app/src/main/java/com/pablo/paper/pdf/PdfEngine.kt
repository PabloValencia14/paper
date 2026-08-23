package com.pablo.paper.pdf

import android.graphics.Bitmap
import android.net.Uri

data class PageSize(val width: Int, val height: Int) {
    val aspectRatio: Float
        get() = if (height > 0) width.toFloat() / height.toFloat() else 1.0f
}

interface PdfEngine {
    suspend fun open(uri: Uri): Boolean
    fun getPageCount(): Int
    fun getPageSize(pageIndex: Int): PageSize?
    suspend fun renderPage(pageIndex: Int, targetWidth: Int, targetHeight: Int): Bitmap?
    suspend fun renderPageRegion(pageIndex: Int, bitmapWidth: Int, bitmapHeight: Int, transform: android.graphics.Matrix): Bitmap?
    fun prefetchPages(currentPageIndex: Int, targetWidth: Int, targetHeight: Int)
    fun close()
}
