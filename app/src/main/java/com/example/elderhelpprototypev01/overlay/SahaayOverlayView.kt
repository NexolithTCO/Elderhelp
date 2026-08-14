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
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.elderhelpprototypev01.MainActivity
import com.example.elderhelpprototypev01.R
import com.example.elderhelpprototypev01.highlight.HighlightManager

/**
 * SahaayOverlayView – Professional System Assistant Overlay UI with Highlight Off button.
 */
class SahaayOverlayView(
    context: Context,
    private val windowManager: WindowManager,
    private val windowParams: WindowManager.LayoutParams,
    private val actionHandler: OverlayActionHandler = StubOverlayActionHandler()
) : FrameLayout(context) {

    private var isExpanded = false

    private val actionContainer: FrameLayout
    private val menuBackdrop: View
    private val mainButton: FrameLayout
    private val pulseRing: View
    private val subViews = mutableListOf<View>()

    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var isDragging = false
    private val DRAG_THRESHOLD = 12 // pixels

    companion object {
        private const val ANIM_DURATION = 240L
        private const val BUTTON_SIZE_DP = 64
        private const val SUB_SIZE_DP = 46
        const val TOTAL_VIEW_SIZE_DP = 260
    }

    init {
        val density = context.resources.displayMetrics.density
        val btnPx = (BUTTON_SIZE_DP * density).toInt()
        val subPx = (SUB_SIZE_DP * density).toInt()
        val totalPx = (TOTAL_VIEW_SIZE_DP * density).toInt()

        layoutParams = LayoutParams(totalPx, totalPx)

        // ---- Ambient Outer Pulse Ring ----
        pulseRing = View(context).apply {
            background = createCircleGradient(
                intArrayOf(Color.parseColor("#301B6B7D"), Color.parseColor("#001B6B7D"))
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

        // ---- Main Floating Button with Sahaay Logo ----
        mainButton = buildMainButton(context, btnPx)
        val mainParams = LayoutParams(btnPx, btnPx).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        }
        addView(mainButton, mainParams)

        // ---- Expanded Action Container ----
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

        // ---- Transparent Backdrop Stub ----
        menuBackdrop = View(context).apply {
            visibility = View.GONE
        }
        actionContainer.addView(menuBackdrop)

        // ---- Sub-Action Buttons Radial Layout ----
        val centerX = totalPx / 2
        val centerY = totalPx / 2
        val radius = (92 * density).toInt()

        data class SubAction(
            val label: String,
            val symbol: String,
            val accentColor: String,
            val angleRad: Double,
            val action: () -> Unit
        )

        val subActions = listOf(
            SubAction("Read", "👁", "#2E7D32", Math.toRadians(270.0)) {
                val intent = SahaayOverlayService.analyzeScreenIntent(context, "Read this screen")
                context.startService(intent)
                collapseMenu()
            },
            SubAction("Voice", "🎙", "#1B6B7D", Math.toRadians(320.0)) {
                val intent = SahaayOverlayService.voiceHighlightIntent(context)
                context.startService(intent)
                collapseMenu()
            },
            SubAction("SOS", "🆘", "#BA1A1A", Math.toRadians(220.0)) {
                val intent = Intent(context, MainActivity::class.java).apply {
                    putExtra(MainActivity.EXTRA_TRIGGER_SOS, true)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                }
                context.startActivity(intent)
                collapseMenu()
            },
            SubAction("Explain", "💡", "#C4820E", Math.toRadians(175.0)) {
                val intent = SahaayOverlayService.analyzeScreenIntent(context, "Explain this screen")
                context.startService(intent)
                collapseMenu()
            },
            SubAction("Clear", "✕", "#64748B", Math.toRadians(5.0)) {
                HighlightManager.clearHighlight(context)
                val intent = SahaayOverlayService.clearHighlightIntent(context)
                context.startService(intent)
                collapseMenu()
            }
        )

        for (sub in subActions) {
            val subView = buildSubButton(context, sub.symbol, sub.label, sub.accentColor, subPx, sub.action)
            val subW = (subPx + (28 * density)).toInt()
            val subH = (subPx + (28 * density)).toInt()

            val x = centerX + (radius * Math.cos(sub.angleRad)).toInt() - (subW / 2)
            val y = centerY + (radius * Math.sin(sub.angleRad)).toInt() - (subH / 2)

            val subParams = LayoutParams(subW, subH).apply {
                leftMargin = x.coerceIn(0, totalPx - subW)
                topMargin = y.coerceIn(0, totalPx - subH)
            }
            actionContainer.addView(subView, subParams)
            subViews.add(subView)
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

    private fun buildMainButton(context: Context, sizePx: Int): FrameLayout {
        val density = context.resources.displayMetrics.density

        val frame = FrameLayout(context).apply {
            elevation = 14f * density
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#1B6B7D")) // Sahaay Primary Teal
                setStroke((3f * density).toInt(), Color.parseColor("#C4820E")) // Sahaay Accent Amber
            }
        }

        val logoView = ImageView(context).apply {
            setImageResource(R.drawable.ic_sahaay_logo)
            scaleType = ImageView.ScaleType.FIT_CENTER
        }
        val iconPad = (4 * density).toInt()
        logoView.setPadding(iconPad, iconPad, iconPad, iconPad)
        frame.addView(logoView, LayoutParams(sizePx, sizePx))

        return frame
    }

    private fun buildSubButton(
        context: Context,
        symbol: String,
        label: String,
        accentColorHex: String,
        sizePx: Int,
        onClick: () -> Unit
    ): View {
        val density = context.resources.displayMetrics.density

        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            elevation = 12f * density
        }

        // Circular Icon Badge
        val circle = FrameLayout(context).apply {
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.WHITE)
                setStroke((2f * density).toInt(), Color.parseColor(accentColorHex))
            }
        }

        val symbolView = TextView(context).apply {
            text = symbol
            textSize = 20f
            setTextColor(Color.parseColor(accentColorHex))
            gravity = Gravity.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        circle.addView(symbolView, FrameLayout.LayoutParams(sizePx, sizePx))

        // Pill Label
        val labelContainer = FrameLayout(context).apply {
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 6f * density
                setColor(Color.parseColor("#1E293B"))
                setStroke((1f * density).toInt(), Color.parseColor(accentColorHex))
            }
        }

        val labelView = TextView(context).apply {
            text = label
            textSize = 10f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setPadding(
                (5 * density).toInt(),
                (2 * density).toInt(),
                (5 * density).toInt(),
                (2 * density).toInt()
            )
        }
        labelContainer.addView(labelView)

        layout.addView(circle, LinearLayout.LayoutParams(sizePx, sizePx))
        layout.addView(
            labelContainer,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = (2 * density).toInt()
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
                pulseRing.alpha = (1.35f - scale) / 0.35f * 0.5f
            }
        }
        anim.start()
    }

    private fun animateButtonPress(view: View, isPressed: Boolean, onEnd: () -> Unit = {}) {
        val targetScale = if (isPressed) 0.90f else 1.0f
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", view.scaleX, targetScale)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", view.scaleY, targetScale)
        AnimatorSet().apply {
            playTogether(scaleX, scaleY)
            duration = 90
            interpolator = DecelerateInterpolator()
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    onEnd()
                }
            })
            start()
        }
    }

    private fun expandMenu() {
        isExpanded = true
        actionContainer.visibility = View.VISIBLE

        val containerScaleX = ObjectAnimator.ofFloat(actionContainer, "scaleX", 0.3f, 1f)
        val containerScaleY = ObjectAnimator.ofFloat(actionContainer, "scaleY", 0.3f, 1f)
        val containerAlpha = ObjectAnimator.ofFloat(actionContainer, "alpha", 0f, 1f)

        AnimatorSet().apply {
            playTogether(containerScaleX, containerScaleY, containerAlpha)
            duration = ANIM_DURATION
            interpolator = DecelerateInterpolator()
            start()
        }

        subViews.forEachIndexed { index, view ->
            view.scaleX = 0.5f
            view.scaleY = 0.5f
            view.alpha = 0f
            view.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setStartDelay(index * 25L)
                .setDuration(180L)
                .setInterpolator(DecelerateInterpolator())
                .start()
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
            duration = 200
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                windowParams.x = it.animatedValue as Int
                windowManager.updateViewLayout(this@SahaayOverlayView, windowParams)
            }
        }
        anim.start()
    }
}
