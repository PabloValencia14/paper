package com.pablo.paper.domain.model

/**
 * Customizable actions that can be triggered by active stylus buttons
 * (e.g. Xiaomi Smart Pen, Samsung S Pen, active tablet styluses).
 */
enum class StylusButtonAction(val displayName: String, val description: String) {
    TEMPORARY_ERASER(
        "Borrador Temporal (Mantener)",
        "Borra trazos mientras mantengas presionado el botón y regresa al lápiz al soltarlo"
    ),
    TOGGLE_ERASER(
        "Alternar Lápiz / Borrador",
        "Alterna con un clic entre la herramienta de dibujo y el borrador"
    ),
    SWITCH_TO_HIGHLIGHTER(
        "Cambiar a Resaltador",
        "Cambia directamente a la herramienta de resaltado"
    ),
    TOGGLE_HAND_TOOL(
        "Alternar Modo Desplazamiento",
        "Alterna entre el modo desplazamiento para navegar y la pluma para escribir"
    ),
    TOGGLE_LAST_TOOL(
        "Alternar Última Herramienta",
        "Alterna rápidamente entre las dos últimas herramientas utilizadas"
    ),
    COLOR_CYCLE(
        "Siguiente Color Favorito",
        "Cambia secuencialmente entre tus colores recientes guardados"
    ),
    UNDO(
        "Deshacer Trazo",
        "Deshace el último trazo o anotación en la página"
    ),
    REDO(
        "Rehacer Trazo",
        "Rehace el último trazo deshecho"
    ),
    SELECT_TEXT(
        "Modo Selección de Texto",
        "Activa el modo de selección para copiar texto o consultar a la IA"
    ),
    NEXT_PAGE(
        "Página Siguiente",
        "Avanza instantáneamente a la página siguiente"
    ),
    PREVIOUS_PAGE(
        "Página Anterior",
        "Retrocede instantáneamente a la página anterior"
    ),
    STICKY_NOTE(
        "Añadir Nota Adhesiva",
        "Activa la herramienta de notas adhesivas"
    ),
    NONE(
        "Ninguna Acción",
        "Desactiva las funciones para este botón"
    )
}
