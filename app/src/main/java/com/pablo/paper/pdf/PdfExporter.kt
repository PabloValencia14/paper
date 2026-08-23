package com.pablo.paper.pdf

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import com.pablo.paper.data.repository.AnnotationRepository
import com.pablo.paper.domain.model.AnnotationType
import com.pablo.paper.domain.model.Document
import com.pablo.paper.domain.model.InkTool
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

object PdfExporter {

    suspend fun exportAnnotatedPdf(
        context: Context,
        document: Document,
        pdfEngine: PdfEngine,
        annotationRepository: AnnotationRepository,
        includeAnnotations: Boolean = true,
        onlyAnnotatedPages: Boolean = false
    ): File? = withContext(Dispatchers.IO) {
        val totalPages = pdfEngine.getPageCount()
        if (totalPages <= 0) return@withContext null

        val pdfDocument = PdfDocument()

        try {
            var exportedPageCounter = 0
            for (pageIndex in 0 until totalPages) {
                val annotations = if (includeAnnotations) {
                    annotationRepository.getAnnotationsForPage(document.id, pageIndex)
                } else {
                    emptyList()
                }

                if (onlyAnnotatedPages && annotations.isEmpty()) {
                    continue
                }

                exportedPageCounter++
                val pageSize = pdfEngine.getPageSize(pageIndex) ?: PageSize(595, 842)
                val width = pageSize.width
                val height = pageSize.height

                val pageInfo = PdfDocument.PageInfo.Builder(width, height, exportedPageCounter).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas

                // 1. Render original page bitmap
                val renderScale = 2.0f
                val bmp = pdfEngine.renderPage(pageIndex, (width * renderScale).toInt(), (height * renderScale).toInt())
                if (bmp != null && !bmp.isRecycled) {
                    val destRect = android.graphics.Rect(0, 0, width, height)
                    canvas.drawBitmap(bmp, null, destRect, null)
                }

                // 2. Render all annotations for this page (if enabled)
                if (includeAnnotations) {

                // 2.1 Highlighters (draw with transparency)
                val highlightAnnotations = annotations.filter {
                    it.stroke?.tool == InkTool.HIGHLIGHTER || it.stroke?.tool == InkTool.TEXT_HIGHLIGHT || it.type == AnnotationType.HIGHLIGHT
                }
                for (ann in highlightAnnotations) {
                    val stroke = ann.stroke ?: continue
                    val paint = Paint().apply {
                        color = stroke.color.toInt()
                        alpha = ((stroke.opacity.coerceIn(0.15f, 0.4f)) * 255).toInt()
                        style = Paint.Style.FILL
                        isAntiAlias = true
                    }

                    if (stroke.tool == InkTool.TEXT_HIGHLIGHT) {
                        for (i in 0 until stroke.points.size - 1 step 2) {
                            val p1 = stroke.points[i]
                            val p2 = stroke.points[i + 1]
                            val left = Math.min(p1.x, p2.x) * width
                            val right = Math.max(p1.x, p2.x) * width
                            val top = Math.min(p1.y, p2.y) * height
                            val bottom = Math.max(p1.y, p2.y) * height
                            canvas.drawRoundRect(RectF(left, top, right, bottom), 4f, 4f, paint)
                        }
                    } else {
                        // Freehand highlighter
                        paint.style = Paint.Style.STROKE
                        paint.strokeWidth = stroke.width
                        paint.strokeCap = Paint.Cap.ROUND
                        paint.strokeJoin = Paint.Join.ROUND
                        if (stroke.points.size >= 2) {
                            val path = Path().apply {
                                moveTo(stroke.points[0].x * width, stroke.points[0].y * height)
                                for (i in 1 until stroke.points.size) {
                                    lineTo(stroke.points[i].x * width, stroke.points[i].y * height)
                                }
                            }
                            canvas.drawPath(path, paint)
                        }
                    }
                }

                // 2.2 Inks, Shapes, Underlines, Sticky Notes, Signatures
                val otherAnnotations = annotations.filterNot {
                    it.stroke?.tool == InkTool.HIGHLIGHTER || it.stroke?.tool == InkTool.TEXT_HIGHLIGHT || it.type == AnnotationType.HIGHLIGHT
                }
                for (ann in otherAnnotations) {
                    val stroke = ann.stroke ?: continue
                    val paint = Paint().apply {
                        color = stroke.color.toInt()
                        alpha = ((stroke.opacity.coerceIn(0.1f, 1.0f)) * 255).toInt()
                        strokeWidth = stroke.width
                        isAntiAlias = true
                        style = Paint.Style.STROKE
                        strokeCap = Paint.Cap.ROUND
                        strokeJoin = Paint.Join.ROUND
                    }

                    when (ann.type) {
                        AnnotationType.SHAPE_RECTANGLE -> {
                            if (stroke.points.size >= 2) {
                                val p1 = stroke.points.first()
                                val p2 = stroke.points.last()
                                val left = Math.min(p1.x, p2.x) * width
                                val right = Math.max(p1.x, p2.x) * width
                                val top = Math.min(p1.y, p2.y) * height
                                val bottom = Math.max(p1.y, p2.y) * height
                                canvas.drawRoundRect(RectF(left, top, right, bottom), 6f, 6f, paint)
                            }
                        }
                        AnnotationType.SHAPE_OVAL -> {
                            if (stroke.points.size >= 2) {
                                val p1 = stroke.points.first()
                                val p2 = stroke.points.last()
                                val left = Math.min(p1.x, p2.x) * width
                                val right = Math.max(p1.x, p2.x) * width
                                val top = Math.min(p1.y, p2.y) * height
                                val bottom = Math.max(p1.y, p2.y) * height
                                canvas.drawOval(RectF(left, top, right, bottom), paint)
                            }
                        }
                        AnnotationType.SHAPE_ARROW -> {
                            if (stroke.points.size >= 2) {
                                val p1 = stroke.points.first()
                                val p2 = stroke.points.last()
                                val startX = p1.x * width
                                val startY = p1.y * height
                                val endX = p2.x * width
                                val endY = p2.y * height

                                canvas.drawLine(startX, startY, endX, endY, paint)

                                // Arrowhead
                                val angle = atan2((endY - startY).toDouble(), (endX - startX).toDouble())
                                val headLen = 16f
                                val x1 = endX - headLen * cos(angle - Math.PI / 6).toFloat()
                                val y1 = endY - headLen * sin(angle - Math.PI / 6).toFloat()
                                val x2 = endX - headLen * cos(angle + Math.PI / 6).toFloat()
                                val y2 = endY - headLen * sin(angle + Math.PI / 6).toFloat()

                                val arrowHeadPath = Path().apply {
                                    moveTo(endX, endY)
                                    lineTo(x1, y1)
                                    lineTo(x2, y2)
                                    close()
                                }
                                val fillPaint = Paint(paint).apply { style = Paint.Style.FILL }
                                canvas.drawPath(arrowHeadPath, fillPaint)
                            }
                        }
                        AnnotationType.SHAPE_LINE -> {
                            if (stroke.points.size >= 2) {
                                val p1 = stroke.points.first()
                                val p2 = stroke.points.last()
                                canvas.drawLine(p1.x * width, p1.y * height, p2.x * width, p2.y * height, paint)
                            }
                        }
                        AnnotationType.STICKY_NOTE -> {
                            if (stroke.points.isNotEmpty()) {
                                val p = stroke.points.first()
                                val cx = p.x * width
                                val cy = p.y * height

                                // Yellow rounded square pin
                                val notePaint = Paint().apply {
                                    color = 0xFFFFD54F.toInt()
                                    style = Paint.Style.FILL
                                    isAntiAlias = true
                                }
                                canvas.drawRoundRect(RectF(cx - 10f, cy - 10f, cx + 10f, cy + 10f), 4f, 4f, notePaint)

                                val borderPaint = Paint().apply {
                                    color = 0xFF5D4037.toInt()
                                    style = Paint.Style.STROKE
                                    strokeWidth = 1.5f
                                    isAntiAlias = true
                                }
                                canvas.drawRoundRect(RectF(cx - 10f, cy - 10f, cx + 10f, cy + 10f), 4f, 4f, borderPaint)
                            }
                        }
                        AnnotationType.TEXT_BOX -> {
                            if (stroke.points.isNotEmpty()) {
                                val p = stroke.points.first()
                                val cx = p.x * width
                                val cy = p.y * height
                                val text = ann.textContent ?: ""
                                if (text.isNotEmpty()) {
                                    val textPaint = Paint().apply {
                                        color = stroke.color.toInt()
                                        textSize = 14f
                                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                                        isAntiAlias = true
                                    }
                                    val bgPaint = Paint().apply {
                                        color = android.graphics.Color.WHITE
                                        style = Paint.Style.FILL
                                        isAntiAlias = true
                                    }
                                    val borderPaint = Paint().apply {
                                        color = stroke.color.toInt()
                                        strokeWidth = 1.5f
                                        style = Paint.Style.STROKE
                                        isAntiAlias = true
                                    }
                                    val lines = text.split("\n")
                                    var maxW = 0f
                                    for (l in lines) {
                                        val w = textPaint.measureText(l)
                                        if (w > maxW) maxW = w
                                    }
                                    val lineHeight = textPaint.fontSpacing
                                    val totalH = lineHeight * lines.size
                                    val pad = 6f
                                    val rect = RectF(cx - pad, cy - lineHeight + pad / 2f, cx + maxW + pad, cy + totalH - lineHeight + pad)
                                    canvas.drawRoundRect(rect, 4f, 4f, bgPaint)
                                    canvas.drawRoundRect(rect, 4f, 4f, borderPaint)
                                    var curY = cy
                                    for (l in lines) {
                                        canvas.drawText(l, cx, curY, textPaint)
                                        curY += lineHeight
                                    }
                                }
                            }
                        }
                        AnnotationType.STAMP -> {
                            if (stroke.points.isNotEmpty()) {
                                val p = stroke.points.first()
                                val cx = p.x * width
                                val cy = p.y * height
                                val stampText = ann.textContent ?: "APROBADO"
                                canvas.save()
                                canvas.rotate(-6f, cx, cy)
                                val stampColor = stroke.color.toInt()
                                val textPaint = Paint().apply {
                                    color = stampColor
                                    textSize = 15f
                                    typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                                    isAntiAlias = true
                                }
                                val borderPaint = Paint().apply {
                                    color = stampColor
                                    strokeWidth = 2.5f
                                    style = Paint.Style.STROKE
                                    isAntiAlias = true
                                }
                                val bgPaint = Paint().apply {
                                    color = android.graphics.Color.argb(35, android.graphics.Color.red(stampColor), android.graphics.Color.green(stampColor), android.graphics.Color.blue(stampColor))
                                    style = Paint.Style.FILL
                                }
                                val textW = textPaint.measureText(stampText)
                                val textH = textPaint.fontSpacing
                                val padX = 12f
                                val padY = 6f
                                val rect = RectF(cx - textW / 2f - padX, cy - textH / 2f - padY, cx + textW / 2f + padX, cy + textH / 2f + padY)
                                canvas.drawRoundRect(rect, 6f, 6f, bgPaint)
                                canvas.drawRoundRect(rect, 6f, 6f, borderPaint)
                                canvas.drawText(stampText, cx - textW / 2f, cy + textH * 0.28f, textPaint)
                                canvas.restore()
                            }
                        }
                        else -> {
                            // Pen, Underline, Strikethrough, Signature
                            if (stroke.points.size >= 2) {
                                val path = Path().apply {
                                    moveTo(stroke.points[0].x * width, stroke.points[0].y * height)
                                    for (i in 1 until stroke.points.size) {
                                        lineTo(stroke.points[i].x * width, stroke.points[i].y * height)
                                    }
                                }
                                canvas.drawPath(path, paint)
                            }
                        }
                    }
                }
                }

                pdfDocument.finishPage(page)
            }

            // Save to Downloads / Documents folder
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) downloadsDir.mkdirs()

            val sanitizedDocName = document.name.removeSuffix(".pdf")
            val outputFile = File(downloadsDir, "${sanitizedDocName}_Anotado_${System.currentTimeMillis()}.pdf")

            FileOutputStream(outputFile).use { out ->
                pdfDocument.writeTo(out)
            }

            outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            pdfDocument.close()
        }
    }

    fun sharePdf(context: Context, file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Compartir PDF anotado con...")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error al compartir: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
