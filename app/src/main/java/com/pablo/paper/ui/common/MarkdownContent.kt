package com.pablo.paper.ui.common

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pablo.paper.ui.theme.AccentBlue
import com.pablo.paper.ui.theme.BorderSubtle
import com.pablo.paper.ui.theme.TextPrimary
import com.pablo.paper.ui.theme.TextPrimaryDark
import com.pablo.paper.ui.theme.TextSecondary

@Composable
fun MarkdownContent(
    markdown: String,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    isDarkMode: Boolean = isSystemInDarkTheme()
) {
    val context = LocalContext.current
    val parsedBlocks = remember(markdown) { parseMarkdownBlocks(markdown) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        parsedBlocks.forEach { block ->
            when (block) {
                is MarkdownBlock.Header -> {
                    val fontSize = when (block.level) {
                        1 -> 16.sp
                        2 -> 15.sp
                        else -> 14.sp
                    }
                    val color = if (block.level == 1) AccentBlue else textColor
                    Text(
                        text = renderInlineStyles(block.text, color),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = fontSize
                        ),
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )
                }
                is MarkdownBlock.Paragraph -> {
                    Text(
                        text = renderInlineStyles(block.text, textColor),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = textColor,
                            fontSize = 13.sp,
                            lineHeight = 19.sp
                        )
                    )
                }
                is MarkdownBlock.BulletItem -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp, top = 1.dp, bottom = 1.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = AccentBlue,
                                fontSize = 14.sp
                            ),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = renderInlineStyles(block.text, textColor),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = textColor,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                is MarkdownBlock.NumberedItem -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp, top = 1.dp, bottom = 1.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "${block.number}.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = AccentBlue,
                                fontSize = 12.sp
                            ),
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text(
                            text = renderInlineStyles(block.text, textColor),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = textColor,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                is MarkdownBlock.Quote -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(AccentBlue.copy(alpha = 0.08f))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(18.dp)
                                .background(AccentBlue, RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = renderInlineStyles(block.text, textColor),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontStyle = FontStyle.Italic,
                                color = textColor,
                                fontSize = 12.5.sp
                            )
                        )
                    }
                }
                is MarkdownBlock.MermaidDiagram -> {
                    MermaidDiagramCard(
                        mermaidCode = block.code,
                        isDarkMode = isDarkMode,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                is MarkdownBlock.MathEquation -> {
                    MathEquationCard(
                        latexFormula = block.formula,
                        isDarkMode = isDarkMode,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                is MarkdownBlock.CodeBlock -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF161822))
                            .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = block.language.ifBlank { "code" },
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TextSecondary,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                )
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .clickable {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = ClipData.newPlainText("Código", block.code)
                                            clipboard.setPrimaryClip(clip)
                                            Toast.makeText(context, "Código copiado", Toast.LENGTH_SHORT).show()
                                        }
                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.ContentCopy,
                                        contentDescription = "Copiar código",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Copiar",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = TextSecondary,
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = block.code,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFFE2E8F0),
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp
                                )
                            )
                        }
                    }
                }
                is MarkdownBlock.Divider -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(BorderSubtle)
                    )
                }
            }
        }
    }
}

sealed interface MarkdownBlock {
    data class Header(val level: Int, val text: String) : MarkdownBlock
    data class Paragraph(val text: String) : MarkdownBlock
    data class BulletItem(val text: String) : MarkdownBlock
    data class NumberedItem(val number: String, val text: String) : MarkdownBlock
    data class Quote(val text: String) : MarkdownBlock
    data class CodeBlock(val language: String, val code: String) : MarkdownBlock
    data class MermaidDiagram(val code: String) : MarkdownBlock
    data class MathEquation(val formula: String) : MarkdownBlock
    data object Divider : MarkdownBlock
}

