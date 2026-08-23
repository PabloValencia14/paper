package com.pablo.paper.ui.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pablo.paper.ui.theme.AccentBlue
import com.pablo.paper.ui.theme.BorderSubtle
import com.pablo.paper.ui.theme.BorderSubtleDark
import com.pablo.paper.ui.theme.CanvasBackground
import com.pablo.paper.ui.theme.SurfaceToolbar
import com.pablo.paper.ui.theme.TextPrimary
import com.pablo.paper.ui.theme.TextPrimaryDark
import com.pablo.paper.ui.theme.TextSecondary
import com.pablo.paper.ui.theme.TextSecondaryDark

@Composable
fun PageNavigator(
    visible: Boolean,
    currentPage: Int,
    pageCount: Int,
    isDarkMode: Boolean = false,
    onPageSelected: (Int) -> Unit,
    onToggleOutline: () -> Unit = {},
    onTogglePageGrid: () -> Unit = {},
    onClose: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var inputPageText by remember(currentPage) { mutableStateOf(currentPage.toString()) }
    var sliderValue by remember(currentPage) { mutableFloatStateOf(currentPage.toFloat()) }

    val primaryTextColor = if (isDarkMode) TextPrimaryDark else TextPrimary
    val secondaryTextColor = if (isDarkMode) TextSecondaryDark else TextSecondary
    val inputBgColor = if (isDarkMode) Color(0x28FFFFFF) else CanvasBackground
    val subtleBorderColor = if (isDarkMode) BorderSubtleDark else BorderSubtle

    LaunchedEffect(currentPage) {
        sliderValue = currentPage.toFloat()
        inputPageText = currentPage.toString()
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            com.pablo.paper.ui.common.LiquidGlassSurface(
                modifier = Modifier
                    .widthIn(max = 560.dp)
                    .fillMaxWidth(0.85f),
                shape = RoundedCornerShape(20.dp),
                isDarkMode = isDarkMode,
                elevation = 16.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top Row: Page [21] of 43 · 48%       [-1] [+1] [Go]  [TOC] [Grid] [✕]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Page input label & box
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Page",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = primaryTextColor,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))

                            // Editable page number field
                            BasicTextField(
                                value = inputPageText,
                                onValueChange = { newText ->
                                    if (newText.length <= 5 && newText.all { it.isDigit() }) {
                                        inputPageText = newText
                                    }
                                },
                                textStyle = TextStyle(
                                    color = primaryTextColor,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Go
                                ),
                                keyboardActions = KeyboardActions(
                                    onGo = {
                                        focusManager.clearFocus()
                                        val parsed = inputPageText.toIntOrNull()
                                        if (parsed != null && parsed in 1..pageCount) {
                                            onPageSelected(parsed)
                                        }
                                    }
                                ),
                                singleLine = true,
                                modifier = Modifier
                                    .width(52.dp)
                                    .height(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(inputBgColor)
                                    .padding(horizontal = 6.dp, vertical = 7.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            val progressPct = if (pageCount > 0) ((currentPage.toFloat() / pageCount.toFloat()) * 100).toInt() else 0
                            Text(
                                text = "of $pageCount · $progressPct%",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = secondaryTextColor,
                                    fontSize = 13.sp
                                )
                            )
                        }

                        // Navigation and shortcut buttons
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // -1 button
                            OutlinedButton(
                                onClick = {
                                    if (currentPage > 1) onPageSelected(currentPage - 1)
                                },
                                enabled = currentPage > 1,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, subtleBorderColor),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = primaryTextColor
                                ),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text(
                                    text = "- 1",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }

                            // +1 button
                            OutlinedButton(
                                onClick = {
                                    if (currentPage < pageCount) onPageSelected(currentPage + 1)
                                },
                                enabled = currentPage < pageCount,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, subtleBorderColor),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = primaryTextColor
                                ),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text(
                                    text = "+ 1",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }

                            // Go button
                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    val parsed = inputPageText.toIntOrNull()
                                    if (parsed != null && parsed in 1..pageCount) {
                                        onPageSelected(parsed)
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AccentBlue,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text(
                                    text = "Go",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            // Shortcut to TOC
                            IconButton(onClick = onToggleOutline, modifier = Modifier.size(32.dp)) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                                    contentDescription = "Table of Contents",
                                    tint = primaryTextColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Shortcut to Grid
                            IconButton(onClick = onTogglePageGrid, modifier = Modifier.size(32.dp)) {
                                Icon(
                                    imageVector = Icons.Rounded.GridView,
                                    contentDescription = "Thumbnails",
                                    tint = primaryTextColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Close button
                            IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Close",
                                    tint = secondaryTextColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Bottom Row: 1 ━━━━━━━━━━━━━●━━━━━━━━━━━━ 43
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "1",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = secondaryTextColor,
                                fontSize = 12.sp
                            )
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Slider(
                            value = sliderValue,
                            onValueChange = { newValue ->
                                sliderValue = newValue
                                val targetPage = kotlin.math.round(newValue).toInt().coerceIn(1, pageCount)
                                inputPageText = targetPage.toString()
                                if (targetPage != currentPage) {
                                    onPageSelected(targetPage)
                                }
                            },
                            onValueChangeFinished = {
                                val targetPage = kotlin.math.round(sliderValue).toInt().coerceIn(1, pageCount)
                                if (targetPage != currentPage) {
                                    onPageSelected(targetPage)
                                }
                            },
                            valueRange = 1f..pageCount.toFloat().coerceAtLeast(1f),
                            steps = 0,
                            colors = SliderDefaults.colors(
                                thumbColor = AccentBlue,
                                activeTrackColor = AccentBlue,
                                inactiveTrackColor = subtleBorderColor
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = "$pageCount",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = secondaryTextColor,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }
        }
    }
}
