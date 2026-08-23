package com.pablo.paper.pdf

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pablo.paper.ai.OpenRouterClient
import com.pablo.paper.domain.model.AssistantMessage
import com.pablo.paper.domain.model.Document
import com.pablo.paper.domain.model.MessageRole
import com.pablo.paper.ocr.PdfTextExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.regex.Pattern

data class OutlineItem(
    val title: String,
    val pageNumber: Int,
    val level: Int = 0,
    val isAiGenerated: Boolean = false
)

object OutlineExtractor {

    private val gson = Gson()

    private val HEADING_PATTERNS = listOf(
        Pattern.compile("^(EJERCICIO|PRACTICA|PRÁCTICA|TEMA|CAPÍTULO|CAPITULO|SECCIÓN|SECCION|UNIDAD|PARTE|MÓDULO|MODULO|PASO)\\s*(\\d+|[IVXLCDM]+)?[:.\\s-]*(.*)$", Pattern.CASE_INSENSITIVE),
        Pattern.compile("^(\\d+(\\.\\d+)*)[:.\\s-]+\\s*([A-ZÁÉÍÓÚÑa-záéíóúñ].*)$"),
        Pattern.compile("^([IVXLCDM]+\\.)\\s+([A-ZÁÉÍÓÚÑa-záéíóúñ].*)$")
    )

    private val TOC_PAGE_PATTERNS = listOf(
        Pattern.compile("^(ÍNDICE|INDICE|TABLA DE CONTENIDOS|CONTENIDO|SUMARIO|TABLE OF CONTENTS|CONTENTS)\\b", Pattern.CASE_INSENSITIVE)
    )

    private val TOC_LINE_PATTERN = Pattern.compile("^(.*?)(\\s*[.…_\\-]{2,}|\\s{3,})(\\d{1,4})$")

