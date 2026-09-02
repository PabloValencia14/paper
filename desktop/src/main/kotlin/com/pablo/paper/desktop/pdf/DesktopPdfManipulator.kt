package com.pablo.paper.desktop.pdf

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.pdfbox.Loader
import org.apache.pdfbox.multipdf.PDFMergerUtility
import org.apache.pdfbox.multipdf.Splitter
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.encryption.AccessPermission
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.font.Standard14Fonts
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm
import java.io.File
import java.io.FileOutputStream

object DesktopPdfManipulator {

    suspend fun mergePdfs(inputFiles: List<File>, outputFile: File): Boolean = withContext(Dispatchers.IO) {
        try {
            val merger = PDFMergerUtility()
            merger.destinationFileName = outputFile.absolutePath
            for (file in inputFiles) {
                if (file.exists()) merger.addSource(file)
            }
            merger.mergeDocuments(org.apache.pdfbox.io.IOUtils.createMemoryOnlyStreamCache())
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun splitPdf(inputFile: File, splitEvery: Int, outputDir: File): List<File> = withContext(Dispatchers.IO) {
        val result = mutableListOf<File>()
        try {
            val doc = Loader.loadPDF(inputFile)
            val splitter = Splitter()
            splitter.setSplitAtPage(splitEvery)
            val pages = splitter.split(doc)

            var part = 1
            for (partDoc in pages) {
                val outFile = File(outputDir, "${inputFile.nameWithoutExtension}_part$part.pdf")
                partDoc.save(outFile)
                partDoc.close()
                result.add(outFile)
                part++
            }
            doc.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        result
    }

    suspend fun extractPages(inputFile: File, pageIndices: List<Int>, outputFile: File): Boolean = withContext(Dispatchers.IO) {
        try {
            val srcDoc = Loader.loadPDF(inputFile)
            val destDoc = PDDocument()

            for (index in pageIndices) {
                if (index in 0 until srcDoc.numberOfPages) {
                    destDoc.addPage(srcDoc.getPage(index))
                }
            }
            destDoc.save(outputFile)
            destDoc.close()
            srcDoc.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deletePages(inputFile: File, pageIndicesToDelete: Set<Int>, outputFile: File): Boolean = withContext(Dispatchers.IO) {
        try {
            val srcDoc = Loader.loadPDF(inputFile)
            val destDoc = PDDocument()

            for (i in 0 until srcDoc.numberOfPages) {
                if (i !in pageIndicesToDelete) {
                    destDoc.addPage(srcDoc.getPage(i))
                }
            }
            destDoc.save(outputFile)
            destDoc.close()
            srcDoc.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun rotatePages(inputFile: File, pageIndices: List<Int>, degrees: Int, outputFile: File): Boolean = withContext(Dispatchers.IO) {
        try {
            val doc = Loader.loadPDF(inputFile)
            val targetIndices = if (pageIndices.isEmpty()) (0 until doc.numberOfPages).toList() else pageIndices

            for (index in targetIndices) {
                if (index in 0 until doc.numberOfPages) {
                    val page = doc.getPage(index)
                    page.rotation = (page.rotation + degrees + 360) % 360
                }
            }
            doc.save(outputFile)
            doc.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun reorderPages(inputFile: File, newOrder: List<Int>, outputFile: File): Boolean = withContext(Dispatchers.IO) {
        try {
            val srcDoc = Loader.loadPDF(inputFile)
            val destDoc = PDDocument()

            for (index in newOrder) {
                if (index in 0 until srcDoc.numberOfPages) {
                    destDoc.addPage(srcDoc.getPage(index))
                }
            }
            destDoc.save(outputFile)
            destDoc.close()
            srcDoc.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun addWatermark(
        inputFile: File,
        watermarkText: String,
        opacity: Float = 0.3f,
        rotationDegrees: Float = 45f,
        outputFile: File
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val doc = Loader.loadPDF(inputFile)
            val font = PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD)
            val fontSize = 42f

            val gState = PDExtendedGraphicsState()
            gState.nonStrokingAlphaConstant = opacity

            for (page in doc.pages) {
                val mediaBox = page.mediaBox ?: page.cropBox
                val contentStream = PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true)
                contentStream.setGraphicsStateParameters(gState)
                contentStream.setNonStrokingColor(0.6f, 0.6f, 0.6f)
                contentStream.beginText()
                contentStream.setFont(font, fontSize)

                val stringWidth = font.getStringWidth(watermarkText) / 1000f * fontSize
                val x = (mediaBox.width - stringWidth) / 2f
                val y = mediaBox.height / 2f

                contentStream.setTextMatrix(
                    org.apache.pdfbox.util.Matrix.getRotateInstance(Math.toRadians(rotationDegrees.toDouble()), x, y)
                )
                contentStream.showText(watermarkText)
                contentStream.endText()
                contentStream.close()
            }
            doc.save(outputFile)
            doc.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun addHeaderFooter(
        inputFile: File,
        headerText: String,
        footerText: String,
        includePageNumbers: Boolean,
        outputFile: File
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val doc = Loader.loadPDF(inputFile)
            val font = PDType1Font(Standard14Fonts.FontName.HELVETICA)
            val fontSize = 10f
            val totalPages = doc.numberOfPages

            for (p in 0 until totalPages) {
                val page = doc.getPage(p)
                val mediaBox = page.mediaBox ?: page.cropBox
                val contentStream = PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true)
                contentStream.setFont(font, fontSize)
                contentStream.setNonStrokingColor(0.2f, 0.2f, 0.2f)

                // Header
                if (headerText.isNotBlank()) {
                    contentStream.beginText()
                    contentStream.newLineAtOffset(40f, mediaBox.height - 25f)
                    contentStream.showText(headerText)
                    contentStream.endText()
                }

                // Footer
                val resolvedFooter = if (includePageNumbers) {
                    val pageStr = "Página ${p + 1} de $totalPages"
                    if (footerText.isNotBlank()) "$footerText | $pageStr" else pageStr
                } else {
                    footerText
                }

                if (resolvedFooter.isNotBlank()) {
                    contentStream.beginText()
                    contentStream.newLineAtOffset(40f, 20f)
                    contentStream.showText(resolvedFooter)
                    contentStream.endText()
                }

                contentStream.close()
            }
            doc.save(outputFile)
            doc.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun protectPdf(
        inputFile: File,
        userPass: String,
        ownerPass: String,
        canPrint: Boolean = true,
        canCopy: Boolean = true,
        canEdit: Boolean = false,
        outputFile: File
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val doc = Loader.loadPDF(inputFile)
            val ap = AccessPermission()
            ap.setCanPrint(canPrint)
            ap.setCanExtractContent(canCopy)
            ap.setCanModify(canEdit)
            ap.setCanModifyAnnotations(canEdit)
            ap.setCanFillInForm(true)

            val spp = StandardProtectionPolicy(ownerPass, userPass, ap)
            spp.encryptionKeyLength = 256
            spp.permissions = ap
            doc.protect(spp)

            doc.save(outputFile)
            doc.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun flattenPdf(inputFile: File, outputFile: File): Boolean = withContext(Dispatchers.IO) {
        try {
            val doc = Loader.loadPDF(inputFile)
            val acroForm: PDAcroForm? = doc.documentCatalog.acroForm
            acroForm?.flatten()

            for (page in doc.pages) {
                val annotations = page.annotations
                // Flatten annotations
                annotations.clear()
            }

            doc.save(outputFile)
            doc.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun redactDocument(
        inputFile: File,
        redactionRects: Map<Int, List<FloatArray>>,
        outputFile: File
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val doc = Loader.loadPDF(inputFile)
            for ((pageIdx, rectList) in redactionRects) {
                if (pageIdx in 0 until doc.numberOfPages) {
                    val page = doc.getPage(pageIdx)
                    val mediaBox = page.mediaBox ?: page.cropBox
                    val contentStream = PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true)
                    contentStream.setNonStrokingColor(0f, 0f, 0f)

                    for (rect in rectList) {
                        // rect is [x1, y1, x2, y2] normalized
                        val x = rect[0] * mediaBox.width
                        val y = (1.0f - rect[3]) * mediaBox.height
                        val w = (rect[2] - rect[0]) * mediaBox.width
                        val h = (rect[3] - rect[1]) * mediaBox.height
                        contentStream.addRect(x, y, w, h)
                        contentStream.fill()
                    }
                    contentStream.close()
                }
            }
            doc.save(outputFile)
            doc.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
