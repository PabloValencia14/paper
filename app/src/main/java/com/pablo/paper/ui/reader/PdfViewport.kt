package com.pablo.paper.ui.reader

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Paint
import android.graphics.Shader
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.pablo.paper.domain.model.PaperTexture
import com.pablo.paper.domain.model.ReaderAction
import com.pablo.paper.domain.model.ReaderState
import com.pablo.paper.domain.model.ViewMode
import com.pablo.paper.pdf.CoordinateTransformer
import com.pablo.paper.pdf.PageSize

@Composable
fun rememberTextureShader(
    texture: PaperTexture,
    isDark: Boolean,
    density: Float,
    points: Float
): BitmapShader? {
    return remember(texture, isDark, density, points) {
        when (texture) {
            PaperTexture.SMOOTH -> null
            PaperTexture.DOT_GRID -> {
                val step = (points * density * 1.15f).toInt().coerceAtLeast(12)
                val bmp = Bitmap.createBitmap(step, step, Bitmap.Config.ARGB_8888)
                val canvas = android.graphics.Canvas(bmp)
                val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = if (isDark) 0x38FFFFFF.toInt() else 0x38000000.toInt()
                    style = Paint.Style.FILL
                }
                val dotRadius = (1.4f * density).coerceIn(1.5f, 3.5f)
                canvas.drawCircle(step / 2f, step / 2f, dotRadius, paint)
                BitmapShader(bmp, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
            }
            PaperTexture.GRID -> {
                val step = (points * density * 1.15f).toInt().coerceAtLeast(12)
                val bmp = Bitmap.createBitmap(step, step, Bitmap.Config.ARGB_8888)
                val canvas = android.graphics.Canvas(bmp)
                val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = if (isDark) 0x24FFFFFF.toInt() else 0x24000000.toInt()
                    strokeWidth = (1f * density).coerceAtLeast(1f)
                }
                canvas.drawLine(0f, 0f, step.toFloat(), 0f, paint)
                canvas.drawLine(0f, 0f, 0f, step.toFloat(), paint)
                BitmapShader(bmp, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
            }
            PaperTexture.LINED -> {
                val step = (points * density * 1.4f).toInt().coerceAtLeast(16)
                val bmp = Bitmap.createBitmap(16, step, Bitmap.Config.ARGB_8888)
                val canvas = android.graphics.Canvas(bmp)
                val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = if (isDark) 0x30FFFFFF.toInt() else 0x301565C0.toInt()
                    strokeWidth = (1.2f * density).coerceAtLeast(1f)
                }
                canvas.drawLine(0f, step - 1f, 16f, step - 1f, paint)
                BitmapShader(bmp, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
            }
            PaperTexture.ISOMETRIC -> {
                val stepX = (points * density * 1.25f).toInt().coerceAtLeast(16)
                val stepY = (stepX * 1.732f).toInt().coerceAtLeast(28)
                val bmp = Bitmap.createBitmap(stepX, stepY, Bitmap.Config.ARGB_8888)
                val canvas = android.graphics.Canvas(bmp)
                val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = if (isDark) 0x22FFFFFF.toInt() else 0x22000000.toInt()
                    strokeWidth = (1f * density).coerceAtLeast(1f)
                }
                canvas.drawLine(0f, 0f, stepX.toFloat(), stepY.toFloat(), paint)
                canvas.drawLine(stepX.toFloat(), 0f, 0f, stepY.toFloat(), paint)
                canvas.drawLine(0f, 0f, stepX.toFloat(), 0f, paint)
                BitmapShader(bmp, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
            }
            PaperTexture.FINE_GRAIN -> {
                val size = 384
                val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
                val canvas = android.graphics.Canvas(bmp)
                val random = java.util.Random(1337)
                val sizeF = size.toFloat()

                // 1. Base micro-stipples (cotton pulp texture) with toroidal wrap - ultra fine & subtle
                val stipplePaint = Paint(Paint.ANTI_ALIAS_FLAG)
                for (i in 0 until 4000) {
                    val x = random.nextFloat() * sizeF
                    val y = random.nextFloat() * sizeF
                    val radius = 0.25f + random.nextFloat() * 0.70f
                    val alpha = if (isDark) (5 + random.nextInt(10)) else (4 + random.nextInt(8))
                    stipplePaint.color = if (isDark) (alpha shl 24) or 0x00FFFFFF else (alpha shl 24)

                    for (dx in floatArrayOf(-sizeF, 0f, sizeF)) {
                        for (dy in floatArrayOf(-sizeF, 0f, sizeF)) {
                            val nx = x + dx
                            val ny = y + dy
                            if (nx + radius >= 0 && nx - radius <= sizeF && ny + radius >= 0 && ny - radius <= sizeF) {
                                canvas.drawCircle(nx, ny, radius, stipplePaint)
                            }
                        }
                    }
                }

                // 2. Interlocking delicate organic paper fibers with toroidal wrap
                val fiberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    strokeWidth = 0.55f
                    style = Paint.Style.STROKE
                }
                for (i in 0 until 220) {
                    val x = random.nextFloat() * sizeF
                    val y = random.nextFloat() * sizeF
                    val len = 2.5f + random.nextFloat() * 6.0f
                    val angle = random.nextFloat() * kotlin.math.PI.toFloat()
                    val x2 = x + kotlin.math.cos(angle) * len
                    val y2 = y + kotlin.math.sin(angle) * len
                    val alpha = if (isDark) (6 + random.nextInt(10)) else (5 + random.nextInt(8))
                    fiberPaint.color = if (isDark) (alpha shl 24) or 0x00FFFFFF else (alpha shl 24)

                    for (dx in floatArrayOf(-sizeF, 0f, sizeF)) {
                        for (dy in floatArrayOf(-sizeF, 0f, sizeF)) {
                            canvas.drawLine(x + dx, y + dy, x2 + dx, y2 + dy, fiberPaint)
                        }
                    }
                }

                BitmapShader(bmp, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
            }
            PaperTexture.PARCHMENT -> {
                val size = 512
                val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
                val canvas = android.graphics.Canvas(bmp)
                val random = java.util.Random(9999)
                val sizeF = size.toFloat()

                // 1. Organic cloudy mottling / aging blotches (soft radiant warm clouds - luminous, not dark)
                for (i in 0 until 28) {
                    val cx = random.nextFloat() * sizeF
                    val cy = random.nextFloat() * sizeF
                    val radius = 80f + random.nextFloat() * 160f
                    val baseAlpha = if (isDark) (4 + random.nextInt(8)) else (3 + random.nextInt(6))
                    val colorCenter = if (isDark) ((baseAlpha shl 24) or 0x00E0D4B8) else ((baseAlpha shl 24) or 0x00B89C68)
                    val colorEdge = 0x00000000

                    for (dx in floatArrayOf(-sizeF, 0f, sizeF)) {
                        for (dy in floatArrayOf(-sizeF, 0f, sizeF)) {
                            val nx = cx + dx
                            val ny = cy + dy
                            if (nx + radius >= 0 && nx - radius <= sizeF && ny + radius >= 0 && ny - radius <= sizeF) {
                                val radialGradient = android.graphics.RadialGradient(
                                    nx, ny, radius,
                                    colorCenter, colorEdge,
                                    Shader.TileMode.CLAMP
                                )
                                val radialPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                                    shader = radialGradient
                                }
                                canvas.drawCircle(nx, ny, radius, radialPaint)
                            }
                        }
                    }
                }

                // 2. Vintage specks & flecks with toroidal wrap - light and delicate
                val speckPaint = Paint(Paint.ANTI_ALIAS_FLAG)
                for (i in 0 until 600) {
                    val x = random.nextFloat() * sizeF
                    val y = random.nextFloat() * sizeF
                    val r = 0.3f + random.nextFloat() * 0.9f
                    val alpha = if (isDark) (6 + random.nextInt(12)) else (5 + random.nextInt(10))
                    speckPaint.color = if (isDark) ((alpha shl 24) or 0x00F5EBE0) else ((alpha shl 24) or 0x008C7050)
                    for (dx in floatArrayOf(-sizeF, 0f, sizeF)) {
                        for (dy in floatArrayOf(-sizeF, 0f, sizeF)) {
                            val nx = x + dx
                            val ny = y + dy
                            if (nx + r >= 0 && nx - r <= sizeF && ny + r >= 0 && ny - r <= sizeF) {
                                canvas.drawCircle(nx, ny, r, speckPaint)
                            }
                        }
                    }
                }

                BitmapShader(bmp, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
            }
        }
    }
}

