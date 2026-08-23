package com.pablo.paper.data.repository

import com.google.gson.Gson
import com.pablo.paper.data.db.AnnotationDao
import com.pablo.paper.data.db.AnnotationEntity
import com.pablo.paper.domain.model.Annotation
import com.pablo.paper.domain.model.InkStroke
import com.pablo.paper.domain.model.InkTool
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class AnnotationRepositoryImpl(
    private val annotationDao: AnnotationDao
) : AnnotationRepository {

    private val gson = Gson()

    override fun getAnnotationsForPageFlow(documentId: String, pageIndex: Int): Flow<List<Annotation>> {
        return annotationDao.getAnnotationsForPageFlow(documentId, pageIndex).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getAnnotationsForPage(documentId: String, pageIndex: Int): List<Annotation> = withContext(Dispatchers.IO) {
        annotationDao.getAnnotationsForPage(documentId, pageIndex).map { it.toDomain() }
    }

    override suspend fun getAllAnnotationsForDocument(documentId: String): List<Annotation> = withContext(Dispatchers.IO) {
        annotationDao.getAllAnnotationsForDocument(documentId).map { it.toDomain() }
    }

    override suspend fun saveAnnotation(annotation: Annotation) = withContext(Dispatchers.IO) {
        annotationDao.insertAnnotation(annotation.toEntity())
    }

    override suspend fun saveAnnotations(annotations: List<Annotation>) = withContext(Dispatchers.IO) {
        annotationDao.insertAnnotations(annotations.map { it.toEntity() })
    }

    override suspend fun deleteAnnotation(id: String) = withContext(Dispatchers.IO) {
        annotationDao.deleteAnnotation(id)
    }

    override suspend fun deleteAnnotations(ids: List<String>) = withContext(Dispatchers.IO) {
        if (ids.isNotEmpty()) {
            annotationDao.deleteAnnotationsByIds(ids)
        }
    }

    override suspend fun clearAnnotationsForPage(documentId: String, pageIndex: Int) = withContext(Dispatchers.IO) {
        annotationDao.clearAnnotationsForPage(documentId, pageIndex)
    }

    private fun AnnotationEntity.toDomain(): Annotation {
        val points = if (!pointsJson.isNullOrEmpty()) {
            val type = object : com.google.gson.reflect.TypeToken<List<com.pablo.paper.domain.model.InkPoint>>() {}.type
            gson.fromJson<List<com.pablo.paper.domain.model.InkPoint>>(pointsJson, type)
        } else emptyList()

        val isTextOrNote = type == com.pablo.paper.domain.model.AnnotationType.STICKY_NOTE ||
                type == com.pablo.paper.domain.model.AnnotationType.TEXT_NOTE ||
                type == com.pablo.paper.domain.model.AnnotationType.TEXT_BOX ||
                type == com.pablo.paper.domain.model.AnnotationType.STAMP
        val rects = if (!isTextOrNote && !highlightRectsJson.isNullOrEmpty()) {
            val type = object : com.google.gson.reflect.TypeToken<List<FloatArray>>() {}.type
            gson.fromJson<List<FloatArray>>(highlightRectsJson, type)
        } else null

        val textContent = if (isTextOrNote) highlightRectsJson else null

        val stroke = if (points.isNotEmpty()) {
            InkStroke(
                points = points,
                color = color,
                width = strokeWidth,
                opacity = opacity,
                tool = tool
            )
        } else null

        return Annotation(
            id = id,
            documentId = documentId,
            pageIndex = pageIndex,
            type = type,
            stroke = stroke,
            highlightRects = rects,
            textContent = textContent,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    private fun Annotation.toEntity(): AnnotationEntity {
        val pointsJson = stroke?.points?.let { gson.toJson(it) }
        val isTextOrNote = type == com.pablo.paper.domain.model.AnnotationType.STICKY_NOTE ||
                type == com.pablo.paper.domain.model.AnnotationType.TEXT_NOTE ||
                type == com.pablo.paper.domain.model.AnnotationType.TEXT_BOX ||
                type == com.pablo.paper.domain.model.AnnotationType.STAMP
        val rectsJson = if (isTextOrNote) textContent else highlightRects?.let { gson.toJson(it) }

        return AnnotationEntity(
            id = id,
            documentId = documentId,
            pageIndex = pageIndex,
            type = type,
            tool = stroke?.tool ?: InkTool.PEN,
            pointsJson = pointsJson,
            highlightRectsJson = rectsJson,
            color = stroke?.color ?: 0xFF000000,
            strokeWidth = stroke?.width ?: 2.5f,
            opacity = stroke?.opacity ?: 1.0f,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
