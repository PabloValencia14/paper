package com.pablo.paper.desktop.ui.dock

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pablo.paper.desktop.model.OutlineNode
import com.pablo.paper.desktop.state.LeftDockTab
import com.pablo.paper.desktop.state.WorkspaceState

@Composable
fun DesktopLeftDock(
    state: WorkspaceState,
    modifier: Modifier = Modifier
) {
    val tab = state.activeTab

    Row(
        modifier = modifier
            .width(290.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surface)
            .border(width = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        // Vertical Tab Strip on the far left
        Column(
            modifier = Modifier
                .width(44.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .border(width = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LeftDockIconButton(
                icon = Icons.Default.GridOn,
                isSelected = state.leftDockTab == LeftDockTab.THUMBNAILS,
                tooltip = "Miniaturas",
                onClick = { state.leftDockTab = LeftDockTab.THUMBNAILS }
            )
            LeftDockIconButton(
                icon = Icons.Default.Bookmark,
                isSelected = state.leftDockTab == LeftDockTab.OUTLINE,
                tooltip = "Marcadores",
                onClick = { state.leftDockTab = LeftDockTab.OUTLINE }
            )
            LeftDockIconButton(
                icon = Icons.Default.ChatBubble,
                isSelected = state.leftDockTab == LeftDockTab.COMMENTS,
                tooltip = "Comentarios",
                onClick = { state.leftDockTab = LeftDockTab.COMMENTS }
            )
            LeftDockIconButton(
                icon = Icons.Default.Security,
                isSelected = state.leftDockTab == LeftDockTab.SIGNATURES,
                tooltip = "Firmas",
                onClick = { state.leftDockTab = LeftDockTab.SIGNATURES }
            )
            LeftDockIconButton(
                icon = Icons.Default.AttachFile,
                isSelected = state.leftDockTab == LeftDockTab.ATTACHMENTS,
                tooltip = "Adjuntos",
                onClick = { state.leftDockTab = LeftDockTab.ATTACHMENTS }
            )
        }

        // Active Tab Pane Content
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(10.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = state.leftDockTab.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(
                    onClick = { state.isLeftDockOpen = false },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar dock",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
            )

            if (tab == null || !tab.isLoaded) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "Sin documento abierto",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
                return@Column
            }

            when (state.leftDockTab) {
                LeftDockTab.THUMBNAILS -> {
                    ThumbnailsList(tab = tab)
                }
                LeftDockTab.OUTLINE -> {
                    OutlineTree(nodes = tab.outlineNodes, onSelectPage = { tab.currentPage = it })
                }
                LeftDockTab.COMMENTS -> {
                    CommentsList(tab = tab)
                }
                LeftDockTab.SIGNATURES -> {
                    SignaturesList(tab = tab)
                }
                LeftDockTab.ATTACHMENTS -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay archivos adjuntos en este PDF", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun LeftDockIconButton(
    icon: ImageVector,
    isSelected: Boolean,
    tooltip: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val bg = when {
        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
        isHovered -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        else -> Color.Transparent
    }

    val tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .hoverable(interactionSource = interactionSource)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = tooltip, tint = tint, modifier = Modifier.size(18.dp))
    }
}

@Composable
fun ThumbnailsList(tab: com.pablo.paper.desktop.state.TabDocumentState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(tab.pageCount) { pageIndex ->
            val isCurrent = pageIndex == tab.currentPage
            var thumbBmp by remember(pageIndex) { mutableStateOf<ImageBitmap?>(null) }

            LaunchedEffect(pageIndex) {
                thumbBmp = tab.engine.renderThumbnail(pageIndex, 180)
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { tab.currentPage = pageIndex }
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(130.dp, 175.dp)
                        .shadow(if (isCurrent) 6.dp else 2.dp, RoundedCornerShape(4.dp))
                        .background(Color.White, RoundedCornerShape(4.dp))
                        .border(
                            width = if (isCurrent) 2.dp else 1.dp,
                            color = if (isCurrent) MaterialTheme.colorScheme.primary else Color(0xFFCBD5E1),
                            shape = RoundedCornerShape(4.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val bmp = thumbBmp
                    if (bmp != null) {
                        Image(
                            bitmap = bmp,
                            contentDescription = "Página ${pageIndex + 1}",
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(4.dp))
                        )
                    } else {
                        CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                    }
                }
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isCurrent) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Pág. ${pageIndex + 1}",
                        fontSize = 11.sp,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                        color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun OutlineTree(
    nodes: List<OutlineNode>,
    onSelectPage: (Int) -> Unit
) {
    if (nodes.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "Este documento no contiene marcadores jerárquicos.",
                fontSize = 11.sp,
                color = Color.Gray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
        return
    }

    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(3.dp)) {
        items(nodes) { node ->
            OutlineNodeRow(node = node, onSelectPage = onSelectPage)
        }
    }
}

@Composable
fun OutlineNodeRow(node: OutlineNode, onSelectPage: (Int) -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(5.dp))
                .background(if (isHovered) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f) else Color.Transparent)
                .hoverable(interactionSource = interactionSource)
                .clickable(interactionSource = interactionSource, indication = null) { onSelectPage(node.pageIndex) }
                .padding(start = (node.level * 12 + 4).dp, top = 5.dp, bottom = 5.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(4.dp))
            Text(
                text = node.title,
                fontSize = 12.sp,
                fontWeight = if (node.level == 0) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 5.dp, vertical = 1.dp)
            ) {
                Text(
                    text = "${node.pageIndex + 1}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        node.children.forEach { child ->
            OutlineNodeRow(node = child, onSelectPage = onSelectPage)
        }
    }
}

@Composable
fun CommentsList(tab: com.pablo.paper.desktop.state.TabDocumentState) {
    val annotations = tab.annotations
    if (annotations.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "No hay anotaciones ni comentarios en este documento.",
                fontSize = 11.sp,
                color = Color.Gray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
        return
    }

    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(annotations) { ann ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .clickable { tab.currentPage = ann.pageIndex }
                    .padding(10.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = ann.type.name.replace("_", " "),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Pág. ${ann.pageIndex + 1}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    if (!ann.textContent.isNullOrBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = ann.textContent,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SignaturesList(tab: com.pablo.paper.desktop.state.TabDocumentState) {
    val meta = tab.metadata
    Column(
        modifier = Modifier.fillMaxSize().padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(if (meta.isSigned) Color(0xFF10B981).copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .border(1.dp, if (meta.isSigned) Color(0xFF10B981) else Color.LightGray.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            Column {
                Text(
                    text = if (meta.isSigned) "✓ Certificado Digital Válido" else "Sin firmas digitales",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (meta.isSigned) Color(0xFF059669) else Color.Gray
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (meta.isSigned) "El documento cuenta con firma criptográfica PKCS#12 intacta." else "No se han detectado firmas electrónicas en este PDF.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
