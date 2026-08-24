package com.pablo.paper.ui.common

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.style.TextAlign
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
                is MarkdownBlock.Table -> {
                    MarkdownTableCard(
                        table = block,
                        textColor = textColor,
                        isDarkMode = isDarkMode,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
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

@Composable
fun MarkdownTableCard(
    table: MarkdownBlock.Table,
    textColor: Color,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val borderColor = if (isDarkMode) Color(0xFF2E384D) else Color(0xFFE2E8F0)
    val headerBg = if (isDarkMode) Color(0xFF1E2433) else Color(0xFFEDF2F7)
    val altRowBg = if (isDarkMode) Color(0x0AFFFFFF) else Color(0x05000000)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isDarkMode) Color(0xFF141721).copy(alpha = 0.8f) else Color(0xFFFAFAFA))
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .background(headerBg)
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                table.headers.forEachIndexed { colIdx, headerText ->
                    val align = table.alignments.getOrElse(colIdx) { TextAlign.Start }
                    Box(
                        modifier = Modifier
                            .widthIn(min = 90.dp, max = 260.dp)
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        contentAlignment = when (align) {
                            TextAlign.Center -> Alignment.Center
                            TextAlign.End -> Alignment.CenterEnd
                            else -> Alignment.CenterStart
                        }
                    ) {
                        Text(
                            text = renderInlineStyles(headerText, if (isDarkMode) Color(0xFF93C5FD) else AccentBlue),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp
                            ),
                            textAlign = align
                        )
                    }
                }
            }

            // Divider below header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(borderColor)
            )

            // Data Rows
            table.rows.forEachIndexed { rowIdx, rowCells ->
                val isAlt = rowIdx % 2 == 1
                Row(
                    modifier = Modifier
                        .background(if (isAlt) altRowBg else Color.Transparent)
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    table.headers.indices.forEach { colIdx ->
                        val cellText = rowCells.getOrElse(colIdx) { "" }
                        val align = table.alignments.getOrElse(colIdx) { TextAlign.Start }
                        Box(
                            modifier = Modifier
                                .widthIn(min = 90.dp, max = 260.dp)
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            contentAlignment = when (align) {
                                TextAlign.Center -> Alignment.Center
                                TextAlign.End -> Alignment.CenterEnd
                                else -> Alignment.CenterStart
                            }
                        ) {
                            Text(
                                text = renderInlineStyles(cellText, textColor),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                ),
                                textAlign = align
                            )
                        }
                    }
                }
                if (rowIdx < table.rows.size - 1) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.5.dp)
                            .background(borderColor.copy(alpha = 0.5f))
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
    data class Table(
        val headers: List<String>,
        val alignments: List<TextAlign>,
        val rows: List<List<String>>
    ) : MarkdownBlock
    data object Divider : MarkdownBlock
}

private fun isTableSeparatorLine(line: String): Boolean {
    val clean = line.trim()
    if (!clean.contains("-")) return false
    val parts = clean.split('|').map { it.trim() }.filter { it.isNotEmpty() }
    if (parts.isEmpty()) return false
    return parts.all { col ->
        col.matches(Regex("^:?-+:?$"))
    }
}

private fun parseTableRow(line: String): List<String> {
    var raw = line.trim()
    if (raw.startsWith("|")) raw = raw.substring(1)
    if (raw.endsWith("|")) raw = raw.substring(0, raw.length - 1)
    return raw.split('|').map { it.trim() }
}

private fun parseTableAlignments(separatorLine: String): List<TextAlign> {
    val cols = parseTableRow(separatorLine)
    return cols.map { col ->
        val trimmed = col.trim()
        val startsWithColon = trimmed.startsWith(":")
        val endsWithColon = trimmed.endsWith(":")
        when {
            startsWithColon && endsWithColon -> TextAlign.Center
            endsWithColon -> TextAlign.End
            else -> TextAlign.Start
        }
    }
}