    /**
     * AI-Powered Table of Contents Extraction.
     * Uses OpenRouter model (default: Dots3-Note) to analyze pages and generate a semantic index.
     */
    suspend fun extractOutlineWithAi(
        document: Document,
        pdfEngine: PdfEngine,
        pageCount: Int,
        openRouterClient: OpenRouterClient,
        apiKey: String,
        modelId: String
    ): Result<List<OutlineItem>> = withContext(Dispatchers.IO) {
        try {
            val pagesToSample = minOf(pageCount, 30)
            val docSummaryBuilder = StringBuilder()
            docSummaryBuilder.append("DOCUMENT NAME: ${document.name}\nTOTAL PAGES: $pageCount\n\n")

            for (pageIdx in 0 until pagesToSample) {
                val pageNum = pageIdx + 1
                val pageData = PdfTextExtractor.getPageText(document.id, pageIdx, pdfEngine)
                val lines = pageData.fullText.lines().map { it.trim() }.filter { it.isNotBlank() }

                val sampleLines = if (lines.size <= 10) lines else lines.take(10)
                val sampleText = sampleLines.joinToString("\n")
                if (sampleText.isNotBlank()) {
                    docSummaryBuilder.append("--- PÁGINA $pageNum ---\n$sampleText\n\n")
                }
            }

            val prompt = """
                Analiza las páginas del documento anterior y extrae el Índice de Contenidos / Esquema Estructurado (Table of Contents).
                Instrucciones:
                1. Detecta todos los títulos reales de capítulos, temas, secciones, ejercicios, pasos o prácticas.
                2. NO incluyas encabezados repetitivos que aparezcan en todas las páginas (como nombres de asignatura o 'PRÁCTICA 1' repetido).
                3. Asigna a cada uno su número exacto de página (pageNumber) donde empieza (1 a $pageCount).
                4. Asigna level: 0 para títulos principales o temas, y level: 1 para subsecciones o pasos.
                5. Limpia el título quitando caracteres extraños.
                6. Responde EXCLUSIVAMENTE con un array JSON válido sin explicaciones adicionales:
                [
                  {"title": "EJERCICIO 1: CREACIÓN DE UN TABLERO", "pageNumber": 1, "level": 0},
                  {"title": "1.1 Configuración de Listas", "pageNumber": 2, "level": 1}
                ]
            """.trimIndent()

            val messages = listOf(
                AssistantMessage(
                    role = MessageRole.USER,
                    content = "${docSummaryBuilder.toString()}\n\n$prompt"
                )
            )

            val effectiveModel = if (modelId.isNotBlank()) modelId else "dots-studio/dots-3-note-preview:free"
            val responseResult = openRouterClient.sendChat(
                apiKey = apiKey,
                modelId = effectiveModel,
                messages = messages,
                systemPrompt = "Eres un asistente experto en indexación de documentos PDF. Devuelves únicamente arrays JSON válidos con el índice."
            )

            if (responseResult.isSuccess) {
                val rawText = responseResult.getOrNull() ?: ""
                val startIndex = rawText.indexOf('[')
                val endIndex = rawText.lastIndexOf(']')

                if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
                    val jsonSlice = rawText.substring(startIndex, endIndex + 1)
                    val listType = object : TypeToken<List<OutlineItem>>() {}.type
                    val parsed: List<OutlineItem> = gson.fromJson(jsonSlice, listType)
                    val valid = parsed
                        .filter { it.title.isNotBlank() && it.pageNumber in 1..pageCount }
                        .map { it.copy(isAiGenerated = true) }
                        .distinctBy { "${it.title}_${it.pageNumber}" }
                        .sortedBy { it.pageNumber }

                    if (valid.isNotEmpty()) {
                        Result.success(valid)
                    } else {
                        Result.failure(Exception("Índice extraído vacío"))
                    }
                } else {
                    Result.failure(Exception("Formato JSON no encontrado en la respuesta"))
                }
            } else {
                Result.failure(responseResult.exceptionOrNull() ?: Exception("Error en consulta IA"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Local heuristic OCR fallback extraction with recurring header filtering.
     */
    suspend fun extractOutline(
        context: Context,
        document: Document,
        pdfEngine: PdfEngine,
        pageCount: Int
    ): List<OutlineItem> = withContext(Dispatchers.IO) {
        val extractedEntries = mutableListOf<OutlineItem>()
        val seenPages = mutableSetOf<Int>()
        val seenTitles = mutableSetOf<String>()

        // 1. Gather all top lines to identify recurring running headers across pages
        val pagesToScan = (0 until minOf(pageCount, 30)).toList()
        val pageTextMap = mutableMapOf<Int, List<String>>()
        val headerFrequency = mutableMapOf<String, Int>()

        for (pageIdx in pagesToScan) {
            val pageData = PdfTextExtractor.getPageText(document.id, pageIdx, pdfEngine)
            val lines = pageData.fullText.lines().map { it.trim() }.filter { it.isNotBlank() }
            pageTextMap[pageIdx] = lines
            // Sample first 2 lines for header repetition
            for (line in lines.take(2)) {
                val norm = line.lowercase().replace(Regex("[^a-záéíóúñ0-9]"), "")
                if (norm.length in 3..40) {
                    headerFrequency[norm] = (headerFrequency[norm] ?: 0) + 1
                }
            }
        }

        // Running headers appear on >= 2 distinct pages
        val recurringHeaderNorms = headerFrequency.filter { it.value >= 2 }.keys

        // 2. Check if any page contains an explicit Table of Contents / Índice
        for (pageIdx in 0 until minOf(4, pageCount)) {
            val lines = pageTextMap[pageIdx] ?: continue
            val fullText = lines.joinToString("\n")

            if (TOC_PAGE_PATTERNS.any { it.matcher(fullText).find() }) {
                for (line in lines) {
                    val matcher = TOC_LINE_PATTERN.matcher(line)
                    if (matcher.find()) {
                        val titlePart = matcher.group(1)?.trim()?.trimEnd('.', ' ', '-', '…') ?: ""
                        val pageNumStr = matcher.group(3)?.trim() ?: ""
                        val targetPage = pageNumStr.toIntOrNull()
                        if (titlePart.isNotBlank() && targetPage != null && targetPage in 1..pageCount) {
                            val normTitle = titlePart.lowercase()
                            if (!seenTitles.contains(normTitle)) {
                                val level = if (titlePart.startsWith("1.") || titlePart.startsWith("2.") || titlePart.startsWith("3.") ||
                                    titlePart.startsWith("4.") || titlePart.startsWith("5.") || titlePart.startsWith("6.") ||
                                    titlePart.startsWith("7.") || titlePart.startsWith("8.") || titlePart.startsWith("9.") ||
                                    titlePart.startsWith("I.") || titlePart.startsWith("II.") || titlePart.startsWith("III.")) 0 else 1

                                extractedEntries.add(OutlineItem(title = titlePart, pageNumber = targetPage, level = level, isAiGenerated = false))
                                seenPages.add(targetPage)
                                seenTitles.add(normTitle)
                            }
                        }
                    }
                }
            }
        }

        // 3. Scan pages for prominent section headings (ignoring running headers)
        for (pageIdx in pagesToScan) {
            val pageNum = pageIdx + 1
            val lines = pageTextMap[pageIdx] ?: continue

            if (lines.isEmpty()) {
                if (pageNum == 1 && !seenPages.contains(1)) {
                    val docTitle = document.name.removeSuffix(".pdf").replace('_', ' ').replace('-', ' ').trim()
                    extractedEntries.add(OutlineItem(title = if (docTitle.isNotBlank()) docTitle else "Portada / Inicio", pageNumber = 1, level = 0, isAiGenerated = false))
                    seenPages.add(1)
                }
                continue
            }

            // Filter out lines that match recurring running headers
            val contentLines = lines.filter { line ->
                val norm = line.lowercase().replace(Regex("[^a-záéíóúñ0-9]"), "")
                !recurringHeaderNorms.contains(norm) &&
                !line.equals("UNIVERSIDAD DE CÓRDOBA", ignoreCase = true) &&
                !line.equals("PÁGINA", ignoreCase = true) &&
                !line.startsWith("Página ", ignoreCase = true)
            }

            val topLines = contentLines.take(8)
            var detectedHeading: String? = null
            var detectedLevel = 0

            // Check pattern matching on non-header lines
            for (line in topLines) {
                for (pat in HEADING_PATTERNS) {
                    val m = pat.matcher(line)
                    if (m.matches()) {
                        val norm = line.lowercase()
                        if (!seenTitles.contains(norm)) {
                            detectedHeading = line
                            detectedLevel = if (line.contains(".") && line.count { it == '.' } > 1) 1 else 0
                            break
                        }
                    }
                }
                if (detectedHeading != null) break
            }

            // Fallback: title-cased or uppercase prominent lines
            if (detectedHeading == null) {
                for (line in topLines) {
                    val words = line.split(" ")
                    val isShort = words.size in 2..10 && line.length in 6..75
                    val isUpper = line.all { !it.isLetter() || it.isUpperCase() }
                    val isTitleLike = isShort && (isUpper || line.first().isUpperCase()) && !line.endsWith(".") && !line.contains(",")

                    val norm = line.lowercase()
                    if (isTitleLike && !seenTitles.contains(norm)) {
                        detectedHeading = line
                        detectedLevel = 0
                        break
                    }
                }
            }

            if (detectedHeading != null && !seenPages.contains(pageNum)) {
                val cleanHeading = detectedHeading.replace(Regex("\\s+"), " ").trim()
                extractedEntries.add(OutlineItem(title = cleanHeading, pageNumber = pageNum, level = detectedLevel, isAiGenerated = false))
                seenPages.add(pageNum)
                seenTitles.add(cleanHeading.lowercase())
            } else if (pageNum == 1 && !seenPages.contains(1)) {
                val candidate = topLines.firstOrNull { it.length in 5..60 } ?: document.name.removeSuffix(".pdf")
                extractedEntries.add(OutlineItem(title = candidate, pageNumber = 1, level = 0, isAiGenerated = false))
                seenPages.add(1)
                seenTitles.add(candidate.lowercase())
            }
        }

        if (extractedEntries.isEmpty()) {
            for (p in 1..pageCount) {
                val title = when (p) {
                    1 -> "Portada / Inicio"
                    else -> "Página $p"
                }
                extractedEntries.add(OutlineItem(title = title, pageNumber = p, level = 0, isAiGenerated = false))
            }
        }

        extractedEntries.distinctBy { "${it.title}_${it.pageNumber}" }.sortedBy { it.pageNumber }
    }
}
