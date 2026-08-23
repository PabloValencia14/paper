package com.pablo.paper

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pablo.paper.ui.library.LibraryScreen
import com.pablo.paper.ui.library.LibraryViewModel
import com.pablo.paper.ui.reader.ReaderScreen
import com.pablo.paper.ui.reader.ReaderViewModel
import com.pablo.paper.ui.theme.PaperTheme

sealed class Screen(val route: String) {
    data object Library : Screen("library")
    data object Reader : Screen("reader/{documentId}") {
        fun createRoute(documentId: String) = "reader/$documentId"
    }
}

@Composable
fun PaperApp(
    initialDocumentId: String? = null,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val app = context.applicationContext as PaperApplication

    val startDestination = if (initialDocumentId != null) {
        Screen.Reader.createRoute(initialDocumentId)
    } else {
        Screen.Library.route
    }

    val themeMode by app.preferencesRepository.themeModeFlow.collectAsState(initial = com.pablo.paper.domain.model.AppThemeMode.SYSTEM)
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        com.pablo.paper.domain.model.AppThemeMode.DARK -> true
        com.pablo.paper.domain.model.AppThemeMode.LIGHT -> false
        com.pablo.paper.domain.model.AppThemeMode.SEPIA -> false
        com.pablo.paper.domain.model.AppThemeMode.SYSTEM -> isSystemDark
        else -> isSystemDark
    }

    PaperTheme(darkTheme = isDark) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = modifier.fillMaxSize()
        ) {
            composable(Screen.Library.route) {
                val libraryViewModel: LibraryViewModel = viewModel(
                    factory = LibraryViewModel.Factory(app.documentRepository, app.preferencesRepository)
                )

                LibraryScreen(
                    viewModel = libraryViewModel,
                    onOpenReader = { docId ->
                        navController.navigate(Screen.Reader.createRoute(docId))
                    }
                )
            }

            composable(
                route = Screen.Reader.route,
                arguments = listOf(
                    navArgument("documentId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val documentId = backStackEntry.arguments?.getString("documentId") ?: return@composable

                val readerViewModel: ReaderViewModel = viewModel(
                    key = documentId,
                    factory = ReaderViewModel.Factory(
                        documentId = documentId,
                        documentRepository = app.documentRepository,
                        annotationRepository = app.annotationRepository,
                        preferencesRepository = app.preferencesRepository,
                        context = context
                    )
                )

                ReaderScreen(
                    viewModel = readerViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
