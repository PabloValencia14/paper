package com.pablo.paper.ui.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

object BionicReadingFormatter {

    /**
     * Converts standard prose text into a Bionic Reading AnnotatedString where
     * the initial fixation letters of each word are styled with bold weight.
     */
    fun formatToBionic(
        text: String,
        textColor: Color = Color.Unspecified,
        boldColor: Color = Color.Unspecified
    ): AnnotatedString {
        return buildAnnotatedString {
            val wordsAndSpaces = Regex("(\\s+|[^\\s]+)").findAll(text)

            for (match in wordsAndSpaces) {
                val token = match.value
                if (token.isBlank()) {
                    append(token)
                } else {
                    val len = token.length
                    val boldLen = when {
                        len == 1 -> 1
                        len in 2..3 -> 1
                        len in 4..6 -> 2
                        len in 7..9 -> 3
                        else -> (len * 0.42f).toInt().coerceAtLeast(3)
                    }

                    val prefix = token.take(boldLen)
                    val suffix = token.drop(boldLen)

                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Black,
                            color = if (boldColor != Color.Unspecified) boldColor else textColor
                        )
                    ) {
                        append(prefix)
                    }

                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Normal,
                            color = if (textColor != Color.Unspecified) textColor else Color.Unspecified
                        )
                    ) {
                        append(suffix)
                    }
                }
            }
        }
    }
}

@Composable
fun BionicText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 16.sp,
    lineHeight: TextUnit = 24.sp,
    color: Color = MaterialTheme.colorScheme.onSurface,
    enabled: Boolean = true
) {
    if (!enabled) {
        Text(
            text = text,
            modifier = modifier,
            fontSize = fontSize,
            lineHeight = lineHeight,
            color = color
        )
    } else {
        val bionicString = BionicReadingFormatter.formatToBionic(
            text = text,
            textColor = color,
            boldColor = color
        )
        Text(
            text = bionicString,
            modifier = modifier,
            fontSize = fontSize,
            lineHeight = lineHeight
        )
    }
}
