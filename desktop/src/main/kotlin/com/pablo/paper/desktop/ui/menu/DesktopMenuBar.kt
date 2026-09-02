package com.pablo.paper.desktop.ui.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.automirrored.filled.Rule
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.FormatShapes
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Print
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
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun DesktopMenuBar(
    state: WorkspaceState,
    modifier: Modifier = Modifier
) {
    var activeMenu by remember { mutableStateOf<String?>(null) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(32.dp)
            .background(MaterialTheme.colorScheme.surface)
            .border(width = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Branding Badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = "PAPER",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(Modifier.width(8.dp))

        val menuItems = listOf("Archivo", "Edición", "Ver", "Herramientas", "Páginas", "Firmar", "IA & Estudio", "Ventana", "Ayuda")

        menuItems.forEach { name ->
            val isExpanded = activeMenu == name
            val interactionSource = remember { MutableInteractionSource() }
            val isHovered by interactionSource.collectIsHoveredAsState()

            val bg = when {
                isExpanded -> MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                isHovered -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                else -> Color.Transparent
            }

            Box {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(5.dp))
                        .background(bg)
                        .clickable(interactionSource = interactionSource, indication = null) {
                            activeMenu = if (activeMenu == name) null else name
                        }
                        .hoverable(interactionSource = interactionSource)
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name,
                        fontSize = 12.sp,
                        fontWeight = if (isExpanded) FontWeight.Bold else FontWeight.Normal,
                        color = if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }

                DropdownMenu(
                    expanded = isExpanded,
                    onDismissRequest = { activeMenu = null },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                ) {
                    when (name) {
                        "Archivo" -> {
                            MenuActionItem(
                                icon = Icons.Default.FolderOpen,
                                label = "Abrir PDF...",
                                shortcut = "Ctrl+O",
                                onClick = {
                                    activeMenu = null
                                    val fd = FileDialog(null as Frame?, "Abrir Documento PDF", FileDialog.LOAD)
                                    fd.setFilenameFilter { _, f -> f.lowercase().endsWith(".pdf") }
                                    fd.isVisible = true
                                    if (fd.file != null) state.openDocument(File(fd.directory, fd.file))
                                }
                            )
                            MenuActionItem(
                                icon = Icons.Default.Save,
                                label = "Guardar",
                                shortcut = "Ctrl+S",
                                enabled = state.activeTab != null,
                                onClick = { activeMenu = null; state.activeTab?.isDirty = false }
                            )
                            MenuActionItem(
                                icon = Icons.Default.Print,
                                label = "Imprimir...",
                                shortcut = "Ctrl+P",
                                enabled = state.activeTab != null,
                                onClick = { activeMenu = null; state.activeDialog = DesktopDialog.PRINT }
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            MenuActionItem(
                                icon = Icons.Default.Close,
                                label = "Cerrar pestaña actual",
                                shortcut = "Ctrl+W",
                                enabled = state.activeTab != null,
                                onClick = { activeMenu = null; state.closeTab(state.activeTabIndex) }
                            )
                            MenuActionItem(
                                icon = Icons.Default.Info,
                                label = "Propiedades del Documento...",
                                enabled = state.activeTab != null,
                                onClick = {
                                    activeMenu = null
                                    state.isRightDockOpen = true
                                    state.rightDockTab = RightDockTab.METADATA
                                }
                            )
                        }
                        "Edición" -> {
                            MenuActionItem(
                                icon = Icons.AutoMirrored.Filled.Undo,
                                label = "Deshacer",
                                shortcut = "Ctrl+Z",
                                enabled = state.activeTab != null,
                                onClick = { activeMenu = null; state.activeTab?.undo() }
                            )
                            MenuActionItem(
                                icon = Icons.AutoMirrored.Filled.Redo,
                                label = "Rehacer",
                                shortcut = "Ctrl+Y",
                                enabled = state.activeTab != null,
                                onClick = { activeMenu = null; state.activeTab?.redo() }
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            MenuActionItem(
                                icon = Icons.Default.Search,
                                label = "Búsqueda avanzada...",
                                shortcut = "Ctrl+Shift+F",
                                onClick = { activeMenu = null; state.activeDialog = DesktopDialog.SEARCH_ADVANCED }
                            )
                            MenuActionItem(
                                icon = Icons.Default.Settings,
                                label = "Preferencias...",
                                shortcut = "Ctrl+K",
                                onClick = { activeMenu = null; state.activeDialog = DesktopDialog.PREFERENCES }
                            )
                        }
                        "Ver" -> {
                            MenuActionItem(
                                icon = Icons.Default.ViewAgenda,
                                label = "Página Simple",
                                isSelected = state.activeTab?.viewMode == ViewMode.SINGLE_PAGE,
                                onClick = { activeMenu = null; state.activeTab?.viewMode = ViewMode.SINGLE_PAGE }
                            )
                            MenuActionItem(
                                icon = Icons.Default.Splitscreen,
                                label = "Desplazamiento Continuo",
                                isSelected = state.activeTab?.viewMode == ViewMode.CONTINUOUS_SCROLL,
                                onClick = { activeMenu = null; state.activeTab?.viewMode = ViewMode.CONTINUOUS_SCROLL }
                            )
                            MenuActionItem(
                                icon = Icons.Default.ViewCarousel,
                                label = "Dos Páginas Encaradas",
                                isSelected = state.activeTab?.viewMode == ViewMode.TWO_PAGE_SPREAD,
                                onClick = { activeMenu = null; state.activeTab?.viewMode = ViewMode.TWO_PAGE_SPREAD }
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            MenuActionItem(
                                icon = Icons.Default.FitScreen,
                                label = "Ajustar al Ancho",
                                shortcut = "Ctrl+2",
                                onClick = { activeMenu = null; state.activeTab?.zoomScale = 1.35f }
                            )
                            MenuActionItem(
                                icon = Icons.Default.FitScreen,
                                label = "Ajustar a Página",
                                shortcut = "Ctrl+0",
                                onClick = { activeMenu = null; state.activeTab?.zoomScale = 1.0f }
                            )
                            MenuActionItem(
                                icon = Icons.AutoMirrored.Filled.RotateRight,
                                label = "Rotar 90° Horario",
                                shortcut = "Ctrl+R",
                                onClick = {
                                    activeMenu = null
                                    val t = state.activeTab
                                    if (t != null) t.rotation = (t.rotation + 90) % 360
                                }
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            MenuActionItem(label = "Tema del Sistema (Automático)", isSelected = state.themeMode == ThemeMode.SYSTEM, onClick = { activeMenu = null; state.themeMode = ThemeMode.SYSTEM })
                            MenuActionItem(label = "Tema Claro", isSelected = state.themeMode == ThemeMode.LIGHT, onClick = { activeMenu = null; state.themeMode = ThemeMode.LIGHT })
                            MenuActionItem(label = "Tema Oscuro", isSelected = state.themeMode == ThemeMode.DARK, onClick = { activeMenu = null; state.themeMode = ThemeMode.DARK })
                            MenuActionItem(label = "Tema Sepia de Lectura", isSelected = state.themeMode == ThemeMode.SEPIA, onClick = { activeMenu = null; state.themeMode = ThemeMode.SEPIA })

                        }
                        "Herramientas" -> {
                            MenuActionItem(icon = Icons.Default.PanTool, label = "Mano / Desplazar", onClick = { activeMenu = null; state.activeTab?.activeTool = DesktopTool.PAN_HAND })
                            MenuActionItem(icon = Icons.Default.NearMe, label = "Selección de Texto", onClick = { activeMenu = null; state.activeTab?.activeTool = DesktopTool.TEXT_SELECTION })
                            MenuActionItem(icon = Icons.Default.Highlight, label = "Resaltador", onClick = { activeMenu = null; state.activeTab?.activeTool = DesktopTool.HIGHLIGHT })
                            MenuActionItem(icon = Icons.Default.FormatUnderlined, label = "Subrayar", onClick = { activeMenu = null; state.activeTab?.activeTool = DesktopTool.UNDERLINE })
                            MenuActionItem(icon = Icons.Default.Draw, label = "Pluma de Dibujo", onClick = { activeMenu = null; state.activeTab?.activeTool = DesktopTool.PEN })
                            MenuActionItem(icon = Icons.Default.ChatBubbleOutline, label = "Nota Adhesiva", onClick = { activeMenu = null; state.activeTab?.activeTool = DesktopTool.STICKY_NOTE })
                            MenuActionItem(icon = Icons.Default.TextFields, label = "Cuadro de Texto", onClick = { activeMenu = null; state.activeTab?.activeTool = DesktopTool.TEXT_BOX })
                            MenuActionItem(icon = Icons.Default.FormatShapes, label = "Formas Geométricas", onClick = { activeMenu = null; state.activeTab?.activeTool = DesktopTool.SHAPE_RECTANGLE })
                            MenuActionItem(icon = Icons.Default.SquareFoot, label = "Herramienta de Medición", onClick = { activeMenu = null; state.activeTab?.activeTool = DesktopTool.MEASURE_DISTANCE })
                            MenuActionItem(icon = Icons.Default.Visibility, label = "Redactar / Censurar", onClick = { activeMenu = null; state.activeTab?.activeTool = DesktopTool.REDACT_MARK })
                        }
                        "Páginas" -> {
                            MenuActionItem(icon = Icons.Default.ViewCarousel, label = "Organizar Páginas (Extraer / Dividir)...", onClick = { activeMenu = null; state.activeDialog = DesktopDialog.ORGANIZE_PAGES })
                            MenuActionItem(icon = Icons.Default.FormatColorFill, label = "Añadir Marca de Agua...", onClick = { activeMenu = null; state.activeDialog = DesktopDialog.WATERMARK })
                            MenuActionItem(icon = Icons.Default.Edit, label = "Encabezado y Pie de Página...", onClick = { activeMenu = null; state.activeDialog = DesktopDialog.HEADER_FOOTER })
                        }
                        "Firmar" -> {
                            MenuActionItem(icon = Icons.Default.Gesture, label = "Rellenar y Firmar...", onClick = { activeMenu = null; state.activeTab?.activeTool = DesktopTool.FILL_AND_SIGN })
                            MenuActionItem(icon = Icons.Default.Security, label = "Firmar con Certificado Digital PKCS#12...", onClick = { activeMenu = null; state.activeDialog = DesktopDialog.CERTIFICATE_SIGN })
                        }
                        "IA & Estudio" -> {
                            MenuActionItem(
                                icon = Icons.Default.AutoAwesome,
                                label = "Abrir Asistente IA",
                                onClick = {
                                    activeMenu = null
                                    state.isRightDockOpen = true
                                    state.rightDockTab = RightDockTab.AI_ASSISTANT
                                }
                            )
                            MenuActionItem(
                                icon = Icons.AutoMirrored.Filled.MenuBook,
                                label = "Explicar Fórmulas Matemáticas LaTeX",
                                onClick = {
                                    activeMenu = null
                                    state.isRightDockOpen = true
                                    state.rightDockTab = RightDockTab.AI_ASSISTANT
                                    state.sendAiMessage("Explica y genera las fórmulas matemáticas, modelos teóricos o ecuaciones relevantes asociadas a este documento utilizando notación matemática LaTeX detallada.")
                                }
                            )
                            MenuActionItem(
                                icon = Icons.Default.Edit,
                                label = "Resumir Página Activa",
                                onClick = {
                                    activeMenu = null
                                    state.isRightDockOpen = true
                                    state.rightDockTab = RightDockTab.AI_ASSISTANT
                                    val p = (state.activeTab?.currentPage ?: 0) + 1
                                    state.sendAiMessage("Genera un resumen analítico detallado con puntos clave de la página $p.")
                                }
                            )
                        }
                        "Ventana" -> {
                            MenuActionItem(label = if (state.isLeftDockOpen) "Ocultar Panel Izquierdo" else "Mostrar Panel Izquierdo", onClick = { activeMenu = null; state.isLeftDockOpen = !state.isLeftDockOpen })
                            MenuActionItem(label = if (state.isRightDockOpen) "Ocultar Panel Derecho" else "Mostrar Panel Derecho", onClick = { activeMenu = null; state.isRightDockOpen = !state.isRightDockOpen })
                        }
                        "Ayuda" -> {
                            MenuActionItem(icon = Icons.Default.Help, label = "Acerca de Paper Desktop...", onClick = { activeMenu = null; state.activeDialog = DesktopDialog.ABOUT })
                        }
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))

        // Right side quick indicator
        Text(
            text = if (state.activeTab != null) state.activeTab!!.title else "Sin archivo abierto",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            maxLines = 1
        )
    }
}

@Composable
fun MenuActionItem(
    label: String,
    icon: ImageVector? = null,
    shortcut: String? = null,
    isSelected: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (icon != null) {
                        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = if (enabled) MaterialTheme.colorScheme.primary else Color.Gray)
                    } else if (isSelected) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    } else {
                        Spacer(Modifier.width(16.dp))
                    }
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        color = if (enabled) MaterialTheme.colorScheme.onSurface else Color.Gray
                    )
                }
                if (!shortcut.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = shortcut, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        },
        enabled = enabled,
        onClick = onClick
    )
}
