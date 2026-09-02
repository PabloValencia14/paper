package com.pablo.paper.desktop.ui.ribbon

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.automirrored.filled.TextSnippet
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.ViewAgenda
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pablo.paper.desktop.model.DesktopTool
import com.pablo.paper.desktop.model.ViewMode
import com.pablo.paper.desktop.state.RightDockTab
import com.pablo.paper.desktop.state.WorkspaceState

@Composable
fun DesktopRibbon(state: WorkspaceState, modifier: Modifier = Modifier) {
    val tab = state.activeTab ?: return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.42f))
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        ToolButton(
            icon = Icons.Default.GridView,
            description = if (state.isLeftDockOpen) "Ocultar navegación" else "Abrir navegación",
            selected = state.isLeftDockOpen,
            onClick = { state.isLeftDockOpen = !state.isLeftDockOpen }
        )
        Rule()

        Text("LECTURA", fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        ToolButton(
            icon = Icons.Default.PanTool,
            description = "Desplazar documento",
            selected = tab.activeTool == DesktopTool.PAN_HAND,
            onClick = { activatePageTool(state, tab, DesktopTool.PAN_HAND) }
        )
        ToolButton(
            icon = Icons.Default.NearMe,
            description = "Seleccionar texto",
            selected = tab.activeTool == DesktopTool.TEXT_SELECTION,
            onClick = { activatePageTool(state, tab, DesktopTool.TEXT_SELECTION) }
        )
        ToolButton(
            icon = Icons.Default.Highlight,
            description = "Seleccionar y resaltar texto",
            selected = tab.activeTool == DesktopTool.HIGHLIGHT,
            onClick = { activatePageTool(state, tab, DesktopTool.HIGHLIGHT) }
        )
        ToolButton(
            icon = Icons.Default.Draw,
            description = "Escritura a mano",
            selected = tab.activeTool == DesktopTool.PEN,
            onClick = { activatePageTool(state, tab, DesktopTool.PEN) }
        )

        if (tab.activeTool == DesktopTool.PEN) {
            AnnotationPalette(
                selected = tab.strokeColor,
                onSelect = { tab.strokeColor = it },
                width = tab.strokeWidth,
                onWidth = { tab.strokeWidth = it }
            )
        }

        Rule()
        Text("VISTA", fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        ToolButton(
            icon = Icons.Default.ViewAgenda,
            description = "Vista de página",
            selected = tab.viewMode == ViewMode.SINGLE_PAGE,
            onClick = { tab.viewMode = ViewMode.SINGLE_PAGE; tab.panOffset = androidx.compose.ui.geometry.Offset.Zero }
        )
        ToolButton(
            icon = Icons.Default.SwapVert,
            description = "Vista continua",
            selected = tab.viewMode == ViewMode.CONTINUOUS_SCROLL,
            onClick = {
                tab.viewMode = ViewMode.CONTINUOUS_SCROLL
                tab.activeTool = DesktopTool.PAN_HAND
                tab.panOffset = androidx.compose.ui.geometry.Offset.Zero
            }
        )
        ToolButton(
            icon = Icons.AutoMirrored.Filled.MenuBook,
            description = "Vista en pliego",
            selected = tab.viewMode == ViewMode.TWO_PAGE_SPREAD,
            onClick = {
                tab.viewMode = ViewMode.TWO_PAGE_SPREAD
                tab.activeTool = DesktopTool.PAN_HAND
                tab.panOffset = androidx.compose.ui.geometry.Offset.Zero
            }
        )
        ToolButton(
            icon = Icons.AutoMirrored.Filled.RotateRight,
            description = "Rotar 90 grados",
            onClick = { tab.rotation = (tab.rotation + 90) % 360 }
        )

        Spacer(Modifier.weight(1f))
        ToolButton(
            icon = Icons.AutoMirrored.Filled.Undo,
            description = "Deshacer",
            enabled = tab.canUndo,
            onClick = tab::undo
        )
        ToolButton(
            icon = Icons.AutoMirrored.Filled.Redo,
            description = "Rehacer",
            enabled = tab.canRedo,
            onClick = tab::redo
        )
        Rule()
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .clickable {
                    state.isRightDockOpen = !state.isRightDockOpen
                    if (state.isRightDockOpen) state.rightDockTab = RightDockTab.MARKDOWN_NOTES
                }
                .padding(horizontal = 7.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.TextSnippet, contentDescription = "Notas", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Notas", fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = if (state.isRightDockOpen && state.rightDockTab == RightDockTab.AI_ASSISTANT) 0.16f else 0.08f))
                .clickable {
                    state.isRightDockOpen = true
                    state.rightDockTab = RightDockTab.AI_ASSISTANT
                }
                .padding(horizontal = 8.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = "Abrir asistente IA", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
            Text("IA", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun AnnotationPalette(selected: Color, onSelect: (Color) -> Unit, width: Float, onWidth: (Float) -> Unit) {
    val colors = listOf(
        Color(0xFF252A29),
        Color(0xFF9A4C39),
        Color(0xFF3E6170),
        Color(0xFF58715B),
        Color(0xFFD3A631)
    )
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        colors.forEach { color ->
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(
                        width = if (selected == color) 2.dp else 1.dp,
                        color = if (selected == color) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                        shape = CircleShape
                    )
                    .semantics { contentDescription = "Color de tinta" }
                    .clickable { onSelect(color) }
            )
        }
        listOf(2f, 4f, 7f).forEach { candidate ->
            Text(
                text = candidate.toInt().toString(),
                fontSize = 10.sp,
                fontWeight = if (width == candidate) FontWeight.Bold else FontWeight.Normal,
                color = if (width == candidate) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .semantics { contentDescription = "Grosor ${candidate.toInt()}" }
                    .clickable { onWidth(candidate) }
                    .padding(horizontal = 4.dp, vertical = 3.dp)
            )
        }
    }
}

@Composable
private fun ToolButton(
    icon: ImageVector,
    description: String,
    selected: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val source = remember { MutableInteractionSource() }
    val hovered by source.collectIsHoveredAsState()
    val background = when {
        selected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        hovered && enabled -> MaterialTheme.colorScheme.surfaceVariant
        else -> Color.Transparent
    }
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(background)
            .hoverable(source, enabled = enabled)
            .border(
                width = if (selected) 1.dp else 0.dp,
                color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else Color.Transparent,
                shape = RoundedCornerShape(6.dp)
            )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = when {
                !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                selected -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun Rule() {
    Box(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .width(1.dp)
            .height(22.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    )
}

private fun activatePageTool(state: WorkspaceState, tab: com.pablo.paper.desktop.state.TabDocumentState, tool: DesktopTool) {
    if (tab.viewMode != ViewMode.SINGLE_PAGE) {
        tab.viewMode = ViewMode.SINGLE_PAGE
        tab.panOffset = androidx.compose.ui.geometry.Offset.Zero
        state.showNotice("Las herramientas de lectura y marca se usan en la vista de página.")
    }
    tab.activeTool = tool
}
