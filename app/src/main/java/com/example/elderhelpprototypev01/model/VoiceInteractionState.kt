package com.example.elderhelpprototypev01.model

/**
 * VoiceInteractionState
 *
 * Richer state machine for the full Voice Interaction Engine lifecycle.
 * Replaces the simpler [VoiceState] for screens that need engine-level awareness.
 *
 * State transitions:
 *
 *   Idle ──► WakeWordListening ──► ActiveListening ──► Processing
 *                                                         │
 *                              ┌──────────────────────────┤
 *                              ▼                          ▼
 *                     WaitingForClarification           Speaking
 *                              │                          │
 *                              └──────────► Done ◄────────┘
 *
 *   Any state ──► Error
 */
sealed class VoiceInteractionState {

    /** No active session. TTS and microphone are both off. */
    object Idle : VoiceInteractionState()

    /**
     * Passively monitoring partial-results for the wake word "Hey Sahayak".
     * Mic is open but only WakeWordDetector is processing input.
     */
    object WakeWordListening : VoiceInteractionState()

    /** Full STT session active — user is speaking a request. */
    object ActiveListening : VoiceInteractionState()

    /** Partial transcript arrived — displayed as live feedback. */
    data class PartialResult(val partial: String) : VoiceInteractionState()

    /** STT complete, request sent to the LLM for analysis. */
    object Processing : VoiceInteractionState()

    /** LLM determined clarification is needed; holds the question to ask. */
    data class WaitingForClarification(val question: String) : VoiceInteractionState()

    /** TTS is currently reading a response aloud. */
    object Speaking : VoiceInteractionState()

    /** Response displayed and TTS finished (or TTS is disabled). */
    object Done : VoiceInteractionState()

    /** Something went wrong — display the friendly message. */
    data class Error(val message: String) : VoiceInteractionState()
}
