package com.example.elderhelpprototypev01.voice

import com.example.elderhelpprototypev01.model.VocalAnchorAction

/**
 * VocalAnchorProcessor
 *
 * Pre-processes a raw STT transcript and detects whether it matches one of
 * the four recognized vocal navigation anchors. If a match is found the
 * ViewModel short-circuits the LLM call entirely.
 *
 * Supported anchors (English + Hindi equivalents):
 *
 *  REPEAT    → "repeat", "फिर से बोलो", "dobara bolo", "phir se bolo"
 *  GO_BACK   → "go back", "पीछे जाओ", "peeche jao", "wapas jao"
 *  STOP      → "stop", "रुको", "ruko", "band karo"
 *  NEXT_STEP → "what should i do next", "अब क्या करना है",
 *               "ab kya karna hai", "next step", "aage kya karna hai"
 *
 * Matching strategy:
 * - Lowercased, punctuation-stripped comparison.
 * - `contains()` check so the phrase can appear anywhere in the utterance.
 * - Hindi Devanagari patterns matched as-is (no transliteration needed for
 *   direct STT output in hi-IN locale).
 *
 * Usage:
 *   val action = VocalAnchorProcessor.detect(transcript)
 *   if (action != null) handleAnchorLocally(action) else sendToLlm(transcript)
 */
object VocalAnchorProcessor {

    private val REPEAT_PATTERNS = setOf(
        "repeat",
        "फिर से बोलो",
        "phir se bolo",
        "dobara bolo",
        "again",
        "ek baar aur"
    )

    private val GO_BACK_PATTERNS = setOf(
        "go back",
        "पीछे जाओ",
        "peeche jao",
        "wapas jao",
        "wapas",
        "back jao"
    )

    private val STOP_PATTERNS = setOf(
        "stop",
        "रुको",
        "ruko",
        "band karo",
        "chup",
        "bas"
    )

    private val NEXT_STEP_PATTERNS = setOf(
        "what should i do next",
        "अब क्या करना है",
        "ab kya karna hai",
        "next step",
        "aage kya karna hai",
        "aage kya",
        "ab kya",
        "next"
    )

    /**
     * Detect whether [transcript] maps to a known vocal anchor.
     *
     * @param transcript The raw STT output string.
     * @return The matching [VocalAnchorAction], or null if no anchor matches.
     */
    fun detect(transcript: String): VocalAnchorAction? {
        if (transcript.isBlank()) return null
        val normalized = normalize(transcript)

        if (REPEAT_PATTERNS.any { normalized.contains(it) }) return VocalAnchorAction.REPEAT
        if (GO_BACK_PATTERNS.any { normalized.contains(it) }) return VocalAnchorAction.GO_BACK
        if (STOP_PATTERNS.any { normalized.contains(it) }) return VocalAnchorAction.STOP
        if (NEXT_STEP_PATTERNS.any { normalized.contains(it) }) return VocalAnchorAction.NEXT_STEP

        return null
    }

    /**
     * Normalize English portion to lowercase; preserve Hindi Devanagari.
     * Strips leading/trailing whitespace and collapses multiple spaces.
     */
    private fun normalize(text: String): String =
        text.lowercase()
            .replace(Regex("[?!.,;:]"), "")
            .trim()
            .replace(Regex("\\s+"), " ")
}
