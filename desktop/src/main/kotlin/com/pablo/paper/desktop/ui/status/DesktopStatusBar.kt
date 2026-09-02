package com.pablo.paper.desktop.ui.status

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
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.Splitscreen
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import com.pablo.paper.desktop.model.ViewMode
import com.pablo.paper.desktop.state.WorkspaceState
import kotlin.math.roundToInt

@Composable
fun DesktopStatusBar(
    state: WorkspaceState,
    modifier: Modifier = Modifier
) {
    val tab = state.activeTab

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(30.dp)
            .background(MaterialTheme.colorScheme.surface)
            .border(width = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Page Navigation
        if (tab != null && tab.isLoaded && tab.pageCount > 0) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = { if (tab.currentPage > 0) tab.currentPage-- },
                    enabled = tab.currentPage > 0,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Página anterior", modifier = Modifier.size(16.dp))
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Pág. ${tab.currentPage + 1} de ${tab.pageCount}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(
                    onClick = { if (tab.currentPage < tab.pageCount - 1) tab.currentPage++ },
                    enabled = tab.currentPage < tab.pageCount - 1,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Página siguiente", modifier = Modifier.size(16.dp))
                }
            }
        } else {
            Text(
                text = "Listo",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }

        Spacer(Modifier.weight(1f))

        // View Mode & Zoom
        if (tab != null && tab.isLoaded) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ViewModeButton(
                    icon = Icons.Default.ViewAgenda,
                    tooltip = "Página Simple",
                    isSelected = tab.viewMode == ViewMode.SINGLE_PAGE,
                    onClick = { tab.viewMode = ViewMode.SINGLE_PAGE }
                )
                ViewModeButton(
                    icon = Icons.Default.Splitscreen,
                    tooltip = "Desplazamiento Continuo",
                    isSelected = tab.viewMode == ViewMode.CONTINUOUS_SCROLL,
                    onClick = { tab.viewMode = ViewMode.CONTINUOUS_SCROLL }
                )
                ViewModeButton(
                    icon = Icons.Default.ViewCarousel,
                    tooltip = "Dos Páginas Encaradas",
                    isSelected = tab.viewMode == ViewMode.TWO_PAGE_SPREAD,
                    onClick = { tab.viewMode = ViewMode.TWO_PAGE_SPREAD }
                )
            }

            Spacer(Modifier.width(16.dp))

            // Zoom Presets & Slider
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = { tab.zoomScale = (tab.zoomScale / 1.15f).coerceAtLeast(0.25f) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.ZoomOut, contentDescription = "Zoom -", modifier = Modifier.size(15.dp))
                }
                Slider(
                    value = tab.zoomScale,
                    onValueChange = { tab.zoomScale = it },
                    valueRange = 0.25f..5.0f,
                    modifier = Modifier.width(110.dp).height(20.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
                IconButton(
                    onClick = { tab.zoomScale = (tab.zoomScale * 1.15f).coerceAtMost(5.0f) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.ZoomIn, contentDescription = "Zoom +", modifier = Modifier.size(15.dp))
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .clickable { tab.zoomScale = 1.0f }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${(tab.zoomScale * 100).roundToInt()}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun ViewModeButton(
    icon: ImageVector,
    tooltip: String,
    isSelected: Boolean,
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

    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(bg)
            .border(width = if (isSelected) 1.dp else 0.dp, color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else Color.Transparent, shape = RoundedCornerShape(5.dp))
            .hoverable(interactionSource = interactionSource)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = tooltip, tint = tint, modifier = Modifier.size(15.dp))
    }
}
