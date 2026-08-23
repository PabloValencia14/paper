package com.pablo.paper.ui.reader

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.automirrored.rounded.Redo
import androidx.compose.material.icons.automirrored.rounded.ShortText
import androidx.compose.material.icons.automirrored.rounded.StickyNote2
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.Approval
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.Create
import androidx.compose.material.icons.rounded.CropPortrait
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.DocumentScanner
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FitScreen
import androidx.compose.material.icons.rounded.Gesture
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.HighlightAlt
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.NightlightRound
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PanTool
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Rectangle
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material.icons.rounded.Title
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.ViewCarousel
import androidx.compose.material.icons.rounded.ViewStream
import androidx.compose.material.icons.rounded.WbSunny
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
import com.pablo.paper.domain.model.ReaderPanel
import com.pablo.paper.domain.model.ReaderState
import com.pablo.paper.domain.model.ViewMode
import com.pablo.paper.ui.common.LiquidGlassButton
import com.pablo.paper.ui.common.LiquidGlassIconButton
import com.pablo.paper.ui.common.LiquidGlassSurface
import com.pablo.paper.ui.theme.AccentBlue
import com.pablo.paper.ui.theme.TextPrimary
import com.pablo.paper.ui.theme.TextPrimaryDark
import com.pablo.paper.ui.theme.TextSecondary
import com.pablo.paper.ui.theme.TextSecondaryDark

/**
 * Barra superior unificada LiquidGlass: Acceso instantáneo a navegación,
 * todas las herramientas de tinta, paleta de color, modos de visualización,
 * búsqueda, notas y asistente de Inteligencia Artificial.
 */
