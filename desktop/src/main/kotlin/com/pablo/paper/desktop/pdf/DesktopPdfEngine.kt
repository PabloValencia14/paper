package com.pablo.paper.desktop.pdf

import androidx.compose.ui.graphics.ImageBitmap
import com.pablo.paper.desktop.model.AcroFormField
import com.pablo.paper.desktop.model.DocumentMetadata
import com.pablo.paper.desktop.model.OutlineNode
import com.pablo.paper.desktop.model.PageInfo
import com.pablo.paper.desktop.model.SearchMatch
import java.io.File

interface DesktopPdfEngine {
    suspend fun open(file: File, password: String? = null): Boolean
    fun getPageCount(): Int
    fun getPageInfo(pageIndex: Int): PageInfo?
    suspend fun renderPage(pageIndex: Int, targetWidth: Int, targetHeight: Int, rotation: Int = 0): ImageBitmap?
    suspend fun renderThumbnail(pageIndex: Int, size: Int = 180): ImageBitmap?
    fun extractText(pageIndex: Int): String
    fun extractPageTextLayout(pageIndex: Int): com.pablo.paper.desktop.model.PageTextLayout
    fun extractAllText(): String

    suspend fun search(query: String, matchCase: Boolean = false): List<SearchMatch>
    fun extractOutline(): List<OutlineNode>
    fun extractMetadata(): DocumentMetadata
    fun extractAcroForms(): List<AcroFormField>
    fun prefetch(currentPage: Int, targetWidth: Int, targetHeight: Int)
    fun close()
}
