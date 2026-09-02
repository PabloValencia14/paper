package com.pablo.paper.desktop.ui.dock

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pablo.paper.desktop.model.AiModelInfo
import com.pablo.paper.desktop.model.MessageRole
import com.pablo.paper.desktop.state.RightDockTab
import com.pablo.paper.desktop.state.WorkspaceState
import com.pablo.paper.desktop.ui.common.DesktopMarkdownContent

@Composable
fun DesktopRightDock(
    state: WorkspaceState,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .width(380.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surface)
            .border(width = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        // Content Area
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = when (state.rightDockTab) {
                            RightDockTab.AI_ASSISTANT -> Icons.Default.AutoAwesome
                            RightDockTab.MARKDOWN_NOTES -> Icons.Default.Edit
                            RightDockTab.FLASHCARDS -> Icons.Default.Quiz
                            RightDockTab.METADATA -> Icons.Default.Info
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = state.rightDockTab.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(
                    onClick = { state.isRightDockOpen = false },
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

            when (state.rightDockTab) {
                RightDockTab.AI_ASSISTANT -> {
                    AiAssistantPane(state = state)
                }
                RightDockTab.MARKDOWN_NOTES -> {
                    MarkdownNotesPane(state = state)
                }
                RightDockTab.FLASHCARDS -> {
                    FlashcardsPane(state = state)
                }
                RightDockTab.METADATA -> {
                    MetadataPane(state = state)
                }
            }
        }

        // Vertical Tab Strip on the far right
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
                icon = Icons.Default.AutoAwesome,
                isSelected = state.rightDockTab == RightDockTab.AI_ASSISTANT,
                tooltip = "Asistente IA",
                onClick = { state.rightDockTab = RightDockTab.AI_ASSISTANT }
            )
            LeftDockIconButton(
                icon = Icons.Default.Edit,
                isSelected = state.rightDockTab == RightDockTab.MARKDOWN_NOTES,
                tooltip = "Notas",
                onClick = { state.rightDockTab = RightDockTab.MARKDOWN_NOTES }
            )
            LeftDockIconButton(
                icon = Icons.Default.Quiz,
                isSelected = state.rightDockTab == RightDockTab.FLASHCARDS,
                tooltip = "Estudio",
                onClick = { state.rightDockTab = RightDockTab.FLASHCARDS }
            )
            LeftDockIconButton(
                icon = Icons.Default.Info,
                isSelected = state.rightDockTab == RightDockTab.METADATA,
                tooltip = "Propiedades",
                onClick = { state.rightDockTab = RightDockTab.METADATA }
            )
        }
    }
}

