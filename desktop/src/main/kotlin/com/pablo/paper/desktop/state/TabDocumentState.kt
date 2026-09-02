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
    var pageCount by mutableIntStateOf(0)
    var currentPage by mutableIntStateOf(0)
    var zoomScale by mutableFloatStateOf(1.0f)
    var panOffset by mutableStateOf(Offset.Zero)
    var viewMode by mutableStateOf(ViewMode.SINGLE_PAGE)
    var rotation by mutableIntStateOf(0)
    var isDirty by mutableStateOf(false)

    // Tools & Style
    var activeTool by mutableStateOf(DesktopTool.PAN_HAND)
    var strokeColor by mutableStateOf(Color(0xFF0D9488))
    var strokeWidth by mutableFloatStateOf(3.0f)
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

    // Active in-progress drawing stroke
    var currentDraftPoints = mutableStateListOf<InkPoint>()



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
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            undoStack.addLast(annotations.toList())
            val next = redoStack.removeLast()
            annotations.clear()
            annotations.addAll(next)
        }
    }

    fun addAnnotation(annotation: Annotation) {
        pushUndoState()
        annotations.add(annotation)
    }

    fun removeAnnotation(annotationId: String) {
        pushUndoState()
        annotations.removeAll { it.id == annotationId }
    }

    fun close() {
        engine.close()
    }
}
