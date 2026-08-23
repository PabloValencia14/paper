package com.pablo.paper.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey
    val id: String,
    val uri: String,
    val name: String,
    val pageCount: Int,
    val currentPage: Int = 1,
    val lastOpened: Long = System.currentTimeMillis(),
    val progress: Float = 0f,
    val thumbnailPath: String? = null
)
