package com.pablo.paper

import com.google.common.truth.Truth.assertThat
import com.pablo.paper.domain.model.InkPoint
import com.pablo.paper.domain.model.InkTool
import com.pablo.paper.ink.TextSnapper
import org.junit.Before
import org.junit.Test

class TextSnapperTest {

    private lateinit var textSnapper: TextSnapper

    @Before
    fun setUp() {
        textSnapper = TextSnapper()
    }

    @Test
    fun snapStrokePoints_penTool_returnsOriginalPoints() {
        val original = listOf(
            InkPoint(0.1f, 0.2f),
            InkPoint(0.15f, 0.22f),
            InkPoint(0.2f, 0.21f)
        )
        val result = textSnapper.snapStrokePoints(original, InkTool.PEN)
        assertThat(result).isEqualTo(original)
    }

    @Test
    fun snapStrokePoints_highlighterTool_returnsOriginalPoints() {
        val original = listOf(
            InkPoint(0.1f, 0.2f),
            InkPoint(0.3f, 0.25f)
        )
        val result = textSnapper.snapStrokePoints(original, InkTool.HIGHLIGHTER)
        assertThat(result).isEqualTo(original)
    }

    @Test
    fun snapStrokePoints_underline_snapsToHorizontalBaseline() {
        val rawSweep = listOf(
            InkPoint(0.1f, 0.30f),
            InkPoint(0.2f, 0.32f),
            InkPoint(0.4f, 0.29f),
            InkPoint(0.6f, 0.31f)
        )

        val snapped = textSnapper.snapStrokePoints(rawSweep, InkTool.UNDERLINE)

        assertThat(snapped).hasSize(2)
        assertThat(snapped[0].x).isEqualTo(0.1f)
        assertThat(snapped[1].x).isEqualTo(0.6f)
        // Both points have the exact same horizontal Y baseline
        assertThat(snapped[0].y).isEqualTo(snapped[1].y)
        // Baseline should be below the mean Y (0.305)
        assertThat(snapped[0].y).isGreaterThan(0.305f)
    }

    @Test
    fun snapStrokePoints_strikethrough_snapsToHorizontalCenter() {
        val rawSweep = listOf(
            InkPoint(0.15f, 0.50f),
            InkPoint(0.35f, 0.52f),
            InkPoint(0.55f, 0.48f)
        )

        val snapped = textSnapper.snapStrokePoints(rawSweep, InkTool.STRIKETHROUGH)

        assertThat(snapped).hasSize(2)
        assertThat(snapped[0].x).isEqualTo(0.15f)
        assertThat(snapped[1].x).isEqualTo(0.55f)
        assertThat(snapped[0].y).isEqualTo(snapped[1].y)
        assertThat(snapped[0].y).isWithin(0.01f).of(0.50f)
    }

    @Test
    fun snapStrokePoints_wavyUnderline_snapsToHorizontalBaseline() {
        val rawSweep = listOf(
            InkPoint(0.20f, 0.70f),
            InkPoint(0.50f, 0.72f),
            InkPoint(0.80f, 0.69f)
        )

        val snapped = textSnapper.snapStrokePoints(rawSweep, InkTool.WAVY_UNDERLINE)

        assertThat(snapped).hasSize(2)
        assertThat(snapped[0].x).isEqualTo(0.20f)
        assertThat(snapped[1].x).isEqualTo(0.80f)
        assertThat(snapped[0].y).isEqualTo(snapped[1].y)
        assertThat(snapped[0].y).isGreaterThan(0.70f)
    }

    @Test
    fun snapStrokePoints_textHighlight_snapsToTopAndBaseline() {
        val rawSweep = listOf(
            InkPoint(0.10f, 0.40f),
            InkPoint(0.30f, 0.41f),
            InkPoint(0.50f, 0.39f)
        )

        val snapped = textSnapper.snapStrokePoints(rawSweep, InkTool.TEXT_HIGHLIGHT)

        assertThat(snapped).hasSize(2)
        assertThat(snapped[0].x).isEqualTo(0.10f)
        assertThat(snapped[1].x).isEqualTo(0.50f)
        // Top Y should be above mean Y (0.40), Bottom Y should be below mean Y (0.40)
        assertThat(snapped[0].y).isLessThan(0.40f)
        assertThat(snapped[1].y).isGreaterThan(0.40f)
    }
}
