package com.example.elderhelpprototypev01.model

/**
 * Structured response returned by the LLM service.
 *
 * The Gemini model is instructed to ALWAYS return this structure as JSON.
 * Fields map directly from the JSON schema defined in the system prompt.
 *
 * Safety note: the LLM is instructed NEVER to claim it performed an action.
 * All responses must be guidance only.
 *
 * Voice Interaction Engine additions:
 * - [isVocalAnchor]    true when the response was produced by a local vocal-anchor
 *                      short-circuit (REPEAT / GO_BACK / STOP / NEXT_STEP) rather
 *                      than the LLM.
 * - [vocalAnchorAction] the specific anchor that was triggered, or null.
 */
data class AssistantResponse(
    /** High-level intent category detected by the LLM */
    val intent: String = "GENERAL",
    /** What the user is trying to accomplish */
    val goal: String = "",
    /** The main response text to show and/or speak to the user */
    val response: String,
    /** True when the LLM needs more information before helping */
    val needsClarification: Boolean = false,
    /** If needsClarification is true, this is the question to ask */
    val clarifyingQuestion: String? = null,
    /** What the user should do next (guidance only, no automation) */
    val suggestedNextStep: String? = null,
    /** An optional helpful tip or safety reminder */
    val helpfulTip: String? = null,
    /** True when something went wrong (network, API, parse error) */
    val isError: Boolean = false,
    /** User-friendly error message if isError is true */
    val errorMessage: String? = null,
    /**
     * True when this response was produced by a local vocal-anchor handler
     * (REPEAT / GO_BACK / STOP / NEXT_STEP) rather than the LLM.
     * These responses are TTS-only and are NOT added to conversation history.
     */
    val isVocalAnchor: Boolean = false,
    /** The specific vocal anchor that triggered this response, if any. */
    val vocalAnchorAction: VocalAnchorAction? = null
) {
    companion object {
        /** Factory: create a friendly error response */
        fun error(message: String) = AssistantResponse(
            intent = "ERROR",
            goal = "",
            response = message,
            isError = true,
            errorMessage = message
        )

        /** Factory: create a loading/processing placeholder */
        fun loading() = AssistantResponse(
            intent = "LOADING",
            goal = "",
            response = "Let me think about that for a moment..."
        )
    }
}
