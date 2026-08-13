package com.example.elderhelpprototypev01.overlay

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.elderhelpprototypev01.MainActivity
import com.example.elderhelpprototypev01.R

/**
 * SahaayOverlayView – Floating Assistant UI.
 *
 * Designed with premium aesthetics:
 *   - Rich vibrant gradient main floating button with glowing ring
 *   - Smooth spring animations for sub-actions menu
 *   - Pill-styled text badges for high legibility over any app background
 *   - Pulse micro-animations on interactive touch
 */
class SahaayOverlayView(
    context: Context,
    private val windowManager: WindowManager,
    private val windowParams: WindowManager.LayoutParams,
    private val actionHandler: OverlayActionHandler = StubOverlayActionHandler()
) : FrameLayout(context) {

    private var isExpanded = false

    // Layout Containers
    private val actionContainer: FrameLayout
    private val mainButton: FrameLayout
    private val pulseRing: View

    // Drag Tracking
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var isDragging = false
    private val DRAG_THRESHOLD = 12 // pixels

    companion object {
        private const val ANIM_DURATION = 260L
        private const val BUTTON_SIZE_DP = 60
        private const val SUB_SIZE_DP = 50
        const val TOTAL_VIEW_SIZE_DP = 240
    }

    init {
        val density = context.resources.displayMetrics.density
        val btnPx = (BUTTON_SIZE_DP * density).toInt()
        val subPx = (SUB_SIZE_DP * density).toInt()
        val totalPx = (TOTAL_VIEW_SIZE_DP * density).toInt()

        layoutParams = LayoutParams(totalPx, totalPx)

        // ---- Outer Pulse Ring for Main Button ----
        pulseRing = View(context).apply {
            background = createCircleGradient(
                intArrayOf(Color.parseColor("#402563EB"), Color.parseColor("#002563EB"))
            )
            visibility = View.VISIBLE
        }
        val ringPx = (btnPx * 1.35f).toInt()
        val ringParams = LayoutParams(ringPx, ringPx).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            bottomMargin = -((ringPx - btnPx) / 2)
        }
        addView(pulseRing, ringParams)
        startPulseRingAnimation()

        // ---- Main Floating Sahaay Button ----
        mainButton = buildMainButton(context, btnPx)
        val mainParams = LayoutParams(btnPx, btnPx).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        }
        addView(mainButton, mainParams)

        // ---- Action Container (Sub-Buttons Menu) ----
        actionContainer = FrameLayout(context).apply {
            alpha = 0f
            scaleX = 0.3f
            scaleY = 0.3f
            visibility = View.GONE
        }
        val containerParams = LayoutParams(totalPx, totalPx).apply {
            gravity = Gravity.TOP or Gravity.START
        }
        addView(actionContainer, containerParams)

        val center = totalPx / 2
        val radius = (96 * density).toInt()

        data class SubAction(
            val label: String,
            val emoji: String,
            val angleRad: Double,
            val action: () -> Unit
        )

        val subActions = listOf(
            SubAction("Voice", "🎙️", Math.toRadians(0.0)) { // Right
                val intent = Intent(context, MainActivity::class.java).apply {
                    putExtra(MainActivity.EXTRA_OPEN_VOICE_TAB, true)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                }
                context.startActivity(intent)
                collapseMenu()
            },
            SubAction("Read Screen", "👁️", Math.toRadians(270.0)) { // Top
                val intent = SahaayOverlayService.analyzeScreenIntent(context, "Read this screen")
                context.startService(intent)
                collapseMenu()
            },
            SubAction("Explain", "💡", Math.toRadians(180.0)) { // Left
                val intent = SahaayOverlayService.analyzeScreenIntent(context, "Explain this screen")
                context.startService(intent)
                collapseMenu()
            },
            SubAction("SOS", "🆘", Math.toRadians(225.0)) { // Bottom-Left — Emergency SOS Redirect
                val intent = Intent(context, MainActivity::class.java).apply {
                    putExtra(MainActivity.EXTRA_TRIGGER_SOS, true)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                }
                context.startActivity(intent)
                collapseMenu()
            }
        )

        for (sub in subActions) {
            val subView = buildSubButton(context, sub.emoji, sub.label, subPx, sub.action)
            val x = center + (radius * Math.cos(sub.angleRad)).toInt() - subPx / 2
            val y = center + (radius * Math.sin(sub.angleRad)).toInt() - subPx / 2
            val subParams = LayoutParams(subPx + 36, subPx + 34).apply {
                leftMargin = x - 18
                topMargin = y
            }
            actionContainer.addView(subView, subParams)
        }

        // ---- Touch & Drag Handling ----
        mainButton.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isDragging = false
                    initialX = windowParams.x
                    initialY = windowParams.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    animateButtonPress(mainButton, true)
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
                    animateButtonPress(mainButton, false)
                    if (!isDragging) {
                        if (isExpanded) collapseMenu() else expandMenu()
                    } else {
                        snapToEdge()
                    }
                    true
                }
                else -> false
            }
        }
    }

    // ------------------------------------------------------------------
    // Custom UI Builders
    // ------------------------------------------------------------------

    private fun buildMainButton(context: Context, sizePx: Int): FrameLayout {
        val density = context.resources.displayMetrics.density

        val frame = FrameLayout(context)
        frame.elevation = 16f * density

        // Gradient Background
        val gradient = GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(
                Color.parseColor("#2563EB"), // Royal Blue
                Color.parseColor("#1D4ED8"),
                Color.parseColor("#1E40AF")  // Deep Indigo
            )
        ).apply {
            shape = GradientDrawable.OVAL
            setStroke((2.5f * density).toInt(), Color.parseColor("#80FFFFFF"))
        }
        frame.background = gradient

        // White Mic Icon
        val icon = ImageView(context).apply {
            setImageResource(R.drawable.ic_overlay_mic)
            setColorFilter(Color.WHITE)
        }
        val iconPad = (15 * density).toInt()
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
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }

        // Circular Icon Frame
        val circle = FrameLayout(context).apply {
            elevation = 10f * density
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.WHITE)
                setStroke((1.5f * density).toInt(), Color.parseColor("#E2E8F0"))
            }
        }

        val emojiView = TextView(context).apply {
            text = emoji
            textSize = 20f
            gravity = Gravity.CENTER
        }
        circle.addView(emojiView, ViewGroup.LayoutParams(sizePx, sizePx))

        // Pill-style Label Container
        val labelContainer = FrameLayout(context).apply {
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 10f * density
                setColor(Color.parseColor("#F1F5F9"))
                setStroke((1f * density).toInt(), Color.parseColor("#CBD5E1"))
            }
            elevation = 4f * density
        }

        val labelView = TextView(context).apply {
            text = label
            textSize = 11f
            setTextColor(Color.parseColor("#0F172A"))
            gravity = Gravity.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setPadding(
                (6 * density).toInt(),
                (2 * density).toInt(),
                (6 * density).toInt(),
                (2 * density).toInt()
            )
        }
        labelContainer.addView(labelView)

        layout.addView(circle, ViewGroup.LayoutParams(sizePx, sizePx))
        layout.addView(
            labelContainer,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = (4 * density).toInt()
            }
        )

        layout.setOnClickListener {
            animateButtonPress(layout, true) {
                animateButtonPress(layout, false) {
                    onClick()
                }
            }
        }
        return layout
    }

    private fun createCircleGradient(colors: IntArray): GradientDrawable {
        return GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors).apply {
            shape = GradientDrawable.OVAL
        }
    }

    // ------------------------------------------------------------------
    // Micro Animations
    // ------------------------------------------------------------------

    private fun startPulseRingAnimation() {
        val anim = ValueAnimator.ofFloat(1f, 1.35f).apply {
            duration = 1600
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { value ->
                val scale = value.animatedValue as Float
                pulseRing.scaleX = scale
                pulseRing.scaleY = scale
                pulseRing.alpha = (1.35f - scale) / 0.35f * 0.6f
            }
        }
        anim.start()
    }

    private fun animateButtonPress(view: View, isPressed: Boolean, onEnd: () -> Unit = {}) {
        val targetScale = if (isPressed) 0.88f else 1.0f
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", view.scaleX, targetScale)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", view.scaleY, targetScale)
        AnimatorSet().apply {
            playTogether(scaleX, scaleY)
            duration = 100
            interpolator = DecelerateInterpolator()
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    onEnd()
                }
            })
            start()
        }
    }

    // ------------------------------------------------------------------
    // Expand / Collapse Menu Animations
    // ------------------------------------------------------------------

    private fun expandMenu() {
        isExpanded = true
        actionContainer.visibility = View.VISIBLE

        val scaleX = ObjectAnimator.ofFloat(actionContainer, "scaleX", 0.3f, 1f)
        val scaleY = ObjectAnimator.ofFloat(actionContainer, "scaleY", 0.3f, 1f)
        val alpha = ObjectAnimator.ofFloat(actionContainer, "alpha", 0f, 1f)

        AnimatorSet().apply {
            playTogether(scaleX, scaleY, alpha)
            duration = ANIM_DURATION
            interpolator = OvershootInterpolator(1.35f)
            start()
        }

        // Pulse effect on main button
        ObjectAnimator.ofFloat(mainButton, "scaleX", 1f, 1.15f, 1f).apply {
            duration = 240
            start()
        }
        ObjectAnimator.ofFloat(mainButton, "scaleY", 1f, 1.15f, 1f).apply {
            duration = 240
            start()
        }
    }

    fun collapseMenu() {
        if (!isExpanded) return
        isExpanded = false

        val scaleX = ObjectAnimator.ofFloat(actionContainer, "scaleX", 1f, 0.3f)
        val scaleY = ObjectAnimator.ofFloat(actionContainer, "scaleY", 1f, 0.3f)
        val alpha = ObjectAnimator.ofFloat(actionContainer, "alpha", 1f, 0f)

        AnimatorSet().apply {
            playTogether(scaleX, scaleY, alpha)
            duration = 180
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
    // Edge Snapping
    // ------------------------------------------------------------------

    private fun snapToEdge() {
        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val midX = screenWidth / 2
        val snapRight = windowParams.x > midX

        val targetX = if (snapRight) {
            screenWidth - (BUTTON_SIZE_DP * displayMetrics.density).toInt() - 16
        } else {
            16
        }

        val anim = ObjectAnimator.ofInt(windowParams.x, targetX).apply {
            duration = 220
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                windowParams.x = it.animatedValue as Int
                windowManager.updateViewLayout(this@SahaayOverlayView, windowParams)
            }
        }
        anim.start()
    }
}