@Composable
fun AiAssistantPane(state: WorkspaceState) {
    var inputPrompt by remember { mutableStateOf("") }
    var showModelMenu by remember { mutableStateOf(false) }
    var showApiKeyPopover by remember { mutableStateOf(false) }
    var showPassword by remember { mutableStateOf(false) }
    val chatListState = rememberLazyListState()

    LaunchedEffect(state.assistantMessages.size) {
        if (state.assistantMessages.isNotEmpty()) {
            chatListState.animateScrollToItem(state.assistantMessages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Model Selector Bar with Free Tag Badges
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(modifier = Modifier.weight(1f)) {
                val currentModel = state.availableModels.find { it.id == state.selectedModelId }
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { showModelMenu = true }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = currentModel?.name ?: state.selectedModelId,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (currentModel?.isFree == true) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF10B981).copy(alpha = 0.15f))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text("GRATIS", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color(0xFF059669))
                        }
                    }
                }

                DropdownMenu(
                    expanded = showModelMenu,
                    onDismissRequest = { showModelMenu = false },
                    modifier = Modifier
                        .width(320.dp)
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    state.availableModels.forEach { model ->
                        val isPicked = model.id == state.selectedModelId
                        DropdownMenuItem(
                            text = {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = model.name,
                                            fontWeight = if (isPicked) FontWeight.Bold else FontWeight.SemiBold,
                                            fontSize = 12.sp,
                                            color = if (isPicked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                        if (model.isFree) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color(0xFF10B981).copy(alpha = 0.15f))
                                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                                            ) {
                                                Text("FREE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF059669))
                                            }
                                        }
                                    }
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = model.description,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2
                                    )
                                }
                            },
                            onClick = {
                                state.selectedModelId = model.id
                                showModelMenu = false
                            }
                        )
                    }
                }
            }

            // API Key & Clear Actions
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { showApiKeyPopover = !showApiKeyPopover },
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = "Configurar API Key",
                        tint = if (state.openRouterApiKey.isNotBlank()) Color(0xFF10B981) else Color.Gray,
                        modifier = Modifier.size(15.dp)
                    )
                }
                IconButton(
                    onClick = { state.clearChat() },
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Limpiar conversación",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // API Key Popover
        if (showApiKeyPopover) {
            Spacer(Modifier.height(6.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("Clave de API de OpenRouter:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = state.openRouterApiKey,
                        onValueChange = { state.openRouterApiKey = it },
                        placeholder = { Text("sk-or-v1-...", fontSize = 11.sp) },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }, modifier = Modifier.size(24.dp)) {
                                Icon(if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text("Los modelos marcados como (Free) pueden usarse sin saldo.", fontSize = 10.sp, color = Color.Gray)
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Quick Prompt Pills
        val quickScroll = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(quickScroll),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            PromptPill(label = "📐 Fórmulas LaTeX", icon = Icons.Default.Psychology) {
                state.sendAiMessage("Explica y genera las fórmulas matemáticas, modelos teóricos o ecuaciones relevantes asociadas a este documento utilizando notación matemática LaTeX detallada ($$...$$).")
            }
            PromptPill(label = "📊 Diagrama UML", icon = Icons.Default.AutoAwesome) {
                state.sendAiMessage("Genera un diagrama conceptual o de arquitectura en formato Mermaid que represente los conceptos descritos en este documento.")
            }
            PromptPill(label = "📑 Resumir página", icon = Icons.Default.Edit) {
                val p = (state.activeTab?.currentPage ?: 0) + 1
                state.sendAiMessage("Resume con puntos clave y rigor analítico la página $p de este documento.")
            }
            PromptPill(label = "📋 Tabla comparativa", icon = Icons.Default.Quiz) {
                state.sendAiMessage("Genera una tabla Markdown que compare y sintetice los conceptos y variables clave del documento.")
            }
        }

        Spacer(Modifier.height(10.dp))

        // Chat Message Stream
        LazyColumn(
            state = chatListState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (state.assistantMessages.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = "¿En qué puedo ayudarte con este documento?",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Elige una sugerencia arriba o escribe tu consulta.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }

            items(state.assistantMessages) { msg ->
                val isUser = msg.role == MessageRole.USER

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(if (isUser) 0.9f else 1.0f),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUser) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                            }
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 0.5.dp,
                            color = if (isUser) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isUser) Icons.Default.Edit else Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = if (isUser) "Tú" else "Paper AI (${msg.modelUsed?.split("/")?.lastOrNull() ?: "Asistente"})",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                            DesktopMarkdownContent(markdown = msg.content)
                        }
                    }
                }
            }

            if (state.isAiThinking) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                        Text(
                            text = "Pensando y analizando con ${state.selectedModelId}...",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Chat Input Box
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            OutlinedTextField(
                value = inputPrompt,
                onValueChange = { inputPrompt = it },
                placeholder = { Text("Escribe una pregunta sobre el PDF...", fontSize = 12.sp, color = Color.Gray) },
                modifier = Modifier
                    .weight(1f)
                    .onPreviewKeyEvent { event ->
                        if (event.key == Key.Enter && event.type == KeyEventType.KeyDown && !event.isCtrlPressed) {
                            val p = inputPrompt.trim()
                            if (p.isNotBlank() && !state.isAiThinking) {
                                state.sendAiMessage(p)
                                inputPrompt = ""
                            }
                            true
                        } else false
                    },
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                maxLines = 4
            )
            IconButton(
                onClick = {
                    val p = inputPrompt.trim()
                    if (p.isNotBlank()) {
                        state.sendAiMessage(p)
                        inputPrompt = ""
                    }
                },
                enabled = inputPrompt.isNotBlank() && !state.isAiThinking,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (inputPrompt.isNotBlank() && !state.isAiThinking) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Enviar",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun PromptPill(label: String, icon: ImageVector, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isHovered) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .border(1.dp, if (isHovered) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else Color.Transparent, RoundedCornerShape(12.dp))
            .hoverable(interactionSource = interactionSource)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
        Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun MarkdownNotesPane(state: WorkspaceState) {
    val tab = state.activeTab
    if (tab == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Abre un PDF para redactar notas", fontSize = 12.sp, color = Color.Gray)
        }
        return
    }

    var isPreview by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                PromptPill(label = if (isPreview) "Modo Edición" else "Vista Previa", icon = Icons.Default.Edit) {
                    isPreview = !isPreview
                }
            }
        }
        Spacer(Modifier.height(8.dp))

        if (isPreview) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(10.dp)
            ) {
                item {
                    DesktopMarkdownContent(markdown = tab.documentNotes.ifBlank { "*No hay apuntes redactados aún.*" })
                }
            }
        } else {
            OutlinedTextField(
                value = tab.documentNotes,
                onValueChange = { tab.documentNotes = it; tab.isDirty = true },
                placeholder = { Text("Escribe tus apuntes en Markdown...", fontSize = 12.sp) },
                modifier = Modifier.fillMaxSize(),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                shape = RoundedCornerShape(8.dp)
            )
        }
    }
}

@Composable
fun FlashcardsPane(state: WorkspaceState) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Fichas de Estudio y Active Recall", fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Text(
            text = "Genera tarjetas de preguntas y respuestas inteligentes basadas en el contenido del documento PDF.",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        PromptPill(label = "✨ Generar 5 Fichas con IA", icon = Icons.Default.Quiz) {
            state.sendAiMessage("Genera 5 tarjetas de estudio clave con preguntas y respuestas detalladas basadas en este documento.")
        }
    }
}

@Composable
fun MetadataPane(state: WorkspaceState) {
    val meta = state.activeTab?.metadata ?: return
    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            MetadataCard(label = "Título", value = meta.title.ifBlank { "Sin título" })
            MetadataCard(label = "Autor", value = meta.author.ifBlank { "Desconocido" })
            MetadataCard(label = "Total Páginas", value = "${meta.pageCount}")
            MetadataCard(label = "Versión PDF", value = "PDF ${meta.pdfVersion}")
            MetadataCard(label = "Tamaño en disco", value = "${(meta.fileSize / 1024)} KB")
            MetadataCard(label = "Cifrado AES", value = if (meta.isEncrypted) "Protegido con Contraseña" else "Sin cifrado")
            MetadataCard(label = "Firma Criptográfica", value = if (meta.isSigned) "Firmado Digitalmente" else "Sin firmas")
            MetadataCard(label = "Formularios AcroForms", value = if (meta.hasAcroForms) "Contiene campos interactivos" else "No")
        }
    }
}

@Composable
fun MetadataCard(label: String, value: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
