package com.pablo.paper.ocr

import android.graphics.Bitmap
import android.graphics.RectF
import android.util.LruCache
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.pablo.paper.domain.model.Annotation
import com.pablo.paper.domain.model.AnnotationType
import com.pablo.paper.pdf.PdfEngine
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class RecognizedWord(
    val text: String,
    val normalizedBounds: RectF // normalized 0f..1f
)

data class PageTextData(
    val pageIndex: Int,
    val fullText: String,
    val words: List<RecognizedWord>
)

object PdfTextExtractor {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    // OCR data can contain thousands of words and bounding boxes per page. Bound it explicitly so
    // scanning/searching a long book cannot retain every page for the process lifetime.
    private val pageTextCache = object : LruCache<String, PageTextData>(MAX_CACHED_PAGES) {}

    private fun cacheKey(documentId: String, pageIndex: Int): String = "$documentId:$pageIndex"

    suspend fun getPageText(
        documentId: String,
        pageIndex: Int,
        pdfEngine: PdfEngine
    ): PageTextData = withContext(Dispatchers.IO) {
        val key = cacheKey(documentId, pageIndex)
        val cached = pageTextCache[key]
        if (cached != null) return@withContext cached

        // Render high-res bitmap for OCR (e.g. 1400 x 1900)
        val bitmap = pdfEngine.renderPage(pageIndex, 1400, 1900)
        if (bitmap == null || bitmap.isRecycled) {
            return@withContext PageTextData(pageIndex, "", emptyList())
        }

        val data = processBitmap(bitmap, pageIndex)
        pageTextCache.put(key, data)
        data
    }

    suspend fun processBitmap(bitmap: Bitmap, pageIndex: Int = 0): PageTextData = withContext(Dispatchers.IO) {
        try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val visionText: Text = Tasks.await(recognizer.process(image))

            val bmpW = bitmap.width.toFloat().coerceAtLeast(1f)
            val bmpH = bitmap.height.toFloat().coerceAtLeast(1f)

            val wordsList = mutableListOf<RecognizedWord>()
            val fullTextBuilder = StringBuilder()

            for (block in visionText.textBlocks) {
                for (line in block.lines) {
                    for (element in line.elements) {
                        val box = element.boundingBox
                        if (box != null) {
                            val normRect = RectF(
                                box.left / bmpW,
                                box.top / bmpH,
                                box.right / bmpW,
                                box.bottom / bmpH
                            )
                            wordsList.add(RecognizedWord(element.text, normRect))
                        }
                    }
                    fullTextBuilder.append(line.text).append("\n")
                }
                fullTextBuilder.append("\n")
            }

            PageTextData(
                pageIndex = pageIndex,
                fullText = fullTextBuilder.toString().trim(),
                words = wordsList
            )
        } catch (e: Exception) {
            e.printStackTrace()
            PageTextData(pageIndex, "", emptyList())
        }
    }

    /**
     * Extracts text within a normalized bounding box [left, top, right, bottom] (0..1).
     */
    fun extractTextInRect(pageData: PageTextData, rect: RectF): String {
        val matchingWords = pageData.words.filter { word ->
            RectF.intersects(word.normalizedBounds, rect)
        }
        return matchingWords.joinToString(" ") { it.text }
    }

    /**
     * Extracts text that intersects with any of the user's highlights / underlines on this page.
     */
    fun extractHighlightedText(pageData: PageTextData, annotations: List<Annotation>): String {
        if (annotations.isEmpty()) return ""

        val highlightBoxes = mutableListOf<RectF>()
        for (annot in annotations) {
            if (annot.type == AnnotationType.HIGHLIGHT ||
                annot.type == AnnotationType.UNDERLINE ||
                annot.type == AnnotationType.STRIKETHROUGH
            ) {
                val stroke = annot.stroke
                if (stroke != null && stroke.points.isNotEmpty()) {
                    var minX = 1f
                    var maxX = 0f
                    var minY = 1f
                    var maxY = 0f
                    for (pt in stroke.points) {
                        if (pt.x < minX) minX = pt.x
                        if (pt.x > maxX) maxX = pt.x
                        if (pt.y < minY) minY = pt.y
                        if (pt.y > maxY) maxY = pt.y
                    }
                    // Expand slightly vertically to capture the line of text
                    val expandY = 0.015f
                    highlightBoxes.add(
                        RectF(
                            minX.coerceIn(0f, 1f),
                            (minY - expandY).coerceIn(0f, 1f),
                            maxX.coerceIn(0f, 1f),
                            (maxY + expandY).coerceIn(0f, 1f)
                        )
                    )
                }
            }
        }

        if (highlightBoxes.isEmpty()) return ""

        val matchingWords = pageData.words.filter { word ->
            highlightBoxes.any { box -> RectF.intersects(word.normalizedBounds, box) }
        }

        return matchingWords.joinToString(" ") { it.text }
    }

    fun clearCache() {
        pageTextCache.evictAll()
    }

    private const val MAX_CACHED_PAGES = 16
}

