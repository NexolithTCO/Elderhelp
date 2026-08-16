package com.example.elderhelpprototypev01.highlight

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.view.View
import com.example.elderhelpprototypev01.voice.TextToSpeechManager

/**
 * HighlightOverlayView
 *
 * Rock-Solid, Non-Flickering High-Brightness Electric Yellow Box Highlight:
 *  - Hardware accelerated drawing (ZERO software layer flickering)
 *  - Pure Electric Yellow Primary Stroke (#FFFF00) with High Contrast Dark Edge Contour
 *  - High Brightness Semi-Transparent Electric Yellow Glow Fill (#65FFFF00)
 *  - Pointer arrow pointing directly at target UI element
 *  - Instruction tooltip card explaining the next step in plain human prose
 */
class HighlightOverlayView(
    context: Context,
    private val data: HighlightData,
    private val onDismiss: () -> Unit
) : View(context) {

    private val density = context.resources.displayMetrics.density

    // High Contrast Black Outer Contour (makes yellow box stand out vividly on white screens)
    private val outerContourPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.parseColor("#F0000000") // Deep solid contrast black
        strokeWidth = 14f * density
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    // High Brightness Electric Yellow Primary Stroke
    private val yellowStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.parseColor("#FFFF00") // Pure Electric Neon Yellow
        strokeWidth = 10f * density
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    // High Brightness Electric Yellow Fill Tint
    private val yellowFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#65FFFF00") // ~40% opacity high brightness yellow
    }

    // Dark Slate Tooltip Card Background
    private val cardBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#F80F172A") // Premium Dark Slate Navy (97% opacity)
    }

    private val cardBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.parseColor("#FFFF00") // Electric Yellow Card Border
        strokeWidth = 2.5f * density
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 15f * context.resources.displayMetrics.scaledDensity
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private val subTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFFF00")
        textSize = 12f * context.resources.displayMetrics.scaledDensity
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private val badgeBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#44FFFF00")
    }

    private val arrowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#FFFF00")
    }

    private var pulseAlpha = 255
    private var animator: ValueAnimator? = null

    init {
        // Use default hardware acceleration (do NOT force software layer)
        startPulseAnimation()
    }

    private fun startPulseAnimation() {
        animator = ValueAnimator.ofInt(210, 255).apply {
            duration = 800
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            addUpdateListener { anim ->
                pulseAlpha = anim.animatedValue as Int
                yellowStrokePaint.alpha = pulseAlpha
                postInvalidateOnAnimation()
            }
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val rawRect = RectF(data.bounds)
        if (rawRect.isEmpty || rawRect.width() <= 0 || rawRect.height() <= 0) {
            // Fallback to screen center region if bounds are non-positive
            rawRect.set(
                width / 5f,
                height / 3.2f,
                4 * width / 5f,
                height / 3.2f + 140f * density
            )
        }

        val padding = 4f * density
        val highlightRect = RectF(
            (rawRect.left - padding).coerceAtLeast(4f * density),
            (rawRect.top - padding).coerceAtLeast(4f * density),
            (rawRect.right + padding).coerceAtMost(width.toFloat() - 4f * density),
            (rawRect.bottom + padding).coerceAtMost(height.toFloat() - 4f * density)
        )

        val cornerRadius = 10f * density

        // 1. Draw High-Brightness Electric Yellow Box Fill & Double Contour
        canvas.drawRoundRect(highlightRect, cornerRadius, cornerRadius, yellowFillPaint)
        canvas.drawRoundRect(highlightRect, cornerRadius, cornerRadius, outerContourPaint)
        canvas.drawRoundRect(highlightRect, cornerRadius, cornerRadius, yellowStrokePaint)

        // 2. Draw pointing arrow & explanation tooltip box
        drawTooltipAndArrow(canvas, highlightRect)
    }

    private fun drawTooltipAndArrow(canvas: Canvas, highlightRect: RectF) {
        val screenWidth = width.toFloat()
        val screenHeight = height.toFloat()

        val cardMargin = 16f * density
        val cardPadding = 16f * density
        val cardWidth = (screenWidth - cardMargin * 2).coerceAtMost(360f * density)

        val placeAbove = highlightRect.top > (200f * density)
        val cardHeight = 112f * density
        val cardY = if (placeAbove) {
            (highlightRect.top - cardHeight - 20f * density).coerceAtLeast(cardMargin)
        } else {
            (highlightRect.bottom + 24f * density).coerceAtMost(screenHeight - cardHeight - cardMargin)
        }

        val cardX = ((screenWidth - cardWidth) / 2f).coerceAtLeast(cardMargin)
        val cardRect = RectF(cardX, cardY, cardX + cardWidth, cardY + cardHeight)

        // Draw Card Background & Electric Yellow Border
        val cardRadius = 20f * density
        canvas.drawRoundRect(cardRect, cardRadius, cardRadius, cardBgPaint)
        canvas.drawRoundRect(cardRect, cardRadius, cardRadius, cardBorderPaint)

        // Draw Yellow Arrow pointing directly to target region
        val arrowPath = Path()
        val targetCenterX = highlightRect.centerX().coerceIn(cardX + 24f * density, cardX + cardWidth - 24f * density)
        if (placeAbove) {
            arrowPath.moveTo(targetCenterX - 12f * density, cardRect.bottom)
            arrowPath.lineTo(targetCenterX + 12f * density, cardRect.bottom)
            arrowPath.lineTo(targetCenterX, highlightRect.top - 2f * density)
        } else {
            arrowPath.moveTo(targetCenterX - 12f * density, cardRect.top)
            arrowPath.lineTo(targetCenterX + 12f * density, cardRect.top)
            arrowPath.lineTo(targetCenterX, highlightRect.bottom + 2f * density)
        }
        arrowPath.close()
        canvas.drawPath(arrowPath, arrowPaint)

        // Draw Header Badge ("✨ SAHAAY GUIDE")
        val badgeRect = RectF(
            cardX + cardPadding,
            cardY + cardPadding,
            cardX + cardPadding + 134f * density,
            cardY + cardPadding + 24f * density
        )
        canvas.drawRoundRect(badgeRect, 10f * density, 10f * density, badgeBgPaint)
        canvas.drawText("✨ SAHAAY GUIDE", cardX + cardPadding + 8f * density, cardY + cardPadding + 16f * density, subTextPaint)

        // Draw Explanation Text inside Card (Sanitized into clean human prose)
        var textY = cardY + cardPadding + 44f * density
        val cleanExplanation = TextToSpeechManager.sanitizeForSpeech(
            data.explanation.ifBlank { "Tap the highlighted yellow region to continue." }
        )
        val lines = wrapText(cleanExplanation, textPaint, cardWidth - cardPadding * 2)

        for (line in lines.take(2)) {
            canvas.drawText(line, cardX + cardPadding, textY, textPaint)
            textY += 21f * density
        }
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(testLine) <= maxWidth) {
                currentLine = testLine
            } else {
                if (currentLine.isNotEmpty()) lines.add(currentLine)
                currentLine = word
            }
        }
        if (currentLine.isNotEmpty()) lines.add(currentLine)
        return lines
    }

    override fun onDetachedFromWindow() {
        animator?.cancel()
        animator = null
        super.onDetachedFromWindow()
    }
}
