package com.pablo.paper.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pablo.paper.ui.theme.AccentBlue
import com.pablo.paper.ui.theme.AccentBlueLight
import com.pablo.paper.ui.theme.TextPrimary
import com.pablo.paper.ui.theme.TextPrimaryDark
import com.pablo.paper.ui.theme.TextSecondary
import com.pablo.paper.ui.theme.TextSecondaryDark

/**
 * High-end Liquid Glass surface with:
 * 1. Translucent frosted glass tint
 * 2. Water / crystal specular reflection sheen on the upper half
 * 3. Prismatic specular dual-edge border
 * 4. Ambient depth shadow
 */
@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    isDarkMode: Boolean = false,
    elevation: Dp = 8.dp,
    borderAlpha: Float = 1.0f,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    // Glass base tints (crystal frosted translucent)
    val baseGlassColor = if (isDarkMode) {
        Color(0xEA141824) // Deep obsidian crystal glass
    } else {
        Color(0xF0FFFFFF) // Frosted ice white glass
    }

    // Specular border gradient (bright reflection at top-left to subtle shadow at bottom-right)
    val specularBorderBrush = if (isDarkMode) {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.55f * borderAlpha),
                Color(0x403B82F6),
                Color(0x10FFFFFF),
                Color(0x40000000)
            ),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.95f * borderAlpha),
                Color.White.copy(alpha = 0.45f * borderAlpha),
                Color(0x203B82F6),
                Color(0x25000000)
            ),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
    }

    val shadowSpotColor = if (isDarkMode) Color.Black.copy(alpha = 0.7f) else Color(0x30000000)
    val shadowAmbientColor = if (isDarkMode) Color(0x352563EB) else Color(0x15000000)

    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = rememberRipple(color = AccentBlue),
            onClick = onClick
        )
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                spotColor = shadowSpotColor,
                ambientColor = shadowAmbientColor
            )
            .clip(shape)
            .background(baseGlassColor)
            // Liquid water & glass specular reflection layer
            .drawWithContent {
                drawContent()
                val w = size.width
                val h = size.height

                // Top glossy reflection gradient (simulating curved glass surface / water drop refraction)
                val glossBrush = Brush.verticalGradient(
                    colors = listOf(
                        if (isDarkMode) Color.White.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.45f),
                        if (isDarkMode) Color.White.copy(alpha = 0.04f) else Color.White.copy(alpha = 0.09f),
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = h * 0.52f
                )

                drawRect(
                    brush = glossBrush,
                    size = Size(w, h * 0.52f)
                )

                // Subtle bottom inner refraction glow
                val bottomGlowBrush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        if (isDarkMode) Color(0x183B82F6) else Color.White.copy(alpha = 0.25f)
                    ),
                    startY = h * 0.75f,
                    endY = h
                )
                drawRect(
                    brush = bottomGlowBrush,
                    topLeft = Offset(0f, h * 0.75f),
                    size = Size(w, h * 0.25f)
                )
            }
            .border(
                border = BorderStroke(1.dp, specularBorderBrush),
                shape = shape
            )
            .then(clickableModifier)
    ) {
        content()
    }
}

/**
 * Liquid Glass Panel specifically designed for sidebars, floating panels, and side sheets.
 */
@Composable
fun LiquidGlassPanel(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    isDarkMode: Boolean = false,
    elevation: Dp = 16.dp,
    content: @Composable BoxScope.() -> Unit
) {
    LiquidGlassSurface(
        modifier = modifier,
        shape = shape,
        isDarkMode = isDarkMode,
        elevation = elevation,
        content = content
    )
}

/**
 * Liquid Glass Dialog / Modal container wrapping BasicAlertDialog with full Liquid Glass styling.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiquidGlassDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    isDarkMode: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        LiquidGlassSurface(
            shape = shape,
            isDarkMode = isDarkMode,
            elevation = 24.dp,
            content = content
        )
    }
}

/**
 * Liquid Glass Button styled like an Aqua crystal / water capsule.
 */
