package com.pablo.paper.desktop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.WindowState
import com.pablo.paper.desktop.state.DesktopDialog
import com.pablo.paper.desktop.state.NoticeTone
import com.pablo.paper.desktop.state.WorkspaceState
import com.pablo.paper.desktop.ui.dialogs.DesktopDialogHost
import com.pablo.paper.desktop.ui.dock.DesktopLeftDock
import com.pablo.paper.desktop.ui.dock.DesktopRightDock
import com.pablo.paper.desktop.ui.header.DesktopTitleBar
import com.pablo.paper.desktop.ui.ribbon.DesktopRibbon
import com.pablo.paper.desktop.ui.theme.PaperDesktopTheme
import com.pablo.paper.desktop.ui.viewport.DesktopPdfCanvas
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

@Composable
fun FrameWindowScope.DesktopWindowContent(
    state: WorkspaceState,
    windowState: WindowState,
    onCloseWindow: () -> Unit
) {
    PaperDesktopTheme(themeMode = state.themeMode) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f))
                .onPreviewKeyEvent { event -> handleShortcut(event, state) }
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                DesktopTitleBar(state, windowState, onCloseWindow)
                if (state.activeTab?.isLoaded == true) DesktopRibbon(state)
                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    if (state.isLeftDockOpen && state.activeTab != null) DesktopLeftDock(state)
                    Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                        DesktopPdfCanvas(state)
                    }
                    if (state.isRightDockOpen && state.activeTab != null) DesktopRightDock(state)
                }
            }
            DesktopDialogHost(state)
            state.notice?.let { notice ->
                NoticeStrip(
                    text = notice.text,
                    tone = notice.tone,
                    onDismiss = state::dismissNotice,
                    modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
                )
            }
        }
    }
}

private fun handleShortcut(event: androidx.compose.ui.input.key.KeyEvent, state: WorkspaceState): Boolean {
    if (event.type != KeyEventType.KeyDown) return false
    if (event.key == Key.Escape) {
        when {
            state.activeDialog != DesktopDialog.NONE -> state.activeDialog = DesktopDialog.NONE
            state.activeTab?.selectedTextRange != null -> state.activeTab?.clearSelection()
            state.isRightDockOpen -> state.isRightDockOpen = false
            state.isLeftDockOpen -> state.isLeftDockOpen = false
            else -> return false
        }
        return true
    }
    if (!event.isCtrlPressed) return false
    return when (event.key) {
        Key.O -> {
            openPdfPicker(state)
            true
        }
        Key.S -> {
            state.saveActiveSession()
            true
        }
        Key.P -> {
            if (state.activeTab?.isLoaded == true) state.activeDialog = DesktopDialog.PRINT
            true
        }
        Key.F -> {
            if (state.activeTab?.isLoaded == true) state.activeDialog = DesktopDialog.SEARCH_ADVANCED
            true
        }
        Key.K -> {
            state.activeDialog = DesktopDialog.PREFERENCES
            true
        }
        Key.Z -> {
            state.activeTab?.undo()
            true
        }
        Key.Y -> {
            state.activeTab?.redo()
            true
        }
        Key.C -> {
            state.activeTab?.selectedTextRange?.selectedText?.takeIf { it.isNotBlank() }?.let { text ->
                java.awt.Toolkit.getDefaultToolkit().systemClipboard.setContents(java.awt.datatransfer.StringSelection(text), null)
                state.activeTab?.clearSelection()
            } != null
        }
        Key.W -> {
            state.closeTab(state.activeTabIndex)
            true
        }
        else -> false
    }
}

@Composable
private fun NoticeStrip(text: String, tone: NoticeTone, onDismiss: () -> Unit, modifier: Modifier) {
    val accent = when (tone) {
        NoticeTone.INFO -> MaterialTheme.colorScheme.secondary
        NoticeTone.SUCCESS -> Color(0xFF58715B)
        NoticeTone.ERROR -> MaterialTheme.colorScheme.primary
    }
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(7.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, accent.copy(alpha = 0.55f), RoundedCornerShape(7.dp))
            .padding(start = 10.dp, end = 3.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(end = 6.dp))
        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Cerrar aviso", modifier = Modifier.size(14.dp))
        }
    }
}

private fun openPdfPicker(state: WorkspaceState) {
    javax.swing.SwingUtilities.invokeLater {
        val picker = FileDialog(null as Frame?, "Abrir PDF", FileDialog.LOAD)
        picker.setFilenameFilter { _, name -> name.endsWith(".pdf", ignoreCase = true) }
        picker.isVisible = true
        if (picker.file != null) state.openDocument(File(picker.directory, picker.file))
    }
}
