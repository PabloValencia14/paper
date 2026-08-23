package com.pablo.paper.ui.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pablo.paper.R
import com.pablo.paper.domain.model.ViewMode
import com.pablo.paper.ui.theme.AccentBlue
import com.pablo.paper.ui.theme.SelectedControlBackground
import com.pablo.paper.ui.theme.SurfaceCard
import com.pablo.paper.ui.theme.TextPrimary

@Composable
fun ViewModeDropdown(
    expanded: Boolean,
    currentMode: ViewMode,
    onDismissRequest: () -> Unit,
    onSelectMode: (ViewMode) -> Unit,
    modifier: Modifier = Modifier
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier
            .width(160.dp)
            .background(SurfaceCard)
    ) {
        val modes = listOf(
            ViewMode.FULL_PAGE to R.string.full_page,
            ViewMode.FIT_WIDTH to R.string.fit_width,
            ViewMode.CONTINUOUS_SCROLL to R.string.continuous_scroll,
            ViewMode.TWO_PAGE to R.string.two_page
        )

        modes.forEach { (mode, stringRes) ->
            val isSelected = mode == currentMode
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(stringRes),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) AccentBlue else TextPrimary,
                            fontSize = 14.sp
                        )
                    )
                },
                onClick = {
                    onSelectMode(mode)
                    onDismissRequest()
                },
                modifier = Modifier.background(
                    if (isSelected) SelectedControlBackground else SurfaceCard
                )
            )
        }
    }
}
