package com.pablo.paper.ui.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pablo.paper.domain.model.ViewMode
import com.pablo.paper.ui.theme.BorderSubtle
import com.pablo.paper.ui.theme.TextSecondary

@Composable
fun PageStatusBadge(
    currentPage: Int,
    pageCount: Int,
    viewMode: ViewMode,
    modifier: Modifier = Modifier
) {
    val modeText = when (viewMode) {
        ViewMode.FULL_PAGE -> "Página completa"
        ViewMode.FIT_WIDTH -> "Ajustar ancho"
        ViewMode.ACTUAL_SIZE -> "100% Real"
        ViewMode.CONTINUOUS_SCROLL -> "Scroll continuo"
        ViewMode.TWO_PAGE -> "Doble página (Libro)"
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = Color(0xEEFFFFFF),
        border = BorderStroke(1.dp, BorderSubtle),
        modifier = modifier
            .statusBarsPadding()
            .padding(top = 70.dp, end = 28.dp)
            .shadow(2.dp, RoundedCornerShape(6.dp))
    ) {
        Box(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)) {
            Text(
                text = "Page $currentPage/$pageCount · $modeText",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
            )
        }
    }
}