private fun parseMarkdownBlocks(markdown: String): List<MarkdownBlock> {
    val lines = markdown.lines()
    val blocks = mutableListOf<MarkdownBlock>()
    var inCodeBlock = false
    var codeLang = ""
    val codeBuilder = StringBuilder()

    var inMathBlock = false
    val mathBuilder = StringBuilder()

    for (rawLine in lines) {
        val trimmed = rawLine.trim()

        // Handle ``` code blocks
        if (trimmed.startsWith("```")) {
            if (inCodeBlock) {
                val fullCode = codeBuilder.toString().trimEnd()
                val lang = codeLang.lowercase()
                when {
                    lang in listOf("mermaid", "uml", "plantuml", "sequence", "classdiagram", "flowchart") || isLikelyMermaid(fullCode) -> {
                        blocks.add(MarkdownBlock.MermaidDiagram(fullCode))
                    }
                    lang in listOf("math", "latex", "tex", "katex") -> {
                        blocks.add(MarkdownBlock.MathEquation(fullCode))
                    }
                    else -> {
                        blocks.add(MarkdownBlock.CodeBlock(codeLang, fullCode))
                    }
                }
                codeBuilder.clear()
                inCodeBlock = false
            } else {
                inCodeBlock = true
                codeLang = trimmed.removePrefix("```").trim()
            }
            continue
        }

        if (inCodeBlock) {
            if (codeBuilder.isNotEmpty()) codeBuilder.append("\n")
            codeBuilder.append(rawLine)
            continue
        }

        // Handle $$ math blocks
        if (trimmed.startsWith("$$")) {
            if (trimmed.endsWith("$$") && trimmed.length > 2) {
                // Single line $$ ... $$
                val formula = trimmed.removePrefix("$$").removeSuffix("$$").trim()
                if (formula.isNotEmpty()) {
                    blocks.add(MarkdownBlock.MathEquation(formula))
                }
                continue
            } else if (inMathBlock) {
                // Closing multiline $$
                val formula = mathBuilder.toString().trim()
                if (formula.isNotEmpty()) {
                    blocks.add(MarkdownBlock.MathEquation(formula))
                }
                mathBuilder.clear()
                inMathBlock = false
                continue
            } else {
                // Opening multiline $$
                inMathBlock = true
                val rest = trimmed.removePrefix("$$").trim()
                if (rest.isNotEmpty()) mathBuilder.append(rest)
                continue
            }
        }

        if (inMathBlock) {
            if (trimmed.endsWith("$$")) {
                val lastPart = trimmed.removeSuffix("$$").trim()
                if (lastPart.isNotEmpty()) {
                    if (mathBuilder.isNotEmpty()) mathBuilder.append("\n")
                    mathBuilder.append(lastPart)
                }
                val formula = mathBuilder.toString().trim()
                if (formula.isNotEmpty()) {
                    blocks.add(MarkdownBlock.MathEquation(formula))
                }
                mathBuilder.clear()
                inMathBlock = false
            } else {
                if (mathBuilder.isNotEmpty()) mathBuilder.append("\n")
                mathBuilder.append(rawLine)
            }
            continue
        }

        if (trimmed.isEmpty()) {
            continue
        }

        when {
            trimmed.startsWith("##### ") -> {
                blocks.add(MarkdownBlock.Header(5, trimmed.removePrefix("##### ").trim()))
            }
            trimmed.startsWith("#### ") -> {
                blocks.add(MarkdownBlock.Header(4, trimmed.removePrefix("#### ").trim()))
            }
            trimmed.startsWith("### ") -> {
                blocks.add(MarkdownBlock.Header(3, trimmed.removePrefix("### ").trim()))
            }
            trimmed.startsWith("## ") -> {
                blocks.add(MarkdownBlock.Header(2, trimmed.removePrefix("## ").trim()))
            }
            trimmed.startsWith("# ") -> {
                blocks.add(MarkdownBlock.Header(1, trimmed.removePrefix("# ").trim()))
            }
            trimmed.startsWith("> ") -> {
                blocks.add(MarkdownBlock.Quote(trimmed.removePrefix("> ").trim()))
            }
            trimmed.startsWith("---") || trimmed.startsWith("***") || trimmed.startsWith("___") -> {
                blocks.add(MarkdownBlock.Divider)
            }
            trimmed.startsWith("- ") || trimmed.startsWith("* ") || trimmed.startsWith("+ ") -> {
                blocks.add(MarkdownBlock.BulletItem(trimmed.substring(2).trim()))
            }
            trimmed.matches(Regex("^\\d+\\.\\s+.*")) -> {
                val dotIndex = trimmed.indexOf('.')
                val number = trimmed.substring(0, dotIndex)
                val text = trimmed.substring(dotIndex + 1).trim()
                blocks.add(MarkdownBlock.NumberedItem(number, text))
            }
            else -> {
                blocks.add(MarkdownBlock.Paragraph(trimmed))
            }
        }
    }

    if (inCodeBlock && codeBuilder.isNotEmpty()) {
        val fullCode = codeBuilder.toString().trimEnd()
        if (codeLang.lowercase() in listOf("mermaid", "uml", "plantuml") || isLikelyMermaid(fullCode)) {
            blocks.add(MarkdownBlock.MermaidDiagram(fullCode))
        } else {
            blocks.add(MarkdownBlock.CodeBlock(codeLang, fullCode))
        }
    }

    if (inMathBlock && mathBuilder.isNotEmpty()) {
        blocks.add(MarkdownBlock.MathEquation(mathBuilder.toString().trim()))
    }

    return blocks
}

