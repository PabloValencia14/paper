package com.pablo.paper.ui.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.FitScreen
import androidx.compose.material.icons.rounded.FormatSize
import androidx.compose.material.icons.rounded.Grain
import androidx.compose.material.icons.rounded.GridOn
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Reorder
import androidx.compose.material.icons.rounded.Texture
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pablo.paper.domain.model.PaperColor
import com.pablo.paper.domain.model.PaperTexture
import com.pablo.paper.ui.common.LiquidGlassDialog
import com.pablo.paper.ui.common.LiquidGlassSurface
import com.pablo.paper.ui.theme.AccentBlue
import com.pablo.paper.ui.theme.AccentBlueLight

@Composable
fun PaperCustomizerDialog(
    selectedPaperColor: PaperColor,
    selectedPaperTexture: PaperTexture,
    selectedPaperTexturePoints: Float = 24f,
    isSeamlessCanvas: Boolean,
    isDarkMode: Boolean,
    onSelectPaperColor: (PaperColor) -> Unit,
    onSelectPaperTexture: (PaperTexture) -> Unit,
    onSelectPaperTexturePoints: (Float) -> Unit = {},
    onToggleSeamlessCanvas: () -> Unit,
    onDismiss: () -> Unit
) {
    LiquidGlassDialog(
        onDismissRequest = onDismiss,
        isDarkMode = isDarkMode,
        shape = RoundedCornerShape(24.dp)
    ) {
        val textColor = if (isDarkMode) Color(0xFFF0F0F5) else Color(0xFF1C1C1E)
        val textSecColor = if (isDarkMode) Color(0xFFA0A0AB) else Color(0xFF70707B)

        Column(
            modifier = Modifier
                .width(600.dp)
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LiquidGlassSurface(
                        isDarkMode = isDarkMode,
                        shape = CircleShape,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(40.dp)) {
                            Icon(
                                imageVector = Icons.Rounded.ColorLens,
                                contentDescription = null,
                                tint = AccentBlue,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Personalización de Papel",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                                fontSize = 18.sp
                            )
                        )
                        Text(
                            text = "Ajusta el color, textura, escala y vista inmersiva",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = textSecColor,
                                fontSize = 12.sp
                            )
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Cerrar",
                        tint = textSecColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SECTION 1: SEAMLESS FULL SCREEN MODE SWITCH
            LiquidGlassSurface(
                isDarkMode = isDarkMode,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isSeamlessCanvas) AccentBlue.copy(alpha = 0.2f) else (if (isDarkMode) Color(0x30FFFFFF) else Color(0x15000000))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.FitScreen,
                                contentDescription = null,
                                tint = if (isSeamlessCanvas) AccentBlue else textSecColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Lienzo Continuo sin Bordes",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = textColor,
                                    fontSize = 14.sp
                                )
                            )
                            Text(
                                text = "Funde la página con el fondo de la pantalla",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = textSecColor,
                                    fontSize = 11.5.sp
                                )
                            )
                        }
                    }

                    Switch(
                        checked = isSeamlessCanvas,
                        onCheckedChange = { onToggleSeamlessCanvas() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = AccentBlue,
                            uncheckedThumbColor = textSecColor,
                            uncheckedTrackColor = if (isDarkMode) Color(0xFF333338) else Color(0xFFE0E0E6)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SECTION 2: PAPER COLOR SWATCHES
            Text(
                text = "Color del Papel",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    fontSize = 14.sp
                )
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PaperColor.entries.forEach { paperColor ->
                    val isSelected = selectedPaperColor == paperColor
                    val swatchColor = Color(if (isDarkMode || paperColor.isDarkPalette) paperColor.darkColor else paperColor.lightColor)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelectPaperColor(paperColor) }
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(swatchColor)
                                .border(
                                    width = if (isSelected) 3.dp else 1.2.dp,
                                    color = if (isSelected) AccentBlue else (if (isDarkMode) Color.White.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.15f)),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = if (paperColor.isDarkPalette || (isDarkMode && paperColor != PaperColor.WHITE)) Color.White else Color.Black,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = paperColor.displayName,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) AccentBlue else textSecColor,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // SECTION 3: PAPER TEXTURE SELECTOR
            Text(
                text = "Textura del Papel",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    fontSize = 14.sp
                )
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PaperTexture.entries.forEach { texture ->
                    val isSelected = selectedPaperTexture == texture
                    val textureIcon: ImageVector = when (texture) {
                        PaperTexture.SMOOTH -> Icons.Rounded.Layers
                        PaperTexture.FINE_GRAIN -> Icons.Rounded.Grain
                        PaperTexture.DOT_GRID -> Icons.Rounded.Texture
                        PaperTexture.GRID -> Icons.Rounded.GridOn
                        PaperTexture.LINED -> Icons.Rounded.Reorder
                        PaperTexture.PARCHMENT -> Icons.Rounded.Grain
                        PaperTexture.ISOMETRIC -> Icons.Rounded.GridOn
                    }

                    LiquidGlassSurface(
                        isDarkMode = isDarkMode,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .width(115.dp)
                            .clickable { onSelectPaperTexture(texture) }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isSelected) AccentBlue.copy(alpha = if (isDarkMode) 0.25f else 0.12f) else Color.Transparent)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) AccentBlue else Color.Transparent,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) AccentBlue else (if (isDarkMode) Color(0x30FFFFFF) else Color(0x15000000))),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = textureIcon,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else textColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = texture.displayName,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                    color = if (isSelected) (if (isDarkMode) AccentBlueLight else AccentBlue) else textColor,
                                    fontSize = 12.sp
                                )
                            )
                            Text(
                                text = texture.description,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = textSecColor,
                                    fontSize = 9.5.sp,
                                    lineHeight = 12.sp
                                ),
                                maxLines = 2
                            )
                        }
                    }
                }
            }

            // SECTION 4: TEXTURE SCALE / DENSITY SELECTOR (if texture is not smooth)
            if (selectedPaperTexture != PaperTexture.SMOOTH) {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tamaño y Separación de Pauta",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            fontSize = 14.sp
                        )
                    )
                    Text(
                        text = "${selectedPaperTexturePoints.toInt()} pt",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = AccentBlue,
                            fontSize = 13.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Quick preset buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "Fina (16pt)" to 16f,
                        "Estándar (24pt)" to 24f,
                        "Amplia (32pt)" to 32f,
                        "Grande (44pt)" to 44f
                    ).forEach { (label, pt) ->
                        val isPresetSelected = kotlin.math.abs(selectedPaperTexturePoints - pt) < 2f
                        LiquidGlassSurface(
                            isDarkMode = isDarkMode,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onSelectPaperTexturePoints(pt) }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (isPresetSelected) AccentBlue.copy(alpha = 0.2f) else Color.Transparent)
                                    .border(
                                        width = if (isPresetSelected) 1.5.dp else 0.5.dp,
                                        color = if (isPresetSelected) AccentBlue else Color.Transparent,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = if (isPresetSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isPresetSelected) (if (isDarkMode) AccentBlueLight else AccentBlue) else textColor,
                                        fontSize = 10.5.sp
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Slider(
                    value = selectedPaperTexturePoints,
                    onValueChange = { onSelectPaperTexturePoints(it) },
                    valueRange = 12f..52f,
                    steps = 20,
                    colors = SliderDefaults.colors(
                        thumbColor = AccentBlue,
                        activeTrackColor = AccentBlue,
                        inactiveTrackColor = if (isDarkMode) Color(0xFF383842) else Color(0xFFE0E0E6)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
