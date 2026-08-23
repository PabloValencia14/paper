package com.pablo.paper.ui.ink

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pablo.paper.domain.model.InkTool
import com.pablo.paper.ui.theme.BorderSubtle
import com.pablo.paper.ui.theme.SurfaceToolbar
import com.pablo.paper.ui.theme.TextSecondary

@Composable
fun ToolInfoBanner(
    tool: InkTool,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = SurfaceToolbar.copy(alpha = 0.95f),
            border = BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.shadow(4.dp, RoundedCornerShape(8.dp))
        ) {
            Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                Text(
                    text = tool.tooltipMessage,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = TextSecondary
                    )
                )
            }
        }
    }
}
