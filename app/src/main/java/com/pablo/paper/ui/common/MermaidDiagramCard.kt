package com.pablo.paper.ui.common

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountTree
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.FullscreenExit
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.pablo.paper.ui.theme.AccentBlue
import com.pablo.paper.ui.theme.BorderSubtle
import com.pablo.paper.ui.theme.TextSecondary
import java.net.URLEncoder

@Composable
fun MermaidDiagramCard(
    mermaidCode: String,
    modifier: Modifier = Modifier,
    isDarkMode: Boolean = isSystemInDarkTheme()
) {
    val context = LocalContext.current
    var showRawCode by remember { mutableStateOf(false) }
    var isFullscreen by remember { mutableStateOf(false) }

    val diagramType = remember(mermaidCode) { detectDiagramType(mermaidCode) }
    val cardBg = if (isDarkMode) Color(0xFF141721).copy(alpha = 0.9f) else Color(0xFFF8FAFC).copy(alpha = 0.95f)
    val cardBorder = if (isDarkMode) Color(0xFF2E384D) else Color(0xFFE2E8F0)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .shadow(6.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = cardBg,
        border = BorderStroke(1.dp, cardBorder)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isDarkMode) Color(0xFF1A1F2C) else Color(0xFFEEF2F6))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Diagram Type Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(AccentBlue.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AccountTree,
                            contentDescription = null,
                            tint = AccentBlue,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = diagramType,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 11.5.sp
                        )
                    )
                }

                // Actions: Toggle Code / Copy / Fullscreen
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Toggle Code / Visual
                    Surface(
                        onClick = { showRawCode = !showRawCode },
                        shape = RoundedCornerShape(8.dp),
                        color = if (showRawCode) AccentBlue.copy(alpha = 0.18f) else Color.Transparent,
                        border = BorderStroke(1.dp, if (showRawCode) AccentBlue else cardBorder),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (showRawCode) Icons.Rounded.Visibility else Icons.Rounded.Code,
                                contentDescription = if (showRawCode) "Ver diagrama" else "Ver código",
                                tint = if (showRawCode) AccentBlue else TextSecondary,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = if (showRawCode) "Diagrama" else "Código",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (showRawCode) AccentBlue else TextSecondary
                                )
                            )
                        }
                    }

                    // Copy Button
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Código Mermaid", mermaidCode.trim()))
                            Toast.makeText(context, "Diagrama copiado", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ContentCopy,
                            contentDescription = "Copiar código",
                            tint = TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    // Expand / Fullscreen Button
                    IconButton(
                        onClick = { isFullscreen = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Fullscreen,
                            contentDescription = "Pantalla completa",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Content: Visual Diagram or Raw Code
            if (showRawCode) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F1117))
                        .padding(12.dp)
                ) {
                    Text(
                        text = mermaidCode.trim(),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFE2E8F0),
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 180.dp, max = 340.dp)
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    MermaidWebView(
                        mermaidCode = mermaidCode,
                        isDarkMode = isDarkMode,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    // Fullscreen Interactive Modal
    if (isFullscreen) {
        Dialog(
            onDismissRequest = { isFullscreen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                color = if (isDarkMode) Color(0xFF0B0D13) else Color(0xFFF1F5F9),
                border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF2E384D) else Color(0xFFCBD5E1))
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Modal Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.AccountTree,
                                contentDescription = null,
                                tint = AccentBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Visor de $diagramType",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }

                        IconButton(
                            onClick = { isFullscreen = false },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Cerrar",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Large Interactive WebView
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(12.dp)
                    ) {
                        MermaidWebView(
                            mermaidCode = mermaidCode,
                            isDarkMode = isDarkMode,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MermaidWebView(
    mermaidCode: String,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier
) {
    var isRendering by remember { mutableStateOf(true) }

    val safeHtml = remember(mermaidCode, isDarkMode) {
        buildMermaidHtml(mermaidCode, isDarkMode)
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setBackgroundColor(0x00000000) // Transparent
                    setLayerType(View.LAYER_TYPE_HARDWARE, null)
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        builtInZoomControls = true
                        displayZoomControls = false
                        cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
                    }
                    webChromeClient = WebChromeClient()
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isRendering = false
                        }
                    }
                    loadDataWithBaseURL("https://cdn.jsdelivr.net", safeHtml, "text/html", "UTF-8", null)
                }
            },
            update = { webView ->
                webView.loadDataWithBaseURL("https://cdn.jsdelivr.net", safeHtml, "text/html", "UTF-8", null)
            }
        )

        if (isRendering) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
                color = AccentBlue
            )
        }
    }
}

