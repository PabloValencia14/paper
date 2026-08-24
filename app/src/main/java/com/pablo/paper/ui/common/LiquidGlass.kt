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
 * Clean, modern elevated surface for toolbars, dialogs and floating panels.
 */
@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    isDarkMode: Boolean = false,
    elevation: Dp = 6.dp,
    borderAlpha: Float = 1.0f,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val baseSurfaceColor = if (isDarkMode) {
        Color(0xFF1B2232)
    } else {
        Color(0xFFFFFFFF)
    }

    val borderStroke = BorderStroke(
        1.dp,
        if (isDarkMode) Color(0x28FFFFFF) else Color(0x18000000)
    )

    val shadowSpotColor = if (isDarkMode) Color.Black.copy(alpha = 0.5f) else Color(0x18000000)
    val shadowAmbientColor = if (isDarkMode) Color.Black.copy(alpha = 0.3f) else Color(0x0C000000)

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
            .background(baseSurfaceColor)
            .border(
                border = borderStroke,
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
    elevation: Dp = 12.dp,
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
 * Liquid Glass Dialog / Modal container wrapping BasicAlertDialog with full clean styling.
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
            elevation = 16.dp,
            content = content
        )
    }
}

/**
 * Clean modern button.
 */
@Composable
fun LiquidGlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    isDarkMode: Boolean = false,
    shape: Shape = RoundedCornerShape(12.dp),
    accentColor: Color = AccentBlue,
    contentPadding: androidx.compose.foundation.layout.PaddingValues = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val surfaceColor = when {
        isSelected -> if (isDarkMode) accentColor.copy(alpha = 0.28f) else accentColor.copy(alpha = 0.14f)
        isPressed -> if (isDarkMode) Color(0x22FFFFFF) else Color(0x10000000)
        isDarkMode -> Color(0x12FFFFFF)
        else -> Color(0x06000000)
    }

    val borderStroke = when {
        isSelected -> BorderStroke(1.dp, accentColor)
        isDarkMode -> BorderStroke(1.dp, Color(0x1EFFFFFF))
        else -> BorderStroke(1.dp, Color(0x12000000))
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = if (isSelected) 3.dp else if (isPressed) 1.dp else 1.dp,
                shape = shape,
                spotColor = if (isSelected) accentColor.copy(alpha = 0.3f) else Color(0x10000000)
            )
            .clip(shape)
            .background(surfaceColor)
            .border(borderStroke, shape)
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
 * Clean modern Icon Button (for toolbars and action docks).
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
    size: Dp = 34.dp,
    iconSize: Dp = 18.dp,
    shape: Shape = RoundedCornerShape(10.dp)
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
        isSelected -> if (isDarkMode) accentColor.copy(alpha = 0.28f) else accentColor.copy(alpha = 0.14f)
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
