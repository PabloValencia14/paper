package com.pablo.paper.ui.reader

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pablo.paper.ai.OpenRouterModels
import com.pablo.paper.domain.model.AssistantMessage
import com.pablo.paper.domain.model.Document
import com.pablo.paper.domain.model.MessageRole
import com.pablo.paper.ui.theme.AccentBlue
import com.pablo.paper.ui.theme.BorderSubtle
import com.pablo.paper.ui.theme.BorderSubtleDark
import com.pablo.paper.ui.theme.CanvasBackground
import com.pablo.paper.ui.theme.SurfaceToolbar
import com.pablo.paper.ui.theme.TextPrimary
import com.pablo.paper.ui.theme.TextPrimaryDark
import com.pablo.paper.ui.theme.TextSecondary
import com.pablo.paper.ui.theme.TextSecondaryDark
import com.pablo.paper.ui.common.MarkdownContent

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AssistantPanel(
    document: Document?,
    currentPage: Int,
    pageCount: Int,
    messages: List<AssistantMessage>,
    isLoading: Boolean,
    aiProvider: com.pablo.paper.ai.AiProvider = com.pablo.paper.ai.AiProvider.GOOGLE_GEMINI,
    selectedModel: String,
    apiKey: String,
    isApiKeyDialogOpen: Boolean,
    isDarkMode: Boolean = false,
    onSendMessage: (String) -> Unit,
    onExplainHighlights: () -> Unit = {},
    onSelectAiProvider: (com.pablo.paper.ai.AiProvider) -> Unit = {},
    onSelectModel: (String) -> Unit,
    onSetApiKey: (String) -> Unit,
    onSetApiKeyDialogOpen: (Boolean) -> Unit,
    onClearChat: () -> Unit,
    onAppendNote: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    var isModelMenuExpanded by remember { mutableStateOf(false) }
    var isCustomModelDialogOpen by remember { mutableStateOf(false) }
    var customModelInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size, isLoading) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF8B5CF6).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFF8B5CF6),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Asistente IA",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 15.sp
                        )
                    )
                    Text(
                        text = if (apiKey.isNotBlank()) "Conectado a ${aiProvider.shortName}" else "API Key: ${aiProvider.shortName} (Gratis)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (apiKey.isNotBlank()) Color(0xFF10B981) else Color(0xFFF59E0B),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // API Key Settings
                IconButton(
                    onClick = { onSetApiKeyDialogOpen(true) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Key,
                        contentDescription = "Configurar API Key",
                        tint = if (apiKey.isNotBlank()) Color(0xFF10B981) else Color(0xFFF59E0B),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Clear Chat
                if (messages.isNotEmpty()) {
                    IconButton(
                        onClick = onClearChat,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "Limpiar Chat",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Close
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Cerrar",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // AI Provider Quick Selector Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            com.pablo.paper.ai.AiProvider.entries.forEach { provider ->
                val isSelected = aiProvider == provider
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) AccentBlue.copy(alpha = 0.18f) else Color.Transparent,
                    border = BorderStroke(1.dp, if (isSelected) AccentBlue else (if (isDarkMode) BorderSubtleDark else BorderSubtle)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectAiProvider(provider) }
                ) {
                    Text(
                        text = provider.shortName,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 10.5.sp
                        ),
                        color = if (isSelected) AccentBlue else (if (isDarkMode) TextSecondaryDark else TextSecondary),
                        modifier = Modifier.padding(vertical = 5.dp, horizontal = 4.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Model Selector Dropdown Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(CanvasBackground)
                .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                .clickable { isModelMenuExpanded = true }
                .padding(horizontal = 10.dp, vertical = 7.dp)
        ) {
            val providerModels = OpenRouterModels.getModelsForProvider(aiProvider)
            val matchedModel = providerModels.find { it.id == selectedModel } ?: OpenRouterModels.FREE_MODELS.find { it.id == selectedModel }
            val providerLabel = matchedModel?.provider ?: aiProvider.shortName
            val modelNameLabel = matchedModel?.name ?: selectedModel

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF8B5CF6).copy(alpha = 0.15f))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = providerLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF8B5CF6),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = modelNameLabel,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
                        ),
                        maxLines = 1
                    )
                }
                Icon(
                    imageVector = Icons.Rounded.ArrowDropDown,
                    contentDescription = "Cambiar Modelo",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            DropdownMenu(
                expanded = isModelMenuExpanded,
                onDismissRequest = { isModelMenuExpanded = false }
            ) {
                providerModels.forEach { model ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = model.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (model.id == selectedModel) FontWeight.Bold else FontWeight.Normal,
                                            color = if (model.id == selectedModel) AccentBlue else TextPrimary
                                        )
                                    )
                                    if (model.id == selectedModel) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = null,
                                            tint = AccentBlue,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = model.description,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        },
                        onClick = {
                            onSelectModel(model.id)
                            isModelMenuExpanded = false
                        }
                    )
                }

                DropdownMenuItem(
                    text = {
                        Text(
                            text = "Introducir otro modelo (Personalizado)...",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = AccentBlue,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    },
                    onClick = {
                        isModelMenuExpanded = false
                        customModelInput = selectedModel
                        isCustomModelDialogOpen = true
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Chat Conversation or Quick Prompts
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
        ) {
            if (messages.isEmpty() && !isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Acciones rápidas para este documento:",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = TextSecondary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            PromptChip(
                                text = "Diagrama UML",
                                onClick = { onSendMessage("Genera un diagrama UML en sintaxis Mermaid que represente la arquitectura o conceptos de este tema de forma clara y visual.") }
                            )
                            PromptChip(
                                text = "Fórmulas matemáticas",
                                onClick = { onSendMessage("Explica y genera las fórmulas matemáticas, modelos teóricos o ecuaciones relevantes asociadas a este tema utilizando notación matemática LaTeX detallada.") }
                            )
                            PromptChip(
                                text = "Explicar anotaciones",
                                onClick = { onSendMessage("Explica y analiza detalladamente las anotaciones y marcas que he realizado en la página actual.") }
                            )
                            PromptChip(
                                text = "Resumir página $currentPage",
                                onClick = { onSendMessage("Por favor, haz un resumen claro y estructurado de los puntos principales de la página $currentPage del documento actual.") }
                            )
                            PromptChip(
                                text = "Analizar estructura",
                                onClick = { onSendMessage("Analiza y resume la estructura general y temas del documento a partir del índice de contenidos.") }
                            )
                            PromptChip(
                                text = "Conceptos clave",
                                onClick = { onSendMessage("Explica los conceptos clave y términos importantes que aparecen en esta página.") }
                            )
                            PromptChip(
                                text = "Extraer notas",
                                onClick = { onSendMessage("Extrae las ideas principales de esta página en formato Markdown con viñetas y títulos para mis notas.") }
                            )
                            PromptChip(
                                text = "3 preguntas de repaso",
                                onClick = { onSendMessage("Genera 3 preguntas de autoevaluación o estudio basadas en el contenido de esta sección con sus respuestas.") }
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    items(messages, key = { it.id }) { msg ->
                        MessageBubble(
                            message = msg,
                            isDarkMode = isDarkMode,
                            onCopy = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Mensaje IA", msg.content)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Copiado al portapapeles", Toast.LENGTH_SHORT).show()
                            },
                            onInsertToNotes = {
                                onAppendNote(msg.content)
                                Toast.makeText(context, "Añadido a tus Notas", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                    if (isLoading) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(SurfaceToolbar)
                                        .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(14.dp),
                                            strokeWidth = 2.dp,
                                            color = AccentBlue
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Consultando modelo...",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = TextSecondary,
                                                fontSize = 12.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Input Box & Send Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = inputText,
                onValueChange = { inputText = it },
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp
                ),
                cursorBrush = SolidColor(AccentBlue),
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 6.dp),
                decorationBox = { innerTextField ->
                    if (inputText.isEmpty()) {
                        Text(
                            text = "Pregunta al asistente...",
                            style = TextStyle(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), fontSize = 13.sp)
                        )
                    }
                    innerTextField()
                }
            )

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
                onClick = {
                    if (inputText.isNotBlank() && !isLoading) {
                        val textToSend = inputText
                        inputText = ""
                        onSendMessage(textToSend)
                    }
                },
                enabled = inputText.isNotBlank() && !isLoading,
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(if (inputText.isNotBlank() && !isLoading) AccentBlue else Color.Transparent)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Send,
                    contentDescription = "Enviar",
                    tint = if (inputText.isNotBlank() && !isLoading) Color.White else TextSecondary.copy(alpha = 0.4f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }

    // Custom Model Dialog Modal
    if (isCustomModelDialogOpen) {
        com.pablo.paper.ui.common.LiquidGlassDialog(
            onDismissRequest = { isCustomModelDialogOpen = false },
            isDarkMode = isDarkMode,
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .width(460.dp)
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Rounded.Settings, contentDescription = null, tint = AccentBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Modelo Personalizado", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = if (isDarkMode) TextPrimaryDark else TextPrimary))
                }
                Text(
                    text = "Introduce el ID exacto del modelo en OpenRouter (por ejemplo: deepseek/deepseek-r1:free, meta-llama/llama-3.3-70b-instruct:free, openrouter/free, etc.)",
                    style = MaterialTheme.typography.bodySmall.copy(color = if (isDarkMode) TextSecondaryDark else TextSecondary)
                )
                OutlinedTextField(
                    value = customModelInput,
                    onValueChange = { customModelInput = it },
                    label = { Text("ID del Modelo") },
                    placeholder = { Text("ej. deepseek/deepseek-r1:free") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = if (isDarkMode) BorderSubtleDark else BorderSubtle,
                        focusedContainerColor = if (isDarkMode) Color(0x20FFFFFF) else Color(0x20000000),
                        unfocusedContainerColor = if (isDarkMode) Color(0x15FFFFFF) else Color(0x15000000)
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { isCustomModelDialogOpen = false }) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (customModelInput.isNotBlank()) {
                                onSelectModel(customModelInput.trim())
                            }
                            isCustomModelDialogOpen = false
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                    ) {
                        Text("Seleccionar")
                    }
                }
            }
        }
    }

    // API Key Settings Modal
    if (isApiKeyDialogOpen) {
        var tempKey by remember { mutableStateOf(apiKey) }
        com.pablo.paper.ui.common.LiquidGlassDialog(
            onDismissRequest = { onSetApiKeyDialogOpen(false) },
            isDarkMode = isDarkMode,
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .width(480.dp)
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Rounded.Key, contentDescription = null, tint = AccentBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Configurar Clave: ${aiProvider.shortName}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = if (isDarkMode) TextPrimaryDark else TextPrimary))
                }
                Text(
                    text = "${aiProvider.description}\nWeb: ${aiProvider.helpUrl}",
                    style = MaterialTheme.typography.bodySmall.copy(color = if (isDarkMode) TextSecondaryDark else TextSecondary)
                )
                OutlinedTextField(
                    value = tempKey,
                    onValueChange = { tempKey = it },
                    label = { Text("Clave API (${aiProvider.keyHint})") },
                    placeholder = { Text(aiProvider.keyHint) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = if (isDarkMode) BorderSubtleDark else BorderSubtle,
                        focusedContainerColor = if (isDarkMode) Color(0x20FFFFFF) else Color(0x20000000),
                        unfocusedContainerColor = if (isDarkMode) Color(0x15FFFFFF) else Color(0x15000000)
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { onSetApiKeyDialogOpen(false) }) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSetApiKey(tempKey.trim())
                            onSetApiKeyDialogOpen(false)
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}