@Composable
fun ReaderToolbar(
    state: ReaderState,
    onAction: (ReaderAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val textPrimary = if (state.isDarkMode) TextPrimaryDark else TextPrimary
    val textSecondary = if (state.isDarkMode) TextSecondaryDark else TextSecondary
    var isMoreOpen by remember { mutableStateOf(false) }
    var isViewModeMenuOpen by remember { mutableStateOf(false) }

    val selectedColor = Color(
        if (state.activeInkTool == InkTool.HIGHLIGHTER || state.activeInkTool == InkTool.TEXT_HIGHLIGHT) {
            state.selectedHighlighterColor
        } else {
            state.selectedColor
        }
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        LiquidGlassSurface(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(18.dp),
            isDarkMode = state.isDarkMode,
            elevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 1. Biblioteca
                LiquidGlassButton(
                    onClick = { onAction(ReaderAction.CloseDocument) },
                    isDarkMode = state.isDarkMode,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Volver a la Biblioteca",
                        tint = textPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Biblioteca", fontWeight = FontWeight.SemiBold, color = textPrimary, fontSize = 13.sp)
                }

                ToolbarDivider(state.isDarkMode)

                // 2. Navegación de páginas
                LiquidGlassIconButton(
                    onClick = { onAction(ReaderAction.PreviousPage) },
                    icon = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Página anterior",
                    enabled = state.currentPage > 1,
                    isDarkMode = state.isDarkMode,
                    size = 32.dp
                )

                LiquidGlassButton(
                    onClick = { onAction(ReaderAction.TogglePageNavigator) },
                    isDarkMode = state.isDarkMode,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "${state.currentPage} / ${state.pageCount}",
                        color = textPrimary,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                LiquidGlassIconButton(
                    onClick = { onAction(ReaderAction.NextPage) },
                    icon = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = "Página siguiente",
                    enabled = state.currentPage < state.pageCount,
                    isDarkMode = state.isDarkMode,
                    size = 32.dp
                )

                // Document quick actions
                LiquidGlassIconButton(
                    onClick = { onAction(ReaderAction.ToggleBookmark(state.currentPage)) },
                    icon = if (state.currentPage in state.bookmarkedPages) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                    contentDescription = "Marcador",
                    isSelected = state.currentPage in state.bookmarkedPages,
                    isDarkMode = state.isDarkMode,
                    size = 32.dp
                )
                LiquidGlassIconButton(
                    onClick = { onAction(ReaderAction.ToggleOutline) },
                    icon = Icons.AutoMirrored.Rounded.MenuBook,
                    contentDescription = "Índice",
                    isSelected = state.activePanel == ReaderPanel.Outline,
                    isDarkMode = state.isDarkMode,
                    size = 32.dp
                )
                LiquidGlassIconButton(
                    onClick = { onAction(ReaderAction.ToggleThumbnailsDrawer) },
                    icon = Icons.Rounded.GridView,
                    contentDescription = "Miniaturas",
                    isSelected = state.isThumbnailsDrawerOpen,
                    isDarkMode = state.isDarkMode,
                    size = 32.dp
                )

                ToolbarDivider(state.isDarkMode)

                // 3. Herramientas de tinta (Direct Access)
                LiquidGlassIconButton(
                    onClick = { onAction(ReaderAction.SelectInkTool(InkTool.HAND)) },
                    icon = Icons.Rounded.PanTool,
                    contentDescription = "Mano / Desplazamiento",
                    isSelected = state.activeInkTool == InkTool.HAND,
                    isDarkMode = state.isDarkMode,
                    size = 32.dp
                )
                InkIconButton(Icons.Rounded.Create, "Pluma", InkTool.PEN, state, onAction)
                InkIconButton(Icons.Rounded.HighlightAlt, "Resaltador libre", InkTool.HIGHLIGHTER, state, onAction)
                InkIconButton(Icons.Rounded.Title, "Resaltador de texto", InkTool.TEXT_HIGHLIGHT, state, onAction)
                InkIconButton(Icons.Rounded.Rectangle, "Formas geométricas", InkTool.RECTANGLE, state, onAction)
                InkIconButton(Icons.AutoMirrored.Rounded.StickyNote2, "Nota adhesiva", InkTool.STICKY_NOTE, state, onAction)
                InkIconButton(Icons.Rounded.TextFields, "Cuadro de texto", InkTool.TEXT_BOX, state, onAction)
                InkIconButton(Icons.Rounded.Approval, "Sello", InkTool.STAMP, state, onAction)
                InkIconButton(Icons.Rounded.Gesture, "Lazo de selección", InkTool.LASSO, state, onAction)
                InkIconButton(Icons.Rounded.CleaningServices, "Borrador", InkTool.ERASER, state, onAction)

                ToolbarDivider(state.isDarkMode)

                // 4. Paleta de colores rápidos
                listOf(
                    0xFF111827L, // Negro
                    0xFF174E8CL, // Azul marino
                    0xFFC2410CL, // Naranja
                    0xFFB42318L, // Rojo
                    0xFF6B46C1L, // Púrpura
                    0xFF0F766EL  // Verde azulado
                ).forEach { colorLong ->
                    val isSelected = selectedColor.value.toLong() == colorLong
                    IconButton(
                        onClick = { onAction(ReaderAction.SelectColor(colorLong)) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 18.dp else 13.dp)
                                .background(Color(colorLong), CircleShape)
                        )
                    }
                }

                // Selector de paleta completa
                LiquidGlassIconButton(
                    onClick = { onAction(ReaderAction.ToggleColorPicker) },
                    icon = Icons.Rounded.Palette,
                    contentDescription = "Paleta de colores",
                    isSelected = state.showColorPicker,
                    isDarkMode = state.isDarkMode,
                    size = 32.dp
                )

                // Selector de grosor de trazo
                listOf(2f to 3.dp, 4f to 6.dp, 7f to 9.dp).forEach { (width, dotSize) ->
                    val isSelected = kotlin.math.abs(state.selectedStrokeWidth - width) < 0.6f
                    LiquidGlassButton(
                        onClick = { onAction(ReaderAction.SetStrokeWidth(width)) },
                        isSelected = isSelected,
                        isDarkMode = state.isDarkMode,
                        shape = CircleShape,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(6.dp)
                    ) {
                        Box(
                            Modifier
                                .size(dotSize)
                                .background(selectedColor, CircleShape)
                        )
                    }
                }

                ToolbarDivider(state.isDarkMode)

                // 5. Historial (Deshacer / Rehacer)
                LiquidGlassIconButton(
                    onClick = { onAction(ReaderAction.Undo) },
                    icon = Icons.AutoMirrored.Rounded.Undo,
                    contentDescription = "Deshacer",
                    enabled = state.canUndo,
                    isDarkMode = state.isDarkMode,
                    size = 32.dp
                )
                LiquidGlassIconButton(
                    onClick = { onAction(ReaderAction.Redo) },
                    icon = Icons.AutoMirrored.Rounded.Redo,
                    contentDescription = "Rehacer",
                    enabled = state.canRedo,
                    isDarkMode = state.isDarkMode,
                    size = 32.dp
                )

                ToolbarDivider(state.isDarkMode)

                // 6. Modo de Página (Dropdown)
                Box {
                    val modeLabel = when (state.viewMode) {
                        ViewMode.FULL_PAGE -> "Página completa"
                        ViewMode.FIT_WIDTH -> "Ajustar ancho"
                        ViewMode.CONTINUOUS_SCROLL -> "Scroll continuo"
                        ViewMode.TWO_PAGE -> "Doble página"
                        else -> "Página completa"
                    }
                    LiquidGlassButton(
                        onClick = { isViewModeMenuOpen = true },
                        isDarkMode = state.isDarkMode,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Rounded.ViewCarousel, null, tint = AccentBlue, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(3.dp))
                        Text(
                            text = modeLabel,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = textPrimary
                        )
                        Icon(Icons.Rounded.ArrowDropDown, null, tint = textSecondary, modifier = Modifier.size(16.dp))
                    }

                    DropdownMenu(
                        expanded = isViewModeMenuOpen,
                        onDismissRequest = { isViewModeMenuOpen = false },
                        modifier = Modifier.background(if (state.isDarkMode) Color(0xFF1E293B) else Color.White)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Página completa", color = textPrimary) },
                            leadingIcon = { Icon(Icons.Rounded.CropPortrait, null, tint = AccentBlue) },
                            onClick = {
                                isViewModeMenuOpen = false
                                onAction(ReaderAction.SelectViewMode(ViewMode.FULL_PAGE))
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Ajustar ancho", color = textPrimary) },
                            leadingIcon = { Icon(Icons.Rounded.FitScreen, null, tint = AccentBlue) },
                            onClick = {
                                isViewModeMenuOpen = false
                                onAction(ReaderAction.SelectViewMode(ViewMode.FIT_WIDTH))
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Scroll continuo", color = textPrimary) },
                            leadingIcon = { Icon(Icons.Rounded.ViewStream, null, tint = AccentBlue) },
                            onClick = {
                                isViewModeMenuOpen = false
                                onAction(ReaderAction.SelectViewMode(ViewMode.CONTINUOUS_SCROLL))
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Doble página", color = textPrimary) },
                            leadingIcon = { Icon(Icons.AutoMirrored.Rounded.MenuBook, null, tint = AccentBlue) },
                            onClick = {
                                isViewModeMenuOpen = false
                                onAction(ReaderAction.SelectViewMode(ViewMode.TWO_PAGE))
                            }
                        )
                    }
                }

                // 7. Bionic Reading / Filtros de lectura
                LiquidGlassButton(
                    onClick = { onAction(ReaderAction.ToggleBionicReading) },
                    isSelected = state.isBionicReadingEnabled,
                    isDarkMode = state.isDarkMode,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Texto", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = textPrimary)
                }

                // 8. Búsqueda
                LiquidGlassIconButton(
                    onClick = { onAction(ReaderAction.ToggleSearch) },
                    icon = Icons.Rounded.Search,
                    contentDescription = "Buscar texto",
                    isSelected = state.isSearchVisible,
                    isDarkMode = state.isDarkMode,
                    size = 34.dp
                )

                // 9. Notas del documento
                LiquidGlassButton(
                    onClick = { onAction(ReaderAction.TogglePanel(ReaderPanel.Markdown)) },
                    isSelected = state.activePanel == ReaderPanel.Markdown,
                    isDarkMode = state.isDarkMode,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Rounded.Description, null, tint = AccentBlue, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(3.dp))
                    Text("Notas", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = textPrimary)
                }

                // 10. Botón Asistente IA (Destacado)
                LiquidGlassButton(
                    onClick = { onAction(ReaderAction.TogglePanel(ReaderPanel.Assistant)) },
                    isSelected = state.activePanel == ReaderPanel.Assistant,
                    isDarkMode = state.isDarkMode,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = "Asistente IA",
                        tint = Color(0xFF8B5CF6),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "IA",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (state.activePanel == ReaderPanel.Assistant) Color(0xFF8B5CF6) else textPrimary
                    )
                }

                // 11. Menú "Más herramientas"
                Box {
                    LiquidGlassIconButton(
                        onClick = { isMoreOpen = true },
                        icon = Icons.Rounded.MoreHoriz,
                        contentDescription = "Más herramientas",
                        isDarkMode = state.isDarkMode,
                        size = 34.dp
                    )

                    DropdownMenu(
                        expanded = isMoreOpen,
                        onDismissRequest = { isMoreOpen = false },
                        modifier = Modifier.background(if (state.isDarkMode) Color(0xFF1E293B) else Color.White)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Organizar páginas", color = textPrimary) },
                            leadingIcon = { Icon(Icons.Rounded.GridView, null, tint = textSecondary) },
                            onClick = {
                                isMoreOpen = false
                                onAction(ReaderAction.TogglePageOrganizer)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Exportar documento...", color = textPrimary) },
                            leadingIcon = { Icon(Icons.Rounded.Share, null, tint = textSecondary) },
                            onClick = {
                                isMoreOpen = false
                                onAction(ReaderAction.OpenPdfExportDialog)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Personalizar papel y fondo", color = textPrimary) },
                            leadingIcon = { Icon(Icons.Rounded.ColorLens, null, tint = textSecondary) },
                            onClick = {
                                isMoreOpen = false
                                onAction(ReaderAction.TogglePaperCustomizer)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Configurar lápiz óptico", color = textPrimary) },
                            leadingIcon = { Icon(Icons.Rounded.Tune, null, tint = textSecondary) },
                            onClick = {
                                isMoreOpen = false
                                onAction(ReaderAction.OpenStylusSettingsDialog)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (state.isDarkMode) "Modo claro" else "Modo oscuro", color = textPrimary) },
                            leadingIcon = { Icon(if (state.isDarkMode) Icons.Rounded.WbSunny else Icons.Rounded.NightlightRound, null, tint = textSecondary) },
                            onClick = {
                                isMoreOpen = false
                                onAction(ReaderAction.ToggleDarkMode)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Información del documento", color = textPrimary) },
                            leadingIcon = { Icon(Icons.Rounded.Info, null, tint = textSecondary) },
                            onClick = {
                                isMoreOpen = false
                                onAction(ReaderAction.ToggleDocInfo)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InkIconButton(
    icon: ImageVector,
    description: String,
    tool: InkTool,
    state: ReaderState,
    onAction: (ReaderAction) -> Unit
) {
    LiquidGlassIconButton(
        onClick = { onAction(ReaderAction.SelectInkTool(tool)) },
        icon = icon,
        contentDescription = description,
        isSelected = state.activeInkTool == tool,
        isDarkMode = state.isDarkMode,
        size = 32.dp
    )
}

@Composable
private fun ToolbarDivider(isDark: Boolean) {
    Spacer(
        Modifier
            .width(1.dp)
            .height(20.dp)
            .background(if (isDark) Color(0x35FFFFFF) else Color(0x200F365F))
    )
}
