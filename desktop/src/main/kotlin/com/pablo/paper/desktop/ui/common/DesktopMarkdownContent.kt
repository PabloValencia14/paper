package com.pablo.paper.desktop.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

sealed interface MarkdownBlock {
    data class Header(val level: Int, val text: String) : MarkdownBlock
    data class Paragraph(val text: String) : MarkdownBlock
    data class CodeBlock(val code: String, val language: String? = null) : MarkdownBlock
    data class MathBlock(val latex: String) : MarkdownBlock
    data class BulletList(val items: List<String>) : MarkdownBlock
    data class NumberedList(val items: List<String>) : MarkdownBlock
    data class Blockquote(val text: String) : MarkdownBlock
    data class Table(
        val headers: List<String>,
        val alignments: List<TextAlign>,
        val rows: List<List<String>>
    ) : MarkdownBlock
}

@Composable
fun DesktopMarkdownContent(
    markdown: String,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val blocks = remember(markdown) { parseMarkdownBlocks(markdown) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.Header -> {
                    val (style, size) = when (block.level) {
                        1 -> FontWeight.Bold to 18.sp
                        2 -> FontWeight.SemiBold to 16.sp
                        else -> FontWeight.Medium to 14.sp
                    }
                    Text(
                        text = renderInlineStyles(block.text, textColor),
                        fontSize = size,
                        fontWeight = style,
                        color = textColor,
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )
                }
                is MarkdownBlock.Paragraph -> {
                    Text(
                        text = renderInlineStyles(block.text, textColor),
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = textColor
                    )
                }
                is MarkdownBlock.CodeBlock -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Box(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = block.code,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                is MarkdownBlock.MathBlock -> {
                    DesktopMathCard(latex = block.latex)
                }
                is MarkdownBlock.BulletList -> {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        block.items.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("•", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                                Text(
                                    text = renderInlineStyles(item, textColor),
                                    fontSize = 13.sp,
                                    color = textColor,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
                is MarkdownBlock.NumberedList -> {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        block.items.forEachIndexed { idx, item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("${idx + 1}.", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text(
                                    text = renderInlineStyles(item, textColor),
                                    fontSize = 13.sp,
                                    color = textColor,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
                is MarkdownBlock.Blockquote -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(24.dp)
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = renderInlineStyles(block.text, textColor),
                            fontSize = 12.sp,
                            fontStyle = FontStyle.Italic,
                            color = textColor.copy(alpha = 0.85f)
                        )
                    }
                }
                is MarkdownBlock.Table -> {
                    DesktopTableCard(table = block)
                }
            }
        }
    }
}

@Composable
fun DesktopMathCard(latex: String) {
    var copied by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Functions,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Fórmula Matemática LaTeX",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(
                    onClick = {
                        val selection = StringSelection(latex)
                        Toolkit.getDefaultToolkit().systemClipboard.setContents(selection, selection)
                        copied = true
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copiar LaTeX",
                        tint = if (copied) Color(0xFF10B981) else Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(6.dp))
                    .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = latex,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp
                )
            }
        }
    }
}


