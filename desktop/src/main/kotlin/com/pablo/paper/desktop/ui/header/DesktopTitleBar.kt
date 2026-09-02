package com.pablo.paper.desktop.ui.header

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Minimize
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import com.pablo.paper.desktop.state.DesktopDialog
import com.pablo.paper.desktop.state.TabDocumentState
import com.pablo.paper.desktop.state.WorkspaceState
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

@Composable
fun FrameWindowScope.DesktopTitleBar(
    state: WorkspaceState,
    windowState: WindowState,
    onCloseWindow: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showFileMenu by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    val tabsScroll = rememberScrollState()

    WindowDraggableArea(
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(start = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(5.dp))
                        .clickable { showFileMenu = true }
                        .padding(horizontal = 6.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = "Menú de archivo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Paper",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                FileMenu(
                    expanded = showFileMenu,
                    onDismiss = { showFileMenu = false },
                    state = state
                )
            }

            Spacer(Modifier.width(12.dp))

            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .horizontalScroll(tabsScroll),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                state.tabs.forEachIndexed { index, tab ->
                    DocumentTab(
                        tab = tab,
                        selected = index == state.activeTabIndex,
                        onSelect = { state.activeTabIndex = index },
                        onClose = { state.closeTab(index) }
                    )
                }
            }

            IconButton(
                onClick = { state.activeDialog = DesktopDialog.SEARCH_ADVANCED },
                enabled = state.activeTab?.isLoaded == true,
                modifier = Modifier.size(38.dp)
            ) {
                Icon(Icons.Default.Search, contentDescription = "Buscar en el documento", modifier = Modifier.size(18.dp))
            }
            Box {
                IconButton(onClick = { showMoreMenu = true }, modifier = Modifier.size(38.dp)) {
                    Icon(Icons.Default.MoreHoriz, contentDescription = "Más opciones", modifier = Modifier.size(20.dp))
                }
                DropdownMenu(expanded = showMoreMenu, onDismissRequest = { showMoreMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("Preferencias") },
                        leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(17.dp)) },
                        onClick = { showMoreMenu = false; state.activeDialog = DesktopDialog.PREFERENCES }
                    )
                    DropdownMenuItem(
                        text = { Text("Acerca de Paper") },
                        leadingIcon = { Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(17.dp)) },
                        onClick = { showMoreMenu = false; state.activeDialog = DesktopDialog.ABOUT }
                    )
                }
            }
            WindowControl(
                icon = Icons.Default.Minimize,
                description = "Minimizar",
                onClick = { windowState.isMinimized = true }
            )
            WindowControl(
                icon = Icons.Default.CropSquare,
                description = if (windowState.placement == WindowPlacement.Maximized) "Restaurar" else "Maximizar",
                onClick = {
                    windowState.placement = if (windowState.placement == WindowPlacement.Maximized) {
                        WindowPlacement.Floating
                    } else {
                        WindowPlacement.Maximized
                    }
                }
            )
            IconButton(
                onClick = onCloseWindow,
                modifier = Modifier.size(42.dp).fillMaxHeight()
            ) {
                Icon(Icons.Default.Close, contentDescription = "Cerrar Paper", modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun FileMenu(expanded: Boolean, onDismiss: () -> Unit, state: WorkspaceState) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss, modifier = Modifier.width(236.dp)) {
        DropdownMenuItem(
            text = { Text("Abrir PDF") },
            leadingIcon = { Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(17.dp)) },
            onClick = { onDismiss(); openPdfPicker(state) }
        )
        DropdownMenuItem(
            text = { Text("Guardar sesión") },
            leadingIcon = { Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(17.dp)) },
            enabled = state.activeTab != null,
            onClick = { onDismiss(); state.saveActiveSession() }
        )
        DropdownMenuItem(
            text = { Text("Imprimir") },
            leadingIcon = { Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(17.dp)) },
            enabled = state.activeTab?.isLoaded == true,
            onClick = { onDismiss(); state.activeDialog = DesktopDialog.PRINT }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
        DropdownMenuItem(
            text = { Text("Cerrar documento") },
            leadingIcon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(17.dp)) },
            enabled = state.activeTab != null,
            onClick = { onDismiss(); state.closeTab(state.activeTabIndex) }
        )
    }
}

@Composable
private fun DocumentTab(tab: TabDocumentState, selected: Boolean, onSelect: () -> Unit, onClose: () -> Unit) {
    Row(
        modifier = Modifier
            .height(34.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(if (selected) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent)
            .clickable(onClick = onSelect)
            .padding(start = 9.dp, end = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (tab.isDirty) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
            Spacer(Modifier.width(6.dp))
        }
        Text(
            text = tab.title,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(150.dp),
            color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
        IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Cerrar ${tab.title}", modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
private fun WindowControl(icon: androidx.compose.ui.graphics.vector.ImageVector, description: String, onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.size(40.dp).fillMaxHeight()) {
        Icon(icon, contentDescription = description, modifier = Modifier.size(16.dp))
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
