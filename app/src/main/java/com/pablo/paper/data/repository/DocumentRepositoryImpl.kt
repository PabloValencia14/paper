package com.pablo.paper.data.repository

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import com.pablo.paper.data.db.DocumentDao
import com.pablo.paper.data.db.DocumentEntity
import com.pablo.paper.domain.model.Document
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class DocumentRepositoryImpl(
    private val context: Context,
    private val documentDao: DocumentDao
) : DocumentRepository {

    override fun getDocumentsFlow(): Flow<List<Document>> {
        return documentDao.getAllDocumentsFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getDocumentCountFlow(): Flow<Int> {
        return documentDao.getDocumentCountFlow()
    }

    override suspend fun getDocumentById(id: String): Document? = withContext(Dispatchers.IO) {
        documentDao.getDocumentById(id)?.toDomain()
    }

    override suspend fun importDocumentFromUri(uri: Uri): Document? = withContext(Dispatchers.IO) {
        try {
            // Take persistable permission if content scheme
            if (uri.scheme == "content") {
                val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                try {
                    context.contentResolver.takePersistableUriPermission(uri, takeFlags)
                } catch (e: SecurityException) {
                    // Ignored if uri is temporary or already persisted
                }
            }

            // Check if already in database
            val existing = documentDao.getDocumentByUri(uri.toString())
            if (existing != null) {
                // Update last opened timestamp
                val updated = existing.copy(lastOpened = System.currentTimeMillis())
                documentDao.updateDocument(updated)
                return@withContext updated.toDomain()
            }

            // Get display name
            val displayName = queryDisplayName(uri) ?: "Document_${System.currentTimeMillis()}.pdf"

            // Inspect PDF for page count
            val pfd = if (uri.scheme == "file") {
                val path = uri.path ?: return@withContext null
                ParcelFileDescriptor.open(File(path), ParcelFileDescriptor.MODE_READ_ONLY)
            } else {
                context.contentResolver.openFileDescriptor(uri, "r") ?: return@withContext null
            }
            val (pageCount, thumbnailPath) = pfd.use { descriptor ->
                val renderer = PdfRenderer(descriptor)
                val count = renderer.pageCount
                val thumb = renderFirstPageThumbnail(renderer, uri.toString())
                renderer.close()
                Pair(count, thumb)
            }

            val docId = UUID.randomUUID().toString()
            val entity = DocumentEntity(
                id = docId,
                uri = uri.toString(),
                name = displayName,
                pageCount = pageCount.coerceAtLeast(1),
                currentPage = 1,
                lastOpened = System.currentTimeMillis(),
                progress = if (pageCount > 0) 1f / pageCount else 0f,
                thumbnailPath = thumbnailPath
            )

            documentDao.insertDocument(entity)
            entity.toDomain()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun updateReadingProgress(
        documentId: String,
        page: Int,
        pageCount: Int
    ) = withContext(Dispatchers.IO) {
        val safeCount = pageCount.coerceAtLeast(1)
        val safePage = page.coerceIn(1, safeCount)
        val progress = safePage.toFloat() / safeCount.toFloat()
        documentDao.updateDocumentProgress(
            id = documentId,
            page = safePage,
            progress = progress,
            lastOpened = System.currentTimeMillis()
        )
    }

    override suspend fun deleteDocument(documentId: String) = withContext(Dispatchers.IO) {
        documentDao.deleteDocument(documentId)
    }

    override suspend fun syncDocumentsDirectory(): Int = withContext(Dispatchers.IO) {
        var importedCount = 0
        try {
            val searchDirs = listOf(
                android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOCUMENTS),
                File("/sdcard/Documents"),
                File("/storage/emulated/0/Documents")
            ).filter { it.exists() && it.isDirectory }.distinctBy { it.canonicalPath }

            val pdfFiles = mutableListOf<File>()
            for (dir in searchDirs) {
                dir.walkTopDown()
                    .maxDepth(8)
                    .filter { it.isFile && it.extension.equals("pdf", ignoreCase = true) }
                    .forEach { pdfFiles.add(it) }
            }

            for (file in pdfFiles) {
                val fileUri = Uri.fromFile(file).toString()
                val existing = documentDao.getDocumentByUri(fileUri)
                if (existing == null) {
                    try {
                        val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                        val (pageCount, thumbnailPath) = pfd.use { descriptor ->
                            val renderer = PdfRenderer(descriptor)
                            val count = renderer.pageCount
                            val thumb = renderFirstPageThumbnail(renderer, fileUri)
                            renderer.close()
                            Pair(count, thumb)
                        }

                        val docId = UUID.randomUUID().toString()
                        val entity = DocumentEntity(
                            id = docId,
                            uri = fileUri,
                            name = file.name,
                            pageCount = pageCount.coerceAtLeast(1),
                            currentPage = 1,
                            lastOpened = file.lastModified(),
                            progress = if (pageCount > 0) 1f / pageCount else 0f,
                            thumbnailPath = thumbnailPath
                        )
                        documentDao.insertDocument(entity)
                        importedCount++
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        importedCount
    }

    override suspend fun searchLibrary(query: String): List<Document> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        val allDocs = documentDao.getAllDocuments().map { it.toDomain() }
        allDocs.filter { it.name.contains(query, ignoreCase = true) }
    }

    private fun renderFirstPageThumbnail(renderer: PdfRenderer, identifier: String): String? {
        if (renderer.pageCount <= 0) return null
        return try {
            val page = renderer.openPage(0)
            val thumbWidth = 320
            val thumbHeight = (thumbWidth * (page.height.toFloat() / page.width.toFloat())).toInt().coerceAtLeast(200)
            val bitmap = Bitmap.createBitmap(thumbWidth, thumbHeight, Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(android.graphics.Color.WHITE)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()

            val thumbDir = File(context.cacheDir, "thumbnails").apply { mkdirs() }
            val thumbFile = File(thumbDir, "thumb_${identifier.hashCode()}.png")
            FileOutputStream(thumbFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
            }
            bitmap.recycle()
            thumbFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun queryDisplayName(uri: Uri): String? {
        if (uri.scheme != "content") {
            return uri.lastPathSegment
        }
        return try {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index >= 0) cursor.getString(index) else null
                } else null
            }
        } catch (e: Exception) {
            uri.lastPathSegment
        }
    }

    private fun DocumentEntity.toDomain(): Document = Document(
        id = id,
        uri = uri,
        name = name,
        pageCount = pageCount,
        currentPage = currentPage,
        lastOpened = lastOpened,
        progress = progress,
        thumbnailPath = thumbnailPath
    )
}
