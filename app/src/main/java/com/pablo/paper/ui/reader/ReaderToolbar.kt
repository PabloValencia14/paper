package com.pablo.paper.ui.reader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.ViewCarousel
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pablo.paper.domain.model.ReaderAction
import com.pablo.paper.domain.model.ReaderPanel
import com.pablo.paper.domain.model.ReaderState
import com.pablo.paper.ui.common.LiquidGlassButton
import com.pablo.paper.ui.common.LiquidGlassIconButton
import com.pablo.paper.ui.common.LiquidGlassSurface
import com.pablo.paper.ui.theme.AccentBlue
import com.pablo.paper.ui.theme.TextPrimary
import com.pablo.paper.ui.theme.TextPrimaryDark
import com.pablo.paper.ui.theme.TextSecondary
import com.pablo.paper.ui.theme.TextSecondaryDark

/** Barra de lectura tranquila: las acciones frecuentes quedan visibles; el resto se agrupa. */
@Composable
fun ReaderToolbar(
    state: ReaderState,
    onAction: (ReaderAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val textPrimary = if (state.isDarkMode) TextPrimaryDark else TextPrimary
    val textSecondary = if (state.isDarkMode) TextSecondaryDark else TextSecondary
    var isMoreOpen by remember { mutableStateOf(false) }
    val fileName = state.document?.name ?: "Documento"
    val documentTitle = fileName.substringBeforeLast('.', fileName)

    Box(
        modifier = modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        LiquidGlassSurface(
            modifier = Modifier.fillMaxWidth().height(58.dp),
            shape = RoundedCornerShape(20.dp),
            isDarkMode = state.isDarkMode,
            elevation = 8.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LiquidGlassButton(
                    onClick = { onAction(ReaderAction.CloseDocument) },
                    isDarkMode = state.isDarkMode,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Biblioteca", fontWeight = FontWeight.SemiBold, color = textPrimary)
                }

                Spacer(Modifier.width(12.dp))
                Box(modifier = Modifier.weight(1f)) {
                    Text(
                        text = documentTitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = textPrimary)
                    )
                    Text(
                        text = "Página ${state.currentPage} de ${state.pageCount}",
                        style = MaterialTheme.typography.labelSmall.copy(color = textSecondary)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    LiquidGlassIconButton(
                        onClick = { onAction(ReaderAction.PreviousPage) },
                        icon = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Página anterior",
                        enabled = state.currentPage > 1,
                        isDarkMode = state.isDarkMode,
                        size = 40.dp
                    )
                    LiquidGlassButton(
                        onClick = { onAction(ReaderAction.TogglePageNavigator) },
                        isDarkMode = state.isDarkMode,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("${state.currentPage} / ${state.pageCount}", color = textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    LiquidGlassIconButton(
                        onClick = { onAction(ReaderAction.NextPage) },
                        icon = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = "Página siguiente",
                        enabled = state.currentPage < state.pageCount,
                        isDarkMode = state.isDarkMode,
                        size = 40.dp
                    )
                }

                Spacer(Modifier.width(8.dp))
                LiquidGlassIconButton(
                    onClick = { onAction(ReaderAction.ToggleSearch) },
                    icon = Icons.Rounded.Search,
                    contentDescription = "Buscar en el documento",
                    isDarkMode = state.isDarkMode,
                    size = 42.dp
                )
                LiquidGlassButton(
                    onClick = { onAction(ReaderAction.EnterInkMode) },
                    isDarkMode = state.isDarkMode,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Rounded.Edit, contentDescription = null, tint = AccentBlue)
                    Spacer(Modifier.width(5.dp))
                    Text("Anotar", color = textPrimary, fontWeight = FontWeight.Bold)
                }
                Box {
                    LiquidGlassIconButton(
                        onClick = { isMoreOpen = true },
                        icon = Icons.Rounded.MoreHoriz,
                        contentDescription = "Más herramientas",
                        isDarkMode = state.isDarkMode,
                        size = 42.dp
                    )
                    DropdownMenu(expanded = isMoreOpen, onDismissRequest = { isMoreOpen = false }) {
                        ToolbarMenuItem("Marcador", if (state.currentPage in state.bookmarkedPages) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder) {
                            isMoreOpen = false
                            onAction(ReaderAction.ToggleBookmark(state.currentPage))
                        }
                        ToolbarMenuItem("Índice", Icons.AutoMirrored.Rounded.MenuBook) { isMoreOpen = false; onAction(ReaderAction.ToggleOutline) }
                        ToolbarMenuItem("Miniaturas", Icons.Rounded.GridView) { isMoreOpen = false; onAction(ReaderAction.ToggleThumbnailsDrawer) }
                        ToolbarMenuItem("Modo de página", Icons.Rounded.ViewCarousel) { isMoreOpen = false; onAction(ReaderAction.ToggleViewModeDropdown) }
                        ToolbarMenuItem("Asistente", Icons.Rounded.AutoAwesome) { isMoreOpen = false; onAction(ReaderAction.TogglePanel(ReaderPanel.Assistant)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolbarMenuItem(label: String, icon: ImageVector, onClick: () -> Unit) {
    DropdownMenuItem(text = { Text(label) }, leadingIcon = { Icon(icon, contentDescription = null) }, onClick = onClick)
}
