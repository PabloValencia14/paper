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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Functions
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
import com.pablo.paper.ui.theme.AccentBlue
import com.pablo.paper.ui.theme.TextSecondary

@Composable
fun MathEquationCard(
    latexFormula: String,
    modifier: Modifier = Modifier,
    isDarkMode: Boolean = isSystemInDarkTheme()
) {
    val context = LocalContext.current
    var showRawCode by remember { mutableStateOf(false) }

    val cardBg = if (isDarkMode) Color(0xFF141721).copy(alpha = 0.85f) else Color(0xFFF8FAFC).copy(alpha = 0.95f)
    val cardBorder = if (isDarkMode) Color(0xFF2E384D) else Color(0xFFE2E8F0)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .shadow(4.dp, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        color = cardBg,
        border = BorderStroke(1.dp, cardBorder)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isDarkMode) Color(0xFF1A1F2C) else Color(0xFFEEF2F6))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Formula Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF10B981).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Functions,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = "Fórmula Matemática",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 11.sp
                        )
                    )
                }

                // Actions: Toggle LaTeX / Copy
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        onClick = { showRawCode = !showRawCode },
                        shape = RoundedCornerShape(8.dp),
                        color = if (showRawCode) AccentBlue.copy(alpha = 0.18f) else Color.Transparent,
                        border = BorderStroke(1.dp, if (showRawCode) AccentBlue else cardBorder),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (showRawCode) Icons.Rounded.Visibility else Icons.Rounded.Code,
                                contentDescription = if (showRawCode) "Ver fórmula" else "Ver LaTeX",
                                tint = if (showRawCode) AccentBlue else TextSecondary,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = if (showRawCode) "Fórmula" else "LaTeX",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (showRawCode) AccentBlue else TextSecondary
                                )
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Fórmula LaTeX", latexFormula.trim()))
                            Toast.makeText(context, "LaTeX copiado", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ContentCopy,
                            contentDescription = "Copiar LaTeX",
                            tint = TextSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }

            // Formula or Raw LaTeX
            if (showRawCode) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F1117))
                        .padding(12.dp)
                ) {
                    Text(
                        text = latexFormula.trim(),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFE2E8F0),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 60.dp, max = 220.dp)
                        .padding(horizontal = 6.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    KaTeXWebView(
                        latex = latexFormula,
                        isDarkMode = isDarkMode,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun KaTeXWebView(
    latex: String,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier
) {
    var isRendering by remember { mutableStateOf(true) }

    val safeHtml = remember(latex, isDarkMode) {
        buildKaTeXHtml(latex, isDarkMode)
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        AndroidView(
            modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp, max = 220.dp),
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    setBackgroundColor(0x00000000)
                    setLayerType(View.LAYER_TYPE_HARDWARE, null)
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
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
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = Color(0xFF10B981)
            )
        }
    }
}

private fun buildKaTeXHtml(latex: String, isDark: Boolean): String {
    val cleanLatex = latex.trim()
        .removePrefix("$$").removeSuffix("$$")
        .removePrefix("\\[").removeSuffix("\\]")
        .trim()
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", " ")

    val textColor = if (isDark) "#F8FAFC" else "#0F172A"

    return """
        <!DOCTYPE html>
        <html>
        <head>
          <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
          <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/katex.min.css">
          <script src="https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/katex.min.js"></script>
          <style>
            * { box-sizing: border-box; }
            html, body {
              margin: 0;
              padding: 8px 12px;
              background: transparent !important;
              color: $textColor;
              font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
              display: flex;
              justify-content: center;
              align-items: center;
              min-height: 100%;
              overflow-x: auto;
              overflow-y: hidden;
            }
            #katex-target {
              display: inline-block;
              font-size: 1.18em;
              color: $textColor !important;
            }
            .katex {
              color: $textColor !important;
            }
          </style>
        </head>
        <body>
          <div id="katex-target"></div>
          <script>
            try {
              katex.render("$cleanLatex", document.getElementById("katex-target"), {
                displayMode: true,
                throwOnError: false
              });
            } catch (e) {
              document.getElementById("katex-target").innerText = "$cleanLatex";
            }
          </script>
        </body>
        </html>
    """.trimIndent()
}