private fun parseMarkdownBlocks(markdown: String): List<MarkdownBlock> {
    val lines = markdown.lines()
    val blocks = mutableListOf<MarkdownBlock>()
    var i = 0

    var inCodeBlock = false
    var codeLang = ""
    val codeBuilder = StringBuilder()

    var inMathBlock = false
    val mathBuilder = StringBuilder()

    while (i < lines.size) {
        val rawLine = lines[i]
        val trimmed = rawLine.trim()

        // 1. Code blocks (```)
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
            i++
            continue
        }

        if (inCodeBlock) {
            if (codeBuilder.isNotEmpty()) codeBuilder.append("\n")
            codeBuilder.append(rawLine)
            i++
            continue
        }

        // 2. Display LaTeX Math Blocks ($$, \[, \begin{...})
        if (trimmed.startsWith("$$")) {
            if (trimmed.endsWith("$$") && trimmed.length > 2) {
                val formula = trimmed.removePrefix("$$").removeSuffix("$$").trim()
                if (formula.isNotEmpty()) {
                    blocks.add(MarkdownBlock.MathEquation(formula))
                }
                i++
                continue
            } else if (inMathBlock) {
                val formula = mathBuilder.toString().trim()
                if (formula.isNotEmpty()) {
                    blocks.add(MarkdownBlock.MathEquation(formula))
                }
                mathBuilder.clear()
                inMathBlock = false
                i++
                continue
            } else {
                inMathBlock = true
                val rest = trimmed.removePrefix("$$").trim()
                if (rest.isNotEmpty()) mathBuilder.append(rest)
                i++
                continue
            }
        }

        if (trimmed.startsWith("\\[")) {
            if (trimmed.endsWith("\\]") && trimmed.length > 3) {
                val formula = trimmed.removePrefix("\\[").removeSuffix("\\]").trim()
                if (formula.isNotEmpty()) {
                    blocks.add(MarkdownBlock.MathEquation(formula))
                }
                i++
                continue
            } else {
                inMathBlock = true
                val rest = trimmed.removePrefix("\\[").trim()
                if (rest.isNotEmpty()) mathBuilder.append(rest)
                i++
                continue
            }
        }

        if (inMathBlock) {
            if (trimmed.endsWith("$$") || trimmed.endsWith("\\]")) {
                val lastPart = trimmed.removeSuffix("$$").removeSuffix("\\]").trim()
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
            i++
            continue
        }

        if (trimmed.startsWith("\\begin{equation}") || trimmed.startsWith("\\begin{align}") || trimmed.startsWith("\\begin{gather}") || trimmed.startsWith("\\begin{matrix}") || trimmed.startsWith("\\begin{pmatrix}") || trimmed.startsWith("\\begin{bmatrix}")) {
            val formulaBuilder = StringBuilder(trimmed)
            var j = i + 1
            var closed = trimmed.contains("\\end{")
            while (j < lines.size && !closed) {
                val nextL = lines[j].trim()
                formulaBuilder.append("\n").append(lines[j])
                if (nextL.contains("\\end{")) {
                    closed = true
                }
                j++
            }
            blocks.add(MarkdownBlock.MathEquation(formulaBuilder.toString().trim()))
            i = j
            continue
        }

        if (trimmed.isEmpty()) {
            i++
            continue
        }

        // 3. Markdown Tables (| Col 1 | Col 2 | ... \n |---|---|...)
        if (trimmed.contains("|") && i + 1 < lines.size && isTableSeparatorLine(lines[i + 1])) {
            val headers = parseTableRow(trimmed)
            val alignments = parseTableAlignments(lines[i + 1])
            val rows = mutableListOf<List<String>>()
            var j = i + 2
            while (j < lines.size) {
                val rowLine = lines[j].trim()
                if (rowLine.isEmpty() || !rowLine.contains("|")) break
                rows.add(parseTableRow(rowLine))
                j++
            }
            if (headers.isNotEmpty()) {
                blocks.add(MarkdownBlock.Table(headers, alignments, rows))
            }
            i = j
            continue
        }

        // 4. Standard Markdown Elements
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
        i++
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
            val inlineDollarStart = text.indexOf("$", cursor)
            val inlineParenStart = text.indexOf("\\(", cursor)

            val inlineMathStart = when {
                inlineDollarStart != -1 && inlineParenStart != -1 -> minOf(inlineDollarStart, inlineParenStart)
                inlineDollarStart != -1 -> inlineDollarStart
                inlineParenStart != -1 -> inlineParenStart
                else -> -1
            }

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
                nextSpecial == inlineDollarStart -> {
                    val mathEnd = text.indexOf("$", inlineDollarStart + 1)
                    if (mathEnd != -1) {
                        withStyle(
                            SpanStyle(
                                fontFamily = FontFamily.Serif,
                                fontStyle = FontStyle.Italic,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF10B981) // High-contrast emerald math
                            )
                        ) {
                            append(" ${text.substring(inlineDollarStart + 1, mathEnd)} ")
                        }
                        cursor = mathEnd + 1
                    } else {
                        append("$")
                        cursor = inlineDollarStart + 1
                    }
                }
                nextSpecial == inlineParenStart -> {
                    val mathEnd = text.indexOf("\\)", inlineParenStart + 2)
                    if (mathEnd != -1) {
                        withStyle(
                            SpanStyle(
                                fontFamily = FontFamily.Serif,
                                fontStyle = FontStyle.Italic,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF10B981)
                            )
                        ) {
                            append(" ${text.substring(inlineParenStart + 2, mathEnd)} ")
                        }
                        cursor = mathEnd + 2
                    } else {
                        append("\\(")
                        cursor = inlineParenStart + 2
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
