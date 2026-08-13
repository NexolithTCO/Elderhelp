package com.example.elderhelpprototypev01.overlay

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.elderhelpprototypev01.MainActivity
import com.example.elderhelpprototypev01.R

/**
 * SahaayOverlayView – the entire floating assistant UI.
 *
 * Architecture:
 *   - mainButton: 56dp circle – the always-visible Sahaay mic button
 *   - actionContainer: invisible by default, holds 4 radial sub-action buttons
 *   - Draggable via onTouch on mainButton
 *   - Expand/collapse animated via ObjectAnimator (scaleX/Y + alpha)
 *
 * Future: replace stub Toast messages with OverlayActionHandler calls.
 */
class SahaayOverlayView(
    context: Context,
    private val windowManager: WindowManager,
    private val windowParams: WindowManager.LayoutParams,
    private val actionHandler: OverlayActionHandler = StubOverlayActionHandler()
) : FrameLayout(context) {

    private var isExpanded = false

    // Sub-action buttons container
    private val actionContainer: FrameLayout
    private val mainButton: FrameLayout

    // Track drag
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var isDragging = false
    private val DRAG_THRESHOLD = 12 // pixels

    companion object {
        private const val ANIM_DURATION = 220L
        private val BUTTON_SIZE_DP = 56
        private val SUB_SIZE_DP = 48
        val TOTAL_VIEW_SIZE_DP = 220
    }

    init {
        val density = context.resources.displayMetrics.density
        val btnPx = (BUTTON_SIZE_DP * density).toInt()
        val subPx = (SUB_SIZE_DP * density).toInt()
        val totalPx = (TOTAL_VIEW_SIZE_DP * density).toInt()

        // Root layout: large transparent area to contain the expanded radial menu
        layoutParams = LayoutParams(totalPx, totalPx)

        // ---- Main Sahaay Button ----
        mainButton = buildMainButton(context, btnPx)
        val mainParams = LayoutParams(btnPx, btnPx).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        }
        addView(mainButton, mainParams)

        // ---- Action Container (4 sub-buttons around main button) ----
        actionContainer = FrameLayout(context)
        actionContainer.alpha = 0f
        actionContainer.scaleX = 0.5f
        actionContainer.scaleY = 0.5f
        actionContainer.visibility = View.GONE
        val containerParams = LayoutParams(totalPx, totalPx).apply {
            gravity = Gravity.TOP or Gravity.START
        }
        addView(actionContainer, containerParams)

        // Sub-buttons: Voice (right), Screen (top), Explain (left), Help (bottom-left)
        // Positions are relative to totalPx center
        val center = totalPx / 2
        val radius = (90 * density).toInt()

        data class SubAction(val label: String, val emoji: String, val angleRad: Double, val action: () -> Unit, val desc: String)

        val subActions = listOf(
            SubAction("Voice", "🎙️", Math.toRadians(0.0), { // Right
                val intent = Intent(context, MainActivity::class.java).apply {
                    putExtra(MainActivity.EXTRA_OPEN_VOICE_TAB, true)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                }
                context.startActivity(intent)
                collapseMenu()
            }, "Voice"),
            SubAction("Screen", "👁️", Math.toRadians(270.0), { // Top
                val intent = Intent(context, MainActivity::class.java).apply {
                    putExtra(MainActivity.EXTRA_ANALYZE_SCREEN, "Read this screen")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                }
                context.startActivity(intent)
                collapseMenu()
            }, "Read Screen"),
            SubAction("Explain", "💡", Math.toRadians(180.0), { // Left
                val intent = Intent(context, MainActivity::class.java).apply {
                    putExtra(MainActivity.EXTRA_ANALYZE_SCREEN, "Explain this screen")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                }
                context.startActivity(intent)
                collapseMenu()
            }, "Explain"),
            SubAction("Help", "🆘", Math.toRadians(225.0), { // Bottom-left
                val intent = Intent(context, MainActivity::class.java).apply {
                    putExtra(MainActivity.EXTRA_ANALYZE_SCREEN, "What should I do next?")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                }
                context.startActivity(intent)
                collapseMenu()
            }, "Help")
        )

        for (sub in subActions) {
            val subView = buildSubButton(context, sub.emoji, sub.label, subPx, sub.action)
            val x = center + (radius * Math.cos(sub.angleRad)).toInt() - subPx / 2
            val y = center + (radius * Math.sin(sub.angleRad)).toInt() - subPx / 2
            val subParams = LayoutParams(subPx + 20, subPx + 30).apply {
                leftMargin = x
                topMargin = y
            }
            actionContainer.addView(subView, subParams)
        }

        // ---- Touch Handling ----
        mainButton.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isDragging = false
                    initialX = windowParams.x
                    initialY = windowParams.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - initialTouchX
                    val dy = event.rawY - initialTouchY
                    if (!isDragging && (Math.abs(dx) > DRAG_THRESHOLD || Math.abs(dy) > DRAG_THRESHOLD)) {
                        isDragging = true
                        if (isExpanded) collapseMenu()
                    }
                    if (isDragging) {
                        windowParams.x = initialX + dx.toInt()
                        windowParams.y = initialY + dy.toInt()
                        windowManager.updateViewLayout(this@SahaayOverlayView, windowParams)
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!isDragging) {
                        // Tap – toggle expand/collapse
                        if (isExpanded) collapseMenu() else expandMenu()
                    } else {
                        // Snap to nearest edge after drag
                        snapToEdge()
                    }
                    true
                }
                else -> false
            }
        }
    }

    // ------------------------------------------------------------------
    // Builders
    // ------------------------------------------------------------------

    private fun buildMainButton(context: Context, sizePx: Int): FrameLayout {
        val frame = FrameLayout(context)
        frame.elevation = 12f * context.resources.displayMetrics.density
        frame.background = createCircleDrawable(context, 0xFF1E56A0.toInt())

        val icon = ImageView(context)
        icon.setImageResource(R.drawable.ic_overlay_mic)
        icon.setColorFilter(0xFFFFFFFF.toInt())
        val iconPad = (14 * context.resources.displayMetrics.density).toInt()
        icon.setPadding(iconPad, iconPad, iconPad, iconPad)
        frame.addView(icon, LayoutParams(sizePx, sizePx))

        return frame
    }

    private fun buildSubButton(
        context: Context,
        emoji: String,
        label: String,
        sizePx: Int,
        onClick: () -> Unit
    ): LinearLayout {
        val density = context.resources.displayMetrics.density
        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL
        layout.gravity = Gravity.CENTER

        // Circular icon
        val circle = FrameLayout(context)
        circle.elevation = 8f * density
        circle.background = createCircleDrawable(context, 0xFFFFFFFF.toInt())

        val emojiView = TextView(context)
        emojiView.text = emoji
        emojiView.textSize = 18f
        emojiView.gravity = Gravity.CENTER
        val pad = (8 * density).toInt()
        emojiView.setPadding(pad, pad, pad, pad)
        circle.addView(emojiView, ViewGroup.LayoutParams(sizePx, sizePx))

        // Label
        val labelView = TextView(context)
        labelView.text = label
        labelView.textSize = 12f
        labelView.setTextColor(0xFF1D1D1F.toInt())
        labelView.gravity = Gravity.CENTER
        labelView.typeface = android.graphics.Typeface.DEFAULT_BOLD
        labelView.setShadowLayer(3f, 0f, 1f, 0xFFFFFFFF.toInt())

        layout.addView(circle, ViewGroup.LayoutParams(sizePx, sizePx))
        layout.addView(labelView, ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ))

        layout.setOnClickListener { onClick() }
        return layout
    }

    private fun createCircleDrawable(context: Context, color: Int): android.graphics.drawable.GradientDrawable {
        return android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.OVAL
            setColor(color)
        }
    }

    // ------------------------------------------------------------------
    // Expand / Collapse Animation
    // ------------------------------------------------------------------

    private fun expandMenu() {
        isExpanded = true
        actionContainer.visibility = View.VISIBLE

        val scaleX = ObjectAnimator.ofFloat(actionContainer, "scaleX", 0.4f, 1f)
        val scaleY = ObjectAnimator.ofFloat(actionContainer, "scaleY", 0.4f, 1f)
        val alpha = ObjectAnimator.ofFloat(actionContainer, "alpha", 0f, 1f)

        AnimatorSet().apply {
            playTogether(scaleX, scaleY, alpha)
            duration = ANIM_DURATION
            interpolator = OvershootInterpolator(1.2f)
            start()
        }

        // Pulse the main button
        ObjectAnimator.ofFloat(mainButton, "scaleX", 1f, 1.12f, 1f).apply {
            duration = 220
            start()
        }
        ObjectAnimator.ofFloat(mainButton, "scaleY", 1f, 1.12f, 1f).apply {
            duration = 220
            start()
        }
    }

    fun collapseMenu() {
        if (!isExpanded) return
        isExpanded = false

        val scaleX = ObjectAnimator.ofFloat(actionContainer, "scaleX", 1f, 0.4f)
        val scaleY = ObjectAnimator.ofFloat(actionContainer, "scaleY", 1f, 0.4f)
        val alpha = ObjectAnimator.ofFloat(actionContainer, "alpha", 1f, 0f)

        AnimatorSet().apply {
            playTogether(scaleX, scaleY, alpha)
            duration = 160
            interpolator = DecelerateInterpolator()
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    actionContainer.visibility = View.GONE
                }
            })
            start()
        }
    }

    // ------------------------------------------------------------------
    // Snap to screen edge after drag
    // ------------------------------------------------------------------

    private fun snapToEdge() {
        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val midX = screenWidth / 2
        val snapRight = windowParams.x > midX

        val targetX = if (snapRight) {
            screenWidth - (BUTTON_SIZE_DP * context.resources.displayMetrics.density).toInt() - 16
        } else {
            16
        }

        val anim = ObjectAnimator.ofInt(windowParams.x, targetX)
        anim.duration = 200
        anim.interpolator = DecelerateInterpolator()
        anim.addUpdateListener {
            windowParams.x = it.animatedValue as Int
            windowManager.updateViewLayout(this, windowParams)
        }
        anim.start()
    }

    // ------------------------------------------------------------------
    // Placeholder toast
    // ------------------------------------------------------------------

    private fun showPlaceholder(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}
