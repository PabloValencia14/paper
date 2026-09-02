package com.pablo.paper.desktop.pdf

import androidx.compose.ui.graphics.ImageBitmap
import com.pablo.paper.desktop.model.AcroFormField
import com.pablo.paper.desktop.model.DocumentMetadata
import com.pablo.paper.desktop.model.OutlineNode
import com.pablo.paper.desktop.model.PageInfo
import com.pablo.paper.desktop.model.SearchMatch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineNode
import org.apache.pdfbox.rendering.ImageType
import org.apache.pdfbox.rendering.PDFRenderer
import org.apache.pdfbox.text.PDFTextStripper
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

class PdfBoxEngine(
    private val cache: PdfBitmapCache = PdfBitmapCache()
) : DesktopPdfEngine {

    private var document: PDDocument? = null
    private var renderer: PDFRenderer? = null
    private var docIdentifier: String = ""
    private var openedFile: File? = null
    private val mutex = Mutex()
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private var prefetchJob: Job? = null

    companion object {
        private const val MAX_DIMENSION = 8192
    }

    override suspend fun open(file: File, password: String?): Boolean = withContext(Dispatchers.IO) {
        close()
        mutex.withLock {
            try {
                if (!file.exists() || !file.canRead()) return@withContext false
                docIdentifier = file.absolutePath.hashCode().toString()
                openedFile = file

                val loadedDoc = if (!password.isNullOrBlank()) {
                    Loader.loadPDF(file, password)
                } else {
                    Loader.loadPDF(file)
                }

                document = loadedDoc
                val newRenderer = PDFRenderer(loadedDoc)
                newRenderer.isSubsamplingAllowed = true
                renderer = newRenderer
                true
            } catch (e: Exception) {
                e.printStackTrace()
                close()
                false
            }
        }
    }

    override fun getPageCount(): Int {
        return document?.numberOfPages ?: 0
    }

    override fun getPageInfo(pageIndex: Int): PageInfo? {
        val doc = document ?: return null
        if (pageIndex < 0 || pageIndex >= doc.numberOfPages) return null
        return try {
            val page = doc.getPage(pageIndex)
            val cropBox = page.cropBox ?: page.mediaBox
            val width = cropBox.width.toInt()
            val height = cropBox.height.toInt()
            val rotation = page.rotation
            PageInfo(pageIndex, width, height, rotation)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun renderPage(
        pageIndex: Int,
        targetWidth: Int,
        targetHeight: Int,
        rotation: Int
    ): ImageBitmap? = withContext(Dispatchers.IO) {
        val doc = document ?: return@withContext null
        val r = renderer ?: return@withContext null
        if (pageIndex < 0 || pageIndex >= doc.numberOfPages) return@withContext null
        if (targetWidth <= 0 || targetHeight <= 0) return@withContext null

        val w = targetWidth.coerceIn(100, MAX_DIMENSION)
        val h = targetHeight.coerceIn(100, MAX_DIMENSION)
        val cacheKey = PdfBitmapCache.buildKey(docIdentifier, pageIndex, w, h, rotation)

        // 1. Check L1 Memory Cache (Instant 0.001 ms)
        val cached = cache.get(cacheKey)
        if (cached != null) return@withContext cached

        // 2. High-performance rendering
        mutex.withLock {
            val secondCheck = cache.get(cacheKey)
            if (secondCheck != null) return@withContext secondCheck

            try {
                val page = doc.getPage(pageIndex)
                val cropBox = page.cropBox ?: page.mediaBox
                val origW = if (page.rotation == 90 || page.rotation == 270) cropBox.height else cropBox.width
                val origH = if (page.rotation == 90 || page.rotation == 270) cropBox.width else cropBox.height

                val scaleX = w.toFloat() / origW
                val scaleY = h.toFloat() / origH
                val scale = min(scaleX, scaleY).coerceAtLeast(0.1f)

                val renderedImg: BufferedImage = r.renderImage(pageIndex, scale, ImageType.RGB)

                val finalImg = if (rotation != 0) {
                    rotateImage(renderedImg, rotation)
                } else {
                    renderedImg
                }

                cache.put(cacheKey, finalImg)
            } catch (oom: OutOfMemoryError) {
                cache.clear()
                System.gc()
                null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    override suspend fun renderThumbnail(pageIndex: Int, size: Int): ImageBitmap? {
        val info = getPageInfo(pageIndex) ?: return null
        val aspect = info.aspectRatio
        val (tw, th) = if (aspect >= 1.0f) {
            size to (size / aspect).toInt().coerceAtLeast(40)
        } else {
            (size * aspect).toInt().coerceAtLeast(40) to size
        }
        return renderPage(pageIndex, tw, th, 0)
    }

    private fun rotateImage(src: BufferedImage, degrees: Int): BufferedImage {
        val rads = Math.toRadians(degrees.toDouble())
        val sin = Math.abs(Math.sin(rads))
        val cos = Math.abs(Math.cos(rads))
        val w = src.width
        val h = src.height
        val newW = Math.floor(w * cos + h * sin).toInt()
        val newH = Math.floor(h * cos + w * sin).toInt()

        val rotated = BufferedImage(newW, newH, src.type)
        val g2d = rotated.createGraphics()
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        val at = AffineTransform()
        at.translate((newW - w) / 2.0, (newH - h) / 2.0)
        at.rotate(rads, w / 2.0, h / 2.0)
        g2d.transform = at
        g2d.drawImage(src, 0, 0, null)
        g2d.dispose()
        return rotated
    }

    override fun extractText(pageIndex: Int): String {
        val doc = document ?: return ""
        if (pageIndex < 0 || pageIndex >= doc.numberOfPages) return ""
        return try {
            val stripper = PDFTextStripper()
            stripper.startPage = pageIndex + 1
            stripper.endPage = pageIndex + 1
            stripper.getText(doc).trim()
        } catch (e: Exception) {
            ""
        }
    }

    override fun extractPageTextLayout(pageIndex: Int): com.pablo.paper.desktop.model.PageTextLayout {
        val doc = document ?: return com.pablo.paper.desktop.model.PageTextLayout(pageIndex)
        if (pageIndex < 0 || pageIndex >= doc.numberOfPages) return com.pablo.paper.desktop.model.PageTextLayout(pageIndex)

        return try {
            val page = doc.getPage(pageIndex)
            val cropBox = page.cropBox ?: page.mediaBox
            val pw = cropBox.width
            val ph = cropBox.height

            val glyphList = mutableListOf<com.pablo.paper.desktop.model.TextGlyph>()
            val wordList = mutableListOf<com.pablo.paper.desktop.model.TextWord>()

            var curWordStr = StringBuilder()
            var curWordGlyphs = mutableListOf<com.pablo.paper.desktop.model.TextGlyph>()
            var minX = 1f
            var minY = 1f
            var maxX = 0f
            var maxY = 0f

            fun flush() {
                if (curWordStr.isNotBlank()) {
                    wordList.add(
                        com.pablo.paper.desktop.model.TextWord(
                            text = curWordStr.toString(),
                            x = minX.coerceIn(0f, 1f),
                            y = minY.coerceIn(0f, 1f),
                            width = (maxX - minX).coerceAtLeast(0.005f),
                            height = (maxY - minY).coerceAtLeast(0.005f),
                            glyphs = curWordGlyphs.toList()
                        )
                    )
                }
                curWordStr.clear()
                curWordGlyphs.clear()
                minX = 1f
                minY = 1f
                maxX = 0f
                maxY = 0f
            }

            val extractor = object : PDFTextStripper() {
                override fun processTextPosition(text: org.apache.pdfbox.text.TextPosition) {
                    val ch = text.unicode ?: return
                    val x = text.xDirAdj / pw
                    val y = text.yDirAdj / ph
                    val w = text.widthDirAdj / pw
                    val h = text.heightDir / ph

                    val g = com.pablo.paper.desktop.model.TextGlyph(ch, x, y, w, h)
                    glyphList.add(g)

                    if (ch.isBlank()) {
                        flush()
                    } else {
                        if (curWordStr.isEmpty()) {
                            minX = x
                            minY = y
                            maxX = x + w
                            maxY = y + h
                        } else {
                            minX = minOf(minX, x)
                            minY = minOf(minY, y)
                            maxX = maxOf(maxX, x + w)
                            maxY = maxOf(maxY, y + h)
                        }
                        curWordStr.append(ch)
                        curWordGlyphs.add(g)
                    }
                }
            }

            extractor.startPage = pageIndex + 1
            extractor.endPage = pageIndex + 1
            val fullText = extractor.getText(doc)
            flush()

            com.pablo.paper.desktop.model.PageTextLayout(
                pageIndex = pageIndex,
                words = wordList,
                fullText = fullText
            )
        } catch (e: Exception) {
            e.printStackTrace()
            com.pablo.paper.desktop.model.PageTextLayout(pageIndex)
        }
    }


    override fun extractAllText(): String {
        val doc = document ?: return ""
        return try {
            val stripper = PDFTextStripper()
            stripper.getText(doc).trim()
        } catch (e: Exception) {
            ""
        }
    }

    override suspend fun search(query: String, matchCase: Boolean): List<SearchMatch> = withContext(Dispatchers.IO) {
        val doc = document ?: return@withContext emptyList()
        if (query.isBlank()) return@withContext emptyList()

        val results = mutableListOf<SearchMatch>()
        val totalPages = doc.numberOfPages
        val q = if (matchCase) query else query.lowercase(Locale.getDefault())

        val stripper = PDFTextStripper()
        for (p in 0 until totalPages) {
            stripper.startPage = p + 1
            stripper.endPage = p + 1
            val text = try {
                stripper.getText(doc)
            } catch (e: Exception) {
                continue
            }

            val searchTarget = if (matchCase) text else text.lowercase(Locale.getDefault())
            var startIdx = 0
            var countInPage = 0
            while (true) {
                val idx = searchTarget.indexOf(q, startIdx)
                if (idx == -1) break

                val snippetStart = max(0, idx - 40)
                val snippetEnd = min(text.length, idx + q.length + 40)
                val snippet = "..." + text.substring(snippetStart, snippetEnd).replace("\n", " ").trim() + "..."

                results.add(
                    SearchMatch(
                        pageIndex = p,
                        snippet = snippet,
                        matchIndexInPage = countInPage
                    )
                )
                countInPage++
                startIdx = idx + q.length
            }
        }
        results
    }

    override fun extractOutline(): List<OutlineNode> {
        val doc = document ?: return emptyList()
        val outline = doc.documentCatalog.documentOutline ?: return emptyList()
        return extractOutlineNodes(outline, 0)
    }

    private fun extractOutlineNodes(node: PDOutlineNode, level: Int): List<OutlineNode> {
        val result = mutableListOf<OutlineNode>()
        var current: PDOutlineItem? = node.firstChild
        while (current != null) {
            var targetPage = 0
            try {
                val dest = current.destination
                if (dest != null) {
                    val page = current.findDestinationPage(document)
                    if (page != null) {
                        targetPage = document?.pages?.indexOf(page)?.coerceAtLeast(0) ?: 0
                    }
                }
            } catch (e: Exception) {}

            val children = if (current.hasChildren()) extractOutlineNodes(current, level + 1) else emptyList()
            result.add(
                OutlineNode(
                    title = current.title ?: "Sección",
                    pageIndex = targetPage,
                    level = level,
                    children = children
                )
            )
            current = current.nextSibling
        }
        return result
    }

    override fun extractMetadata(): DocumentMetadata {
        val doc = document ?: return DocumentMetadata()
        val info = doc.documentInformation
        val file = openedFile
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        return DocumentMetadata(
            title = info?.title ?: file?.nameWithoutExtension ?: "",
            author = info?.author ?: "",
            subject = info?.subject ?: "",
            keywords = info?.keywords ?: "",
            creator = info?.creator ?: "",
            producer = info?.producer ?: "",
            creationDate = info?.creationDate?.let { sdf.format(it.time) } ?: "",
            modificationDate = info?.modificationDate?.let { sdf.format(it.time) } ?: "",
            pdfVersion = doc.version.toString(),
            pageCount = doc.numberOfPages,
            fileSize = file?.length() ?: 0L,
            isEncrypted = doc.isEncrypted,
            isSigned = doc.signatureDictionaries.isNotEmpty(),
            hasAcroForms = doc.documentCatalog.acroForm != null
        )
    }

    override fun extractAcroForms(): List<AcroFormField> {
        val doc = document ?: return emptyList()
        val acroForm = doc.documentCatalog.acroForm ?: return emptyList()
        val fields = mutableListOf<AcroFormField>()

        for (field in acroForm.fieldTree) {
            val name = field.fullyQualifiedName
            val type = field.javaClass.simpleName.removePrefix("PD")
            val value = field.valueAsString ?: ""
            val isReadOnly = field.isReadOnly
            val isRequired = field.isRequired

            // Widget page index
            var pageIndex = 0
            val widgets = field.widgets
            val bounds = if (widgets.isNotEmpty()) {
                val w = widgets[0]
                val rect = w.rectangle
                val p = w.page
                if (p != null) {
                    pageIndex = doc.pages.indexOf(p).coerceAtLeast(0)
                }
                floatArrayOf(rect.lowerLeftX, rect.lowerLeftY, rect.upperRightX, rect.upperRightY)
            } else {
                floatArrayOf(0f, 0f, 0f, 0f)
            }

            fields.add(
                AcroFormField(
                    name = name,
                    type = type,
                    pageIndex = pageIndex,
                    bounds = bounds,
                    value = value,
                    isReadOnly = isReadOnly,
                    isRequired = isRequired
                )
            )
        }
        return fields
    }

    override fun prefetch(currentPage: Int, targetWidth: Int, targetHeight: Int) {
        val doc = document ?: return
        val total = doc.numberOfPages
        if (targetWidth <= 0 || targetHeight <= 0) return

        prefetchJob?.cancel()
        prefetchJob = scope.launch {
            val pagesToPrefetch = listOf(currentPage + 1, currentPage - 1, currentPage + 2)
                .filter { it in 0 until total }

            for (p in pagesToPrefetch) {
                if (!isActive) break
                val key = PdfBitmapCache.buildKey(docIdentifier, p, targetWidth, targetHeight, 0)
                if (cache.get(key) == null) {
                    renderPage(p, targetWidth, targetHeight, 0)
                }
            }
        }
    }

    override fun close() {
        prefetchJob?.cancel()
        try {
            document?.close()
        } catch (e: Exception) {}
        document = null
        renderer = null
        openedFile = null
    }
}
