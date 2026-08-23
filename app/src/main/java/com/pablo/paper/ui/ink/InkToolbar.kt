package com.pablo.paper.ui.ink

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.Redo
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.BorderColor
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pablo.paper.domain.model.InkTool
import com.pablo.paper.domain.model.ReaderAction
import com.pablo.paper.domain.model.ReaderState
import com.pablo.paper.ui.theme.AccentBlue
import com.pablo.paper.ui.theme.TextPrimary
import com.pablo.paper.ui.theme.TextPrimaryDark
import com.pablo.paper.ui.theme.TextSecondary
import com.pablo.paper.ui.theme.TextSecondaryDark

/**
 * Barra de tinta de acceso directo. Color, grosor y herramienta activa son visibles sin abrir
 * diálogos; las herramientas infrecuentes permanecen disponibles en "Más".
 */
@Composable
fun InkToolbar(
    state: ReaderState,
    onAction: (ReaderAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val textPrimary = if (state.isDarkMode) TextPrimaryDark else TextPrimary
    val textSecondary = if (state.isDarkMode) TextSecondaryDark else TextSecondary
    val surface = if (state.isDarkMode) Color(0xEE152235) else Color(0xFDF9FBFF)
    val selectedColor = Color(
        if (state.activeInkTool == InkTool.HIGHLIGHTER || state.activeInkTool == InkTool.TEXT_HIGHLIGHT) {
            state.selectedHighlighterColor
        } else {
            state.selectedColor
        }
    )
    var moreOpen by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 10.dp)) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = surface,
            shape = RoundedCornerShape(22.dp),
            border = BorderStroke(1.dp, if (state.isDarkMode) Color(0x305E91C8) else Color(0x1E174E8C)),
            shadowElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ToolChoice("Pluma", Icons.Rounded.Edit, state.activeInkTool == InkTool.PEN, state.isDarkMode) {
                        onAction(ReaderAction.SelectInkTool(InkTool.PEN))
                    }
                    ToolChoice("Resaltar", Icons.Rounded.BorderColor, state.activeInkTool == InkTool.HIGHLIGHTER, state.isDarkMode) {
                        onAction(ReaderAction.SelectInkTool(InkTool.HIGHLIGHTER))
                    }
                    ToolChoice("Borrar", Icons.Rounded.CleaningServices, state.activeInkTool == InkTool.ERASER, state.isDarkMode) {
                        onAction(ReaderAction.SelectInkTool(InkTool.ERASER))
                    }
                    ToolChoice("Flecha", Icons.AutoMirrored.Rounded.ArrowForward, state.activeInkTool == InkTool.ARROW, state.isDarkMode) {
                        onAction(ReaderAction.SelectInkTool(InkTool.ARROW))
                    }
                    Box {
                        Surface(
                            modifier = Modifier.clickable { moreOpen = true },
                            color = if (moreOpen) AccentBlue.copy(alpha = .14f) else Color.Transparent,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (moreOpen) AccentBlue.copy(alpha = .6f) else Color.Transparent)
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.MoreHoriz, null, tint = if (state.isDarkMode) TextSecondaryDark else Color(0xFF65758B), modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Más", color = textPrimary, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium))
                            }
                        }
                        DropdownMenu(
                            expanded = moreOpen,
                            onDismissRequest = { moreOpen = false },
                            modifier = Modifier.background(if (state.isDarkMode) Color(0xFF1E293B) else Color.White)
                        ) {
                            InkMenuItem("Mano", InkTool.HAND, state.isDarkMode, onAction) { moreOpen = false }
                            InkMenuItem("Selección de texto", InkTool.SELECT_TEXT, state.isDarkMode, onAction) { moreOpen = false }
                            InkMenuItem("Subrayar", InkTool.UNDERLINE, state.isDarkMode, onAction) { moreOpen = false }
                            InkMenuItem("Rectángulo", InkTool.RECTANGLE, state.isDarkMode, onAction) { moreOpen = false }
                            InkMenuItem("Lazo", InkTool.LASSO, state.isDarkMode, onAction) { moreOpen = false }
                            InkMenuItem("Nota adhesiva", InkTool.STICKY_NOTE, state.isDarkMode, onAction) { moreOpen = false }
                            InkMenuItem("Cuadro de texto", InkTool.TEXT_BOX, state.isDarkMode, onAction) { moreOpen = false }
                            InkMenuItem("Firma", InkTool.SIGNATURE, state.isDarkMode, onAction) { moreOpen = false }
                        }
                    }

                    Spacer(Modifier.weight(1f))

                    IconButton(onClick = { onAction(ReaderAction.Undo) }, enabled = state.canUndo) {
                        Icon(Icons.AutoMirrored.Rounded.Undo, "Deshacer", tint = if (state.canUndo) textPrimary else textSecondary.copy(alpha = 0.4f))
                    }
                    IconButton(onClick = { onAction(ReaderAction.Redo) }, enabled = state.canRedo) {
                        Icon(Icons.AutoMirrored.Rounded.Redo, "Rehacer", tint = if (state.canRedo) textPrimary else textSecondary.copy(alpha = 0.4f))
                    }
                    Surface(
                        modifier = Modifier.clickable { onAction(ReaderAction.ExitInkMode) },
                        color = AccentBlue,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Listo", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Color", style = MaterialTheme.typography.labelSmall.copy(color = textSecondary, fontWeight = FontWeight.Bold))
                    Spacer(Modifier.width(8.dp))
                    listOf(0xFF111827L, 0xFF174E8CL, 0xFFC2410CL, 0xFFB42318L, 0xFF6B46C1L, 0xFF0F766EL).forEach { color ->
                        val isSelected = selectedColor.value.toLong() == color
                        Box(
                            modifier = Modifier
                                .padding(end = 6.dp)
                                .size(if (isSelected) 28.dp else 22.dp)
                                .background(Color(color), CircleShape)
                                .clickable { onAction(ReaderAction.SelectColor(color)) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                    }

                    Spacer(Modifier.width(16.dp))
                    Text("Trazo", style = MaterialTheme.typography.labelSmall.copy(color = textSecondary, fontWeight = FontWeight.Bold))
                    Spacer(Modifier.width(8.dp))
                    listOf(2f to "Fino", 4f to "Medio", 7f to "Grueso").forEach { (width, label) ->
                        val isSelected = kotlin.math.abs(state.selectedStrokeWidth - width) < 0.6f
                        Surface(
                            modifier = Modifier.padding(end = 6.dp).clickable { onAction(ReaderAction.SetStrokeWidth(width)) },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) AccentBlue.copy(alpha = .14f) else Color.Transparent,
                            border = BorderStroke(1.dp, if (isSelected) AccentBlue.copy(alpha = .65f) else textSecondary.copy(alpha = .25f))
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(if (width < 3f) 4.dp else if (width < 5f) 7.dp else 10.dp).background(selectedColor, CircleShape))
                                Spacer(Modifier.width(5.dp))
                                Text(label, fontSize = 12.sp, color = textPrimary, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolChoice(label: String, icon: ImageVector, selected: Boolean, isDarkMode: Boolean, onClick: () -> Unit) {
    val textPrimary = if (isDarkMode) TextPrimaryDark else TextPrimary
    val textSecondary = if (isDarkMode) TextSecondaryDark else TextSecondary
    Surface(
        modifier = Modifier.padding(end = 6.dp).clickable(onClick = onClick),
        color = if (selected) AccentBlue.copy(alpha = .14f) else Color.Transparent,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (selected) AccentBlue.copy(alpha = .6f) else Color.Transparent)
    ) {
        Row(modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = if (selected) AccentBlue else textSecondary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(5.dp))
            Text(label, color = if (selected) AccentBlue else textPrimary, style = MaterialTheme.typography.labelMedium.copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium))
        }
    }
}

@Composable
private fun InkMenuItem(label: String, tool: InkTool, isDarkMode: Boolean, onAction: (ReaderAction) -> Unit, close: () -> Unit) {
    DropdownMenuItem(
        text = { Text(label, color = if (isDarkMode) TextPrimaryDark else TextPrimary) },
        onClick = { close(); onAction(ReaderAction.SelectInkTool(tool)) }
    )
}
