package com.pablo.paper.desktop.ui.ribbon

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.FormatShapes
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material.icons.filled.ViewSidebar
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pablo.paper.desktop.model.DesktopTool
import com.pablo.paper.desktop.state.DesktopDialog
import com.pablo.paper.desktop.state.RightDockTab
import com.pablo.paper.desktop.state.WorkspaceState

@Composable
fun DesktopRibbon(
    state: WorkspaceState,
    modifier: Modifier = Modifier
) {
    val tab = state.activeTab
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .border(width = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        // Single Compact Streamlined Toolbar Row (Height 42.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .horizontalScroll(scrollState)
                .padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            // Sidebar Toggle Buttons
            ToolbarIconButton(
                icon = Icons.Default.GridOn,
                tooltip = if (state.isLeftDockOpen) "Ocultar panel izquierdo" else "Mostrar miniaturas y marcadores",
                isSelected = state.isLeftDockOpen,
                onClick = { state.isLeftDockOpen = !state.isLeftDockOpen }
            )

            ToolbarDivider()

            // Navigation Tools
            ToolbarIconButton(
                icon = Icons.Default.PanTool,
                tooltip = "Mano / Desplazar",
                isSelected = tab?.activeTool == DesktopTool.PAN_HAND,
                onClick = { tab?.activeTool = DesktopTool.PAN_HAND }
            )
            ToolbarIconButton(
                icon = Icons.Default.NearMe,
                tooltip = "Selección de texto",
                isSelected = tab?.activeTool == DesktopTool.TEXT_SELECTION,
                onClick = { tab?.activeTool = DesktopTool.TEXT_SELECTION }
            )

            ToolbarDivider()

            // Annotation Tools
            ToolbarIconButton(
                icon = Icons.Default.Highlight,
                tooltip = "Resaltador",
                isSelected = tab?.activeTool == DesktopTool.HIGHLIGHT,
                onClick = { tab?.activeTool = DesktopTool.HIGHLIGHT }
            )
            ToolbarIconButton(
                icon = Icons.Default.FormatUnderlined,
                tooltip = "Subrayar",
                isSelected = tab?.activeTool == DesktopTool.UNDERLINE,
                onClick = { tab?.activeTool = DesktopTool.UNDERLINE }
            )
            ToolbarIconButton(
                icon = Icons.Default.Draw,
                tooltip = "Pluma libre",
                isSelected = tab?.activeTool == DesktopTool.PEN,
                onClick = { tab?.activeTool = DesktopTool.PEN }
            )
            ToolbarIconButton(
                icon = Icons.Default.ChatBubbleOutline,
                tooltip = "Nota adhesiva",
                isSelected = tab?.activeTool == DesktopTool.STICKY_NOTE,
                onClick = { tab?.activeTool = DesktopTool.STICKY_NOTE }
            )
            ToolbarIconButton(
                icon = Icons.Default.TextFields,
                tooltip = "Cuadro de texto",
                isSelected = tab?.activeTool == DesktopTool.TEXT_BOX,
                onClick = { tab?.activeTool = DesktopTool.TEXT_BOX }
            )
            ToolbarIconButton(
                icon = Icons.Default.FormatShapes,
                tooltip = "Formas geométricas",
                isSelected = tab?.activeTool == DesktopTool.SHAPE_RECTANGLE,
                onClick = { tab?.activeTool = DesktopTool.SHAPE_RECTANGLE }
            )

            ToolbarDivider()

            // Acrobat PDF Tools
            ToolbarIconButton(
                icon = Icons.Default.ViewCarousel,
                tooltip = "Organizar y Dividir Páginas",
                onClick = { state.activeDialog = DesktopDialog.ORGANIZE_PAGES }
            )
            ToolbarIconButton(
                icon = Icons.Default.FormatColorFill,
                tooltip = "Marca de agua",
                onClick = { state.activeDialog = DesktopDialog.WATERMARK }
            )
            ToolbarIconButton(
                icon = Icons.Default.Gesture,
                tooltip = "Rellenar y Firmar",
                isSelected = tab?.activeTool == DesktopTool.FILL_AND_SIGN,
                onClick = { tab?.activeTool = DesktopTool.FILL_AND_SIGN }
            )
            ToolbarIconButton(
                icon = Icons.Default.Lock,
                tooltip = "Cifrado AES-256",
                onClick = { state.activeDialog = DesktopDialog.PASSWORD_SECURITY }
            )

            ToolbarDivider()

            // Page Layout Modes
            ToolbarIconButton(
                icon = Icons.Default.Description,
                tooltip = "Página Individual",
                isSelected = tab?.viewMode == com.pablo.paper.desktop.model.ViewMode.SINGLE_PAGE,
                onClick = { tab?.viewMode = com.pablo.paper.desktop.model.ViewMode.SINGLE_PAGE }
            )
            ToolbarIconButton(
                icon = Icons.Default.SwapVert,
                tooltip = "Scroll Continuo",
                isSelected = tab?.viewMode == com.pablo.paper.desktop.model.ViewMode.CONTINUOUS_SCROLL,
                onClick = { tab?.viewMode = com.pablo.paper.desktop.model.ViewMode.CONTINUOUS_SCROLL }
            )
            ToolbarIconButton(
                icon = Icons.AutoMirrored.Filled.MenuBook,
                tooltip = "Vista de Libro (2 Páginas)",
                isSelected = tab?.viewMode == com.pablo.paper.desktop.model.ViewMode.TWO_PAGE_SPREAD,
                onClick = { tab?.viewMode = com.pablo.paper.desktop.model.ViewMode.TWO_PAGE_SPREAD }
            )

            ToolbarDivider()

            // Zoom & Rotate
            ToolbarIconButton(
                icon = Icons.Default.ZoomIn,
                tooltip = "Acercar (Zoom +)",
                onClick = { tab?.zoomScale = ((tab?.zoomScale ?: 1f) * 1.2f).coerceAtMost(8.0f) }
            )
            ToolbarIconButton(
                icon = Icons.Default.ZoomOut,
                tooltip = "Alejar (Zoom -)",
                onClick = { tab?.zoomScale = ((tab?.zoomScale ?: 1f) / 1.2f).coerceAtLeast(0.25f) }
            )
            ToolbarIconButton(
                icon = Icons.AutoMirrored.Filled.RotateRight,
                tooltip = "Rotar 90°",
                onClick = { if (tab != null) tab.rotation = (tab.rotation + 90) % 360 }
            )

            Spacer(Modifier.weight(1f))

            // AI & Study Quick Launch
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (state.isRightDockOpen) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                    .clickable {
                        state.isRightDockOpen = !state.isRightDockOpen
                        if (state.isRightDockOpen) state.rightDockTab = RightDockTab.AI_ASSISTANT
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Asistente IA",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "Asistente IA",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.width(4.dp))

            // Undo / Redo
            IconButton(
                onClick = { tab?.undo() },
                enabled = tab != null,
                modifier = Modifier.size(30.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Undo,
                    contentDescription = "Deshacer",
                    tint = if (tab != null) MaterialTheme.colorScheme.onSurface else Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
            }
            IconButton(
                onClick = { tab?.redo() },
                enabled = tab != null,
                modifier = Modifier.size(30.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Redo,
                    contentDescription = "Rehacer",
                    tint = if (tab != null) MaterialTheme.colorScheme.onSurface else Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Contextual Property Bar (Color / Stroke Width)
        if (tab != null && (tab.activeTool == DesktopTool.PEN || tab.activeTool == DesktopTool.HIGHLIGHT || tab.activeTool == DesktopTool.SHAPE_RECTANGLE || tab.activeTool == DesktopTool.STICKY_NOTE)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Color:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                val palette = listOf(
                    Color(0xFF0D9488), // Teal
                    Color(0xFFEF4444), // Red
                    Color(0xFF3B82F6), // Blue
                    Color(0xFF10B981), // Green
                    Color(0xFFF59E0B), // Yellow/Amber
                    Color(0xFF8B5CF6), // Purple
                    Color(0xFF000000)  // Black
                )
                palette.forEach { col ->
                    val isPicked = tab.strokeColor == col
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(col)
                            .border(
                                width = if (isPicked) 2.dp else 1.dp,
                                color = if (isPicked) MaterialTheme.colorScheme.onSurface else Color.LightGray.copy(alpha = 0.5f),
                                shape = CircleShape
                            )
                            .clickable { tab.strokeColor = col },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isPicked) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = if (col == Color.Black || col == Color(0xFF0D9488) || col == Color(0xFF3B82F6)) Color.White else Color.Black,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.width(10.dp))
                Text("Grosor:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                val widths = listOf(1.5f, 3.0f, 6.0f, 12.0f)
                widths.forEach { w ->
                    val isPicked = tab.strokeWidth == w
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isPicked) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent)
                            .border(
                                width = if (isPicked) 1.dp else 0.dp,
                                color = if (isPicked) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .clickable { tab.strokeWidth = w }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${w.toInt()}px",
                            fontSize = 10.sp,
                            fontWeight = if (isPicked) FontWeight.Bold else FontWeight.Normal,
                            color = if (isPicked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ToolbarIconButton(
    icon: ImageVector,
    tooltip: String,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val bg = when {
        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
        isHovered -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        else -> Color.Transparent
    }

    val tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    val borderCol = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.35f) else Color.Transparent

    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .border(1.dp, borderCol, RoundedCornerShape(6.dp))
            .hoverable(interactionSource = interactionSource)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = tooltip,
            tint = tint,
            modifier = Modifier.size(17.dp)
        )
    }
}

@Composable
fun ToolbarDivider() {
    Box(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .width(1.dp)
            .height(24.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    )
}
