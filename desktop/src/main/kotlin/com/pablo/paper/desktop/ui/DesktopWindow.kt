package com.pablo.paper.desktop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.WindowState
import com.pablo.paper.desktop.state.DesktopDialog
import com.pablo.paper.desktop.state.WorkspaceState
import com.pablo.paper.desktop.ui.dialogs.DesktopDialogHost
import com.pablo.paper.desktop.ui.dock.DesktopLeftDock
import com.pablo.paper.desktop.ui.dock.DesktopRightDock
import com.pablo.paper.desktop.ui.header.DesktopTitleBar
import com.pablo.paper.desktop.ui.ribbon.DesktopRibbon
import com.pablo.paper.desktop.ui.status.DesktopStatusBar
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
                .border(width = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                .onPreviewKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown && event.isCtrlPressed) {
                        when (event.key) {
                            Key.O -> {
                                javax.swing.SwingUtilities.invokeLater {
                                    val fd = FileDialog(null as Frame?, "Abrir PDF", FileDialog.LOAD)
                                    fd.setFilenameFilter { _, name -> name.lowercase().endsWith(".pdf") }
                                    fd.isVisible = true
                                    if (fd.file != null) state.openDocument(File(fd.directory, fd.file))
                                }
                                true
                            }
                            Key.S -> {
                                state.activeTab?.isDirty = false
                                true
                            }
                            Key.P -> {
                                state.activeDialog = DesktopDialog.PRINT
                                true
                            }
                            Key.F -> {
                                state.activeDialog = DesktopDialog.SEARCH_ADVANCED
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
                                val sel = state.activeTab?.selectedTextRange
                                if (sel != null && sel.selectedText.isNotBlank()) {
                                    val stringSel = java.awt.datatransfer.StringSelection(sel.selectedText)
                                    java.awt.Toolkit.getDefaultToolkit().systemClipboard.setContents(stringSel, stringSel)
                                    true
                                } else false
                            }
                            Key.W -> {
                                state.closeTab(state.activeTabIndex)
                                true
                            }
                            else -> false

                        }
                    } else false
                }
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 1. Unified Custom Windows Title Bar (Tabs + Menu + Window Controls)
                DesktopTitleBar(
                    state = state,
                    windowState = windowState,
                    onCloseWindow = onCloseWindow
                )

                // 2. Streamlined Toolbar (Only shown when document is loaded or for fast actions)
                if (state.activeTab != null && state.activeTab!!.isLoaded) {
                    DesktopRibbon(state = state)
                }

                // 3. Central Content Area
                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    // Left Dock (Thumbnails / Bookmarks)
                    if (state.activeTab != null && state.isLeftDockOpen) {
                        DesktopLeftDock(state = state)
                    }

                    // Main Canvas Viewport or Welcome Screen
                    Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                        DesktopPdfCanvas(workspaceState = state)
                    }

                    // Right Dock (AI Assistant / Notes)
                    if (state.activeTab != null && state.isRightDockOpen) {
                        DesktopRightDock(state = state)
                    }
                }
            }

            // Dialogs Host

            DesktopDialogHost(state = state)
        }
    }
}
