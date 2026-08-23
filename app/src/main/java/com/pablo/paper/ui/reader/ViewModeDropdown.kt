package com.pablo.paper.ui.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.CropPortrait
import androidx.compose.material.icons.rounded.FitScreen
import androidx.compose.material.icons.rounded.ViewStream
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pablo.paper.domain.model.ViewMode
import com.pablo.paper.ui.theme.AccentBlue
import com.pablo.paper.ui.theme.SurfaceCard
import com.pablo.paper.ui.theme.TextPrimary

@Composable
fun ViewModeDropdown(
    expanded: Boolean,
    currentMode: ViewMode,
    isDarkMode: Boolean = false,
    onDismissRequest: () -> Unit,
    onSelectMode: (ViewMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (isDarkMode) androidx.compose.ui.graphics.Color(0xFF1E293B) else SurfaceCard
    val textPrimary = if (isDarkMode) androidx.compose.ui.graphics.Color(0xFFF1F5F9) else TextPrimary

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier
            .width(200.dp)
            .background(bg)
    ) {
        val modes = listOf(
            ViewMode.FULL_PAGE to ("Página completa" to Icons.Rounded.CropPortrait),
            ViewMode.FIT_WIDTH to ("Ajustar al ancho" to Icons.Rounded.FitScreen),
            ViewMode.CONTINUOUS_SCROLL to ("Scroll continuo" to Icons.Rounded.ViewStream),
            ViewMode.TWO_PAGE to ("Doble página (Libro)" to Icons.AutoMirrored.Rounded.MenuBook)
        )

        modes.forEach { (mode, pair) ->
            val (label, icon) = pair
            val isSelected = mode == currentMode
            DropdownMenuItem(
                text = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) AccentBlue else textPrimary,
                            fontSize = 13.5.sp
                        )
                    )
                },
                leadingIcon = {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = if (isSelected) AccentBlue else textPrimary.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                },
                onClick = {
                    onSelectMode(mode)
                    onDismissRequest()
                },
                modifier = Modifier.background(
                    if (isSelected) AccentBlue.copy(alpha = 0.12f) else Color.Transparent
                )
            )
        }
    }
}
