package com.example.elderhelpprototypev01.model

/**
 * VocalAnchorAction
 *
 * Represents one of the four recognized vocal navigation anchors.
 * These are short-circuit commands that bypass the LLM entirely:
 *
 *  REPEAT    — replay the last TTS utterance
 *  GO_BACK   — pop the back stack / return to previous screen
 *  STOP      — halt TTS and enter idle state
 *  NEXT_STEP — speak the suggestedNextStep from the last assistant response
 *
 * Detected by [com.example.elderhelpprototypev01.voice.VocalAnchorProcessor]
 * before the transcript reaches the LLM.
 */
enum class VocalAnchorAction {
    REPEAT,
    GO_BACK,
    STOP,
    NEXT_STEP
}
