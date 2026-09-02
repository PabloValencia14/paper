package com.pablo.paper.desktop.model

import androidx.compose.ui.graphics.Color
import java.io.File

enum class DesktopTool {
    PAN_HAND,
    TEXT_SELECTION,
    MARQUEE_ZOOM,
    
    // Markup & Annotations
    HIGHLIGHT,
    UNDERLINE,
    STRIKETHROUGH,
    SQUIGGLY,
    PEN,
    CALLIGRAPHY,
    HIGHLIGHTER_FREE,
    ERASER_STROKE,
    ERASER_AREA,
    
    // Shapes
    SHAPE_RECTANGLE,
    SHAPE_OVAL,
    SHAPE_ARROW,
    SHAPE_LINE,
    SHAPE_POLYGON,
    SHAPE_CLOUD,
    
    // Notes & Text
    STICKY_NOTE,
    TEXT_BOX,
    CALLOUT,
    STAMP,
    
    // Measurement
    MEASURE_DISTANCE,
    MEASURE_PERIMETER,
    MEASURE_AREA,
    
    // Signatures & Forms
    FILL_AND_SIGN,
    CERTIFICATE_SIGN,
    FORM_TEXT_FIELD,
    FORM_CHECKBOX,
    FORM_RADIO,
    FORM_DROPDOWN,
    
    // Redaction & Security
    REDACT_MARK,
    REDACT_APPLY,
    
    // Study Tools
    BIONIC_READING,
    STUDY_MASK,
    DIGITAL_RULER
}

enum class ViewMode {
    SINGLE_PAGE,
    CONTINUOUS_SCROLL,
    TWO_PAGE_SPREAD,
    READ_MODE,
    FULLSCREEN
}

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
    SEPIA,
    INVERTED,
    HIGH_CONTRAST
}


enum class AnnotationType {
    INK,
    HIGHLIGHT,
    UNDERLINE,
    STRIKETHROUGH,
    SQUIGGLY,
    SHAPE_RECTANGLE,
    SHAPE_OVAL,
    SHAPE_ARROW,
    SHAPE_LINE,
    SHAPE_POLYGON,
    SHAPE_CLOUD,
    STICKY_NOTE,
    TEXT_BOX,
    CALLOUT,
    STAMP,
    SIGNATURE,
    MEASUREMENT,
    REDACTION
}

data class InkPoint(
    val x: Float,
    val y: Float,
    val pressure: Float = 1.0f,
    val timestamp: Long = System.currentTimeMillis()
)

data class InkStroke(
    val points: List<InkPoint>,
    val color: Long = 0xFF000000L,
    val strokeWidth: Float = 3.0f,
    val alpha: Float = 1.0f,
    val isHighlighter: Boolean = false,
    val isCalligraphy: Boolean = false
)

data class Annotation(
    val id: String,
    val pageIndex: Int,
    val type: AnnotationType,
    val stroke: InkStroke? = null,
    val rects: List<FloatArray>? = null, // [x1, y1, x2, y2] normalized 0..1
    val textContent: String? = null,
    val author: String = "Usuario",
    val color: Long = 0xFFFFD700L,
    val opacity: Float = 1.0f,
    val strokeWidth: Float = 2.0f,
    val isResolved: Boolean = false,
    val replies: List<AnnotationReply> = emptyList(),
    val measurementValue: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class AnnotationReply(
    val id: String,
    val author: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class OutlineNode(
    val title: String,
    val pageIndex: Int,
    val level: Int = 0,
    val children: List<OutlineNode> = emptyList()
)

data class PageInfo(
    val pageIndex: Int,
    val width: Int,
    val height: Int,
    val rotation: Int = 0
) {
    val aspectRatio: Float
        get() = if (height > 0) width.toFloat() / height.toFloat() else 1.0f
}

data class DocumentMetadata(
    val title: String = "",
    val author: String = "",
    val subject: String = "",
    val keywords: String = "",
    val creator: String = "",
    val producer: String = "",
    val creationDate: String = "",
    val modificationDate: String = "",
    val pdfVersion: String = "1.7",
    val pageCount: Int = 0,
    val fileSize: Long = 0L,
    val isEncrypted: Boolean = false,
    val isSigned: Boolean = false,
    val hasAcroForms: Boolean = false
)

enum class StampType(val label: String, val colorHex: Long) {
    APPROVED("APROBADO", 0xFF10B981L),
    CONFIDENTIAL("CONFIDENCIAL", 0xFFEF4444L),
    DRAFT("BORRADOR", 0xFF6B7280L),
    SIGN_HERE("FIRMAR AQUÍ", 0xFF3B82F6L),
    FINAL("FINAL", 0xFF8B5CF6L),
    RECEIVED("RECIBIDO", 0xFF059669L),
    REJECTED("RECHAZADO", 0xFFDC2626L),
    CUSTOM("PERSONALIZADO", 0xFFF59E0BL)
}

data class AcroFormField(
    val name: String,
    val type: String, // Text, Checkbox, Radio, Choice, Button, Signature
    val pageIndex: Int,
    val bounds: FloatArray, // [x1, y1, x2, y2]
    val value: String = "",
    val isReadOnly: Boolean = false,
    val isRequired: Boolean = false,
    val options: List<String> = emptyList()
)

data class DigitalCertificate(
    val alias: String,
    val subject: String,
    val issuer: String,
    val validFrom: String,
    val validTo: String,
    val file: File? = null
)

data class SearchMatch(
    val pageIndex: Int,
    val snippet: String,
    val matchIndexInPage: Int,
    val bounds: List<FloatArray> = emptyList()
)

data class TextGlyph(
    val char: String,
    val x: Float,      // normalized 0..1
    val y: Float,      // normalized 0..1
    val width: Float,  // normalized 0..1
    val height: Float  // normalized 0..1
)

data class TextWord(
    val text: String,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val glyphs: List<TextGlyph> = emptyList()
) {
    val bounds: FloatArray
        get() = floatArrayOf(x, y, x + width, y + height)
}

data class TextLine(
    val text: String,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val words: List<TextWord> = emptyList()
) {
    val bounds: FloatArray
        get() = floatArrayOf(x, y, x + width, y + height)
}

data class PageTextLayout(
    val pageIndex: Int,
    val lines: List<TextLine> = emptyList(),
    val words: List<TextWord> = emptyList(),
    val fullText: String = ""
)

data class TextSelectionRange(
    val pageIndex: Int,
    val selectedWords: List<TextWord> = emptyList(),
    val selectedText: String = ""
)

