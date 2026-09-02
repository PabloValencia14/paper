package com.pablo.paper.desktop.ui.dock

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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pablo.paper.desktop.model.MessageRole
import com.pablo.paper.desktop.state.RightDockTab
import com.pablo.paper.desktop.state.WorkspaceState
import com.pablo.paper.desktop.ui.common.DesktopMarkdownContent

@Composable
fun DesktopRightDock(state: WorkspaceState, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .width(336.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier.weight(1f).fillMaxHeight().padding(horizontal = 12.dp, vertical = 9.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(state.rightDockTab.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                IconButton(onClick = { state.isRightDockOpen = false }, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar panel", modifier = Modifier.size(16.dp))
                }
            }
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)))
            Spacer(Modifier.height(8.dp))
            when (state.rightDockTab) {
                RightDockTab.AI_ASSISTANT -> AiAssistantPane(state)
                RightDockTab.MARKDOWN_NOTES -> MarkdownNotesPane(state)
                RightDockTab.METADATA -> MetadataPane(state)
            }
        }
        Column(
            modifier = Modifier
                .width(42.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f))
                .padding(top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            RightRailButton(Icons.Default.AutoAwesome, "Asistente", state.rightDockTab == RightDockTab.AI_ASSISTANT) { state.rightDockTab = RightDockTab.AI_ASSISTANT }
            RightRailButton(Icons.Default.EditNote, "Notas", state.rightDockTab == RightDockTab.MARKDOWN_NOTES) { state.rightDockTab = RightDockTab.MARKDOWN_NOTES }
            RightRailButton(Icons.Default.Info, "Información del documento", state.rightDockTab == RightDockTab.METADATA) { state.rightDockTab = RightDockTab.METADATA }
        }
    }
}

@Composable
private fun RightRailButton(icon: androidx.compose.ui.graphics.vector.ImageVector, description: String, selected: Boolean, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent)
    ) {
        Icon(icon, contentDescription = description, modifier = Modifier.size(18.dp), tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun AiAssistantPane(state: WorkspaceState) {
    var prompt by remember { mutableStateOf("") }
    var showModels by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    LaunchedEffect(state.assistantMessages.size) {
        if (state.assistantMessages.isNotEmpty()) listState.animateScrollToItem(state.assistantMessages.lastIndex)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                .padding(start = 8.dp, end = 3.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.SmartToy, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Box(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.clickable { showModels = true }.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = state.availableModels.firstOrNull { it.id == state.selectedModelId }?.name ?: state.selectedModelId,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
                DropdownMenu(expanded = showModels, onDismissRequest = { showModels = false }, modifier = Modifier.width(278.dp)) {
                    state.availableModels.forEach { model ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(model.name, fontSize = 12.sp, fontWeight = if (model.id == state.selectedModelId) FontWeight.Bold else FontWeight.Normal)
                                    if (model.description.isNotBlank()) Text(model.description, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                                }
                            },
                            onClick = { state.selectedModelId = model.id; showModels = false }
                        )
                    }
                }
            }
            IconButton(onClick = state::refreshAiModels, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Refresh, contentDescription = "Actualizar modelos del proxy", modifier = Modifier.size(15.dp))
            }
            IconButton(onClick = state::clearChat, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.DeleteSweep, contentDescription = "Limpiar conversación", modifier = Modifier.size(16.dp))
            }
        }
        Text(
            text = "IA local · ${state.aiEndpoint.removePrefix("http://").removePrefix("https://")}",
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 5.dp)
        )
        Spacer(Modifier.height(8.dp))

        if (state.assistantMessages.isEmpty()) {
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
                Text("Pregunta sobre lo que estás leyendo.", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text("El contexto se toma del PDF abierto y se envía solo al proxy local configurado.", fontSize = 11.sp, lineHeight = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
                Spacer(Modifier.height(10.dp))
                PromptAction("Resumir la página") { state.sendAiMessage("Resume con precisión la página activa en puntos clave.") }
                PromptAction("Explicar un concepto") { state.sendAiMessage("Explica los conceptos centrales de la página activa y distingue lo que afirma el documento de tus inferencias.") }
                PromptAction("Extraer preguntas de estudio") { state.sendAiMessage("Formula preguntas de estudio basadas únicamente en el contenido del documento y responde brevemente a cada una.") }
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(state.assistantMessages, key = { it.id }) { message ->
                val user = message.role == MessageRole.USER
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 2.dp,
                            color = if (user) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(2.dp)
                        )
                        .padding(start = 9.dp, end = 5.dp, top = 5.dp, bottom = 7.dp)
                ) {
                    Text(
                        text = if (user) "TÚ" else "PAPER",
                        fontSize = 10.sp,
                        letterSpacing = 0.7.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (user) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    DesktopMarkdownContent(markdown = message.content)
                }
            }
            if (state.isAiThinking) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
                        androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Text("Consultando el proxy local…", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = prompt,
                onValueChange = { prompt = it },
                placeholder = { Text("Escribe una pregunta", fontSize = 12.sp) },
                maxLines = 4,
                modifier = Modifier
                    .weight(1f)
                    .onPreviewKeyEvent { event ->
                        if (event.key == Key.Enter && event.type == KeyEventType.KeyDown) {
                            val text = prompt.trim()
                            if (text.isNotBlank() && !state.isAiThinking) {
                                state.sendAiMessage(text)
                                prompt = ""
                            }
                            true
                        } else false
                    },
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
            )
            Spacer(Modifier.width(5.dp))
            IconButton(
                onClick = {
                    val text = prompt.trim()
                    if (text.isNotBlank()) {
                        state.sendAiMessage(text)
                        prompt = ""
                    }
                },
                enabled = prompt.isNotBlank() && !state.isAiThinking,
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (prompt.isNotBlank() && !state.isAiThinking) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar pregunta", tint = if (prompt.isNotBlank() && !state.isAiThinking) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun PromptAction(label: String, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(label, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Start)
    }
}

@Composable
private fun MarkdownNotesPane(state: WorkspaceState) {
    val tab = state.activeTab ?: return
    Column(modifier = Modifier.fillMaxSize()) {
        Text("Apuntes del documento", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Text("Se guardan junto al PDF en una sesión de Paper.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 3.dp))
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = tab.documentNotes,
            onValueChange = { tab.documentNotes = it; tab.isDirty = true },
            placeholder = { Text("Escribe tus apuntes…", fontSize = 12.sp) },
            modifier = Modifier.fillMaxSize(),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
        )
    }
}

@Composable
private fun MetadataPane(state: WorkspaceState) {
    val meta = state.activeTab?.metadata ?: return
    val rows = listOf(
        "Título" to meta.title.ifBlank { "Sin título" },
        "Autor" to meta.author.ifBlank { "No indicado" },
        "Páginas" to meta.pageCount.toString(),
        "Tamaño" to "${meta.fileSize / 1024} KB",
        "PDF" to meta.pdfVersion,
        "Cifrado" to if (meta.isEncrypted) "Sí" else "No",
        "Firmas" to if (meta.isSigned) "Detectadas" else "No detectadas"
    )
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(rows) { (label, value) ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(80.dp))
                Text(value, fontSize = 12.sp, modifier = Modifier.weight(1f))
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)))
        }
    }
}
