package com.pablo.paper.ui.reader

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FlipCameraAndroid
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.pablo.paper.ui.theme.AccentBlue
import com.pablo.paper.ui.theme.AccentBlueLight

data class FlashcardItem(
    val front: String,
    val back: String,
    val page: Int = 1
)

data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val page: Int = 1
)

@Composable
fun FlashcardQuizModal(
    isOpen: Boolean,
    onClose: () -> Unit,
    flashcards: List<FlashcardItem>,
    quizzes: List<QuizQuestion>,
    isLoading: Boolean,
    onGenerateWithAi: (isQuiz: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isOpen) return

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Flashcards, 1: Quiz

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.92f)
                .height(680.dp)
                .clip(RoundedCornerShape(24.dp)),
            color = Color(0xFFF8FAFC),
            tonalElevation = 8.dp,
            shadowElevation = 24.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 24.dp, vertical = 18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = AccentBlue.copy(alpha = 0.12f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (selectedTab == 0) Icons.Rounded.Style else Icons.Rounded.Quiz,
                                    contentDescription = null,
                                    tint = AccentBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = if (selectedTab == 0) "Tarjetas de Estudio (Flashcards)" else "Cuestionario de Repaso",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = Color(0xFF0F172A)
                                )
                            )
                            Text(
                                text = "Generado automáticamente por IA a partir de tu documento",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B))
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onGenerateWithAi(selectedTab == 1) },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Rounded.AutoAwesome, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Regenerar con IA", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        IconButton(onClick = onClose) {
                            Icon(Icons.Rounded.Close, "Cerrar", tint = Color(0xFF64748B))
                        }
                    }
                }

                // Tab Bar
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.White,
                    contentColor = AccentBlue
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Style, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Flashcards (${flashcards.size})", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Quiz, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Test Interactivo (${quizzes.size})", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    )
                }

                // Content
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = AccentBlue, strokeWidth = 3.dp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Analizando el documento y redactando preguntas con IA...",
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF64748B))
                            )
                        }
                    } else if (selectedTab == 0) {
                        FlashcardDeckView(flashcards = flashcards, onRegenerate = { onGenerateWithAi(false) })
                    } else {
                        QuizListView(quizzes = quizzes, onRegenerate = { onGenerateWithAi(true) })
                    }
                }
            }
        }
    }
}

@Composable
private fun FlashcardDeckView(
    flashcards: List<FlashcardItem>,
    onRegenerate: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (flashcards.isEmpty()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Rounded.Style, null, tint = Color(0xFF94A3B8), modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text("No hay tarjetas generadas todavía", fontWeight = FontWeight.Bold, color = Color(0xFF334155))
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRegenerate, colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)) {
                Text("Generar Flashcards")
            }
        }
        return
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }

    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 350),
        label = "flashcard_flip"
    )

    val currentCard = flashcards[currentIndex.coerceIn(0, flashcards.size - 1)]

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Card Count Indicator
        Text(
            text = "Tarjeta ${currentIndex + 1} de ${flashcards.size} • Página ${currentCard.page}",
            style = MaterialTheme.typography.labelMedium.copy(color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
        )

        // 3D Flip Card
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(340.dp)
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 12f * density
                }
                .clickable { isFlipped = !isFlipped },
            shape = RoundedCornerShape(20.dp),
            color = if (rotation <= 90f) Color.White else Color(0xFFEFF6FF),
            border = BorderStroke(1.5.dp, if (rotation <= 90f) Color(0xFFE2E8F0) else AccentBlue.copy(alpha = 0.4f)),
            shadowElevation = 8.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                if (rotation <= 90f) {
                    // Front side
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "PREGUNTA",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = AccentBlue,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = currentCard.front,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0F172A),
                                textAlign = TextAlign.Center
                            )
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "(Toca la tarjeta para ver la respuesta)",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
                        )
                    }
                } else {
                    // Back side (Mirrored for proper reading)
                    Column(
                        modifier = Modifier.graphicsLayer { rotationY = 180f },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "RESPUESTA",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = currentCard.back,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFF1E293B),
                                textAlign = TextAlign.Center,
                                lineHeight = 24.sp
                            )
                        )
                    }
                }
            }
        }

        // Navigation Controls
        Row(
            modifier = Modifier.fillMaxWidth(0.85f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    if (currentIndex > 0) {
                        isFlipped = false
                        currentIndex--
                    }
                },
                enabled = currentIndex > 0,
                colors = ButtonDefaults.outlinedButtonColors()
            ) {
                Icon(Icons.Rounded.ChevronLeft, null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Anterior")
            }

            Button(
                onClick = { isFlipped = !isFlipped },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9), contentColor = Color(0xFF334155))
            ) {
                Icon(Icons.Rounded.FlipCameraAndroid, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Voltear")
            }

            Button(
                onClick = {
                    if (currentIndex < flashcards.size - 1) {
                        isFlipped = false
                        currentIndex++
                    }
                },
                enabled = currentIndex < flashcards.size - 1,
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) {
                Text("Siguiente")
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Rounded.ChevronRight, null)
            }
        }
    }
}

@Composable
private fun QuizListView(
    quizzes: List<QuizQuestion>,
    onRegenerate: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (quizzes.isEmpty()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Rounded.Quiz, null, tint = Color(0xFF94A3B8), modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text("No hay preguntas de test todavía", fontWeight = FontWeight.Bold, color = Color(0xFF334155))
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRegenerate, colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)) {
                Text("Generar Test de Estudio")
            }
        }
        return
    }

    var answers by remember { mutableStateOf<Map<Int, Int>>(emptyMap()) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        itemsIndexed(quizzes) { index, question ->
            val selectedOption = answers[index]
            val hasAnswered = selectedOption != null

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "PREGUNTA ${index + 1}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = AccentBlue,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Página ${question.page}",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = question.question,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Options
                    question.options.forEachIndexed { optIndex, optionText ->
                        val isSelected = selectedOption == optIndex
                        val isCorrect = optIndex == question.correctIndex

                        val bgColor = when {
                            !hasAnswered -> Color(0xFFF8FAFC)
                            isCorrect -> Color(0xFFECFDF5)
                            isSelected && !isCorrect -> Color(0xFFFEF2F2)
                            else -> Color(0xFFF8FAFC)
                        }

                        val borderColor = when {
                            !hasAnswered && isSelected -> AccentBlue
                            hasAnswered && isCorrect -> Color(0xFF10B981)
                            hasAnswered && isSelected && !isCorrect -> Color(0xFFEF4444)
                            else -> Color(0xFFE2E8F0)
                        }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    if (!hasAnswered) {
                                        answers = answers + (index to optIndex)
                                    }
                                },
                            color = bgColor,
                            border = BorderStroke(1.2.dp, borderColor),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isSelected) AccentBlue else Color(0xFFE2E8F0),
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = ('A' + optIndex).toString(),
                                            color = if (isSelected) Color.White else Color(0xFF475569),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = optionText,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color(0xFF1E293B),
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                if (hasAnswered && isCorrect) {
                                    Icon(Icons.Rounded.CheckCircle, null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }

                    // Explanation when answered
                    if (hasAnswered) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF1F5F9),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "💡 Explicación: ${question.explanation}",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF475569)),
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
