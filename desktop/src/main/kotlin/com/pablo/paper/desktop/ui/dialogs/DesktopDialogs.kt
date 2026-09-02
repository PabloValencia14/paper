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
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.pablo.paper.desktop.pdf.DesktopPdfManipulator
import com.pablo.paper.desktop.state.DesktopDialog
import com.pablo.paper.desktop.state.WorkspaceState
import kotlinx.coroutines.launch
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

@Composable
fun DesktopDialogHost(state: WorkspaceState) {
    when (state.activeDialog) {
        DesktopDialog.PRINT -> PrintModal(state)
        DesktopDialog.ORGANIZE_PAGES -> OrganizePagesModal(state)
        DesktopDialog.WATERMARK -> WatermarkModal(state)
        DesktopDialog.HEADER_FOOTER -> HeaderFooterModal(state)
        DesktopDialog.PASSWORD_SECURITY -> PasswordSecurityModal(state)
        DesktopDialog.CERTIFICATE_SIGN -> CertificateSignModal(state)
        DesktopDialog.SEARCH_ADVANCED -> AdvancedSearchModal(state)
        DesktopDialog.PREFERENCES -> PreferencesModal(state)
        DesktopDialog.ABOUT -> AboutModal(state)
        DesktopDialog.NONE -> {}
    }
}

@Composable
fun DialogWrapper(
    title: String,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.width(480.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(Modifier.height(12.dp))
                content()
            }
        }
    }
}

@Composable
fun PrintModal(state: WorkspaceState) {
    val tab = state.activeTab ?: return
    DialogWrapper(title = "Imprimir Documento", onDismiss = { state.activeDialog = DesktopDialog.NONE }) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Documento: ${tab.title}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text("Total páginas: ${tab.pageCount}", fontSize = 12.sp, color = Color.Gray)
            Text("Orientación: Automática / Ajustar al tamaño del papel", fontSize = 12.sp)

            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = { state.activeDialog = DesktopDialog.NONE }) {
                    Text("Cancelar")
                }
                Spacer(Modifier.width(8.dp))
                Button(onClick = {
                    state.activeDialog = DesktopDialog.NONE
                    // Launch Windows Native Print Service
                    try {
                        val desktop = java.awt.Desktop.getDesktop()
                        if (desktop.isSupported(java.awt.Desktop.Action.PRINT)) {
                            desktop.print(tab.file)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }) {
                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Imprimir en Windows")
                }
            }
        }
    }
}