@Composable
fun PdfViewport(
    state: ReaderState,
    pageBounds: Rect,
    displaySize: Size,
    pageSize: PageSize,
    onRenderPage: suspend (Int, Int) -> Bitmap?,
    onAction: (ReaderAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val transformer = remember { CoordinateTransformer() }
    var renderedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val imageBitmap = remember(renderedBitmap) { renderedBitmap?.asImageBitmap() }

    val density = LocalDensity.current.density
    val isDark = state.isDarkMode || state.paperColor.isDarkPalette
    val textureShader = rememberTextureShader(state.paperTexture, isDark, density, state.paperTexturePoints)

    // Render PDF page with native screen resolution & debounced high-resolution re-rendering upon zoom
    LaunchedEffect(state.currentPage, state.zoom, state.viewMode, displaySize.width, displaySize.height) {
        if (displaySize.width > 0f && displaySize.height > 0f) {
            val baseBounds = transformer.calculatePageBounds(
                viewportSize = displaySize,
                pageSize = pageSize,
                zoom = 1.0f,
                panOffset = Offset.Zero,
                viewMode = state.viewMode
            )

            val isZoomed = state.zoom > 1.05f
            if (isZoomed) {
                // Debounce during active continuous pinch / gesture to avoid UI thread contention
                kotlinx.coroutines.delay(160L)
            }

            // Calculate precise physical pixel dimensions based on display density and zoom level
            val effectiveZoom = if (isZoomed) state.zoom.coerceIn(1.0f, 3.5f) else 1.0f
            val targetWidth = (baseBounds.width * density * effectiveZoom).toInt().coerceIn(1200, 3840)
            val targetHeight = (baseBounds.height * density * effectiveZoom).toInt().coerceIn(1600, 4320)

            val bmp = onRenderPage(targetWidth, targetHeight)
            if (bmp != null) {
                renderedBitmap = bmp
            }
        }
    }

    val darkColorMatrix = remember {
        androidx.compose.ui.graphics.ColorMatrix(
            floatArrayOf(
                -1f, 0f, 0f, 0f, 255f,
                0f, -1f, 0f, 0f, 255f,
                0f, 0f, -1f, 0f, 255f,
                0f, 0f, 0f, 1f, 0f
            )
        )
    }

    val sepiaColorMatrix = remember {
        androidx.compose.ui.graphics.ColorMatrix(
            floatArrayOf(
                0.94f, 0.05f, 0.0f, 0.0f, 5.0f,
                0.05f, 0.88f, 0.05f, 0.0f, -10.0f,
                0.0f, 0.05f, 0.72f, 0.0f, -30.0f,
                0.0f, 0.0f, 0.0f, 1.0f, 0.0f
            )
        )
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasW = size.width
        val canvasH = size.height
        if (canvasW <= 0f || canvasH <= 0f) return@Canvas

        val paperBgColor = state.paperColor.getColor(state.isDarkMode)

        // 1. Draw Full-Screen Paper Surface Background
        drawRect(
            color = paperBgColor,
            topLeft = Offset.Zero,
            size = size
        )

        // 2. Draw Procedural Paper Texture Anchored to Document pageBounds and Zoom Scale
        if (textureShader != null) {
            val matrix = android.graphics.Matrix()
            // When zoomed, texture expands proportionally with state.zoom
            matrix.postScale(state.zoom, state.zoom)
            // Anchored to page top-left so it scales and pans in unison with the document
            matrix.postTranslate(pageBounds.left, pageBounds.top)
            textureShader.setLocalMatrix(matrix)

            drawIntoCanvas { canvas ->
                val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                    this.shader = textureShader
                }
                canvas.nativeCanvas.drawRect(0f, 0f, canvasW, canvasH, paint)
            }
        }

        // 3. Draw Traditional Page Border/Shadow if NOT in Seamless Mode
        if (!state.isSeamlessCanvas && pageBounds.width > 0f && pageBounds.height > 0f) {
            val shadowColor = if (isDark) Color(0x60000000) else Color(0x18000000)
            drawRoundRect(
                color = shadowColor,
                topLeft = Offset(pageBounds.left - 2f, pageBounds.top + 2f),
                size = Size(pageBounds.width + 4f, pageBounds.height + 4f),
                cornerRadius = CornerRadius(4f, 4f)
            )
            drawRect(
                color = paperBgColor,
                topLeft = Offset(pageBounds.left, pageBounds.top),
                size = Size(pageBounds.width, pageBounds.height)
            )
        }

        // 4. Draw PDF Content at exact dynamic pageBounds (Seamless Multi-Layer Blending)
        imageBitmap?.let { bmp ->
            if (pageBounds.width > 0f && pageBounds.height > 0f) {
                val colorFilter = when {
                    state.isSepiaMode -> androidx.compose.ui.graphics.ColorFilter.colorMatrix(sepiaColorMatrix)
                    isDark -> androidx.compose.ui.graphics.ColorFilter.colorMatrix(darkColorMatrix)
                    else -> null
                }

                val blendMode = if (state.isSeamlessCanvas) {
                    if (isDark) androidx.compose.ui.graphics.BlendMode.Screen else androidx.compose.ui.graphics.BlendMode.Multiply
                } else {
                    androidx.compose.ui.graphics.drawscope.DrawScope.DefaultBlendMode
                }

                drawImage(
                    image = bmp,
                    dstOffset = IntOffset(pageBounds.left.toInt(), pageBounds.top.toInt()),
                    dstSize = IntSize(pageBounds.width.toInt(), pageBounds.height.toInt()),
                    filterQuality = androidx.compose.ui.graphics.FilterQuality.High,
                    colorFilter = colorFilter,
                    blendMode = blendMode
                )
            }
        }

        // 5. Draw Search Match Highlights on Current Page
        if (state.isSearchVisible && state.searchMatches.isNotEmpty()) {
            val currentMatches = state.searchMatches.filter { it.pageIndex == state.currentPage - 1 }
            val activeMatch = state.searchMatches.getOrNull(state.currentSearchMatchIndex - 1)
            for (match in currentMatches) {
                val isActive = match == activeMatch
                for (rect in match.bounds) {
                    val left = pageBounds.left + rect.left * pageBounds.width
                    val top = pageBounds.top + rect.top * pageBounds.height
                    val right = pageBounds.left + rect.right * pageBounds.width
                    val bottom = pageBounds.top + rect.bottom * pageBounds.height

                    // Glow / Highlight fill
                    drawRoundRect(
                        color = if (isActive) Color(0xFFFF9500).copy(alpha = 0.55f) else Color(0xFFFFEB3B).copy(alpha = 0.45f),
                        topLeft = Offset(left, top),
                        size = Size((right - left).coerceAtLeast(4f), (bottom - top).coerceAtLeast(4f)),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                    // Border outline
                    drawRoundRect(
                        color = if (isActive) Color(0xFFFF9500) else Color(0xFFFFD600).copy(alpha = 0.8f),
                        topLeft = Offset(left, top),
                        size = Size((right - left).coerceAtLeast(4f), (bottom - top).coerceAtLeast(4f)),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = if (isActive) 2.5.dp.toPx() else 1.2.dp.toPx())
                    )
                }
            }
        }
    }
}
