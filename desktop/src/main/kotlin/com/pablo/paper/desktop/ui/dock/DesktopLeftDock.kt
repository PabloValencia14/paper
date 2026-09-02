package com.pablo.paper.desktop.ui.dock

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pablo.paper.desktop.model.OutlineNode
import com.pablo.paper.desktop.state.LeftDockTab
import com.pablo.paper.desktop.state.TabDocumentState
import com.pablo.paper.desktop.state.WorkspaceState

@Composable
fun DesktopLeftDock(state: WorkspaceState, modifier: Modifier = Modifier) {
    val tab = state.activeTab ?: return
    Row(
        modifier = modifier
            .width(258.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier
                .width(42.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f))
                .padding(top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            DockRailButton(Icons.Default.GridView, "Páginas", state.leftDockTab == LeftDockTab.THUMBNAILS) { state.leftDockTab = LeftDockTab.THUMBNAILS }
            DockRailButton(Icons.Default.Bookmark, "Índice", state.leftDockTab == LeftDockTab.OUTLINE) { state.leftDockTab = LeftDockTab.OUTLINE }
            DockRailButton(Icons.Default.ChatBubbleOutline, "Marcas", state.leftDockTab == LeftDockTab.COMMENTS) { state.leftDockTab = LeftDockTab.COMMENTS }
        }
        Column(modifier = Modifier.weight(1f).fillMaxHeight().padding(horizontal = 10.dp, vertical = 9.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(state.leftDockTab.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                IconButton(onClick = { state.isLeftDockOpen = false }, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar navegación", modifier = Modifier.size(16.dp))
                }
            }
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)))
            Spacer(Modifier.height(8.dp))
            when (state.leftDockTab) {
                LeftDockTab.THUMBNAILS -> ThumbnailsList(tab)
                LeftDockTab.OUTLINE -> OutlineTree(tab.outlineNodes) { tab.currentPage = it }
                LeftDockTab.COMMENTS -> MarksList(tab)
            }
        }
    }
}

@Composable
private fun DockRailButton(icon: androidx.compose.ui.graphics.vector.ImageVector, description: String, selected: Boolean, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent)
    ) {
        Icon(
            icon,
            contentDescription = description,
            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun ThumbnailsList(tab: TabDocumentState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(9.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items((0 until tab.pageCount).toList(), key = { it }) { pageIndex ->
            val selected = pageIndex == tab.currentPage
            var image by remember(pageIndex) { mutableStateOf<ImageBitmap?>(null) }
            LaunchedEffect(pageIndex) { image = tab.engine.renderThumbnail(pageIndex, 170) }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(5.dp))
                    .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.09f) else Color.Transparent)
                    .clickable { tab.currentPage = pageIndex }
                    .padding(7.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(118.dp, 154.dp)
                        .background(Color.White, RoundedCornerShape(2.dp))
                        .border(
                            width = if (selected) 2.dp else 1.dp,
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(2.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    image?.let {
                        Image(it, contentDescription = "Miniatura de la página ${pageIndex + 1}", modifier = Modifier.fillMaxSize())
                    } ?: CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${pageIndex + 1}",
                    fontSize = 11.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun OutlineTree(nodes: List<OutlineNode>, onSelectPage: (Int) -> Unit) {
    if (nodes.isEmpty()) {
        EmptyDockText("Este documento no incluye un índice navegable.")
        return
    }
    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        items(nodes) { node -> OutlineRow(node, onSelectPage) }
    }
}

@Composable
private fun OutlineRow(node: OutlineNode, onSelectPage: (Int) -> Unit) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .clickable { onSelectPage(node.pageIndex) }
                .padding(start = (node.level * 10 + 3).dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, modifier = Modifier.size(15.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(node.title, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            Text("${node.pageIndex + 1}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        node.children.forEach { child -> OutlineRow(child, onSelectPage) }
    }
}

@Composable
private fun MarksList(tab: TabDocumentState) {
    if (tab.annotations.isEmpty()) {
        EmptyDockText("Todavía no hay marcas guardadas en esta sesión.")
        return
    }
    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        items(tab.annotations, key = { it.id }) { annotation ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { tab.currentPage = annotation.pageIndex }
                    .padding(horizontal = 5.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(Color(annotation.color.toULong())))
                Spacer(Modifier.width(7.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(annotationLabel(annotation.type), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Text("Página ${annotation.pageIndex + 1}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = { tab.removeAnnotation(annotation.id) }, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Eliminar marca", modifier = Modifier.size(15.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun EmptyDockText(text: String) {
    Box(Modifier.fillMaxSize().padding(18.dp), contentAlignment = Alignment.Center) {
        Text(text, fontSize = 12.sp, lineHeight = 18.sp, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun annotationLabel(type: com.pablo.paper.desktop.model.AnnotationType): String = when (type) {
    com.pablo.paper.desktop.model.AnnotationType.INK -> "Trazo"
    com.pablo.paper.desktop.model.AnnotationType.HIGHLIGHT -> "Resaltado"
    else -> "Marca"
}
