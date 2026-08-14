package com.example.elderhelpprototypev01.ai

import com.example.elderhelpprototypev01.model.AssistantResponse
import com.example.elderhelpprototypev01.model.ConversationMessage

/**
 * LlmService — the central abstraction for all LLM interactions.
 *
 * This interface decouples the UI and ViewModel from any specific AI provider.
 * Swap Gemini for another model (Claude, GPT-4, local LLM) by
 * providing a different implementation without touching any UI code.
 *
 * All implementations must be:
 * - Safe: never claim to perform actions
 * - Elderly-friendly: simple language, patient tone
 * - Structured: always return a valid AssistantResponse
 * - Resilient: never throw — return AssistantResponse.error() on failures
 */
interface LlmService {
    /**
     * Analyze the user's transcript and return a structured response.
     *
     * @param transcript  The recognized speech text from the user.
     * @param conversation Previous messages for context (in-memory only).
     * @param userLanguage The user's preferred display language (e.g. "Hindi (हिंदी)").
     *                    The LLM will respond in this language when possible.
     * @return A structured [AssistantResponse]. Never throws — errors are
     *         encoded inside the returned object with [AssistantResponse.isError] = true.
     */
    suspend fun analyze(
        transcript: String,
        conversation: List<ConversationMessage> = emptyList(),
        userLanguage: String = "English (India)"
    ): AssistantResponse

    suspend fun extractFormFields(
        transcript: String,
        targetFields: List<String>,
        userLanguage: String = "English (India)"
    ): Map<String, String>
}
