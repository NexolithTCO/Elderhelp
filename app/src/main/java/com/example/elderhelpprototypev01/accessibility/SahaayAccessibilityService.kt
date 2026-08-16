package com.example.elderhelpprototypev01.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * SahaayAccessibilityService
 *
 * Android AccessibilityService that enables Sahaay to inspect the active UI screen,
 * extract visible elements, and detect user touch/click interaction events.
 *
 * Used strictly for GUIDANCE and HIGHLIGHTING.
 * Does NOT execute autonomous clicks, payments, or sensitive actions.
 */
class SahaayAccessibilityService : AccessibilityService() {

    companion object {
        private var _instance: SahaayAccessibilityService? = null
        val instance: SahaayAccessibilityService? get() = _instance

        private val _currentScreenContext = MutableStateFlow<ScreenContext?>(null)
        val currentScreenContext: StateFlow<ScreenContext?> = _currentScreenContext.asStateFlow()

        // Emits whenever the user clicks/taps an element or the screen content changes
        private val _userInteractionEvents = MutableSharedFlow<Long>(extraBufferCapacity = 8)
        val userInteractionEvents: SharedFlow<Long> = _userInteractionEvents.asSharedFlow()

        private var lastEventTime = 0L

        fun isServiceEnabled(context: Context): Boolean {
            val prefString = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false
            return prefString.contains(context.packageName)
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        _instance = this
        captureCurrentScreenContext()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val now = System.currentTimeMillis()

        when (event.eventType) {
            AccessibilityEvent.TYPE_VIEW_CLICKED,
            AccessibilityEvent.TYPE_VIEW_SELECTED,
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                captureCurrentScreenContext(event.packageName?.toString() ?: "")

                // Debounce rapid events within 600ms
                if (now - lastEventTime > 600L && (event.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED || event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)) {
                    lastEventTime = now
                    _userInteractionEvents.tryEmit(now)
                }
            }
        }
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        _instance = null
        _currentScreenContext.value = null
        super.onDestroy()
    }

    fun captureCurrentScreenContext(pkgName: String = ""): ScreenContext? {
        val root = rootInActiveWindow ?: return _currentScreenContext.value
        val actualPkgName = if (pkgName.isNotBlank()) pkgName else (root.packageName?.toString() ?: "")

        val context = AccessibilityNodeParser.parseTree(
            rootNode = root,
            packageName = actualPkgName
        )
        _currentScreenContext.value = context
        return context
    }
}
