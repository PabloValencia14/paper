package com.pablo.paper.ui.library

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pablo.paper.R
import com.pablo.paper.ui.theme.AccentBlue
import com.pablo.paper.ui.theme.AccentBlueLight
import com.pablo.paper.ui.theme.BorderSubtle
import com.pablo.paper.ui.theme.BorderSubtleDark
import com.pablo.paper.ui.theme.SurfaceCard
import com.pablo.paper.ui.theme.SurfaceCardDark
import com.pablo.paper.ui.theme.TextPrimary
import com.pablo.paper.ui.theme.TextPrimaryDark
import com.pablo.paper.ui.theme.TextSecondary
import com.pablo.paper.ui.theme.TextSecondaryDark

@Composable
fun LibraryHeader(
    documentCount: Int,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onOpenDocumentsClicked: () -> Unit,
    onSettingsClicked: () -> Unit = {},
    isDarkMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val textPrimary = if (isDarkMode) TextPrimaryDark else TextPrimary
    val textSecondary = if (isDarkMode) TextSecondaryDark else TextSecondary
    val surface = if (isDarkMode) SurfaceCardDark else SurfaceCard
    val border = if (isDarkMode) BorderSubtleDark else BorderSubtle

    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 32.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFFE6F0FC),
                border = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.16f)),
                modifier = Modifier.size(58.dp)
            ) {
                androidx.compose.foundation.Image(
                    painter = painterResource(R.drawable.paper_brand_mark_v1),
                    contentDescription = "Logotipo de Paper",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.padding(5.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = "PAPER",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = AccentBlueLight,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.6.sp
                    )
                )
                Text(
                    text = "Tu mesa de lectura",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp,
                        letterSpacing = (-0.7).sp
                    )
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Surface(
                shape = RoundedCornerShape(50),
                color = AccentBlue.copy(alpha = if (isDarkMode) 0.28f else 0.10f)
            ) {
                Text(
                    text = "$documentCount PDF",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = if (isDarkMode) Color(0xFFB8D8F7) else AccentBlue,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = surface,
                border = BorderStroke(1.dp, border),
                modifier = Modifier.width(286.dp).height(46.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Search, "Buscar", tint = textSecondary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(9.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChanged,
                        singleLine = true,
                        cursorBrush = SolidColor(AccentBlue),
                        textStyle = TextStyle(color = textPrimary, fontSize = 14.sp),
                        modifier = Modifier.weight(1f),
                        decorationBox = { input ->
                            if (searchQuery.isBlank()) Text("Buscar en tu biblioteca", color = textSecondary, fontSize = 13.sp)
                            input()
                        }
                    )
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { onSearchQueryChanged("") }, modifier = Modifier.size(26.dp)) {
                            Icon(Icons.Rounded.Close, "Limpiar", tint = textSecondary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Surface(
                onClick = onSettingsClicked,
                shape = RoundedCornerShape(14.dp),
                color = surface,
                border = BorderStroke(1.dp, border),
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Settings, "Configuración", tint = textSecondary, modifier = Modifier.size(20.dp))
                }
            }

            Row(
                modifier = Modifier
                    .height(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(AccentBlue)
                    .clickable(onClick = onOpenDocumentsClicked)
                    .padding(horizontal = 17.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Rounded.FolderOpen, null, tint = Color.White, modifier = Modifier.size(19.dp))
                Text(
                    stringResource(R.string.open_documents),
                    style = MaterialTheme.typography.labelLarge.copy(color = Color.White, fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