@Composable
fun LiquidGlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    isDarkMode: Boolean = false,
    shape: Shape = RoundedCornerShape(14.dp),
    accentColor: Color = AccentBlue,
    contentPadding: androidx.compose.foundation.layout.PaddingValues = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 8.dp),
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val surfaceColor = when {
        isSelected -> if (isDarkMode) accentColor.copy(alpha = 0.32f) else accentColor.copy(alpha = 0.18f)
        isPressed -> if (isDarkMode) Color(0x40FFFFFF) else Color(0x30FFFFFF)
        isDarkMode -> Color(0x30FFFFFF)
        else -> Color(0x50FFFFFF)
    }

    val borderBrush = when {
        isSelected -> Brush.linearGradient(
            listOf(accentColor.copy(alpha = 0.9f), accentColor.copy(alpha = 0.4f))
        )
        isDarkMode -> Brush.linearGradient(
            listOf(Color.White.copy(alpha = 0.45f), Color.White.copy(alpha = 0.10f))
        )
        else -> Brush.linearGradient(
            listOf(Color.White.copy(alpha = 0.85f), Color(0x20000000))
        )
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = if (isSelected) 6.dp else if (isPressed) 2.dp else 4.dp,
                shape = shape,
                spotColor = if (isSelected) accentColor.copy(alpha = 0.4f) else Color(0x20000000)
            )
            .clip(shape)
            .background(surfaceColor)
            // Liquid water glossy lens reflection
            .drawWithContent {
                drawContent()
                val glossBrush = Brush.verticalGradient(
                    colors = listOf(
                        if (isDarkMode) Color.White.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.50f),
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = size.height * 0.5f
                )
                drawRect(brush = glossBrush, size = Size(size.width, size.height * 0.5f))
            }
            .border(BorderStroke(1.dp, borderBrush), shape)
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(color = accentColor),
                onClick = onClick
            )
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            content()
        }
    }
}

/**
 * Liquid Glass Icon Button (for toolbars and action docks).
 */
@Composable
fun LiquidGlassIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    enabled: Boolean = true,
    isDarkMode: Boolean = false,
    accentColor: Color = AccentBlue,
    size: Dp = 38.dp,
    iconSize: Dp = 18.dp,
    shape: Shape = RoundedCornerShape(12.dp)
) {
    val textPrimary = if (isDarkMode) TextPrimaryDark else TextPrimary
    val textSec = if (isDarkMode) TextSecondaryDark else TextSecondary

    val iconColor = when {
        !enabled -> textSec.copy(alpha = 0.35f)
        isSelected -> if (isDarkMode) Color.White else accentColor
        else -> textPrimary
    }

    val buttonBg = when {
        !enabled -> Color.Transparent
        isSelected -> if (isDarkMode) accentColor.copy(alpha = 0.40f) else accentColor.copy(alpha = 0.18f)
        else -> Color.Transparent
    }

    val borderStroke = when {
        !enabled -> null
        isSelected -> BorderStroke(1.dp, if (isDarkMode) accentColor else accentColor.copy(alpha = 0.6f))
        else -> null
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .then(if (isSelected) Modifier.background(buttonBg) else Modifier)
            .then(
                if (isSelected) {
                    Modifier.drawWithContent {
                        drawContent()
                        val w = this.size.width
                        val h = this.size.height
                        val gloss = Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.3f), Color.Transparent),
                            0f,
                            h * 0.5f
                        )
                        drawRect(gloss, size = Size(w, h * 0.5f))
                    }
                } else Modifier
            )
            .then(if (borderStroke != null) Modifier.border(borderStroke, shape) else Modifier)
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(color = accentColor),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconColor,
            modifier = Modifier.size(iconSize)
        )
    }
}

/**
 * Liquid Glass Card with water refraction highlights.
 */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    isDarkMode: Boolean = false,
    elevation: Dp = 8.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    LiquidGlassSurface(
        modifier = modifier,
        shape = shape,
        isDarkMode = isDarkMode,
        elevation = elevation,
        onClick = onClick,
        content = content
    )
}
