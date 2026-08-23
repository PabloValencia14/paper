package com.pablo.paper.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.pablo.paper.domain.model.AnnotationType
import com.pablo.paper.domain.model.InkTool

@Entity(
    tableName = "annotations",
    indices = [
        Index(value = ["documentId", "pageIndex"])
    ]
)
data class AnnotationEntity(
    @PrimaryKey
    val id: String,
    val documentId: String,
    val pageIndex: Int,
    val type: AnnotationType,
    val tool: InkTool = InkTool.PEN,
    val pointsJson: String? = null,
    val highlightRectsJson: String? = null,
    val color: Long = 0xFF000000,
    val strokeWidth: Float = 2.5f,
    val opacity: Float = 1.0f,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