@Composable
fun DesktopTableCard(table: MarkdownBlock.Table) {
    val scrollState = rememberScrollState()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Box(modifier = Modifier.horizontalScroll(scrollState).padding(4.dp)) {
            Column {
                // Header
                Row(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    table.headers.forEachIndexed { i, h ->
                        Text(
                            text = h,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            textAlign = table.alignments.getOrElse(i) { TextAlign.Start },
                            modifier = Modifier.width(140.dp).padding(horizontal = 4.dp)
                        )
                    }
                }
                // Rows
                table.rows.forEachIndexed { rIdx, row ->
                    val bg = if (rIdx % 2 == 0) Color.Transparent else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    Row(
                        modifier = Modifier
                            .background(bg)
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        row.forEachIndexed { cIdx, cell ->
                            Text(
                                text = renderInlineStyles(cell, MaterialTheme.colorScheme.onSurface),
                                fontSize = 12.sp,
                                textAlign = table.alignments.getOrElse(cIdx) { TextAlign.Start },
                                modifier = Modifier.width(140.dp).padding(horizontal = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

fun parseMarkdownBlocks(input: String): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()
    val lines = input.lines()
    var i = 0

    while (i < lines.size) {
        val line = lines[i]

        // Math Block $$ ... $$ or \[ ... \]
        if (line.trim().startsWith("$$") || line.trim().startsWith("\\[") || line.trim().startsWith("\\begin{")) {
            val sb = java.lang.StringBuilder()
            val startDelimiter = when {
                line.trim().startsWith("$$") -> "$$"
                line.trim().startsWith("\\[") -> "\\["
                else -> "\\begin"
            }
            sb.append(line.trim().removePrefix("$$").removePrefix("\\["))
            if (!line.trim().endsWith("$$") || line.trim() == "$$") {
                i++
                while (i < lines.size) {
                    val l = lines[i]
                    if (l.trim().endsWith("$$") || l.trim().endsWith("\\]") || l.trim().startsWith("\\end{")) {
                        sb.append("\n").append(l.trim().removeSuffix("$$").removeSuffix("\\]"))
                        break
                    }
                    sb.append("\n").append(l)
                    i++
                }
            }
            blocks.add(MarkdownBlock.MathBlock(sb.toString().trim()))
            i++
            continue
        }

        // Table
        if (line.trim().startsWith("|") && i + 1 < lines.size && lines[i + 1].trim().startsWith("|") && lines[i + 1].contains("-")) {
            val headers = line.split("|").map { it.trim() }.filter { it.isNotEmpty() }
            val alignLine = lines[i + 1]
            val alignments = alignLine.split("|").map { it.trim() }.filter { it.isNotEmpty() }.map {
                when {
                    it.startsWith(":") && it.endsWith(":") -> TextAlign.Center
                    it.endsWith(":") -> TextAlign.End
                    else -> TextAlign.Start
                }
            }
            i += 2
            val rows = mutableListOf<List<String>>()
            while (i < lines.size && lines[i].trim().startsWith("|")) {
                val rowCells = lines[i].split("|").map { it.trim() }.filter { it.isNotEmpty() }
                if (rowCells.isNotEmpty()) rows.add(rowCells)
                i++
            }
            blocks.add(MarkdownBlock.Table(headers, alignments, rows))
            continue
        }

        // Headers
        if (line.startsWith("#")) {
            val level = line.takeWhile { it == '#' }.length
            val text = line.removePrefix("#".repeat(level)).trim()
            blocks.add(MarkdownBlock.Header(level, text))
            i++
            continue
        }

        // Code block
        if (line.startsWith("```")) {
            val lang = line.removePrefix("```").trim().takeIf { it.isNotEmpty() }
            val codeLines = mutableListOf<String>()
            i++
            while (i < lines.size && !lines[i].startsWith("```")) {
                codeLines.add(lines[i])
                i++
            }
            blocks.add(MarkdownBlock.CodeBlock(codeLines.joinToString("\n"), lang))
            i++
            continue
        }

        // Bullet list
        if (line.trim().startsWith("- ") || line.trim().startsWith("* ")) {
            val items = mutableListOf<String>()
            while (i < lines.size && (lines[i].trim().startsWith("- ") || lines[i].trim().startsWith("* "))) {
                items.add(lines[i].trim().substring(2))
                i++
            }
            blocks.add(MarkdownBlock.BulletList(items))
            continue
        }

        // Numbered list
        if (line.trim().matches(Regex("^\\d+\\.\\s.*"))) {
            val items = mutableListOf<String>()
            while (i < lines.size && lines[i].trim().matches(Regex("^\\d+\\.\\s.*"))) {
                items.add(lines[i].trim().replaceFirst(Regex("^\\d+\\.\\s"), ""))
                i++
            }
            blocks.add(MarkdownBlock.NumberedList(items))
            continue
        }

        // Blockquote
        if (line.trim().startsWith(">")) {
            blocks.add(MarkdownBlock.Blockquote(line.trim().removePrefix(">").trim()))
            i++
            continue
        }

        // Paragraph
        if (line.isNotBlank()) {
            blocks.add(MarkdownBlock.Paragraph(line.trim()))
        }
        i++
    }

    return blocks
}

fun renderInlineStyles(text: String, defaultColor: Color): AnnotatedString {
    return buildAnnotatedString {
        var cursor = 0
        val regex = Regex("(\\$\\$?[^$]+\\$\\$?|\\*\\*[^*]+\\*\\*|\\*[^*]+\\*|`[^`]+`)")
        val matches = regex.findAll(text)

        for (match in matches) {
            if (match.range.first > cursor) {
                append(text.substring(cursor, match.range.first))
            }

            val value = match.value
            when {
                value.startsWith("$") -> {
                    val math = value.trim('$')
                    pushStyle(SpanStyle(fontFamily = FontFamily.Monospace, color = Color(0xFF0D9488), fontStyle = FontStyle.Italic))
                    append(math)
                    pop()
                }
                value.startsWith("**") -> {
                    val bold = value.removeSurrounding("**")
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                    append(bold)
                    pop()
                }
                value.startsWith("*") -> {
                    val italic = value.removeSurrounding("*")
                    pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                    append(italic)
                    pop()
                }
                value.startsWith("`") -> {
                    val code = value.removeSurrounding("`")
                    pushStyle(SpanStyle(fontFamily = FontFamily.Monospace, background = Color.LightGray.copy(alpha = 0.2f)))
                    append(code)
                    pop()
                }
            }
            cursor = match.range.last + 1
        }

        if (cursor < text.length) {
            append(text.substring(cursor))
        }
    }
}