private fun isLikelyMermaid(code: String): Boolean {
    val clean = code.trim().lowercase()
    return clean.startsWith("graph ") ||
            clean.startsWith("flowchart ") ||
            clean.startsWith("sequencediagram") ||
            clean.startsWith("classdiagram") ||
            clean.startsWith("statediagram") ||
            clean.startsWith("erdiagram") ||
            clean.startsWith("mindmap") ||
            clean.startsWith("gantt")
}

fun renderInlineStyles(text: String, defaultColor: Color = TextPrimary): androidx.compose.ui.text.AnnotatedString {
    return buildAnnotatedString {
        var cursor = 0
        while (cursor < text.length) {
            val boldStart = text.indexOf("**", cursor)
            val italicStart = text.indexOf("*", cursor)
            val codeStart = text.indexOf("`", cursor)
            val inlineMathStart = text.indexOf("$", cursor)

            val nextSpecial = listOfNotNull(
                if (boldStart != -1) boldStart else null,
                if (italicStart != -1 && italicStart != boldStart) italicStart else null,
                if (codeStart != -1) codeStart else null,
                if (inlineMathStart != -1) inlineMathStart else null
            ).minOrNull()

            if (nextSpecial == null) {
                append(text.substring(cursor))
                break
            }

            if (nextSpecial > cursor) {
                append(text.substring(cursor, nextSpecial))
            }

            when {
                nextSpecial == boldStart -> {
                    val boldEnd = text.indexOf("**", boldStart + 2)
                    if (boldEnd != -1) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(text.substring(boldStart + 2, boldEnd))
                        }
                        cursor = boldEnd + 2
                    } else {
                        append("**")
                        cursor = boldStart + 2
                    }
                }
                nextSpecial == inlineMathStart -> {
                    val mathEnd = text.indexOf("$", inlineMathStart + 1)
                    if (mathEnd != -1) {
                        withStyle(
                            SpanStyle(
                                fontFamily = FontFamily.Serif,
                                fontStyle = FontStyle.Italic,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF34D399) // Emerald high-contrast math
                            )
                        ) {
                            append(" ${text.substring(inlineMathStart + 1, mathEnd)} ")
                        }
                        cursor = mathEnd + 1
                    } else {
                        append("$")
                        cursor = inlineMathStart + 1
                    }
                }
                nextSpecial == codeStart -> {
                    val codeEnd = text.indexOf("`", codeStart + 1)
                    if (codeEnd != -1) {
                        withStyle(
                            SpanStyle(
                                fontFamily = FontFamily.Monospace,
                                background = Color(0x3371717A),
                                color = Color(0xFF60A5FA)
                            )
                        ) {
                            append(" ${text.substring(codeStart + 1, codeEnd)} ")
                        }
                        cursor = codeEnd + 1
                    } else {
                        append("`")
                        cursor = codeStart + 1
                    }
                }
                else -> {
                    val italicEnd = text.indexOf("*", italicStart + 1)
                    if (italicEnd != -1) {
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            append(text.substring(italicStart + 1, italicEnd))
                        }
                        cursor = italicEnd + 1
                    } else {
                        append("*")
                        cursor = italicStart + 1
                    }
                }
            }
        }
    }
}