private fun detectDiagramType(code: String): String {
    val clean = code.trim().lowercase()
    return when {
        clean.startsWith("sequencediagram") -> "UML · Diagrama de Secuencia"
        clean.startsWith("classdiagram") -> "UML · Diagrama de Clases"
        clean.startsWith("statediagram") -> "UML · Máquina de Estados"
        clean.startsWith("erdiagram") -> "UML · Entidad - Relación"
        clean.startsWith("graph") || clean.startsWith("flowchart") -> "Flujograma / Arquitectura"
        clean.startsWith("gantt") -> "Diagrama de Gantt"
        clean.startsWith("mindmap") -> "Mapa Conceptual"
        clean.startsWith("pie") -> "Gráfico Circular"
        clean.startsWith("gitgraph") -> "Grafo Git"
        else -> "Diagrama UML"
    }
}

private fun buildMermaidHtml(code: String, isDark: Boolean): String {
    val cleanCode = code.trim()
        .removePrefix("```mermaid")
        .removePrefix("```uml")
        .removePrefix("```")
        .removeSuffix("```")
        .trim()

    val escapedCodeJson = com.google.gson.Gson().toJson(cleanCode)
    val theme = if (isDark) "dark" else "neutral"
    val textColor = if (isDark) "#F8FAFC" else "#0F172A"
    val primaryBg = if (isDark) "#1E293B" else "#EFF6FF"
    val primaryBorder = if (isDark) "#38BDF8" else "#2563EB"
    val lineColor = if (isDark) "#94A3B8" else "#475569"
    val secondaryBg = if (isDark) "#334155" else "#F1F5F9"

    return """
        <!DOCTYPE html>
        <html>
        <head>
          <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=yes">
          <script src="https://cdn.jsdelivr.net/npm/mermaid@10/dist/mermaid.min.js"></script>
          <style>
            * { box-sizing: border-box; margin: 0; padding: 0; }
            html, body {
              width: 100%;
              min-height: 100%;
              background: transparent !important;
              color: $textColor;
              font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
              display: flex;
              justify-content: center;
              align-items: center;
              overflow: auto;
              padding: 12px;
            }
            #container {
              width: 100%;
              display: flex;
              justify-content: center;
              align-items: center;
            }
            svg {
              max-width: 100% !important;
              height: auto !important;
              filter: drop-shadow(0 2px 8px rgba(0,0,0,0.12));
            }
            .code-fallback {
              background: rgba(30, 41, 59, 0.5);
              border: 1px solid rgba(148, 163, 184, 0.2);
              border-radius: 8px;
              padding: 12px;
              font-family: monospace;
              font-size: 11px;
              color: $textColor;
              white-space: pre-wrap;
              word-break: break-word;
              width: 100%;
            }
          </style>
        </head>
        <body>
          <div id="container">
            <div class="code-fallback" id="loading">Cargando diagrama...</div>
          </div>
          <script>
            const rawCode = $escapedCodeJson;
            try {
              mermaid.initialize({
                startOnLoad: false,
                theme: '$theme',
                securityLevel: 'loose',
                themeVariables: {
                  darkMode: $isDark,
                  background: 'transparent',
                  fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
                  primaryColor: '$primaryBg',
                  primaryTextColor: '$textColor',
                  primaryBorderColor: '$primaryBorder',
                  lineColor: '$lineColor',
                  secondaryColor: '$secondaryBg',
                  tertiaryColor: '$primaryBg'
                }
              });

              mermaid.render('mermaidSvgId', rawCode).then(function(result) {
                document.getElementById('container').innerHTML = result.svg;
              }).catch(function(err) {
                console.warn('Mermaid render error, trying sanitized fallback', err);
                // Fallback: sanitize brackets or bad characters
                let sanitized = rawCode
                  .replace(/<([a-zA-Z0-9_]+)>/g, '[$1]')
                  .replace(/\[\s*\]/g, '[]')
                  .replace(/\b([a-zA-Z0-9_]+)\s*:\s*([a-zA-Z0-9_]+)/g, '$1 $2');
                mermaid.render('mermaidSvgFallbackId', sanitized).then(function(res2) {
                  document.getElementById('container').innerHTML = res2.svg;
                }).catch(function(err2) {
                  document.getElementById('container').innerHTML = '<div class="code-fallback"><code>' + 
                    rawCode.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;') + '</code></div>';
                });
              });
            } catch(e) {
              document.getElementById('container').innerHTML = '<div class="code-fallback"><code>' + 
                rawCode.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;') + '</code></div>';
            }
          </script>
        </body>
        </html>
    """.trimIndent()
}
