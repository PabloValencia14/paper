package com.pablo.paper.desktop

import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.pablo.paper.desktop.state.WorkspaceState
import com.pablo.paper.desktop.ui.DesktopWindowContent
import java.io.File

fun main(args: Array<String>) = application {
    val windowState = rememberWindowState(
        width = 1400.dp,
        height = 900.dp
    )

    val scope = rememberCoroutineScope()
    val workspaceState = remember {
        WorkspaceState(scope).apply {
            // Open initial document if passed via command line
            if (args.isNotEmpty()) {
                val f = File(args[0])
                if (f.exists() && f.name.lowercase().endsWith(".pdf")) {
                    openDocument(f)
                }
            }
        }
    }

    val closeApplication = {
        workspaceState.saveAllSessions(::exitApplication)
    }

    Window(
        onCloseRequest = closeApplication,
        state = windowState,
        undecorated = true,
        title = if (workspaceState.activeTab != null) {
            "${workspaceState.activeTab?.title} - Paper"
        } else {
            "Paper"
        }
    ) {
        DesktopWindowContent(
            state = workspaceState,
            windowState = windowState,
            onCloseWindow = closeApplication
        )
    }
}
