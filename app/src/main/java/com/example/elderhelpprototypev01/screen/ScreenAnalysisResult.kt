package com.example.elderhelpprototypev01.screen

import android.graphics.Rect

/**
 * Result returned by [ScreenAnalysisService] after analyzing a [ScreenContext].
 * Implements the full Sahaay Accessibility Intelligence Specification schema.
 */
data class ScreenAnalysisResult(
    /** Primary classification: SCREEN_EXPLANATION, NEXT_STEP, CLARIFICATION, ERROR_HELP, FIELD_HELP, COMPLETION, SAFETY_WARNING, GENERAL_HELP */
    val responseType: String = "NEXT_STEP",
    /** Spoken output for TTS, sanitized into simple human prose */
    val spokenResponse: String = "",
    /** Short visual summary for cards / tooltips */
    val visualResponse: String = "",
    /** Target element identifier or text */
    val targetElementId: String? = null,
    val targetElementText: String? = null,
    /** Screen bounds (left, top, right, bottom) for drawing high-brightness yellow highlight box */
    val targetElementBounds: Rect? = null,
    /** Guidance action: TAP, TYPE, READ, CONFIRM, NONE */
    val actionGuidance: String = "TAP",
    /** Explanation fallback field */
    val explanation: String = spokenResponse,
    /** Reason why this target element was selected */
    val reason: String? = null,
    /** Confidence score from 0.0 to 1.0 */
    val confidence: Float = 1.0f,
    /** Action category */
    val actionType: String = "GUIDE_HIGHLIGHT",
    val requiresConfirmation: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String? = null
) {
    companion object {
        fun error(message: String) = ScreenAnalysisResult(
            responseType = "ERROR_HELP",
            spokenResponse = message,
            visualResponse = "Error",
            explanation = message,
            confidence = 0.0f,
            actionType = "ERROR",
            isError = true,
            errorMessage = message
        )

        fun noMatch(message: String = "I can see your screen. Please tell me what you would like to do.") = ScreenAnalysisResult(
            responseType = "GENERAL_HELP",
            spokenResponse = message,
            visualResponse = "No Match",
            explanation = message,
            confidence = 0.0f,
            actionType = "NO_MATCH"
        )
    }
}
