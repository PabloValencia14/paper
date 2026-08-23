package com.pablo.paper.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.os.Environment
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object PdfManipulator {

    /**
     * Merge multiple PDF files into a single unified PDF file.
     */
    suspend fun mergePdfs(
        context: Context,
        sourceFiles: List<File>,
        outputFile: File
    ): Boolean = withContext(Dispatchers.IO) {
        if (sourceFiles.isEmpty()) return@withContext false

        val pdfDocument = PdfDocument()
        var currentGlobalPage = 1

        try {
            for (file in sourceFiles) {
                if (!file.exists()) continue
                val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                val renderer = PdfRenderer(pfd)

                for (i in 0 until renderer.pageCount) {
                    val page = renderer.openPage(i)
                    val w = page.width
                    val h = page.height

                    val pageInfo = PdfDocument.PageInfo.Builder(w, h, currentGlobalPage++).create()
                    val docPage = pdfDocument.startPage(pageInfo)

                    val bmp = Bitmap.createBitmap(w * 2, h * 2, Bitmap.Config.ARGB_8888)
                    page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)

                    val destRect = android.graphics.Rect(0, 0, w, h)
                    docPage.canvas.drawBitmap(bmp, null, destRect, null)
                    bmp.recycle()

                    pdfDocument.finishPage(docPage)
                    page.close()
                }

                renderer.close()
                pfd.close()
            }

            FileOutputStream(outputFile).use { out ->
                pdfDocument.writeTo(out)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            pdfDocument.close()
        }
    }

    /**
     * Split / Extract a range of pages from a source PDF into a new PDF.
     */
    suspend fun splitPdf(
        context: Context,
        sourceFile: File,
        startPageIndex: Int,
        endPageIndex: Int,
        outputFile: File
    ): Boolean = withContext(Dispatchers.IO) {
        if (!sourceFile.exists()) return@withContext false

        val pdfDocument = PdfDocument()
        try {
            val pfd = ParcelFileDescriptor.open(sourceFile, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(pfd)
            val total = renderer.pageCount

            val safeStart = startPageIndex.coerceIn(0, total - 1)
            val safeEnd = endPageIndex.coerceIn(safeStart, total - 1)

            var pageNum = 1
            for (i in safeStart..safeEnd) {
                val page = renderer.openPage(i)
                val w = page.width
                val h = page.height

                val pageInfo = PdfDocument.PageInfo.Builder(w, h, pageNum++).create()
                val docPage = pdfDocument.startPage(pageInfo)

                val bmp = Bitmap.createBitmap(w * 2, h * 2, Bitmap.Config.ARGB_8888)
                page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)

                val destRect = android.graphics.Rect(0, 0, w, h)
                docPage.canvas.drawBitmap(bmp, null, destRect, null)
                bmp.recycle()

                pdfDocument.finishPage(docPage)
                page.close()
            }

            renderer.close()
            pfd.close()

            FileOutputStream(outputFile).use { out ->
                pdfDocument.writeTo(out)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            pdfDocument.close()
        }
    }

    /**
     * Re-export a PDF document with altered page order, rotations, duplications or deletions.
     */
    suspend fun rebuildDocument(
        context: Context,
        sourceFile: File,
        pageOrder: List<Int>,
        rotations: Map<Int, Int>,
        insertedBlankPages: Map<Int, String>, // page index -> template (Lined, Grid, Dot, Blank)
        outputFile: File
    ): Boolean = withContext(Dispatchers.IO) {
        if (!sourceFile.exists()) return@withContext false

        val pdfDocument = PdfDocument()
        try {
            val pfd = ParcelFileDescriptor.open(sourceFile, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(pfd)
            val total = renderer.pageCount

            var pageNum = 1

            for (origIdx in pageOrder) {
                if (origIdx in 0 until total) {
                    val page = renderer.openPage(origIdx)
                    val rot = (rotations[origIdx] ?: 0) % 360
                    val isRotated90or270 = (rot == 90 || rot == 270)

                    val w = if (isRotated90or270) page.height else page.width
                    val h = if (isRotated90or270) page.width else page.height

                    val pageInfo = PdfDocument.PageInfo.Builder(w, h, pageNum++).create()
                    val docPage = pdfDocument.startPage(pageInfo)
                    val canvas = docPage.canvas

                    val bmp = Bitmap.createBitmap(page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888)
                    page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)

                    if (rot != 0) {
                        canvas.save()
                        canvas.rotate(rot.toFloat(), w / 2f, h / 2f)
                    }

                    val destRect = if (isRotated90or270) {
                        val offsetW = (w - page.width) / 2
                        val offsetH = (h - page.height) / 2
                        android.graphics.Rect(offsetW, offsetH, offsetW + page.width, offsetH + page.height)
                    } else {
                        android.graphics.Rect(0, 0, w, h)
                    }
                    canvas.drawBitmap(bmp, null, destRect, null)

                    if (rot != 0) {
                        canvas.restore()
                    }

                    bmp.recycle()
                    pdfDocument.finishPage(docPage)
                    page.close()
                }

                // Check if blank page inserted after this index
                if (insertedBlankPages.containsKey(origIdx)) {
                    val template = insertedBlankPages[origIdx] ?: "BLANK"
                    val w = 595
                    val h = 842
                    val pageInfo = PdfDocument.PageInfo.Builder(w, h, pageNum++).create()
                    val docPage = pdfDocument.startPage(pageInfo)
                    val canvas = docPage.canvas

                    // Draw white background
                    canvas.drawColor(Color.WHITE)
                    val gridPaint = Paint().apply {
                        color = Color.parseColor("#E0E0E0")
                        strokeWidth = 1f
                        isAntiAlias = true
                    }

                    when (template.uppercase()) {
                        "LINED" -> {
                            var y = 60f
                            while (y < h - 40f) {
                                canvas.drawLine(40f, y, w - 40f, y, gridPaint)
                                y += 28f
                            }
                        }
                        "GRID" -> {
                            var x = 40f
                            while (x < w - 40f) {
                                canvas.drawLine(x, 40f, x, h - 40f, gridPaint)
                                x += 24f
                            }
                            var y = 40f
                            while (y < h - 40f) {
                                canvas.drawLine(40f, y, w - 40f, y, gridPaint)
                                y += 24f
                            }
                        }
                        "DOT" -> {
                            val dotPaint = Paint().apply {
                                color = Color.parseColor("#BDBDBD")
                                strokeWidth = 2.5f
                                strokeCap = Paint.Cap.ROUND
                                isAntiAlias = true
                            }
                            var x = 40f
                            while (x < w - 40f) {
                                var y = 40f
                                while (y < h - 40f) {
                                    canvas.drawPoint(x, y, dotPaint)
                                    y += 24f
                                }
                                x += 24f
                            }
                        }
                        else -> {
                            // Blank
                        }
                    }

                    pdfDocument.finishPage(docPage)
                }
            }

            renderer.close()
            pfd.close()

            FileOutputStream(outputFile).use { out ->
                pdfDocument.writeTo(out)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            pdfDocument.close()
        }
    }
}
