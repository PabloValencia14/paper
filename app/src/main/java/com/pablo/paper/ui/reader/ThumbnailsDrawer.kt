package com.pablo.paper.ui.reader

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pablo.paper.domain.model.ReaderAction
import com.pablo.paper.domain.model.ReaderState
import com.pablo.paper.domain.model.ThumbnailsFilter
import com.pablo.paper.pdf.PageSize

@Composable
fun ThumbnailsDrawer(
    state: ReaderState,
    onAction: (ReaderAction) -> Unit,
    onRenderThumbnail: suspend (pageIndex: Int, width: Int, height: Int) -> Bitmap?,
    getPageSizeForPage: (Int) -> PageSize,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = state.isThumbnailsDrawerOpen,
        enter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn(),
        exit = slideOutHorizontally(targetOffsetX = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f))
                .clickable { onAction(ReaderAction.SetThumbnailsDrawerOpen(false)) }
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(360.dp)
                    .clickable(enabled = false) {}, // Prevent dismiss when tapping inside
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shadowElevation = 16.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Miniaturas de Página",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(
                            onClick = { onAction(ReaderAction.SetThumbnailsDrawerOpen(false)) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Filter Chips: Todas, Marcadas, Anotadas
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = state.thumbnailsFilter == ThumbnailsFilter.ALL,
                            onClick = { onAction(ReaderAction.SelectThumbnailsFilter(ThumbnailsFilter.ALL)) },
                            label = { Text("Todas (${state.pageCount})", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors()
                        )
                        FilterChip(
                            selected = state.thumbnailsFilter == ThumbnailsFilter.BOOKMARKED,
                            onClick = { onAction(ReaderAction.SelectThumbnailsFilter(ThumbnailsFilter.BOOKMARKED)) },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Marcadas (${state.bookmarkedPages.size})", fontSize = 12.sp)
                                }
                            }
                        )
                        FilterChip(
                            selected = state.thumbnailsFilter == ThumbnailsFilter.ANNOTATED,
                            onClick = { onAction(ReaderAction.SelectThumbnailsFilter(ThumbnailsFilter.ANNOTATED)) },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Create,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Anotadas", fontSize = 12.sp)
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Filtered Pages Grid
                    val allPages = (1..state.pageCount).toList()
                    val filteredPages = when (state.thumbnailsFilter) {
                        ThumbnailsFilter.ALL -> allPages
                        ThumbnailsFilter.BOOKMARKED -> allPages.filter { state.bookmarkedPages.contains(it) }
                        ThumbnailsFilter.ANNOTATED -> allPages.filter { state.annotatedPageIndices.contains(it - 1) }
                    }

                    if (filteredPages.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No hay páginas en esta categoría",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(bottom = 16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            items(filteredPages) { pageNumber ->
                                val pageIndex = pageNumber - 1
                                val isCurrent = pageNumber == state.currentPage
                                val isBookmarked = state.bookmarkedPages.contains(pageNumber)
                                val isAnnotated = state.annotatedPageIndices.contains(pageIndex)

                                ThumbnailItemCard(
                                    pageNumber = pageNumber,
                                    pageIndex = pageIndex,
                                    isCurrent = isCurrent,
                                    isBookmarked = isBookmarked,
                                    isAnnotated = isAnnotated,
                                    getPageSize = { getPageSizeForPage(pageIndex) },
                                    onRenderThumbnail = onRenderThumbnail,
                                    onPageSelected = {
                                        onAction(ReaderAction.GoToPage(pageNumber))
                                        onAction(ReaderAction.SetThumbnailsDrawerOpen(false))
                                    },
                                    onToggleBookmark = {
                                        onAction(ReaderAction.ToggleBookmark(pageNumber))
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ThumbnailItemCard(
    pageNumber: Int,
    pageIndex: Int,
    isCurrent: Boolean,
    isBookmarked: Boolean,
    isAnnotated: Boolean,
    getPageSize: () -> PageSize,
    onRenderThumbnail: suspend (pageIndex: Int, width: Int, height: Int) -> Bitmap?,
    onPageSelected: () -> Unit,
    onToggleBookmark: () -> Unit
) {
    var thumbBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val pageSize = getPageSize()
    val aspect = if (pageSize.height > 0) pageSize.width.toFloat() / pageSize.height.toFloat() else 0.75f

    LaunchedEffect(pageIndex) {
        val bmp = onRenderThumbnail(pageIndex, 280, (280 / aspect).toInt())
        if (bmp != null) {
            thumbBitmap = bmp
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onPageSelected)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(aspect)
                .shadow(if (isCurrent) 8.dp else 2.dp, RoundedCornerShape(6.dp))
                .background(Color.White, RoundedCornerShape(6.dp))
                .border(
                    width = if (isCurrent) 3.dp else 1.dp,
                    color = if (isCurrent) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(6.dp)
                )
                .clip(RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            val bmp = thumbBitmap
            if (bmp != null && !bmp.isRecycled) {
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "Pág $pageNumber",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    text = "$pageNumber",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            // Top-right bookmark button
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(26.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    .clickable(onClick = onToggleBookmark),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = "Marcador",
                    tint = if (isBookmarked) Color(0xFFFFD700) else Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Annotated indicator badge
            if (isAnnotated) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp)
                        .size(20.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.85f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Create,
                        contentDescription = "Anotada",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Pág. $pageNumber",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                fontSize = 11.sp
            ),
            color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}
