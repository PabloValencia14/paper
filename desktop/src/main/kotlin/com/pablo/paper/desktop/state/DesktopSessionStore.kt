package com.pablo.paper.desktop.state

import com.google.gson.GsonBuilder
import com.pablo.paper.desktop.model.Annotation
import com.pablo.paper.desktop.model.ViewMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Stores Paper-only data beside the source PDF without modifying the original.
 * A sidecar is intentionally used until exporting annotations into a PDF is a
 * complete, testable feature.
 */
object DesktopSessionStore {
    private const val FORMAT_VERSION = 1
    private val gson = GsonBuilder().setPrettyPrinting().create()

    data class Session(
        val version: Int = FORMAT_VERSION,
        val currentPage: Int = 0,
        val zoomScale: Float = 1f,
        val viewMode: ViewMode = ViewMode.SINGLE_PAGE,
        val rotation: Int = 0,
        val annotations: List<Annotation> = emptyList(),
        val notes: String = ""
    )

    fun sidecarFor(document: File): File = File(
        document.parentFile,
        "${document.nameWithoutExtension}.paper.json"
    )

    suspend fun load(document: File): Result<Session?> = withContext(Dispatchers.IO) {
        runCatching {
            val sidecar = sidecarFor(document)
            if (!sidecar.exists()) null else gson.fromJson(sidecar.readText(Charsets.UTF_8), Session::class.java)
        }
    }

    suspend fun save(tab: TabDocumentState): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val sidecar = sidecarFor(tab.file)
            val snapshot = Session(
                currentPage = tab.currentPage,
                zoomScale = tab.zoomScale,
                viewMode = tab.viewMode,
                rotation = tab.rotation,
                annotations = tab.annotations.toList(),
                notes = tab.documentNotes
            )
            sidecar.writeText(gson.toJson(snapshot), Charsets.UTF_8)
            sidecar
        }
    }
}
