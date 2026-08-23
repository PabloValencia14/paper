package com.pablo.paper.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {

    @Query("SELECT * FROM documents ORDER BY lastOpened DESC")
    fun getAllDocumentsFlow(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents ORDER BY lastOpened DESC")
    suspend fun getAllDocuments(): List<DocumentEntity>

    @Query("SELECT * FROM documents WHERE id = :id LIMIT 1")
    suspend fun getDocumentById(id: String): DocumentEntity?

    @Query("SELECT * FROM documents WHERE uri = :uri LIMIT 1")
    suspend fun getDocumentByUri(uri: String): DocumentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: DocumentEntity)

    @Update
    suspend fun updateDocument(document: DocumentEntity)

    @Query("UPDATE documents SET currentPage = :page, progress = :progress, lastOpened = :lastOpened WHERE id = :id")
    suspend fun updateDocumentProgress(id: String, page: Int, progress: Float, lastOpened: Long = System.currentTimeMillis())

    @Query("UPDATE documents SET thumbnailPath = :path WHERE id = :id")
    suspend fun updateThumbnailPath(id: String, path: String)

    @Query("DELETE FROM documents WHERE id = :id")
    suspend fun deleteDocument(id: String)

    @Query("SELECT COUNT(*) FROM documents")
    fun getDocumentCountFlow(): Flow<Int>
}
