package com.example.elderhelpprototypev01.highlight

import android.graphics.Rect

/**
 * Encapsulates the visual state of an active screen element highlight overlay.
 */
data class HighlightData(
    val bounds: Rect,
    val targetText: String = "",
    val explanation: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
