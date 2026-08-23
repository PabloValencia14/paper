package com.pablo.paper.ui.library

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Create
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.NoteAdd
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.pablo.paper.ui.common.LiquidGlassCard
import com.pablo.paper.ui.theme.AccentBlue
import com.pablo.paper.ui.theme.AccentBlueLight
import com.pablo.paper.ui.theme.TextPrimary
import com.pablo.paper.ui.theme.TextPrimaryDark
import com.pablo.paper.ui.theme.TextSecondary
import com.pablo.paper.ui.theme.TextSecondaryDark

@Composable
fun QuickActionsRow(
    onImportPdf: () -> Unit,
    onNewNotebook: () -> Unit,
    onAiAssistant: () -> Unit,
    onBookmarks: () -> Unit,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        QuickActionCard(
            title = "Importar PDF",
            subtitle = "Abrir documento",
            icon = Icons.Rounded.FolderOpen,
            iconColor = AccentBlueLight,
            iconBg = AccentBlue.copy(alpha = 0.15f),
            isDarkMode = isDarkMode,
            onClick = onImportPdf,
            modifier = Modifier.weight(1f)
        )

        QuickActionCard(
            title = "Nueva Libreta",
            subtitle = "Apuntes en blanco",
            icon = Icons.Rounded.NoteAdd,
            iconColor = Color(0xFF10B981),
            iconBg = Color(0xFF10B981).copy(alpha = 0.15f),
            isDarkMode = isDarkMode,
            onClick = onNewNotebook,
            modifier = Modifier.weight(1f)
        )

        QuickActionCard(
            title = "Asistente IA",
            subtitle = "Resúmenes & Dots3",
            icon = Icons.Rounded.AutoAwesome,
            iconColor = Color(0xFFA78BFA),
            iconBg = Color(0xFF8B5CF6).copy(alpha = 0.15f),
            isDarkMode = isDarkMode,
            onClick = onAiAssistant,
            modifier = Modifier.weight(1f)
        )

        QuickActionCard(
            title = "Marcadores",
            subtitle = "Páginas guardadas",
            icon = Icons.Rounded.Bookmark,
            iconColor = Color(0xFFFFB300),
            iconBg = Color(0xFFFFB300).copy(alpha = 0.15f),
            isDarkMode = isDarkMode,
            onClick = onBookmarks,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    iconBg: Color,
    isDarkMode: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textPrimary = if (isDarkMode) TextPrimaryDark else TextPrimary
    val textSec = if (isDarkMode) TextSecondaryDark else TextSecondary

    LiquidGlassCard(
        modifier = modifier.clip(RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        isDarkMode = isDarkMode,
        elevation = 6.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.size(12.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = textPrimary,
                        fontSize = 14.sp
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = textSec,
                        fontSize = 11.5.sp
                    )
                )
            }
        }
    }
}
