package com.pablo.paper.desktop.state

import androidx.compose.ui.graphics.ImageBitmap
import com.pablo.paper.desktop.model.AcroFormField
import com.pablo.paper.desktop.model.Annotation
import com.pablo.paper.desktop.model.AnnotationType
import com.pablo.paper.desktop.model.DocumentMetadata
import com.pablo.paper.desktop.model.OutlineNode
import com.pablo.paper.desktop.model.PageInfo
import com.pablo.paper.desktop.model.PageTextLayout
import com.pablo.paper.desktop.model.SearchMatch
import com.pablo.paper.desktop.pdf.DesktopPdfEngine
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.io.File
import java.nio.file.Files

class TabDocumentStateTest : StringSpec({
    "undo and redo preserve annotation history and dirty state" {
        val tab = TabDocumentState(File("notes.pdf"), FakePdfEngine())
        val annotation = Annotation(id = "ink-1", pageIndex = 0, type = AnnotationType.INK)

        tab.addAnnotation(annotation)
        tab.annotations shouldBe listOf(annotation)
        tab.canUndo shouldBe true
        tab.isDirty shouldBe true

        tab.undo()
        tab.annotations shouldBe emptyList()
        tab.canRedo shouldBe true

        tab.redo()
        tab.annotations shouldBe listOf(annotation)
    }

    "session sidecar restores reading position, notes and annotations" {
        val directory = Files.createTempDirectory("paper-desktop-session").toFile()
        try {
            val document = File(directory, "lecture.pdf").apply { writeBytes(byteArrayOf()) }
            val tab = TabDocumentState(document, FakePdfEngine()).apply {
                pageCount = 12
                currentPage = 5
                zoomScale = 1.35f
                documentNotes = "# Idea central"
                addAnnotation(
                    Annotation(
                        id = "highlight-1",
                        pageIndex = 5,
                        type = AnnotationType.HIGHLIGHT,
                        rects = listOf(floatArrayOf(0.1f, 0.2f, 0.3f, 0.24f))
                    )
                )
            }

            DesktopSessionStore.save(tab).getOrThrow()
            val restored = DesktopSessionStore.load(document).getOrThrow()!!

            restored.currentPage shouldBe 5
            restored.zoomScale shouldBe 1.35f
            restored.notes shouldBe "# Idea central"
            restored.annotations.single().pageIndex shouldBe 5
        } finally {
            directory.deleteRecursively()
        }
    }
})

private class FakePdfEngine : DesktopPdfEngine {
    override suspend fun open(file: File, password: String?) = true
    override fun getPageCount() = 0
    override fun getPageInfo(pageIndex: Int): PageInfo? = null
    override suspend fun renderPage(pageIndex: Int, targetWidth: Int, targetHeight: Int, rotation: Int): ImageBitmap? = null
    override suspend fun renderThumbnail(pageIndex: Int, size: Int): ImageBitmap? = null
    override fun extractText(pageIndex: Int) = ""
    override fun extractPageTextLayout(pageIndex: Int) = PageTextLayout(pageIndex)
    override fun extractAllText() = ""
    override suspend fun search(query: String, matchCase: Boolean): List<SearchMatch> = emptyList()
    override fun extractOutline(): List<OutlineNode> = emptyList()
    override fun extractMetadata() = DocumentMetadata()
    override fun extractAcroForms(): List<AcroFormField> = emptyList()
    override fun prefetch(currentPage: Int, targetWidth: Int, targetHeight: Int) = Unit
    override fun close() = Unit
}
