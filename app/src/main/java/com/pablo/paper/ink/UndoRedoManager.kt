package com.pablo.paper.ink

import com.pablo.paper.domain.model.Annotation

sealed interface InkHistoryAction {
    data class Added(val annotation: Annotation) : InkHistoryAction
    data class Deleted(val annotations: List<Annotation>) : InkHistoryAction
    data class Batch(val added: List<Annotation>, val deleted: List<Annotation>) : InkHistoryAction
}

class UndoRedoManager {
    private val undoStack = ArrayDeque<InkHistoryAction>()
    private val redoStack = ArrayDeque<InkHistoryAction>()

    val canUndo: Boolean
        get() = undoStack.isNotEmpty()

    val canRedo: Boolean
        get() = redoStack.isNotEmpty()

    fun recordAdd(annotation: Annotation) {
        undoStack.addLast(InkHistoryAction.Added(annotation))
        redoStack.clear()
    }

    fun recordDelete(annotations: List<Annotation>) {
        if (annotations.isNotEmpty()) {
            undoStack.addLast(InkHistoryAction.Deleted(annotations))
            redoStack.clear()
        }
    }

    fun undo(currentAnnotations: List<Annotation>): List<Annotation>? {
        if (undoStack.isEmpty()) return null
        val action = undoStack.removeLast()
        redoStack.addLast(action)

        val result = currentAnnotations.toMutableList()
        when (action) {
            is InkHistoryAction.Added -> {
                result.removeAll { it.id == action.annotation.id }
            }
            is InkHistoryAction.Deleted -> {
                result.addAll(action.annotations)
            }
            is InkHistoryAction.Batch -> {
                val addedIds = action.added.map { it.id }.toSet()
                result.removeAll { it.id in addedIds }
                result.addAll(action.deleted)
            }
        }
        return result
    }

    fun redo(currentAnnotations: List<Annotation>): List<Annotation>? {
        if (redoStack.isEmpty()) return null
        val action = redoStack.removeLast()
        undoStack.addLast(action)

        val result = currentAnnotations.toMutableList()
        when (action) {
            is InkHistoryAction.Added -> {
                result.add(action.annotation)
            }
            is InkHistoryAction.Deleted -> {
                val deletedIds = action.annotations.map { it.id }.toSet()
                result.removeAll { it.id in deletedIds }
            }
            is InkHistoryAction.Batch -> {
                val deletedIds = action.deleted.map { it.id }.toSet()
                result.removeAll { it.id in deletedIds }
                result.addAll(action.added)
            }
        }
        return result
    }

    fun clear() {
        undoStack.clear()
        redoStack.clear()
    }
}
