package com.pablo.paper

import com.google.common.truth.Truth.assertThat
import com.pablo.paper.domain.model.Annotation
import com.pablo.paper.domain.model.AnnotationType
import com.pablo.paper.domain.model.InkPoint
import com.pablo.paper.domain.model.InkStroke
import com.pablo.paper.ink.EraserEngine
import org.junit.Before
import org.junit.Test

class EraserEngineTest {

    private lateinit var eraserEngine: EraserEngine

    @Before
    fun setUp() {
        eraserEngine = EraserEngine()
    }

    @Test
    fun findIntersectingAnnotations_hitLineSegment_returnsAnnotation() {
        val stroke = InkStroke(
            points = listOf(
                InkPoint(0.2f, 0.2f),
                InkPoint(0.8f, 0.2f) // Horizontal line from x=0.2 to 0.8 at y=0.2
            ),
            color = 0xFF000000,
            width = 2.5f
        )

        val annotation = Annotation(
            id = "test_stroke",
            documentId = "doc1",
            pageIndex = 0,
            type = AnnotationType.INK,
            stroke = stroke
        )

        // Point directly near the segment (0.5, 0.21) with tolerance 0.02
        val hitNear = eraserEngine.findIntersectingAnnotations(
            annotations = listOf(annotation),
            eraserPoint = InkPoint(0.5f, 0.21f),
            normalizedToleranceRadius = 0.02f
        )
        assertThat(hitNear).containsExactly(annotation)

        // Point far away (0.5, 0.5)
        val hitFar = eraserEngine.findIntersectingAnnotations(
            annotations = listOf(annotation),
            eraserPoint = InkPoint(0.5f, 0.5f),
            normalizedToleranceRadius = 0.02f
        )
        assertThat(hitFar).isEmpty()
    }

    @Test
    fun findIntersectingAnnotations_singleDot_detectsProximity() {
        val stroke = InkStroke(
            points = listOf(InkPoint(0.4f, 0.4f)),
            color = 0xFF000000,
            width = 2.5f
        )

        val dotAnnotation = Annotation(
            id = "dot",
            documentId = "doc1",
            pageIndex = 0,
            type = AnnotationType.INK,
            stroke = stroke
        )

        val hit = eraserEngine.findIntersectingAnnotations(
            annotations = listOf(dotAnnotation),
            eraserPoint = InkPoint(0.405f, 0.405f),
            normalizedToleranceRadius = 0.02f
        )
        assertThat(hit).containsExactly(dotAnnotation)
    }
}
