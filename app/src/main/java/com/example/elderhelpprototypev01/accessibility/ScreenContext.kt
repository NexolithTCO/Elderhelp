package com.example.elderhelpprototypev01.accessibility

/**
 * Encapsulates the entire parsed state of the current Android screen.
 * Used as input for AI screen analysis.
 */
data class ScreenContext(
    val packageName: String = "",
    val activityName: String = "",
    val screenTitle: String = "",
    val elements: List<UiElement> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
) {

    /**
     * Generates a lightweight fingerprint of the screen state for change detection.
     * Two screens with the same fingerprint have identical visible elements.
     * Used by [ScreenAssistantEngine] to skip duplicate API calls.
     */
    fun toFingerprint(): String {
        val key = StringBuilder()
        key.append(packageName).append("|")
        key.append(screenTitle).append("|")
        val labels = elements
            .filter { it.visible && (it.label.isNotBlank() || it.editable) }
            .take(30)
            .map { "${it.role}:${it.label.take(40)}" }
            .sorted()
        key.append(labels.joinToString(","))
        return key.toString().hashCode().toString(16)
    }

    /**
     * Returns a compact, token-efficient text summary of all visible UI elements for the LLM prompt.
     *
     * Format per element: `e1. BUTTON "Pay Now" tap [100,200,300,400]`
     * - Stable element IDs (e1, e2...) allow the LLM to reference elements by index
     * - Role flags: `tap` = clickable, `edit` = editable, `off` = disabled
     * - Bounds are comma-separated without spaces
     * - Duplicates and empty containers are filtered out
     * - Capped at 30 elements to limit token usage
     */
    fun toCompactPromptSummary(): String {
        val sb = StringBuilder()
        sb.append("PKG: ").append(packageName).append("\n")
        if (screenTitle.isNotBlank()) sb.append("TITLE: ").append(screenTitle).append("\n")

        val validElements = elements
            .filter { it.visible && (it.label.isNotBlank() || it.editable) }
            .distinctBy { "${it.role}|${it.label.trim().lowercase()}" }
            .take(30)

        if (validElements.isEmpty()) {
            sb.append("ELEMENTS: None detected\n")
        } else {
            sb.append("ELEMENTS (${validElements.size}):\n")
            validElements.forEachIndexed { index, el ->
                val id = "e${index + 1}"
                sb.append("$id. ${el.role}")
                if (el.label.isNotBlank()) sb.append(" \"").append(el.label.take(60)).append("\"")
                if (el.clickable) sb.append(" tap")
                if (el.editable) sb.append(" edit")
                if (!el.enabled) sb.append(" off")
                if (el.isSensitive) sb.append(" PROTECTED")
                sb.append(" [${el.bounds.left},${el.bounds.top},${el.bounds.right},${el.bounds.bottom}]")
                sb.append("\n")
            }
        }
        return sb.toString()
    }

    /**
     * Returns the [UiElement] at a given prompt index (e.g., "e5" → index 4).
     * Used to resolve targetElementId from LLM responses back to bounds.
     */
    fun elementByPromptId(promptId: String): UiElement? {
        val idx = promptId.removePrefix("e").toIntOrNull()?.minus(1) ?: return null
        val validElements = elements
            .filter { it.visible && (it.label.isNotBlank() || it.editable) }
            .distinctBy { "${it.role}|${it.label.trim().lowercase()}" }
            .take(30)
        return validElements.getOrNull(idx)
    }

    /** Check if any element on screen is a sensitive field (OTP, PIN, password, CVV) */
    fun hasSensitiveFields(): Boolean = elements.any { el ->
        val label = el.label.lowercase()
        label.contains("otp") || label.contains("pin") || label.contains("password") ||
        label.contains("cvv") || label.contains("security code") || label.contains("verification")
    }

    /** Get sensitive field labels for safety warnings */
    fun getSensitiveFieldLabels(): List<String> = elements
        .filter { el ->
            val label = el.label.lowercase()
            label.contains("otp") || label.contains("pin") || label.contains("password") ||
            label.contains("cvv") || label.contains("security code") || label.contains("verification")
        }
        .map { it.label }
}
