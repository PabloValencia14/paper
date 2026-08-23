package com.pablo.paper.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.pablo.paper.domain.model.Document
import com.pablo.paper.ui.common.LiquidGlassButton
import com.pablo.paper.ui.common.LiquidGlassCard
import com.pablo.paper.ui.theme.AccentBlue
import com.pablo.paper.ui.theme.AccentBlueLight
import com.pablo.paper.ui.theme.AccentIndigo
import com.pablo.paper.ui.theme.TextPrimary
import com.pablo.paper.ui.theme.TextPrimaryDark
import com.pablo.paper.ui.theme.TextSecondary
import com.pablo.paper.ui.theme.TextSecondaryDark
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ContinueReadingHero(
    document: Document,
    onContinueReading: () -> Unit,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier
) {
    val textPrimary = if (isDarkMode) TextPrimaryDark else TextPrimary
    val textSec = if (isDarkMode) TextSecondaryDark else TextSecondary
    val thumbnailBg = if (isDarkMode) Color(0xFF131722) else Color(0xFFE2E8F0)

    val thumbnailDarkMatrix = remember {
        ColorMatrix(
            floatArrayOf(
                -0.85f, 0f, 0f, 0f, 235f,
                0f, -0.85f, 0f, 0f, 235f,
                0f, 0f, -0.85f, 0f, 235f,
                0f, 0f, 0f, 1f, 0f
            )
        )
    }

    val formattedDate = remember(document.lastOpened) {
        val sdf = SimpleDateFormat("d 'de' MMMM, HH:mm", Locale.getDefault())
        sdf.format(Date(document.lastOpened))
    }

    LiquidGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp)),
        isDarkMode = isDarkMode,
        elevation = 10.dp,
        onClick = onContinueReading
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail Preview Container
            Box(
                modifier = Modifier
                    .width(110.dp)
                    .height(150.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(thumbnailBg)
                    .shadow(6.dp, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (!document.thumbnailPath.isNullOrEmpty() && File(document.thumbnailPath).exists()) {
                    AsyncImage(
                        model = File(document.thumbnailPath),
                        contentDescription = document.name,
                        contentScale = ContentScale.Crop,
                        colorFilter = if (isDarkMode) ColorFilter.colorMatrix(thumbnailDarkMatrix) else null,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.PictureAsPdf,
                        contentDescription = null,
                        tint = AccentBlueLight.copy(alpha = 0.8f),
                        modifier = Modifier.size(46.dp)
                    )
                }

                // Page badge overlay on thumbnail
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.75f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "pág. ${document.currentPage}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.width(22.dp))

            // Document Details & Progress
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                // Category Chip
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AccentBlue.copy(alpha = if (isDarkMode) 0.25f else 0.12f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.MenuBook,
                                contentDescription = null,
                                tint = AccentBlueLight,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Continuar Lectura",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AccentBlueLight,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "Leído el $formattedDate",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = textSec,
                            fontSize = 12.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Title
                Text(
                    text = document.name,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = textPrimary,
                        fontSize = 20.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Progress Bar
                LinearProgressIndicator(
                    progress = { document.progress.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = AccentBlueLight,
                    trackColor = if (isDarkMode) Color(0xFF282D37) else Color(0xFFE2E6ED),
                    strokeCap = StrokeCap.Round
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Página ${document.currentPage} de ${document.pageCount} · ${document.progressPercentage}% completado",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = textSec,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Action Button: Continuar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .height(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(AccentBlue, AccentIndigo)
                                )
                            )
                            .clickable(onClick = onContinueReading)
                            .shadow(6.dp, RoundedCornerShape(12.dp), spotColor = AccentBlue)
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Continuar",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
