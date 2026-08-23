package com.pablo.paper.ui.library

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Create
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.TrendingUp
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
import com.pablo.paper.domain.model.Document
import com.pablo.paper.ui.common.LiquidGlassCard
import com.pablo.paper.ui.theme.AccentBlue
import com.pablo.paper.ui.theme.AccentBlueLight
import com.pablo.paper.ui.theme.TextPrimary
import com.pablo.paper.ui.theme.TextPrimaryDark
import com.pablo.paper.ui.theme.TextSecondary
import com.pablo.paper.ui.theme.TextSecondaryDark

@Composable
fun LibraryStatsWidget(
    documents: List<Document>,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier
) {
    val totalDocs = documents.size
    val totalPages = documents.sumOf { it.pageCount }
    val pagesRead = documents.sumOf { it.currentPage }
    val avgProgress = if (totalDocs > 0) {
        (documents.sumOf { it.progressPercentage } / totalDocs)
    } else 0

    val textPrimary = if (isDarkMode) TextPrimaryDark else TextPrimary
    val textSec = if (isDarkMode) TextSecondaryDark else TextSecondary

    LiquidGlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        isDarkMode = isDarkMode,
        elevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatItem(
                label = "Documentos",
                value = "$totalDocs",
                icon = Icons.Rounded.Description,
                iconColor = AccentBlueLight,
                isDarkMode = isDarkMode
            )

            StatDivider(isDarkMode)

            StatItem(
                label = "Páginas leídas",
                value = "$pagesRead / $totalPages",
                icon = Icons.Rounded.AutoStories,
                iconColor = Color(0xFF10B981),
                isDarkMode = isDarkMode
            )

            StatDivider(isDarkMode)

            StatItem(
                label = "Progreso medio",
                value = "$avgProgress%",
                icon = Icons.Rounded.TrendingUp,
                iconColor = Color(0xFFFFB300),
                isDarkMode = isDarkMode
            )
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    isDarkMode: Boolean
) {
    val textPrimary = if (isDarkMode) TextPrimaryDark else TextPrimary
    val textSec = if (isDarkMode) TextSecondaryDark else TextSecondary

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = if (isDarkMode) 0.20f else 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = textPrimary,
                    fontSize = 15.sp
                )
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = textSec,
                    fontSize = 11.sp
                )
            )
        }
    }
}

@Composable
private fun StatDivider(isDarkMode: Boolean) {
    Box(
        modifier = Modifier
            .height(28.dp)
            .width(1.dp)
            .background(if (isDarkMode) Color(0x25FFFFFF) else Color(0x18000000))
    )
}
