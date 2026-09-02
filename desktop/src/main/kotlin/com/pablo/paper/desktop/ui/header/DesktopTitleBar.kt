package com.pablo.paper.desktop.ui.header

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.FormatShapes
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Minimize
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Splitscreen
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material.icons.filled.Visibility
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import com.pablo.paper.desktop.model.DesktopTool
import com.pablo.paper.desktop.model.ThemeMode
import com.pablo.paper.desktop.model.ViewMode
import com.pablo.paper.desktop.state.DesktopDialog
import com.pablo.paper.desktop.state.RightDockTab
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
    var showMainMenu by remember { mutableStateOf(false) }
    var activeSubmenu by remember { mutableStateOf<String?>(null) }
    val tabScrollState = rememberScrollState()

    WindowDraggableArea(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(MaterialTheme.colorScheme.surface)
            .border(width = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. App Icon & Main Menu Trigger
            Box {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { showMainMenu = !showMainMenu }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = "PAPER",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menú Principal",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                }

                // Main Dropdown Menu
                DropdownMenu(
                    expanded = showMainMenu,
                    onDismissRequest = { showMainMenu = false },
                    modifier = Modifier
                        .width(260.dp)
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    DropdownMenuItem(
                        text = { Row { Text("Abrir PDF..."); Spacer(Modifier.weight(1f)); Text("Ctrl+O", fontSize = 11.sp, color = Color.Gray) } },
                        leadingIcon = { Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        onClick = {
                            showMainMenu = false
                            val fd = FileDialog(null as Frame?, "Abrir PDF", FileDialog.LOAD)
                            fd.setFilenameFilter { _, name -> name.lowercase().endsWith(".pdf") }
                            fd.isVisible = true
                            if (fd.file != null) state.openDocument(File(fd.directory, fd.file))
                        }
                    )
                    DropdownMenuItem(
                        text = { Row { Text("Guardar"); Spacer(Modifier.weight(1f)); Text("Ctrl+S", fontSize = 11.sp, color = Color.Gray) } },
                        leadingIcon = { Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        enabled = state.activeTab != null,
                        onClick = { showMainMenu = false; state.activeTab?.isDirty = false }
                    )
                    DropdownMenuItem(
                        text = { Row { Text("Imprimir en Windows..."); Spacer(Modifier.weight(1f)); Text("Ctrl+P", fontSize = 11.sp, color = Color.Gray) } },
                        leadingIcon = { Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        enabled = state.activeTab != null,
                        onClick = { showMainMenu = false; state.activeDialog = DesktopDialog.PRINT }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    DropdownMenuItem(
                        text = { Row { Text("Organizar / Dividir Páginas..."); } },
                        leadingIcon = { Icon(Icons.Default.ViewCarousel, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        enabled = state.activeTab != null,
                        onClick = { showMainMenu = false; state.activeDialog = DesktopDialog.ORGANIZE_PAGES }
                    )
                    DropdownMenuItem(
                        text = { Row { Text("Proteger con Contraseña..."); } },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        enabled = state.activeTab != null,
                        onClick = { showMainMenu = false; state.activeDialog = DesktopDialog.PASSWORD_SECURITY }
                    )
                    DropdownMenuItem(
                        text = { Row { Text("Firmar con Certificado Digital..."); } },
                        leadingIcon = { Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        onClick = { showMainMenu = false; state.activeDialog = DesktopDialog.CERTIFICATE_SIGN }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    DropdownMenuItem(
                        text = { Row { Text("Preferencias..."); Spacer(Modifier.weight(1f)); Text("Ctrl+K", fontSize = 11.sp, color = Color.Gray) } },
                        leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        onClick = { showMainMenu = false; state.activeDialog = DesktopDialog.PREFERENCES }
                    )
                    DropdownMenuItem(
                        text = { Text("Acerca de Paper Desktop...") },
                        leadingIcon = { Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        onClick = { showMainMenu = false; state.activeDialog = DesktopDialog.ABOUT }
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            // 2. Document Tabs Strip in Header (like Chrome / VS Code / Acrobat Pro)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(tabScrollState),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                state.tabs.forEachIndexed { index, tab ->
                    val isActive = index == state.activeTabIndex
                    val interactionSource = remember { MutableInteractionSource() }
                    val isHovered by interactionSource.collectIsHoveredAsState()

                    val tabBg = when {
                        isActive -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                        isHovered -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        else -> Color.Transparent
                    }

                    Row(
                        modifier = Modifier
                            .height(30.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(tabBg)
                            .border(
                                width = if (isActive) 1.dp else 0.dp,
                                color = if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else Color.Transparent,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .hoverable(interactionSource = interactionSource)
                            .clickable(interactionSource = interactionSource, indication = null) { state.activeTabIndex = index }
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = tab.title,
                            fontSize = 12.sp,
                            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.width(140.dp)
                        )
                        if (tab.isDirty) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFFEA580C)))
                        }
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .clickable { state.closeTab(index) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar pestaña",
                                tint = if (isActive) MaterialTheme.colorScheme.onSurface else Color.Gray,
                                modifier = Modifier.size(11.dp)
                            )
                        }
                    }
                }

                // Add Tab Button
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .clickable {
                            val fd = FileDialog(null as Frame?, "Abrir PDF", FileDialog.LOAD)
                            fd.setFilenameFilter { _, name -> name.lowercase().endsWith(".pdf") }
                            fd.isVisible = true
                            if (fd.file != null) state.openDocument(File(fd.directory, fd.file))
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Nueva pestaña", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                }
            }

            Spacer(Modifier.width(8.dp))

            // 3. Quick Utilities: Theme Toggle, Settings
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                IconButton(
                    onClick = {
                        state.themeMode = when (state.themeMode) {
                            ThemeMode.SYSTEM -> ThemeMode.DARK
                            ThemeMode.DARK -> ThemeMode.LIGHT
                            ThemeMode.LIGHT -> ThemeMode.SEPIA
                            ThemeMode.SEPIA -> ThemeMode.SYSTEM
                            else -> ThemeMode.SYSTEM
                        }
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = when (state.themeMode) {
                            ThemeMode.DARK -> Icons.Default.DarkMode
                            ThemeMode.LIGHT -> Icons.Default.LightMode
                            ThemeMode.SEPIA -> Icons.Default.FormatColorFill
                            else -> Icons.Default.AutoAwesome
                        },
                        contentDescription = "Cambiar tema",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )
                }

                IconButton(
                    onClick = { state.activeDialog = DesktopDialog.PREFERENCES },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Preferencias",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            Spacer(Modifier.width(4.dp))

            // 4. Custom Windows 11 Window Controls (Minimize, Maximize, Close)
            Row(
                modifier = Modifier.fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Minimize Button
                WindowControlButton(
                    icon = Icons.Default.Remove,
                    tooltip = "Minimizar",
                    onClick = { windowState.isMinimized = true }
                )

                // Maximize / Restore Button
                WindowControlButton(
                    icon = if (windowState.placement == WindowPlacement.Maximized) Icons.Default.Splitscreen else Icons.Default.CropSquare,
                    tooltip = if (windowState.placement == WindowPlacement.Maximized) "Restaurar" else "Maximizar",
                    onClick = {
                        windowState.placement = if (windowState.placement == WindowPlacement.Maximized) {
                            WindowPlacement.Floating
                        } else {
                            WindowPlacement.Maximized
                        }
                    }
                )

                // Close Button (with red hover)
                WindowCloseButton(onClick = onCloseWindow)
            }
        }
    }
}

@Composable
fun WindowControlButton(
    icon: ImageVector,
    tooltip: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Box(
        modifier = Modifier
            .width(36.dp)
            .fillMaxHeight()
            .background(if (isHovered) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent)
            .hoverable(interactionSource = interactionSource)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = tooltip,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(13.dp)
        )
    }
}

@Composable
fun WindowCloseButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Box(
        modifier = Modifier
            .width(40.dp)
            .fillMaxHeight()
            .background(if (isHovered) Color(0xFFE81123) else Color.Transparent)
            .hoverable(interactionSource = interactionSource)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Cerrar",
            tint = if (isHovered) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp)
        )
    }
}
