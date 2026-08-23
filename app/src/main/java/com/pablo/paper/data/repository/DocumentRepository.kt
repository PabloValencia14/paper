package com.pablo.paper.data.repository

import android.net.Uri
import com.pablo.paper.domain.model.Document
import kotlinx.coroutines.flow.Flow

interface DocumentRepository {
    fun getDocumentsFlow(): Flow<List<Document>>
    fun getDocumentCountFlow(): Flow<Int>
    suspend fun getDocumentById(id: String): Document?
    suspend fun importDocumentFromUri(uri: Uri): Document?
    suspend fun updateReadingProgress(documentId: String, page: Int, pageCount: Int)
    suspend fun deleteDocument(documentId: String)
    suspend fun generateThumbnailIfNeeded(document: Document): String?
}
