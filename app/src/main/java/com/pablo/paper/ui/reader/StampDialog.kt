package com.pablo.paper.ui.reader

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Approval
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pablo.paper.ui.common.LiquidGlassDialog
import com.pablo.paper.ui.theme.AccentBlue
import com.pablo.paper.ui.theme.TextPrimary
import com.pablo.paper.ui.theme.TextPrimaryDark
import com.pablo.paper.ui.theme.TextSecondary
import com.pablo.paper.ui.theme.TextSecondaryDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class StampTemplate(
    val title: String,
    val subtitle: String?,
    val color: Long
)

private fun getStampTemplates(): List<StampTemplate> {
    val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    return listOf(
        StampTemplate("APROBADO", dateStr, 0xFF34C759),
        StampTemplate("CONFIDENCIAL", "USO INTERNO", 0xFFFF3B30),
        StampTemplate("REVISADO", dateStr, 0xFF007AFF),
        StampTemplate("BORRADOR", "SUJETO A CAMBIOS", 0xFFFF9500),
        StampTemplate("RECHAZADO", dateStr, 0xFFE02424),
        StampTemplate("URGENTE", "ACCIÓN REQUERIDA", 0xFFFFCC00),
        StampTemplate("VISTO BUENO", dateStr, 0xFF5856D6),
        StampTemplate("FECHA", dateStr, 0xFF8E8E93)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StampDialog(
    pageNumber: Int,
    isDarkMode: Boolean = false,
    onStampSelected: (stampText: String, color: Long) -> Unit,
    onDismissRequest: () -> Unit
) {
    val textColor = if (isDarkMode) TextPrimaryDark else TextPrimary
    val textSecColor = if (isDarkMode) TextSecondaryDark else TextSecondary
    val stamps = getStampTemplates()

    LiquidGlassDialog(
        onDismissRequest = onDismissRequest,
        isDarkMode = isDarkMode,
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .width(540.dp)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Approval,
                        contentDescription = null,
                        tint = AccentBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "Insertar Sello Acrobat",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            ),
                            color = textColor
                        )
                        Text(
                            text = "Selecciona un sello para estampar en la página $pageNumber",
                            style = MaterialTheme.typography.bodySmall,
                            color = textSecColor
                        )
                    }
                }

                IconButton(onClick = onDismissRequest) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Cerrar",
                        tint = textSecColor
                    )
                }
            }

            // Grid of Stamps
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
                modifier = Modifier.height(280.dp)
            ) {
                items(stamps) { stamp ->
                    val stampColor = Color(stamp.color)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = stampColor.copy(alpha = if (isDarkMode) 0.18f else 0.10f),
                        border = BorderStroke(2.dp, stampColor.copy(alpha = 0.85f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .clickable {
                                val fullText = if (stamp.subtitle != null) "${stamp.title} · ${stamp.subtitle}" else stamp.title
                                onStampSelected(fullText, stamp.color)
                                onDismissRequest()
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(8.dp)
                                .rotate(-4f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = stamp.title,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.2.sp,
                                        fontSize = 13.sp
                                    ),
                                    color = stampColor
                                )
                                if (stamp.subtitle != null) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = stamp.subtitle,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        ),
                                        color = stampColor.copy(alpha = 0.85f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Cancel Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onDismissRequest,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancelar")
                }
            }
        }
    }
}
