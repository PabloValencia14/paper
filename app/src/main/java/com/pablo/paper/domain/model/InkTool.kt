package com.pablo.paper.domain.model

/**
 * Supported annotation / ink tools matching Paper Driven-Spec and tablet UX.
 */
enum class InkTool {
    HAND,
    SELECT_TEXT,
    PEN,
    HIGHLIGHTER,
    TEXT_HIGHLIGHT,
    UNDERLINE,
    STRIKETHROUGH,
    WAVY_UNDERLINE,
    RECTANGLE,
    OVAL,
    ARROW,
    LINE,
    STICKY_NOTE,
    TEXT_BOX,
    STAMP,
    SIGNATURE,
    LASSO,
    LASER_POINTER,
    ERASER;

    val displayName: String
        get() = when (this) {
            HAND -> "Desplazamiento / Mano"
            SELECT_TEXT -> "Text Select"
            PEN -> "Pen"
            HIGHLIGHTER -> "Highlighter"
            TEXT_HIGHLIGHT -> "Text highlight"
            UNDERLINE -> "Text underline"
            STRIKETHROUGH -> "Strikethrough"
            WAVY_UNDERLINE -> "Wavy underline"
            RECTANGLE -> "Rectangle"
            OVAL -> "Oval"
            ARROW -> "Arrow"
            LINE -> "Straight Line"
            STICKY_NOTE -> "Sticky Note"
            TEXT_BOX -> "Text Box"
            STAMP -> "Stamp"
            SIGNATURE -> "Signature"
            LASSO -> "Lazo de selección"
            LASER_POINTER -> "Puntero Láser"
            ERASER -> "Eraser"
        }

    val defaultStrokeWidth: Float
        get() = when (this) {
            HAND -> 0.0f
            SELECT_TEXT -> 1.0f
            PEN -> 2.0f
            HIGHLIGHTER -> 14.0f
            TEXT_HIGHLIGHT -> 14.0f
            UNDERLINE -> 2.0f
            STRIKETHROUGH -> 2.0f
            WAVY_UNDERLINE -> 2.0f
            RECTANGLE -> 3.0f
            OVAL -> 3.0f
            ARROW -> 3.5f
            LINE -> 3.0f
            STICKY_NOTE -> 1.0f
            TEXT_BOX -> 1.0f
            STAMP -> 1.0f
            SIGNATURE -> 2.5f
            LASSO -> 1.5f
            LASER_POINTER -> 6.0f
            ERASER -> 20.0f
        }

    val defaultAlpha: Float
        get() = when (this) {
            HAND -> 1.0f
            SELECT_TEXT -> 1.0f
            PEN -> 1.0f
            HIGHLIGHTER -> 0.26f
            TEXT_HIGHLIGHT -> 0.26f
            UNDERLINE -> 1.0f
            STRIKETHROUGH -> 0.9f
            WAVY_UNDERLINE -> 1.0f
            RECTANGLE -> 1.0f
            OVAL -> 1.0f
            ARROW -> 1.0f
            LINE -> 1.0f
            STICKY_NOTE -> 1.0f
            TEXT_BOX -> 1.0f
            STAMP -> 1.0f
            SIGNATURE -> 1.0f
            LASSO -> 0.8f
            LASER_POINTER -> 0.95f
            ERASER -> 1.0f
        }

    val tooltipMessage: String
        get() = when (this) {
            HAND -> "Modo Desplazamiento · navega, arrastra y haz zoom sin escribir con el lápiz"
            SELECT_TEXT -> "Text Selection · select text to copy, explain with AI or add to notes"
            PEN -> "Pen selected · write annotations directly on the page"
            HIGHLIGHTER -> "Highlighter selected · hold either pen button for temporary erase"
            TEXT_HIGHLIGHT -> "Text marker selected · sweep across the words to highlight"
            UNDERLINE -> "Underline selected · sweep across words to underline"
            STRIKETHROUGH -> "Strikethrough selected · strike through words"
            WAVY_UNDERLINE -> "Wavy underline selected · annotate key terms"
            RECTANGLE -> "Rectangle shape · drag to draw a box"
            OVAL -> "Oval shape · drag to draw an ellipse or circle"
            ARROW -> "Arrow shape · drag to point an arrow"
            LINE -> "Straight line · drag to draw a clean ruler line"
            STICKY_NOTE -> "Sticky note · tap anywhere to add a comment note"
            TEXT_BOX -> "Text box · tap anywhere to add typed text"
            STAMP -> "Stamp · tap to place a status badge (Aprobado, Confidencial...)"
            SIGNATURE -> "Signature tool · tap to place your signature"
            LASSO -> "Lazo de selección libre · rodea trazos para moverlos, duplicarlos o borrarlos"
            LASER_POINTER -> "Puntero láser · trazo temporal brillante que se desvanece solo"
            ERASER -> "Eraser selected · tap any stroke or shape to remove"
        }
}
