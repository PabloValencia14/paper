package com.pablo.paper.ui.library

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pablo.paper.ai.OpenRouterModels
import com.pablo.paper.domain.model.AppThemeMode
import com.pablo.paper.domain.model.StylusButtonAction
import com.pablo.paper.ui.theme.AccentBlue
import com.pablo.paper.ui.theme.BorderSubtle
import com.pablo.paper.ui.theme.BorderSubtleDark
import com.pablo.paper.ui.theme.SurfaceCard
import com.pablo.paper.ui.theme.SurfaceCardDark
import com.pablo.paper.ui.theme.TextPrimary
import com.pablo.paper.ui.theme.TextPrimaryDark
import com.pablo.paper.ui.theme.TextSecondary
import com.pablo.paper.ui.theme.TextSecondaryDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    themeMode: AppThemeMode,
    stylusPrimaryAction: StylusButtonAction,
    stylusSecondaryAction: StylusButtonAction,
    aiProvider: com.pablo.paper.ai.AiProvider = com.pablo.paper.ai.AiProvider.OPENROUTER,
    openRouterApiKey: String,
    selectedAiModel: String,
    isDarkMode: Boolean,
    onThemeModeChanged: (AppThemeMode) -> Unit,
    onStylusPrimaryActionChanged: (StylusButtonAction) -> Unit,
    onStylusSecondaryActionChanged: (StylusButtonAction) -> Unit,
    onAiProviderChanged: (com.pablo.paper.ai.AiProvider) -> Unit = {},
    onOpenRouterApiKeyChanged: (String) -> Unit,
    onSelectedAiModelChanged: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    var mainTab by remember { mutableIntStateOf(0) } // 0: Stylus, 1: Apariencia, 2: IA
    var stylusButtonTab by remember { mutableIntStateOf(0) } // 0: Boton 1, 1: Boton 2

    val bgColor = if (isDarkMode) com.pablo.paper.ui.theme.GlassSurfaceDark else com.pablo.paper.ui.theme.GlassSurfaceLight
    val textColor = if (isDarkMode) TextPrimaryDark else TextPrimary
    val textSecColor = if (isDarkMode) TextSecondaryDark else TextSecondary
    val borderColor = if (isDarkMode) com.pablo.paper.ui.theme.BorderGlassDark else com.pablo.paper.ui.theme.BorderGlassLight

    com.pablo.paper.ui.common.LiquidGlassDialog(
        onDismissRequest = onDismissRequest,
        isDarkMode = isDarkMode,
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .width(580.dp)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = null,
                            tint = AccentBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "Configuración",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                ),
                                color = textColor
                            )
                            Text(
                                text = "Preferencias generales de la aplicación",
                                style = MaterialTheme.typography.bodySmall,
                                color = textSecColor
                            )
                        }
                    }

                    IconButton(onClick = onDismissRequest) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Cerrar",
                            tint = textSecColor
                        )
                    }
                }

                // Main Navigation Tabs
                TabRow(
                    selectedTabIndex = mainTab,
                    containerColor = Color.Transparent,
                    contentColor = AccentBlue,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[mainTab]),
                            color = AccentBlue
                        )
                    }
                ) {
                    SettingsMainTab(
                        title = "Lápiz Óptico",
                        icon = Icons.Rounded.Edit,
                        selected = mainTab == 0,
                        onClick = { mainTab = 0 }
                    )
                    SettingsMainTab(
                        title = "Apariencia",
                        icon = Icons.Rounded.Palette,
                        selected = mainTab == 1,
                        onClick = { mainTab = 1 }
                    )
                    SettingsMainTab(
                        title = "Inteligencia Artificial",
                        icon = Icons.Rounded.AutoAwesome,
                        selected = mainTab == 2,
                        onClick = { mainTab = 2 }
                    )
                }

                // Tab Content Area
                Box(modifier = Modifier.height(340.dp)) {
                    when (mainTab) {
                        0 -> {
                            // STYLUS SETTINGS
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                TabRow(
                                    selectedTabIndex = stylusButtonTab,
                                    containerColor = Color.Transparent,
                                    contentColor = AccentBlue,
                                    indicator = { tabPositions ->
                                        TabRowDefaults.SecondaryIndicator(
                                            Modifier.tabIndicatorOffset(tabPositions[stylusButtonTab]),
                                            color = AccentBlue
                                        )
                                    }
                                ) {
                                    Tab(
                                        selected = stylusButtonTab == 0,
                                        onClick = { stylusButtonTab = 0 },
                                        text = {
                                            Text(
                                                text = "Botón 1 (Inferior)",
                                                fontWeight = if (stylusButtonTab == 0) FontWeight.Bold else FontWeight.Normal,
                                                color = if (stylusButtonTab == 0) AccentBlue else textSecColor
                                            )
                                        }
                                    )
                                    Tab(
                                        selected = stylusButtonTab == 1,
                                        onClick = { stylusButtonTab = 1 },
                                        text = {
                                            Text(
                                                text = "Botón 2 (Superior)",
                                                fontWeight = if (stylusButtonTab == 1) FontWeight.Bold else FontWeight.Normal,
                                                color = if (stylusButtonTab == 1) AccentBlue else textSecColor
                                            )
                                        }
                                    )
                                }

                                val currentSelectedAction = if (stylusButtonTab == 0) stylusPrimaryAction else stylusSecondaryAction
                                val allActions = StylusButtonAction.entries.toTypedArray()

                                LazyColumn(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(allActions.size) { index ->
                                        val action = allActions[index]
                                        val isSelected = action == currentSelectedAction

                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (isSelected) AccentBlue.copy(alpha = 0.12f) else Color.Transparent,
                                            border = BorderStroke(1.dp, if (isSelected) AccentBlue else borderColor),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    if (stylusButtonTab == 0) {
                                                        onStylusPrimaryActionChanged(action)
                                                    } else {
                                                        onStylusSecondaryActionChanged(action)
                                                    }
                                                }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = action.displayName,
                                                        style = MaterialTheme.typography.bodyMedium.copy(
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                                                        ),
                                                        color = if (isSelected) AccentBlue else textColor
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = action.description,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = textSecColor
                                                    )
                                                }

                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Check,
                                                        contentDescription = "Seleccionado",
                                                        tint = AccentBlue,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        1 -> {
                            // THEME / APPEARANCE SETTINGS
                            val themeOptions = listOf(
                                Triple(AppThemeMode.SYSTEM, "Automático (Sistema de la Tablet)", Icons.Rounded.PhoneAndroid),
                                Triple(AppThemeMode.LIGHT, "Tema Claro", Icons.Rounded.LightMode),
                                Triple(AppThemeMode.DARK, "Tema Oscuro", Icons.Rounded.DarkMode),
                                Triple(AppThemeMode.SEPIA, "Modo Sepia (Descanso Visual)", Icons.Rounded.Description)
                            )

                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = "Tema de la aplicación y lectura:",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = textColor
                                )

                                themeOptions.forEach { (mode, label, icon) ->
                                    val isSelected = themeMode == mode
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) AccentBlue.copy(alpha = 0.12f) else Color.Transparent,
                                        border = BorderStroke(1.dp, if (isSelected) AccentBlue else borderColor),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onThemeModeChanged(mode) }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                Icon(
                                                    imageVector = icon,
                                                    contentDescription = null,
                                                    tint = if (isSelected) AccentBlue else textSecColor,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                                Text(
                                                    text = label,
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                    ),
                                                    color = if (isSelected) AccentBlue else textColor
                                                )
                                            }

                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Check,
                                                    contentDescription = "Seleccionado",
                                                    tint = AccentBlue,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        2 -> {
                            // AI SETTINGS
                            var apiKeyInput by remember(openRouterApiKey) { mutableStateOf(openRouterApiKey) }

                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Clave API de OpenRouter:",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = textColor
                                )

                                OutlinedTextField(
                                    value = apiKeyInput,
                                    onValueChange = {
                                        apiKeyInput = it
                                        onOpenRouterApiKeyChanged(it.trim())
                                    },
                                    placeholder = { Text(aiProvider.keyHint, color = textSecColor) },
                                    supportingText = {
                                        Text(
                                            text = "${aiProvider.description}\nWeb: ${aiProvider.helpUrl}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = textSecColor
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(imageVector = Icons.Rounded.Key, contentDescription = null, tint = AccentBlue)
                                    },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AccentBlue,
                                        unfocusedBorderColor = borderColor
                                    )
                                )

                                Text(
                                    text = "Modelo Predeterminado:",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = textColor
                                )

                                val currentProviderModels = OpenRouterModels.getModelsForProvider(aiProvider)

                                LazyColumn(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    items(currentProviderModels.size) { index ->
                                        val model = currentProviderModels[index]
                                        val isSelected = selectedAiModel == model.id

                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isSelected) AccentBlue.copy(alpha = 0.12f) else Color.Transparent,
                                            border = BorderStroke(1.dp, if (isSelected) AccentBlue else borderColor),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { onSelectedAiModelChanged(model.id) }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = model.name,
                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                        ),
                                                        color = if (isSelected) AccentBlue else textColor
                                                    )
                                                    Text(
                                                        text = model.description,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = textSecColor,
                                                        maxLines = 1
                                                    )
                                                }

                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Check,
                                                        contentDescription = "Seleccionado",
                                                        tint = AccentBlue,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Done Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismissRequest,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentBlue,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Cerrar")
                    }
                }
            }
        }
    }

@Composable
private fun SettingsMainTab(
    title: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Tab(
        selected = selected,
        onClick = onClick,
        text = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (selected) AccentBlue else Color.Gray
                )
                Text(
                    text = title,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    color = if (selected) AccentBlue else Color.Gray,
                    fontSize = 13.sp
                )
            }
        }
    )
}
