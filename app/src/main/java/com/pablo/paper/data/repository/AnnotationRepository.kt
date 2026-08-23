package com.pablo.paper.data.repository

import com.pablo.paper.domain.model.Annotation
import kotlinx.coroutines.flow.Flow

interface AnnotationRepository {
    fun getAnnotationsForPageFlow(documentId: String, pageIndex: Int): Flow<List<Annotation>>
    suspend fun getAnnotationsForPage(documentId: String, pageIndex: Int): List<Annotation>
    suspend fun getAllAnnotationsForDocument(documentId: String): List<Annotation>
    suspend fun saveAnnotation(annotation: Annotation)
    suspend fun saveAnnotations(annotations: List<Annotation>)
    suspend fun deleteAnnotation(id: String)
    suspend fun deleteAnnotations(ids: List<String>)
    suspend fun clearAnnotationsForPage(documentId: String, pageIndex: Int)
}
