package com.pablo.paper

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.google.common.truth.Truth.assertThat
import com.pablo.paper.domain.model.InkPoint
import com.pablo.paper.domain.model.ViewMode
import com.pablo.paper.pdf.CoordinateTransformer
import com.pablo.paper.pdf.PageSize
import org.junit.Before
import org.junit.Test

class CoordinateTransformerTest {

    private lateinit var transformer: CoordinateTransformer

    @Before
    fun setUp() {
        transformer = CoordinateTransformer()
    }

    @Test
    fun calculatePageBounds_fullPage_tallViewport_fitsWidth() {
        val viewportSize = Size(800f, 1200f)
        val pageSize = PageSize(800, 1000) // Aspect ratio = 0.8

        val bounds = transformer.calculatePageBounds(
            viewportSize = viewportSize,
            pageSize = pageSize,
            zoom = 1.0f,
            viewMode = ViewMode.FULL_PAGE
        )

        // Viewport aspect (0.66) < page aspect (0.8) -> width is bounding dimension (800)
        assertThat(bounds.width).isEqualTo(800f)
        assertThat(bounds.height).isEqualTo(1000f)
        assertThat(bounds.left).isEqualTo(0f)
        assertThat(bounds.top).isEqualTo(100f) // Centered vertically in 1200 height
    }

    @Test
    fun calculatePageBounds_fullPage_wideViewport_fitsHeight() {
        val viewportSize = Size(1600f, 1000f)
        val pageSize = PageSize(600, 800) // Aspect ratio = 0.75

        val bounds = transformer.calculatePageBounds(
            viewportSize = viewportSize,
            pageSize = pageSize,
            zoom = 1.0f,
            viewMode = ViewMode.FULL_PAGE
        )

        // Viewport aspect (1.6) > page aspect (0.75) -> height is bounding dimension (1000)
        assertThat(bounds.height).isEqualTo(1000f)
        assertThat(bounds.width).isEqualTo(750f)
        assertThat(bounds.top).isEqualTo(0f)
        assertThat(bounds.left).isEqualTo((1600f - 750f) / 2f) // Centered horizontally
    }

    @Test
    fun screenToPdf_and_pdfToScreen_roundTripPreservesCoordinates() {
        val viewportSize = Size(1000f, 1000f)
        val pageSize = PageSize(500, 500)
        val bounds = transformer.calculatePageBounds(viewportSize, pageSize)

        val originalPdfPoint = InkPoint(x = 0.35f, y = 0.65f, pressure = 0.8f)
        val screenOffset = transformer.pdfToScreen(originalPdfPoint, bounds)
        val convertedBack = transformer.screenToPdf(screenOffset, bounds)

        assertThat(convertedBack).isNotNull()
        assertThat(convertedBack!!.x).isWithin(0.001f).of(originalPdfPoint.x)
        assertThat(convertedBack.y).isWithin(0.001f).of(originalPdfPoint.y)
    }

    @Test
    fun edgeTapDetection_identifiesLeftAndRightZonesCorrectly() {
        val viewportWidth = 1000f

        // Left 18% is < 180f
        assertThat(transformer.isLeftEdgeTap(Offset(50f, 500f), viewportWidth)).isTrue()
        assertThat(transformer.isLeftEdgeTap(Offset(179f, 500f), viewportWidth)).isTrue()
        assertThat(transformer.isLeftEdgeTap(Offset(181f, 500f), viewportWidth)).isFalse()

        // Right 18% is > 820f
        assertThat(transformer.isRightEdgeTap(Offset(821f, 500f), viewportWidth)).isTrue()
        assertThat(transformer.isRightEdgeTap(Offset(950f, 500f), viewportWidth)).isTrue()
        assertThat(transformer.isRightEdgeTap(Offset(819f, 500f), viewportWidth)).isFalse()

        // Center zone is neither
        assertThat(transformer.isLeftEdgeTap(Offset(500f, 500f), viewportWidth)).isFalse()
        assertThat(transformer.isRightEdgeTap(Offset(500f, 500f), viewportWidth)).isFalse()
    }
}