@Composable
fun PromptChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceToolbar)
            .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Composable
fun MessageBubble(
    message: AssistantMessage,
    isDarkMode: Boolean = false,
    onCopy: () -> Unit,
    onInsertToNotes: () -> Unit
) {
    val isUser = message.role == MessageRole.USER
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val bg = if (isUser) AccentBlue.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    val border = if (isUser) AccentBlue.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outline

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = (if (isUser) Modifier.widthIn(max = 380.dp) else Modifier.fillMaxWidth())
                .clip(RoundedCornerShape(14.dp))
                .background(bg)
                .border(1.dp, border, RoundedCornerShape(14.dp))
                .padding(12.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isUser) "Tú" else "Asistente IA",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isUser) AccentBlue else Color(0xFF8B5CF6),
                            fontSize = 11.sp
                        )
                    )

                    if (!isUser) {
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            IconButton(
                                onClick = onInsertToNotes,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Description,
                                    contentDescription = "Añadir a notas",
                                    tint = onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            IconButton(
                                onClick = onCopy,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ContentCopy,
                                    contentDescription = "Copiar",
                                    tint = onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                if (isUser) {
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = onSurface,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    )
                } else {
                    MarkdownContent(
                        markdown = message.content,
                        textColor = onSurface,
                        isDarkMode = isDarkMode
                    )
                }
            }
        }
    }
}
