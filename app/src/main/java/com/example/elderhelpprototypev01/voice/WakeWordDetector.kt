package com.example.elderhelpprototypev01.voice

/**
 * WakeWordDetector
 *
 * Lightweight, zero-dependency utility that checks whether a speech partial-result
 * string contains a recognized wake-word variant for "Hey Sahayak".
 *
 * Design decisions:
 * - Works entirely in-process — no ML model, no network round-trip.
 * - Uses fuzzy matching (contains + normalization) to handle common
 *   speech-to-text transcription errors (e.g. "sahayak" → "sahayk").
 * - Case-insensitive. Strips punctuation before matching.
 *
 * Usage:
 *   if (WakeWordDetector.isWakeWord(partialText)) { activateFullListening() }
 */
object WakeWordDetector {

    /**
     * Known wake-word surface forms, ordered by specificity (most specific first).
     * All patterns are matched after lowercasing and stripping punctuation.
     */
    private val WAKE_PATTERNS = listOf(
        "hey sahayak",
        "hey sahaay",
        "hey sahaayak",
        "hey sahayk",
        "hey sahaak",
        "sahayak",    // bare word — catches "Sahayak, mujhe help karo"
        "sahaay"      // common abbreviation spoken by elderly users
    )

    /**
     * Returns true if [text] contains a recognized wake-word pattern.
     *
     * @param text A partial or final speech-to-text transcript.
     */
    fun isWakeWord(text: String): Boolean {
        if (text.isBlank()) return false
        val normalized = normalize(text)
        return WAKE_PATTERNS.any { pattern -> normalized.contains(pattern) }
    }

    /**
     * Strips punctuation and lowercases [text] for robust fuzzy matching.
     */
    private fun normalize(text: String): String =
        text.lowercase()
            .replace(Regex("[^a-z0-9\\u0900-\\u097f\\s]"), "") // keep Hindi unicode block
            .trim()
}
