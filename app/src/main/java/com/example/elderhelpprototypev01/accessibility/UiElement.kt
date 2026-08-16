package com.example.elderhelpprototypev01.accessibility

import android.graphics.Rect

/**
 * Represents a single visible UI element extracted from the Android Accessibility node tree.
 *
 * Privacy & Safety Guarantee:
 * Sensitive fields (passwords, PINs, OTPs, CVVs) are automatically flagged with [isSensitive] = true,
 * and their text content is replaced with "[PROTECTED SENSITIVE FIELD]" before being processed by AI.
 */
data class UiElement(
    val id: String = "",
    val text: String = "",
    val contentDescription: String = "",
    val role: String = "UNKNOWN", // e.g. BUTTON, EDIT_TEXT, CHECKBOX, RADIO, TEXT, CARD
    val clickable: Boolean = false,
    val editable: Boolean = false,
    val enabled: Boolean = true,
    val visible: Boolean = true,
    val bounds: Rect = Rect(0, 0, 0, 0),
    val isSensitive: Boolean = false
) {
    /** Highlighting target identifier helper */
    val label: String
        get() = text.ifBlank { contentDescription.ifBlank { id } }
}
