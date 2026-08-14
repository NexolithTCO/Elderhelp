package com.example.elderhelpprototypev01.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.example.elderhelpprototypev01.MainActivity
import com.example.elderhelpprototypev01.R
import com.example.elderhelpprototypev01.screen.ScreenAssistantEngine

/**
 * SahaayOverlayService
 *
 * A foreground service that manages the floating Sahaay overlay window.
 * Runs persistently when enabled, keeping the floating button visible
 * above all applications.
 *
 * Handles:
 *   - Voice-activated screen element highlighting (`ACTION_VOICE_HIGHLIGHT`)
 *   - "Read Screen", "Explain", "What Next?" screen analysis (`ACTION_ANALYZE_SCREEN`)
 *   - Stop service (`ACTION_STOP_OVERLAY`)
 */
class SahaayOverlayService : Service() {

    companion object {
        private const val CHANNEL_ID = "sahaay_overlay_channel"
        private const val NOTIFICATION_ID = 1001
        const val ACTION_STOP_OVERLAY = "com.example.elderhelpprototypev01.STOP_OVERLAY"
        const val ACTION_ANALYZE_SCREEN = "com.example.elderhelpprototypev01.ANALYZE_SCREEN"
        const val ACTION_VOICE_HIGHLIGHT = "com.example.elderhelpprototypev01.VOICE_HIGHLIGHT"
        const val ACTION_CLEAR_HIGHLIGHT = "com.example.elderhelpprototypev01.CLEAR_HIGHLIGHT"
        const val EXTRA_GOAL = "screen_goal"

        fun startIntent(context: Context): Intent =
            Intent(context, SahaayOverlayService::class.java)

        fun stopIntent(context: Context): Intent =
            Intent(context, SahaayOverlayService::class.java).apply {
                action = ACTION_STOP_OVERLAY
            }

        fun analyzeScreenIntent(context: Context, goal: String): Intent =
            Intent(context, SahaayOverlayService::class.java).apply {
                action = ACTION_ANALYZE_SCREEN
                putExtra(EXTRA_GOAL, goal)
            }

        fun voiceHighlightIntent(context: Context): Intent =
            Intent(context, SahaayOverlayService::class.java).apply {
                action = ACTION_VOICE_HIGHLIGHT
            }

        fun clearHighlightIntent(context: Context): Intent =
            Intent(context, SahaayOverlayService::class.java).apply {
                action = ACTION_CLEAR_HIGHLIGHT
            }
    }

    private var windowManager: WindowManager? = null
    private var overlayView: SahaayOverlayView? = null

    /** Screen assistant engine — runs accessibility + voice + Gemini + TTS + Highlight directly */
    var screenEngine: ScreenAssistantEngine? = null
        private set

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())

        screenEngine = ScreenAssistantEngine(applicationContext)

        showOverlay()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_OVERLAY -> {
                SahaayPreferences.setOverlayEnabled(this, false)
                stopSelf()
            }
            ACTION_ANALYZE_SCREEN -> {
                val goal = intent.getStringExtra(EXTRA_GOAL) ?: "Read this screen"
                screenEngine?.analyzeAndGuide(goal)
            }
            ACTION_VOICE_HIGHLIGHT -> {
                screenEngine?.startVoiceListeningAndHighlight()
            }
            ACTION_CLEAR_HIGHLIGHT -> {
                com.example.elderhelpprototypev01.highlight.HighlightManager.clearHighlight(this)
                screenEngine?.clearHighlight()
            }
        }
        return START_NOT_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        cleanupService()
        stopSelf()
    }

    override fun onDestroy() {
        cleanupService()
        super.onDestroy()
    }

    private fun cleanupService() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } else {
                @Suppress("DEPRECATION")
                stopForeground(true)
            }
        } catch (e: Exception) {
            // Ignore if notification was not attached
        }
        removeOverlay()
        screenEngine?.destroy()
        screenEngine = null
    }

    // ------------------------------------------------------------------
    // Overlay Window
    // ------------------------------------------------------------------

    private fun showOverlay() {
        if (overlayView != null) return

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val density = resources.displayMetrics.density
        val totalPx = (SahaayOverlayView.TOTAL_VIEW_SIZE_DP * density).toInt()

        val buttonOffsetPx = ((SahaayOverlayView.TOTAL_VIEW_SIZE_DP - 64) / 2 * density).toInt() // Center offset of 64dp button inside 260dp FrameLayout
        val params = WindowManager.LayoutParams(
            totalPx,
            totalPx,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = (resources.displayMetrics.widthPixels - totalPx + buttonOffsetPx - (12 * density).toInt()).coerceAtLeast(0)
            y = (resources.displayMetrics.heightPixels / 4)
        }

        overlayView = SahaayOverlayView(this, windowManager!!, params)
        windowManager!!.addView(overlayView, params)
    }

    private fun removeOverlay() {
        try {
            overlayView?.let { view ->
                view.collapseMenu()
                windowManager?.removeView(view)
            }
        } catch (e: Exception) {
            // View may have already been removed
        }
        overlayView = null
    }

    // ------------------------------------------------------------------
    // Foreground Notification
    // ------------------------------------------------------------------

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Sahaay Assistant",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Sahaay floating assistant overlay"
                setShowBadge(false)
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val openAppPending = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopPending = PendingIntent.getService(
            this,
            1,
            stopIntent(this),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_overlay_mic)
            .setContentTitle("Sahaay is active")
            .setContentText("Floating assistant is running. Tap to open app.")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(openAppPending)
            .addAction(R.drawable.ic_overlay_mic, "Turn off", stopPending)
            .setOngoing(true)
            .build()
    }
}
