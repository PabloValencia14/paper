package com.pablo.paper.ui.reader

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pablo.paper.ui.theme.AccentBlue
import com.pablo.paper.ui.theme.BorderSubtle
import com.pablo.paper.ui.theme.SurfaceToolbar
import com.pablo.paper.ui.theme.TextPrimary
import com.pablo.paper.ui.theme.TextSecondary
import com.pablo.paper.ui.theme.ToolbarShape

import com.pablo.paper.ui.theme.BorderSubtleDark
import com.pablo.paper.ui.theme.SurfaceToolbarDark
import com.pablo.paper.ui.theme.TextPrimaryDark
import com.pablo.paper.ui.theme.TextSecondaryDark

@Composable
fun SearchOverlay(
    query: String,
    matchCount: Int,
    currentMatchIndex: Int,
    isSearching: Boolean = false,
    isDarkMode: Boolean = false,
    onQueryChanged: (String) -> Unit,
    onNextMatch: () -> Unit,
    onPreviousMatch: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor = if (isDarkMode) TextPrimaryDark else TextPrimary
    val textSecColor = if (isDarkMode) TextSecondaryDark else TextSecondary

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 6.dp, end = 16.dp, bottom = 0.dp),
        contentAlignment = Alignment.Center
    ) {
        com.pablo.paper.ui.common.LiquidGlassSurface(
            modifier = Modifier
                .width(520.dp)
                .height(48.dp),
            shape = RoundedCornerShape(18.dp),
            isDarkMode = isDarkMode,
            elevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = "Buscar",
                        tint = AccentBlue,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    BasicTextField(
                        value = query,
                        onValueChange = onQueryChanged,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { onNextMatch() }),
                        cursorBrush = SolidColor(AccentBlue),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = textColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        decorationBox = { innerTextField ->
                            if (query.isEmpty()) {
                                Text(
                                    text = "Buscar en el documento (OCR)...",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = textSecColor.copy(alpha = 0.6f),
                                        fontSize = 14.sp
                                    )
                                )
                            }
                            innerTextField()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (query.isNotEmpty()) {
                    val statusText = when {
                        isSearching && matchCount == 0 -> "Buscando..."
                        matchCount > 0 -> "$currentMatchIndex de $matchCount"
                        else -> "Sin resultados"
                    }

                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (matchCount > 0) AccentBlue else textSecColor,
                            fontWeight = if (matchCount > 0) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = onPreviousMatch,
                        enabled = matchCount > 0,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowUp,
                            contentDescription = "Anterior coincidencia",
                            tint = if (matchCount > 0) textColor else textSecColor.copy(alpha = 0.3f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onNextMatch,
                        enabled = matchCount > 0,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowDown,
                            contentDescription = "Siguiente coincidencia",
                            tint = if (matchCount > 0) textColor else textSecColor.copy(alpha = 0.3f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Cerrar búsqueda",
                        tint = textSecColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