data class SearchMatch(
    val pageIndex: Int,
    val matchIndex: Int,
    val matchedText: String,
    val snippet: String,
    val bounds: List<RectF>
)

object PdfSearchEngine {

    private fun normalize(str: String): String {
        val nfd = java.text.Normalizer.normalize(str, java.text.Normalizer.Form.NFD)
        val pattern = java.util.regex.Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
        return pattern.matcher(nfd).replaceAll("").lowercase().trim()
    }

    suspend fun searchPage(
        documentId: String,
        pageIndex: Int,
        query: String,
        pdfEngine: PdfEngine
    ): List<SearchMatch> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return@withContext emptyList()

        val normalizedQuery = normalize(trimmed)
        if (normalizedQuery.isEmpty()) return@withContext emptyList()

        val pageData = PdfTextExtractor.getPageText(documentId, pageIndex, pdfEngine)
        if (pageData.words.isEmpty()) return@withContext emptyList()

        val matches = mutableListOf<SearchMatch>()
        val words = pageData.words
        val queryTokens = normalizedQuery.split(Regex("\\s+")).filter { it.isNotEmpty() }

        if (queryTokens.isEmpty()) return@withContext emptyList()

        if (queryTokens.size == 1) {
            val singleToken = queryTokens.first()
            for (i in words.indices) {
                val word = words[i]
                val normalizedWord = normalize(word.text)
                if (normalizedWord.contains(singleToken)) {
                    val snippetStart = (i - 4).coerceAtLeast(0)
                    val snippetEnd = (i + 5).coerceAtMost(words.size)
                    val snippetText = words.subList(snippetStart, snippetEnd).joinToString(" ") { it.text }

                    matches.add(
                        SearchMatch(
                            pageIndex = pageIndex,
                            matchIndex = 0,
                            matchedText = word.text,
                            snippet = "...$snippetText...",
                            bounds = listOf(word.normalizedBounds)
                        )
                    )
                }
            }
        } else {
            val tokenCount = queryTokens.size
            for (i in 0..(words.size - tokenCount)) {
                var allMatched = true
                for (j in 0 until tokenCount) {
                    val wNorm = normalize(words[i + j].text)
                    val qNorm = queryTokens[j]
                    if (!wNorm.contains(qNorm)) {
                        allMatched = false
                        break
                    }
                }

                if (allMatched) {
                    val matchingSlice = words.subList(i, i + tokenCount)
                    val matchedWordsText = matchingSlice.joinToString(" ") { it.text }
                    val snippetStart = (i - 3).coerceAtLeast(0)
                    val snippetEnd = (i + tokenCount + 3).coerceAtMost(words.size)
                    val snippetText = words.subList(snippetStart, snippetEnd).joinToString(" ") { it.text }

                    matches.add(
                        SearchMatch(
                            pageIndex = pageIndex,
                            matchIndex = 0,
                            matchedText = matchedWordsText,
                            snippet = "...$snippetText...",
                            bounds = matchingSlice.map { it.normalizedBounds }
                        )
                    )
                }
            }
        }

        matches
    }
}
