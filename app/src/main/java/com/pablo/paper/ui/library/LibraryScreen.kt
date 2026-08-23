package com.pablo.paper.ui.library

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pablo.paper.domain.model.AppThemeMode
import com.pablo.paper.domain.model.Document
import com.pablo.paper.ui.common.LiquidGlassButton
import com.pablo.paper.ui.theme.AccentBlue
import com.pablo.paper.ui.theme.AccentBlueLight
import com.pablo.paper.ui.theme.CanvasBackground
import com.pablo.paper.ui.theme.CanvasBackgroundDark
import com.pablo.paper.ui.theme.TextPrimary
import com.pablo.paper.ui.theme.TextPrimaryDark
import com.pablo.paper.ui.theme.TextSecondary
import com.pablo.paper.ui.theme.TextSecondaryDark

enum class LibraryFilter {
    ALL,
    RECENT,
    ANNOTATED,
    BOOKMARKED
}

@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    onOpenReader: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val documents by viewModel.documents.collectAsState()
    val documentCount by viewModel.documentCount.collectAsState()
    val isImporting by viewModel.isImporting.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(LibraryFilter.ALL) }

    // SAF Document Picker launcher for application/pdf
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.onImportUri(it) }
    }

    LaunchedEffect(Unit) {
        viewModel.navigationEvents.collect { event ->
            when (event) {
                is LibraryNavigationEvent.OpenReader -> onOpenReader(event.documentId)
            }
        }
    }

    val isSettingsOpen by viewModel.isSettingsOpen.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val stylusPrimaryAction by viewModel.stylusPrimaryAction.collectAsState()
    val stylusSecondaryAction by viewModel.stylusSecondaryAction.collectAsState()
    val aiProvider by viewModel.aiProvider.collectAsState()
    val openRouterApiKey by viewModel.openRouterApiKey.collectAsState()
    val selectedAiModel by viewModel.selectedAiModel.collectAsState()

    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.SEPIA -> false
        AppThemeMode.SYSTEM -> isSystemDark
    }

    val currentBg = when (themeMode) {
        AppThemeMode.SEPIA -> Color(0xFFF6F0E4)
        else -> if (isDark) CanvasBackgroundDark else CanvasBackground
    }

    val textPrimary = if (isDark) TextPrimaryDark else TextPrimary
    val textSec = if (isDark) TextSecondaryDark else TextSecondary

    // Filter documents based on search query & selected filter
    val filteredDocuments = remember(documents, searchQuery, selectedFilter) {
        documents.filter { doc ->
            val matchesSearch = searchQuery.isBlank() || doc.name.contains(searchQuery, ignoreCase = true)
            val matchesFilter = when (selectedFilter) {
                LibraryFilter.ALL -> true
                LibraryFilter.RECENT -> true
                LibraryFilter.ANNOTATED -> doc.currentPage > 1 || doc.progressPercentage > 0
                LibraryFilter.BOOKMARKED -> doc.progressPercentage > 0
            }
            matchesSearch && matchesFilter
        }
    }

    val mostRecentDoc = remember(documents) {
        documents.maxByOrNull { it.lastOpened }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = currentBg
    ) { _ ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(currentBg)
        ) {
            // Top Liquid Glass Header
            LibraryHeader(
                documentCount = documentCount,
                searchQuery = searchQuery,
                onSearchQueryChanged = { searchQuery = it },
                onOpenDocumentsClicked = {
                    documentPickerLauncher.launch(arrayOf("application/pdf"))
                },
                onSettingsClicked = {
                    viewModel.setSettingsOpen(true)
                },
                isDarkMode = isDark
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .navigationBarsPadding()
            ) {
                if (documents.isEmpty() && !isImporting) {
                    EmptyLibraryView(
                        onOpenDocumentsClicked = {
                            documentPickerLauncher.launch(arrayOf("application/pdf"))
                        },
                        isDarkMode = isDark
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 226.dp),
                        contentPadding = PaddingValues(horizontal = 36.dp, vertical = 18.dp),
                        horizontalArrangement = Arrangement.spacedBy(18.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // 1. HERO BANNER: Continue Reading (Spans all columns)
                        if (mostRecentDoc != null && searchQuery.isBlank() && selectedFilter == LibraryFilter.ALL) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                ContinueReadingHero(
                                    document = mostRecentDoc,
                                    onContinueReading = { viewModel.onDocumentClicked(mostRecentDoc) },
                                    isDarkMode = isDark,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                        }

                        // 3. SECTION TITLE & FILTER CHIPS (Spans all columns)
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp, bottom = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (searchQuery.isNotBlank()) "Resultados (${filteredDocuments.size})" else "En tu mesa",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = textPrimary
                                    )
                                )

                                // Filter Chips
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    FilterChipItem(
                                        title = "Todos",
                                        isSelected = selectedFilter == LibraryFilter.ALL,
                                        isDarkMode = isDark,
                                        onClick = { selectedFilter = LibraryFilter.ALL }
                                    )
                                    FilterChipItem(
                                        title = "Recientes",
                                        isSelected = selectedFilter == LibraryFilter.RECENT,
                                        isDarkMode = isDark,
                                        onClick = { selectedFilter = LibraryFilter.RECENT }
                                    )
                                    FilterChipItem(
                                        title = "Con Progreso",
                                        isSelected = selectedFilter == LibraryFilter.ANNOTATED,
                                        isDarkMode = isDark,
                                        onClick = { selectedFilter = LibraryFilter.ANNOTATED }
                                    )
                                }
                            }
                        }

                        // 4. DOCUMENT CARDS GRID
                        items(filteredDocuments, key = { it.id }) { doc ->
                            DocumentCard(
                                document = doc,
                                onClick = { viewModel.onDocumentClicked(doc) },
                                onDelete = { viewModel.onDeleteDocument(doc.id) },
                                isDarkMode = isDark
                            )
                        }
                    }
                }

                if (isImporting) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AccentBlue)
                    }
                }
            }
        }

        if (isSettingsOpen) {
            SettingsDialog(
                themeMode = themeMode,
                stylusPrimaryAction = stylusPrimaryAction,
                stylusSecondaryAction = stylusSecondaryAction,
                aiProvider = aiProvider,
                openRouterApiKey = openRouterApiKey,
                selectedAiModel = selectedAiModel,
                isDarkMode = isDark,
                onThemeModeChanged = { viewModel.onThemeModeChanged(it) },
                onStylusPrimaryActionChanged = { viewModel.onStylusPrimaryActionChanged(it) },
                onStylusSecondaryActionChanged = { viewModel.onStylusSecondaryActionChanged(it) },
                onAiProviderChanged = { viewModel.onAiProviderChanged(it) },
                onOpenRouterApiKeyChanged = { viewModel.onOpenRouterApiKeyChanged(it) },
                onSelectedAiModelChanged = { viewModel.onSelectedAiModelChanged(it) },
                onDismissRequest = { viewModel.setSettingsOpen(false) }
            )
        }
    }
}

@Composable
private fun FilterChipItem(
    title: String,
    isSelected: Boolean,
    isDarkMode: Boolean,
    onClick: () -> Unit
) {
    val textPrimary = if (isDarkMode) TextPrimaryDark else TextPrimary
    val textSec = if (isDarkMode) TextSecondaryDark else TextSecondary

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) {
            if (isDarkMode) AccentBlue.copy(alpha = 0.35f) else AccentBlue.copy(alpha = 0.15f)
        } else {
            if (isDarkMode) Color(0x18FFFFFF) else Color(0x10000000)
        },
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) AccentBlueLight else Color.Transparent
        )
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) AccentBlueLight else textSec,
                fontSize = 12.sp
            ),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}
