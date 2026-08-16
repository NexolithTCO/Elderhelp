package com.example.elderhelpprototypev01.accessibility

import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo

/**
 * AccessibilityNodeParser
 *
 * Traverses an [AccessibilityNodeInfo] tree recursively and constructs a clean,
 * structured [ScreenContext].
 *
 * Safety & Privacy Features:
 *  - Identifies password fields, PINs, OTPs, CVVs and sets [UiElement.isSensitive] = true.
 *  - Masks sensitive field values as "[PROTECTED SENSITIVE FIELD]" so secrets are never sent to AI.
 */
object AccessibilityNodeParser {

    private val SENSITIVE_KEYWORDS = listOf(
        "password", "passcode", "pin", "otp", "cvv", "secret", "cvc", "card number"
    )

    fun parseTree(
        rootNode: AccessibilityNodeInfo?,
        packageName: String = "",
        activityName: String = ""
    ): ScreenContext {
        if (rootNode == null) {
            return ScreenContext(packageName = packageName, activityName = activityName)
        }

        val elementsList = mutableListOf<UiElement>()
        traverseNode(rootNode, elementsList)

        // Try to identify a screen title from large header text
        val titleText = elementsList
            .firstOrNull { it.role == "HEADER" || (it.text.length in 3..40 && !it.clickable && it.bounds.top < 400) }
            ?.text ?: ""

        return ScreenContext(
            packageName = packageName,
            activityName = activityName,
            screenTitle = titleText,
            elements = elementsList
        )
    }

    private fun traverseNode(node: AccessibilityNodeInfo, list: MutableList<UiElement>) {
        if (!node.isVisibleToUser) return

        val bounds = Rect()
        node.getBoundsInScreen(bounds)

        // Skip non-visible or off-screen elements with zero width/height
        if (bounds.width() <= 0 || bounds.height() <= 0) return

        val rawText = node.text?.toString() ?: ""
        val rawDesc = node.contentDescription?.toString() ?: ""
        val className = node.className?.toString() ?: ""
        val viewId = node.viewIdResourceName ?: ""

        val isPassword = node.isPassword || isSensitiveField(rawText, rawDesc, viewId)
        val role = determineRole(className, node)

        val displayText = if (isPassword) "[PROTECTED SENSITIVE FIELD]" else rawText
        val displayDesc = if (isPassword) "[PROTECTED SENSITIVE FIELD]" else rawDesc

        // Keep node if it contains text, description, or is clickable/editable
        if (displayText.isNotBlank() || displayDesc.isNotBlank() || node.isClickable || node.isEditable) {
            list.add(
                UiElement(
                    id = viewId,
                    text = displayText.trim(),
                    contentDescription = displayDesc.trim(),
                    role = role,
                    clickable = node.isClickable,
                    editable = node.isEditable,
                    enabled = node.isEnabled,
                    visible = node.isVisibleToUser,
                    bounds = bounds,
                    isSensitive = isPassword
                )
            )
        }

        // Recurse over child nodes
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            traverseNode(child, list)
            child.recycle()
        }
    }

    private fun isSensitiveField(text: String, desc: String, viewId: String): Boolean {
        val combined = "$text $desc $viewId".lowercase()
        return SENSITIVE_KEYWORDS.any { combined.contains(it) }
    }

    private fun determineRole(className: String, node: AccessibilityNodeInfo): String {
        val cls = className.lowercase()
        return when {
            node.isEditable || cls.contains("edittext") -> "EDIT_TEXT"
            cls.contains("button") -> "BUTTON"
            cls.contains("checkbox") -> "CHECKBOX"
            cls.contains("radiobutton") -> "RADIO_BUTTON"
            cls.contains("image") -> "IMAGE"
            cls.contains("textview") -> if (node.isClickable) "BUTTON" else "TEXT"
            cls.contains("cardview") || cls.contains("layout") -> if (node.isClickable) "CARD" else "CONTAINER"
            else -> if (node.isClickable) "CLICKABLE" else "TEXT"
        }
    }
}
