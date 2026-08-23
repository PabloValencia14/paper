package com.pablo.paper.ui.reader

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput

@Composable
fun EdgeNavigationOverlay(
    enabled: Boolean,
    onLeftEdgeTapped: () -> Unit,
    onRightEdgeTapped: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!enabled) return

    Box(modifier = modifier.fillMaxSize()) {
        // Left 18% Edge Target
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.18f)
                .align(Alignment.CenterStart)
                .pointerInput(Unit) {
                    detectTapGestures {
                        onLeftEdgeTapped()
                    }
                }
        )

        // Right 18% Edge Target
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.18f)
                .align(Alignment.CenterEnd)
                .pointerInput(Unit) {
                    detectTapGestures {
                        onRightEdgeTapped()
                    }
                }
        )
    }
}
