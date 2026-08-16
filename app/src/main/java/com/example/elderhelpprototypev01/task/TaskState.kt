package com.example.elderhelpprototypev01.task

/**
 * TaskState
 *
 * Maintains persistent task memory across screen navigation so Sahaay acts like a human assistant
 * who remembers what the user is trying to accomplish throughout a multi-step workflow.
 */
data class TaskState(
    val originalGoal: String = "",
    val currentTaskType: TaskType = TaskType.GENERAL_GUIDANCE,
    val currentStage: TaskStage = TaskStage.DISCOVERY,
    val completedSteps: List<String> = emptyList(),
    val collectedInfo: Map<String, String> = emptyMap(),
    val missingInfo: List<String> = emptyList(),
    val lastHighlightedText: String? = null,
    val lastSpokenInstruction: String? = null,
    val stepCount: Int = 0,
    val confidence: Float = 1.0f,
    val requiresConfirmation: Boolean = false,
    val isCompleted: Boolean = false,
    val isBlockedBySafety: Boolean = false
) {
    fun withNextStep(
        newStage: TaskStage,
        completedStepName: String? = null,
        newInfoKey: String? = null,
        newInfoVal: String? = null,
        targetText: String? = null,
        spokenText: String? = null
    ): TaskState {
        val newCompleted = if (completedStepName != null && !completedSteps.contains(completedStepName)) {
            completedSteps + completedStepName
        } else {
            completedSteps
        }

        val newCollected = if (newInfoKey != null && newInfoVal != null) {
            collectedInfo + (newInfoKey to newInfoVal)
        } else {
            collectedInfo
        }

        val newMissing = missingInfo.filter { it != newInfoKey }

        return copy(
            currentStage = newStage,
            completedSteps = newCompleted,
            collectedInfo = newCollected,
            missingInfo = newMissing,
            lastHighlightedText = targetText ?: lastHighlightedText,
            lastSpokenInstruction = spokenText ?: lastSpokenInstruction,
            stepCount = stepCount + 1,
            isCompleted = newStage == TaskStage.COMPLETED
        )
    }
}

enum class TaskType {
    BOOK_DOCTOR,
    PAY_BILL,
    FILL_PENSION_FORM,
    GENERAL_FORM,
    SCREEN_EXPLANATION,
    GENERAL_GUIDANCE
}

enum class TaskStage {
    DISCOVERY,
    SELECT_DOCTOR,
    SELECT_DATE,
    SELECT_TIME,
    ENTER_BILL_DETAILS,
    REVIEW_BILL,
    BANK_OTP_SAFETY_PAUSE,
    ENTER_PERSONAL_DETAILS,
    ENTER_BANK_DETAILS,
    REVIEW,
    CONFIRMATION,
    COMPLETED,
    SAFETY_BLOCKED
}
