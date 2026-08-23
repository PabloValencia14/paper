package com.pablo.paper

import com.google.common.truth.Truth.assertThat
import com.pablo.paper.domain.model.Annotation
import com.pablo.paper.domain.model.AnnotationType
import com.pablo.paper.domain.model.InkPoint
import com.pablo.paper.domain.model.InkStroke
import com.pablo.paper.ink.UndoRedoManager
import org.junit.Before
import org.junit.Test
import java.util.UUID

class UndoRedoManagerTest {

    private lateinit var undoRedoManager: UndoRedoManager

    @Before
    fun setUp() {
        undoRedoManager = UndoRedoManager()
    }

    private fun createSampleAnnotation(id: String = UUID.randomUUID().toString()): Annotation {
        return Annotation(
            id = id,
            documentId = "doc1",
            pageIndex = 0,
            type = AnnotationType.INK,
            stroke = InkStroke(
                points = listOf(InkPoint(0.1f, 0.1f), InkPoint(0.2f, 0.2f)),
                color = 0xFF000000,
                width = 2.5f
            )
        )
    }

    @Test
    fun addStroke_recordsUndo_canUndoBecomesTrue() {
        assertThat(undoRedoManager.canUndo).isFalse()
        assertThat(undoRedoManager.canRedo).isFalse()

        val ann1 = createSampleAnnotation("ann1")
        undoRedoManager.recordAdd(ann1)

        assertThat(undoRedoManager.canUndo).isTrue()
        assertThat(undoRedoManager.canRedo).isFalse()

        // Perform undo
        val list = listOf(ann1)
        val afterUndo = undoRedoManager.undo(list)

        assertThat(afterUndo).isNotNull()
        assertThat(afterUndo).isEmpty()
        assertThat(undoRedoManager.canUndo).isFalse()
        assertThat(undoRedoManager.canRedo).isTrue()

        // Perform redo
        val afterRedo = undoRedoManager.redo(afterUndo!!)

        assertThat(afterRedo).isNotNull()
        assertThat(afterRedo).containsExactly(ann1)
        assertThat(undoRedoManager.canUndo).isTrue()
        assertThat(undoRedoManager.canRedo).isFalse()
    }

    @Test
    fun deleteStroke_recordsUndo_restoresOnUndo() {
        val ann1 = createSampleAnnotation("ann1")
        val ann2 = createSampleAnnotation("ann2")

        val currentList = listOf(ann1, ann2)

        // Delete ann1
        undoRedoManager.recordDelete(listOf(ann1))
        val afterDelete = listOf(ann2)

        assertThat(undoRedoManager.canUndo).isTrue()

        // Undo the deletion
        val afterUndo = undoRedoManager.undo(afterDelete)

        assertThat(afterUndo).isNotNull()
        assertThat(afterUndo).containsExactly(ann2, ann1)

        // Redo the deletion
        val afterRedo = undoRedoManager.redo(afterUndo!!)

        assertThat(afterRedo).isNotNull()
        assertThat(afterRedo).containsExactly(ann2)
    }

    @Test
    fun newAction_clearsRedoStack() {
        val ann1 = createSampleAnnotation("ann1")
        undoRedoManager.recordAdd(ann1)
        undoRedoManager.undo(listOf(ann1))

        assertThat(undoRedoManager.canRedo).isTrue()

        // Add new annotation -> redo should be invalidated
        val ann2 = createSampleAnnotation("ann2")
        undoRedoManager.recordAdd(ann2)

        assertThat(undoRedoManager.canRedo).isFalse()
        assertThat(undoRedoManager.canUndo).isTrue()
    }
}
