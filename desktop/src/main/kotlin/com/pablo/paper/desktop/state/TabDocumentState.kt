package com.pablo.paper.desktop.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.pablo.paper.desktop.model.AcroFormField
import com.pablo.paper.desktop.model.Annotation
import com.pablo.paper.desktop.model.AnnotationType
import com.pablo.paper.desktop.model.DesktopTool
import com.pablo.paper.desktop.model.DocumentMetadata
import com.pablo.paper.desktop.model.InkPoint
import com.pablo.paper.desktop.model.InkStroke
import com.pablo.paper.desktop.model.OutlineNode
import com.pablo.paper.desktop.model.PageInfo
import com.pablo.paper.desktop.model.SearchMatch
import com.pablo.paper.desktop.model.ViewMode
import com.pablo.paper.desktop.pdf.DesktopPdfEngine
import com.pablo.paper.desktop.pdf.PdfBoxEngine
import java.io.File

class TabDocumentState(
    val file: File,
    val engine: DesktopPdfEngine = PdfBoxEngine()
) {
    val id: String = file.absolutePath
    val title: String = file.name

    var isLoaded by mutableStateOf(false)
    var isLoading by mutableStateOf(true)
    var loadError by mutableStateOf<String?>(null)
    var pageCount by mutableIntStateOf(0)
    var currentPage by mutableIntStateOf(0)
    var zoomScale by mutableFloatStateOf(1.0f)
    var panOffset by mutableStateOf(Offset.Zero)
    var viewMode by mutableStateOf(ViewMode.SINGLE_PAGE)
    var rotation by mutableIntStateOf(0)
    var isDirty by mutableStateOf(false)

    // Tools & Style
    var activeTool by mutableStateOf(DesktopTool.PAN_HAND)
    var strokeColor by mutableStateOf(Color(0xFF252A29))
    var strokeWidth by mutableFloatStateOf(4.0f)
    var strokeAlpha by mutableFloatStateOf(1.0f)

    // Content Lists
    val annotations = mutableStateListOf<Annotation>()
    val outlineNodes = mutableStateListOf<OutlineNode>()
    val searchMatches = mutableStateListOf<SearchMatch>()
    val acroForms = mutableStateListOf<AcroFormField>()
    var metadata by mutableStateOf(DocumentMetadata())

    // Undo / Redo
    private val undoStack = ArrayDeque<List<Annotation>>()
    private val redoStack = ArrayDeque<List<Annotation>>()
    val canUndo: Boolean get() = undoStack.isNotEmpty()
    val canRedo: Boolean get() = redoStack.isNotEmpty()

    // Text Selection
    var selectedTextRange by mutableStateOf<com.pablo.paper.desktop.model.TextSelectionRange?>(null)
    private val textLayoutCache = mutableMapOf<Int, com.pablo.paper.desktop.model.PageTextLayout>()

    fun getPageTextLayout(pageIndex: Int): com.pablo.paper.desktop.model.PageTextLayout {
        return textLayoutCache.getOrPut(pageIndex) {
            engine.extractPageTextLayout(pageIndex)
        }
    }

    fun clearSelection() {
        selectedTextRange = null
    }

    // Document Notes (Markdown)
    var documentNotes by mutableStateOf("")

    // Active in-progress drawing stroke. Points are normalized to the PDF page,
    // so they remain in the right place when zoom, window size, or DPI changes.
    var currentDraftPoints = mutableStateListOf<InkPoint>()
    var draftPageIndex by mutableIntStateOf(-1)



    fun pushUndoState() {
        undoStack.addLast(annotations.toList())
        if (undoStack.size > 50) undoStack.removeFirst()
        redoStack.clear()
        isDirty = true
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            redoStack.addLast(annotations.toList())
            val previous = undoStack.removeLast()
            annotations.clear()
            annotations.addAll(previous)
            isDirty = true
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            undoStack.addLast(annotations.toList())
            val next = redoStack.removeLast()
            annotations.clear()
            annotations.addAll(next)
            isDirty = true
        }
    }

    fun addAnnotation(annotation: Annotation) {
        pushUndoState()
        annotations.add(annotation)
    }

    fun removeAnnotation(annotationId: String) {
        if (annotations.any { it.id == annotationId }) {
            pushUndoState()
            annotations.removeAll { it.id == annotationId }
        }
    }

    fun applySession(session: DesktopSessionStore.Session) {
        currentPage = session.currentPage.coerceIn(0, (pageCount - 1).coerceAtLeast(0))
        zoomScale = session.zoomScale.coerceIn(0.35f, 4f)
        viewMode = session.viewMode
        rotation = ((session.rotation % 360) + 360) % 360
        annotations.clear()
        annotations.addAll(session.annotations)
        documentNotes = session.notes
        isDirty = false
    }

    fun discardDraft() {
        currentDraftPoints.clear()
        draftPageIndex = -1
    }

    fun close() {
        engine.close()
    }
}
