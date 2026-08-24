package com.pablo.paper.ink

import android.view.KeyEvent
import android.view.MotionEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class StylusButton {
    PRIMARY,
    SECONDARY
}

sealed interface StylusEvent {
    data class ButtonDown(val button: StylusButton) : StylusEvent
    data class ButtonUp(val button: StylusButton) : StylusEvent
    data class ButtonClick(val button: StylusButton) : StylusEvent
}

object StylusInputDispatcher {
    private val _events = MutableSharedFlow<StylusEvent>(extraBufferCapacity = 16)
    val events: SharedFlow<StylusEvent> = _events.asSharedFlow()

    private var isPrimaryPressed = false
    private var isSecondaryPressed = false

    private val _isStylusHovering = MutableStateFlow(false)
    val isStylusHovering: StateFlow<Boolean> = _isStylusHovering.asStateFlow()

    private val _isStylusTouching = MutableStateFlow(false)
    val isStylusTouching: StateFlow<Boolean> = _isStylusTouching.asStateFlow()

    @Volatile
    private var lastStylusActiveUptimeMs: Long = 0L

    /**
     * Returns true if the stylus is actively touching the screen, or hovering within cursor proximity (< ~1-2cm),
     * or within a small temporal grace period (350ms) after the stylus lifted.
     */
    fun isStylusNearOrTouching(): Boolean {
        val now = android.os.SystemClock.uptimeMillis()
        return _isStylusTouching.value || _isStylusHovering.value || (now - lastStylusActiveUptimeMs < 350L)
    }

    fun notifyStylusActive() {
        lastStylusActiveUptimeMs = android.os.SystemClock.uptimeMillis()
        _isStylusHovering.value = true
    }

    fun onKeyEvent(event: KeyEvent): Boolean {
        val isDown = event.action == KeyEvent.ACTION_DOWN
        val isUp = event.action == KeyEvent.ACTION_UP
        if (!isDown && !isUp) return false

        val button = when (event.keyCode) {
            KeyEvent.KEYCODE_STYLUS_BUTTON_PRIMARY,
            KeyEvent.KEYCODE_BUTTON_1,
            KeyEvent.KEYCODE_BUTTON_L1,
            KeyEvent.KEYCODE_BUTTON_R1,
            KeyEvent.KEYCODE_PAGE_DOWN,
            KeyEvent.KEYCODE_STEM_PRIMARY,
            KeyEvent.KEYCODE_STEM_1 -> StylusButton.PRIMARY

            KeyEvent.KEYCODE_STYLUS_BUTTON_SECONDARY,
            KeyEvent.KEYCODE_STYLUS_BUTTON_TERTIARY,
            KeyEvent.KEYCODE_STYLUS_BUTTON_TAIL,
            KeyEvent.KEYCODE_BUTTON_2,
            KeyEvent.KEYCODE_BUTTON_L2,
            KeyEvent.KEYCODE_BUTTON_R2,
            KeyEvent.KEYCODE_PAGE_UP,
            KeyEvent.KEYCODE_FORWARD,
            KeyEvent.KEYCODE_STEM_2 -> StylusButton.SECONDARY

            else -> return false
        }

        if (isDown) {
            if (button == StylusButton.PRIMARY && !isPrimaryPressed) {
                isPrimaryPressed = true
                _events.tryEmit(StylusEvent.ButtonDown(button))
            } else if (button == StylusButton.SECONDARY && !isSecondaryPressed) {
                isSecondaryPressed = true
                _events.tryEmit(StylusEvent.ButtonDown(button))
            }
        } else if (isUp) {
            if (button == StylusButton.PRIMARY) {
                isPrimaryPressed = false
                _events.tryEmit(StylusEvent.ButtonUp(button))
                _events.tryEmit(StylusEvent.ButtonClick(button))
            } else if (button == StylusButton.SECONDARY) {
                isSecondaryPressed = false
                _events.tryEmit(StylusEvent.ButtonUp(button))
                _events.tryEmit(StylusEvent.ButtonClick(button))
            }
        }
        return true
    }

    fun onGenericMotionEvent(event: MotionEvent): Boolean {
        val isStylus = isStylusEvent(event)
        if (isStylus) {
            lastStylusActiveUptimeMs = android.os.SystemClock.uptimeMillis()
            when (event.actionMasked) {
                MotionEvent.ACTION_HOVER_ENTER, MotionEvent.ACTION_HOVER_MOVE -> {
                    _isStylusHovering.value = true
                }
                MotionEvent.ACTION_HOVER_EXIT -> {
                    _isStylusHovering.value = false
                }
            }
        }
        return processMotionEvent(event)
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        val isStylus = isStylusEvent(event)
        if (isStylus) {
            lastStylusActiveUptimeMs = android.os.SystemClock.uptimeMillis()
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE, MotionEvent.ACTION_POINTER_DOWN -> {
                    _isStylusTouching.value = true
                    _isStylusHovering.value = true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    _isStylusTouching.value = false
                }
            }
        } else {
            if (event.actionMasked == MotionEvent.ACTION_UP || event.actionMasked == MotionEvent.ACTION_CANCEL) {
                _isStylusTouching.value = false
            }
        }
        return processMotionEvent(event)
    }

    private fun isStylusEvent(event: MotionEvent): Boolean {
        for (i in 0 until event.pointerCount) {
            val tool = event.getToolType(i)
            if (tool == MotionEvent.TOOL_TYPE_STYLUS || tool == MotionEvent.TOOL_TYPE_ERASER) {
                return true
            }
        }
        return false
    }

    private fun processMotionEvent(event: MotionEvent): Boolean {
        val buttonState = event.buttonState
        val hasPrimary = (buttonState and (MotionEvent.BUTTON_STYLUS_PRIMARY or MotionEvent.BUTTON_SECONDARY)) != 0
        val hasSecondary = (buttonState and (MotionEvent.BUTTON_STYLUS_SECONDARY or MotionEvent.BUTTON_TERTIARY or MotionEvent.BUTTON_FORWARD)) != 0

        var handled = false

        if (hasPrimary != isPrimaryPressed) {
            isPrimaryPressed = hasPrimary
            if (hasPrimary) {
                _events.tryEmit(StylusEvent.ButtonDown(StylusButton.PRIMARY))
            } else {
                _events.tryEmit(StylusEvent.ButtonUp(StylusButton.PRIMARY))
                _events.tryEmit(StylusEvent.ButtonClick(StylusButton.PRIMARY))
            }
            handled = true
        }

        if (hasSecondary != isSecondaryPressed) {
            isSecondaryPressed = hasSecondary
            if (hasSecondary) {
                _events.tryEmit(StylusEvent.ButtonDown(StylusButton.SECONDARY))
            } else {
                _events.tryEmit(StylusEvent.ButtonUp(StylusButton.SECONDARY))
                _events.tryEmit(StylusEvent.ButtonClick(StylusButton.SECONDARY))
            }
            handled = true
        }

        return handled
    }
}
