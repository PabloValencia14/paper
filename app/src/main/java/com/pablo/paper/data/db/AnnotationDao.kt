package com.pablo.paper.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AnnotationDao {

    @Query("SELECT * FROM annotations WHERE documentId = :documentId AND pageIndex = :pageIndex ORDER BY createdAt ASC")
    fun getAnnotationsForPageFlow(documentId: String, pageIndex: Int): Flow<List<AnnotationEntity>>

    @Query("SELECT * FROM annotations WHERE documentId = :documentId AND pageIndex = :pageIndex ORDER BY createdAt ASC")
    suspend fun getAnnotationsForPage(documentId: String, pageIndex: Int): List<AnnotationEntity>

    @Query("SELECT * FROM annotations WHERE documentId = :documentId ORDER BY pageIndex ASC, createdAt ASC")
    suspend fun getAllAnnotationsForDocument(documentId: String): List<AnnotationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnotation(annotation: AnnotationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnotations(annotations: List<AnnotationEntity>)

    @Update
    suspend fun updateAnnotation(annotation: AnnotationEntity)

    @Query("DELETE FROM annotations WHERE id = :id")
    suspend fun deleteAnnotation(id: String)

    @Query("DELETE FROM annotations WHERE id IN (:ids)")
    suspend fun deleteAnnotationsByIds(ids: List<String>)

    @Query("DELETE FROM annotations WHERE documentId = :documentId AND pageIndex = :pageIndex")
    suspend fun clearAnnotationsForPage(documentId: String, pageIndex: Int)

    @Query("DELETE FROM annotations WHERE documentId = :documentId")
    suspend fun deleteAllAnnotationsForDocument(documentId: String)
}
