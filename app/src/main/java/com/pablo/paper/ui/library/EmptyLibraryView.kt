package com.pablo.paper.ui.library

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pablo.paper.R
import com.pablo.paper.ui.theme.AccentBlue
import com.pablo.paper.ui.theme.BorderSubtle
import com.pablo.paper.ui.theme.BorderSubtleDark
import com.pablo.paper.ui.theme.SurfaceCard
import com.pablo.paper.ui.theme.SurfaceCardDark
import com.pablo.paper.ui.theme.TextPrimary
import com.pablo.paper.ui.theme.TextPrimaryDark
import com.pablo.paper.ui.theme.TextSecondary
import com.pablo.paper.ui.theme.TextSecondaryDark

@Composable
fun EmptyLibraryView(
    onOpenDocumentsClicked: () -> Unit,
    isDarkMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val surface = if (isDarkMode) SurfaceCardDark else SurfaceCard
    val border = if (isDarkMode) BorderSubtleDark else BorderSubtle
    val primary = if (isDarkMode) TextPrimaryDark else TextPrimary
    val secondary = if (isDarkMode) TextSecondaryDark else TextSecondary

    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = RoundedCornerShape(30.dp),
            color = surface,
            border = BorderStroke(1.dp, border),
            shadowElevation = 3.dp,
            modifier = Modifier.widthIn(max = 510.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 54.dp, vertical = 42.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                androidx.compose.foundation.Image(
                    painter = painterResource(R.drawable.paper_brand_mark_v1),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(124.dp)
                )
                Spacer(modifier = Modifier.height(22.dp))
                Text(
                    text = "Tu biblioteca empieza con una página",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 24.sp,
                        lineHeight = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = primary
                    ),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Importa un PDF para leer, subrayar y mantener tus ideas junto al documento.",
                    style = MaterialTheme.typography.bodyLarge.copy(color = secondary),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(26.dp))
                Button(
                    onClick = onOpenDocumentsClicked,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Icon(Icons.Rounded.FolderOpen, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(stringResource(R.string.open_documents), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