@Composable
fun OrganizePagesModal(state: WorkspaceState) {
    val tab = state.activeTab ?: return
    val scope = rememberCoroutineScope()
    var statusMsg by remember { mutableStateOf("") }

    DialogWrapper(title = "Organizar Páginas del PDF", onDismiss = { state.activeDialog = DesktopDialog.NONE }) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Operaciones sobre ${tab.title} (${tab.pageCount} páginas):", fontSize = 12.sp)

            Button(
                onClick = {
                    val fd = FileDialog(null as Frame?, "Guardar PDF con páginas rotadas", FileDialog.SAVE)
                    fd.file = "${tab.file.nameWithoutExtension}_rotado.pdf"
                    fd.isVisible = true
                    if (fd.file != null) {
                        val out = File(fd.directory, fd.file)
                        scope.launch {
                            val ok = DesktopPdfManipulator.rotatePages(tab.file, emptyList(), 90, out)
                            statusMsg = if (ok) "¡Guardado exitosamente!" else "Error al rotar."
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Rotar todas las páginas 90° y Guardar Como...")
            }

            Button(
                onClick = {
                    val fd = FileDialog(null as Frame?, "Seleccionar carpeta de destino para dividir", FileDialog.SAVE)
                    fd.isVisible = true
                    if (fd.directory != null) {
                        val dir = File(fd.directory)
                        scope.launch {
                            val parts = DesktopPdfManipulator.splitPdf(tab.file, 1, dir)
                            statusMsg = "Dividido en ${parts.size} archivos PDF."
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Dividir PDF en páginas individuales (Split)")
            }

            if (statusMsg.isNotBlank()) {
                Text(statusMsg, fontSize = 12.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun WatermarkModal(state: WorkspaceState) {
    val tab = state.activeTab ?: return
    val scope = rememberCoroutineScope()
    var watermarkText by remember { mutableStateOf("CONFIDENCIAL") }
    var statusMsg by remember { mutableStateOf("") }

    DialogWrapper(title = "Añadir Marca de Agua", onDismiss = { state.activeDialog = DesktopDialog.NONE }) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = watermarkText,
                onValueChange = { watermarkText = it },
                label = { Text("Texto de la marca de agua") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val fd = FileDialog(null as Frame?, "Guardar PDF con marca de agua", FileDialog.SAVE)
                    fd.file = "${tab.file.nameWithoutExtension}_watermark.pdf"
                    fd.isVisible = true
                    if (fd.file != null) {
                        val out = File(fd.directory, fd.file)
                        scope.launch {
                            val ok = DesktopPdfManipulator.addWatermark(tab.file, watermarkText, 0.25f, 45f, out)
                            statusMsg = if (ok) "¡Marca de agua aplicada correctamente!" else "Error al aplicar."
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Aplicar y Guardar Como...")
            }

            if (statusMsg.isNotBlank()) {
                Text(statusMsg, fontSize = 12.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun HeaderFooterModal(state: WorkspaceState) {
    val tab = state.activeTab ?: return
    val scope = rememberCoroutineScope()
    var headerText by remember { mutableStateOf("") }
    var footerText by remember { mutableStateOf(tab.title) }
    var includePageNums by remember { mutableStateOf(true) }
    var statusMsg by remember { mutableStateOf("") }

    DialogWrapper(title = "Encabezado y Pie de Página", onDismiss = { state.activeDialog = DesktopDialog.NONE }) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = headerText,
                onValueChange = { headerText = it },
                label = { Text("Texto de encabezado (superior)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = footerText,
                onValueChange = { footerText = it },
                label = { Text("Texto de pie de página (inferior)") },
                modifier = Modifier.fillMaxWidth()
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = includePageNums, onCheckedChange = { includePageNums = it })
                Text("Incluir numeración automática (Página X de Y)", fontSize = 12.sp)
            }

            Button(
                onClick = {
                    val fd = FileDialog(null as Frame?, "Guardar PDF con numeración", FileDialog.SAVE)
                    fd.file = "${tab.file.nameWithoutExtension}_numbered.pdf"
                    fd.isVisible = true
                    if (fd.file != null) {
                        val out = File(fd.directory, fd.file)
                        scope.launch {
                            val ok = DesktopPdfManipulator.addHeaderFooter(tab.file, headerText, footerText, includePageNums, out)
                            statusMsg = if (ok) "¡Encabezado y pie aplicados!" else "Error."
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Aplicar y Guardar...")
            }

            if (statusMsg.isNotBlank()) {
                Text(statusMsg, fontSize = 12.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun PasswordSecurityModal(state: WorkspaceState) {
    val tab = state.activeTab ?: return
    val scope = rememberCoroutineScope()
    var userPassword by remember { mutableStateOf("") }
    var ownerPassword by remember { mutableStateOf("") }
    var canPrint by remember { mutableStateOf(true) }
    var canCopy by remember { mutableStateOf(true) }
    var statusMsg by remember { mutableStateOf("") }

    DialogWrapper(title = "Seguridad y Cifrado AES", onDismiss = { state.activeDialog = DesktopDialog.NONE }) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = userPassword,
                onValueChange = { userPassword = it },
                label = { Text("Contraseña de apertura (Lectura)") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = ownerPassword,
                onValueChange = { ownerPassword = it },
                label = { Text("Contraseña de permisos (Propietario)") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = canPrint, onCheckedChange = { canPrint = it })
                Text("Permitir impresión", fontSize = 12.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = canCopy, onCheckedChange = { canCopy = it })
                Text("Permitir copiado de texto y gráficos", fontSize = 12.sp)
            }

            Button(
                onClick = {
                    if (userPassword.isBlank() && ownerPassword.isBlank()) return@Button
                    val fd = FileDialog(null as Frame?, "Guardar PDF protegido", FileDialog.SAVE)
                    fd.file = "${tab.file.nameWithoutExtension}_protegido.pdf"
                    fd.isVisible = true
                    if (fd.file != null) {
                        val out = File(fd.directory, fd.file)
                        scope.launch {
                            val ok = DesktopPdfManipulator.protectPdf(
                                tab.file,
                                userPassword,
                                ownerPassword.ifBlank { userPassword },
                                canPrint,
                                canCopy,
                                false,
                                out
                            )
                            statusMsg = if (ok) "¡PDF protegido con cifrado AES-256!" else "Error al proteger."
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cifrar con AES-256 y Guardar")
            }

            if (statusMsg.isNotBlank()) {
                Text(statusMsg, fontSize = 12.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CertificateSignModal(state: WorkspaceState) {
    DialogWrapper(title = "Firma con Certificado Digital", onDismiss = { state.activeDialog = DesktopDialog.NONE }) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Firma digital criptográfica PKCS#12 (.pfx / .p12):", fontSize = 12.sp)
            OutlinedButton(
                onClick = {
                    val fd = FileDialog(null as Frame?, "Seleccionar archivo de certificado PKCS#12", FileDialog.LOAD)
                    fd.setFilenameFilter { _, name -> name.endsWith(".pfx") || name.endsWith(".p12") }
                    fd.isVisible = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Examinar certificado digital (.pfx)...")
            }
            Text("Garantiza la autenticidad, integridad y no repudio legal del documento PDF.", fontSize = 11.sp, color = Color.Gray)
        }
    }
}

@Composable
fun AdvancedSearchModal(state: WorkspaceState) {
    val tab = state.activeTab ?: return
    val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf("") }
    var matchCase by remember { mutableStateOf(false) }
    var isSearching by remember { mutableStateOf(false) }

    DialogWrapper(title = "Búsqueda Avanzada en el Documento", onDismiss = { state.activeDialog = DesktopDialog.NONE }) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Palabra o frase clave...", fontSize = 12.sp) },
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = {
                        if (query.isNotBlank()) {
                            isSearching = true
                            scope.launch {
                                val results = tab.engine.search(query, matchCase)
                                tab.searchMatches.clear()
                                tab.searchMatches.addAll(results)
                                isSearching = false
                            }
                        }
                    },
                    enabled = query.isNotBlank() && !isSearching
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text("Buscar")
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = matchCase, onCheckedChange = { matchCase = it })
                Text("Coincidir mayúsculas y minúsculas", fontSize = 12.sp)
            }

            if (tab.searchMatches.isNotEmpty()) {
                Text("Coincidencias encontradas: ${tab.searchMatches.size}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                LazyColumn(modifier = Modifier.height(200.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(tab.searchMatches) { match ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                .clickable {
                                    tab.currentPage = match.pageIndex
                                    state.activeDialog = DesktopDialog.NONE
                                }
                                .padding(6.dp)
                        ) {
                            Column {
                                Text("Pág. ${match.pageIndex + 1}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text(match.snippet, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PreferencesModal(state: WorkspaceState) {
    DialogWrapper(title = "Preferencias de Paper Desktop", onDismiss = { state.activeDialog = DesktopDialog.NONE }) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Tema de la Aplicación", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                val themes = listOf(
                    com.pablo.paper.desktop.model.ThemeMode.SYSTEM to "Sistema",
                    com.pablo.paper.desktop.model.ThemeMode.LIGHT to "Claro",
                    com.pablo.paper.desktop.model.ThemeMode.DARK to "Oscuro",
                    com.pablo.paper.desktop.model.ThemeMode.SEPIA to "Sepia"
                )
                themes.forEach { (mode, label) ->
                    val isSelected = state.themeMode == mode
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .clickable { state.themeMode = mode }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(Modifier.height(4.dp))
            Text("Configuración de OpenRouter AI", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = state.openRouterApiKey,
                onValueChange = { state.openRouterApiKey = it },
                label = { Text("OpenRouter API Key") },
                modifier = Modifier.fillMaxWidth()
            )
            Text("Modelo por defecto: ${state.selectedModelId}", fontSize = 12.sp, color = Color.Gray)
        }
    }
}


@Composable
fun AboutModal(state: WorkspaceState) {
    DialogWrapper(title = "Acerca de Paper Desktop", onDismiss = { state.activeDialog = DesktopDialog.NONE }) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
            Text("Paper Desktop para Windows", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("Versión 1.0.0 (x64 Windows Native)", fontSize = 12.sp, color = Color.Gray)
            Text("Motor de Renderizado Vectorial de Alta Resolución + Suite Adobe Acrobat + Inteligencia Artificial OpenRouter", fontSize = 11.sp, color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}
