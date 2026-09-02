package com.pablo.paper.desktop.ui.dialogs

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.pablo.paper.desktop.model.ThemeMode
import com.pablo.paper.desktop.state.DesktopDialog
import com.pablo.paper.desktop.state.NoticeTone
import com.pablo.paper.desktop.state.WorkspaceState
import kotlinx.coroutines.launch

@Composable
fun DesktopDialogHost(state: WorkspaceState) {
    when (state.activeDialog) {
        DesktopDialog.PRINT -> PrintDialog(state)
        DesktopDialog.SEARCH_ADVANCED -> SearchDialog(state)
        DesktopDialog.PREFERENCES -> PreferencesDialog(state)
        DesktopDialog.ABOUT -> AboutDialog(state)
        else -> Unit
    }
}

@Composable
private fun PaperDialog(title: String, onDismiss: () -> Unit, content: @Composable () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.width(480.dp),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            shadowElevation = 16.dp
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                    IconButton(onClick = onDismiss, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", modifier = Modifier.size(17.dp))
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f), modifier = Modifier.padding(top = 10.dp, bottom = 14.dp))
                content()
            }
        }
    }
}

@Composable
private fun PrintDialog(state: WorkspaceState) {
    val tab = state.activeTab ?: return
    PaperDialog("Imprimir", onDismiss = { state.activeDialog = DesktopDialog.NONE }) {
        Text(tab.title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Text("La configuración de copias, papel e impresora se realiza en el diálogo nativo de Windows.", fontSize = 12.sp, lineHeight = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 6.dp))
        Row(modifier = Modifier.fillMaxWidth().padding(top = 18.dp), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = { state.activeDialog = DesktopDialog.NONE }) { Text("Cancelar") }
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = {
                    state.activeDialog = DesktopDialog.NONE
                    runCatching {
                        val desktop = java.awt.Desktop.getDesktop()
                        check(desktop.isSupported(java.awt.Desktop.Action.PRINT))
                        desktop.print(tab.file)
                    }.onFailure { state.showNotice("Windows no pudo abrir el diálogo de impresión.", NoticeTone.ERROR) }
                }
            ) {
                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(17.dp))
                Spacer(Modifier.width(7.dp))
                Text("Continuar")
            }
        }
    }
}

@Composable
private fun SearchDialog(state: WorkspaceState) {
    val tab = state.activeTab ?: return
    val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf("") }
    var searching by remember { mutableStateOf(false) }

    PaperDialog("Buscar en el documento", onDismiss = { state.activeDialog = DesktopDialog.NONE }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                singleLine = true,
                placeholder = { Text("Texto a buscar") },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Button(
                enabled = query.isNotBlank() && !searching,
                onClick = {
                    searching = true
                    scope.launch {
                        val matches = tab.engine.search(query.trim())
                        tab.searchMatches.clear()
                        tab.searchMatches.addAll(matches)
                        searching = false
                    }
                }
            ) { Icon(Icons.Default.Search, contentDescription = "Buscar", modifier = Modifier.size(17.dp)) }
        }
        if (searching) Text("Buscando…", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 9.dp))
        if (!searching && query.isNotBlank()) {
            Spacer(Modifier.height(10.dp))
            Text("${tab.searchMatches.size} coincidencias", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            LazyColumn(modifier = Modifier.height(250.dp).padding(top = 5.dp)) {
                items(tab.searchMatches) { match ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 7.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                            .clickable {
                                tab.currentPage = match.pageIndex
                                state.activeDialog = DesktopDialog.NONE
                            }
                            .padding(8.dp)
                    ) {
                        Text("Página ${match.pageIndex + 1}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(match.snippet, fontSize = 11.sp, modifier = Modifier.padding(top = 3.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun PreferencesDialog(state: WorkspaceState) {
    PaperDialog("Preferencias", onDismiss = { state.activeDialog = DesktopDialog.NONE }) {
        Text("Apariencia", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Row(modifier = Modifier.fillMaxWidth().padding(top = 5.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf(
                ThemeMode.SYSTEM to "Sistema",
                ThemeMode.LIGHT to "Claro",
                ThemeMode.DARK to "Oscuro",
                ThemeMode.SEPIA to "Sepia"
            ).forEach { (mode, name) ->
                TextButton(onClick = { state.themeMode = mode }) {
                    Text(name, color = if (state.themeMode == mode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Text("IA local", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text("El valor predeterminado usa el proxy Tailscale del puerto 8082.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 3.dp))
        OutlinedTextField(
            value = state.aiEndpoint,
            onValueChange = { state.aiEndpoint = it.trim() },
            label = { Text("Endpoint OpenAI compatible") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(top = 7.dp),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp)
        )
        OutlinedTextField(
            value = state.aiAccessToken,
            onValueChange = { state.aiAccessToken = it },
            label = { Text("Token opcional del proxy") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(top = 7.dp),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp)
        )
        Text("El token no se persiste en disco por Paper.", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun AboutDialog(state: WorkspaceState) {
    PaperDialog("Acerca de Paper", onDismiss = { state.activeDialog = DesktopDialog.NONE }) {
        Text("Paper Desktop", style = MaterialTheme.typography.titleMedium)
        Text("Visor local de PDF y espacio de lectura con anotaciones no destructivas.", fontSize = 12.sp, lineHeight = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 5.dp))
        Text("Las anotaciones y las notas se guardan en un archivo .paper.json junto al PDF; el documento original no se modifica.", fontSize = 11.sp, lineHeight = 17.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 10.dp))
    }
}
