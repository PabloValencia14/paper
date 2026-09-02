package com.pablo.paper.desktop.ui.viewport

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import com.pablo.paper.desktop.state.TabDocumentState
import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.scene.Scene
import javafx.scene.web.WebView
import java.io.File
import java.util.Base64
import javax.swing.SwingUtilities

@Composable
fun PdfJsViewer(
    tab: TabDocumentState,
    modifier: Modifier = Modifier,
    viewMode: com.pablo.paper.desktop.model.ViewMode
) {
    val file = tab.file
    
    val jfxPanel = remember { JFXPanel() }

    LaunchedEffect(file) {
        if (file == null || !file.exists()) return@LaunchedEffect
        val bytes = file.readBytes()
        val base64 = Base64.getEncoder().encodeToString(bytes)

        Platform.runLater {
            val webView = WebView()
            webView.isContextMenuEnabled = false
            
            val url = javaClass.getResource("/pdfjs/viewer.html")?.toExternalForm() ?: ""
            
            class JavaBackend {
                @Suppress("unused")
                fun getPdfBase64(): String = base64
                
                @Suppress("unused")
                fun getViewMode(): String = viewMode.name
                
                @Suppress("unused")
                fun getZoom(): Double = tab.zoomScale.toDouble()
            }
            
            webView.engine.load(url)
            
            webView.engine.documentProperty().addListener { _, _, doc ->
                if (doc != null) {
                    val window = webView.engine.executeScript("window") as netscape.javascript.JSObject
                    window.setMember("javaBackend", JavaBackend())
                    webView.engine.executeScript("loadPdfFromJava();")
                }
            }
            val scene = Scene(webView)
            jfxPanel.scene = scene
        }
    }

    // React to zoom changes
    LaunchedEffect(tab.zoomScale, viewMode) {
        Platform.runLater {
            val webView = jfxPanel.scene?.root as? WebView
            webView?.engine?.executeScript("updateScale(${tab.zoomScale});")
        }
    }

    SwingPanel(
        background = androidx.compose.ui.graphics.Color.White,
        modifier = modifier.fillMaxSize(),
        factory = {
            jfxPanel
        }
    )
}
