package com.pablo.paper

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val refreshRateHandler = Handler(Looper.getMainLooper())
    private val returnToReadingRefreshRate = Runnable { preferReadingRefreshRate() }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        hideSystemBars()
        preferReadingRefreshRate()

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        val initialUri = extractPdfUri(intent)
        if (initialUri != null) {
            val app = application as PaperApplication
            lifecycleScope.launch {
                val doc = app.documentRepository.importDocumentFromUri(initialUri)
                setContent {
                    PaperApp(initialDocumentId = doc?.id)
                }
            }
        } else {
            setContent {
                PaperApp()
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemBars()
            preferReadingRefreshRate()
        }
    }

    override fun onResume() {
        super.onResume()
        hideSystemBars()
        preferReadingRefreshRate()
    }

    override fun onPause() {
        refreshRateHandler.removeCallbacks(returnToReadingRefreshRate)
        preferReadingRefreshRate()
        super.onPause()
    }

    override fun onDestroy() {
        refreshRateHandler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    /**
     * Reading a static page does not benefit from 120 Hz.  We reserve the fastest available
     * refresh mode for active stylus/touch input, then return to the lowest native mode shortly
     * after the gesture finishes. This keeps pen and scrolling latency intact without keeping
     * the panel at a high refresh rate while the user is simply reading.
     */
    private fun requestResponsiveRefreshRate() {
        refreshRateHandler.removeCallbacks(returnToReadingRefreshRate)
        applyRefreshRate(preferHighest = true)
        refreshRateHandler.postDelayed(returnToReadingRefreshRate, INPUT_REFRESH_GRACE_MS)
    }

    private fun preferReadingRefreshRate() {
        applyRefreshRate(preferHighest = false)
    }

    private fun applyRefreshRate(preferHighest: Boolean) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M) return

        val targetMode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            val currentMode = display?.mode
            val sameResolutionModes = display?.supportedModes
                ?.filter { mode ->
                    currentMode == null ||
                        (mode.physicalWidth == currentMode.physicalWidth &&
                            mode.physicalHeight == currentMode.physicalHeight)
                }
                .orEmpty()
            if (preferHighest) sameResolutionModes.maxByOrNull { it.refreshRate }
            else sameResolutionModes.minByOrNull { it.refreshRate }
        } else {
            null
        }

        val attributes = window.attributes
        val requestedRate = targetMode?.refreshRate ?: if (preferHighest) 90f else 60f
        if (attributes.preferredRefreshRate == requestedRate &&
            (targetMode == null || attributes.preferredDisplayModeId == targetMode.modeId)
        ) return

        attributes.preferredRefreshRate = requestedRate
        if (targetMode != null) {
            attributes.preferredDisplayModeId = targetMode.modeId
        }
        window.attributes = attributes
    }

    private fun hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val uri = extractPdfUri(intent)
        if (uri != null) {
            val app = application as PaperApplication
            lifecycleScope.launch {
                val doc = app.documentRepository.importDocumentFromUri(uri)
                setContent {
                    PaperApp(initialDocumentId = doc?.id)
                }
            }
        }
    }

    private fun extractPdfUri(intent: Intent?): Uri? {
        if (intent?.action == Intent.ACTION_VIEW) {
            return intent.data
        }
        return null
    }

    override fun dispatchKeyEvent(event: android.view.KeyEvent): Boolean {
        if (event.action == android.view.KeyEvent.ACTION_DOWN) {
            requestResponsiveRefreshRate()
        }
        if (com.pablo.paper.ink.StylusInputDispatcher.onKeyEvent(event)) {
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    override fun dispatchGenericMotionEvent(event: android.view.MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_MOVE || event.actionMasked == MotionEvent.ACTION_DOWN) {
            requestResponsiveRefreshRate()
        }
        com.pablo.paper.ink.StylusInputDispatcher.onGenericMotionEvent(event)
        return super.dispatchGenericMotionEvent(event)
    }

    override fun dispatchTouchEvent(event: android.view.MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_DOWN || event.actionMasked == MotionEvent.ACTION_MOVE) {
            requestResponsiveRefreshRate()
        }
        com.pablo.paper.ink.StylusInputDispatcher.onTouchEvent(event)
        return super.dispatchTouchEvent(event)
    }

    private companion object {
        const val INPUT_REFRESH_GRACE_MS = 750L
    }
}
