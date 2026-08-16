package com.example.elderhelpprototypev01.highlight

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.WindowManager
import android.widget.Toast
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * HighlightManager
 *
 * Singleton manager responsible for rendering high-brightness electric yellow highlights
 * over target UI elements across apps using Android [WindowManager].
 *
 * Non-blocking: Uses FLAG_NOT_TOUCHABLE so user taps pass directly through to the app underneath.
 * Fixed window lifecycle: Removes existing view synchronously before adding new view to prevent flickering/removal bugs.
 */
object HighlightManager {

    private var windowManager: WindowManager? = null
    private var activeView: HighlightOverlayView? = null
    private val handler = Handler(Looper.getMainLooper())
    private var dismissRunnable: Runnable? = null

    private val _activeHighlight = MutableStateFlow<HighlightData?>(null)
    val activeHighlight: StateFlow<HighlightData?> = _activeHighlight.asStateFlow()

    /**
     * Display a high-brightness electric yellow highlight box over [bounds].
     * @param context Application/Service Context
     * @param bounds Screen rectangle of target UI element
     * @param targetText Text label of the element
     * @param explanation Instruction explanation
     * @param autoDismissMs Auto dismiss duration (default 20000ms / 20 seconds)
     */
    fun showHighlight(
        context: Context,
        bounds: Rect,
        targetText: String = "",
        explanation: String = "",
        autoDismissMs: Long = 20000L
    ) {
        // Guarantee execution on Main Thread
        if (Looper.myLooper() != Looper.getMainLooper()) {
            handler.post { showHighlight(context, bounds, targetText, explanation, autoDismissMs) }
            return
        }

        // 1. Remove previous view synchronously (NO handler.post queue race condition)
        removeActiveViewInternal()

        val data = HighlightData(
            bounds = bounds,
            targetText = targetText,
            explanation = explanation
        )
        _activeHighlight.value = data

        try {
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager = wm

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = 0
                y = 0
            }

            val view = HighlightOverlayView(context, data) {
                clearHighlight(context)
            }

            activeView = view
            wm.addView(view, params)

            val displayLabel = targetText.ifBlank { "target element" }
            Toast.makeText(context, "✨ Highlighted '$displayLabel' on screen", Toast.LENGTH_SHORT).show()

            // Schedule auto-dismiss (20s)
            dismissRunnable?.let { handler.removeCallbacks(it) }
            dismissRunnable = Runnable { removeActiveViewInternal() }
            handler.postDelayed(dismissRunnable!!, autoDismissMs)

        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Please allow 'Display over other apps' permission in Settings to see screen highlights.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /** Remove active screen highlight overlay safely. */
    fun clearHighlight(context: Context? = null) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            handler.post { removeActiveViewInternal() }
        } else {
            removeActiveViewInternal()
        }
    }

    private fun removeActiveViewInternal() {
        dismissRunnable?.let { handler.removeCallbacks(it) }
        dismissRunnable = null

        val view = activeView
        activeView = null
        _activeHighlight.value = null

        if (view != null) {
            try {
                windowManager?.removeView(view)
            } catch (e: Exception) {
                // View was already detached or window manager released
            }
        }
    }
}
