package com.example.elderhelpprototypev01.task

import com.example.elderhelpprototypev01.accessibility.ScreenContext
import com.example.elderhelpprototypev01.model.ConversationMessage

/**
 * TaskPlanner
 *
 * Universal task state evaluator that preserves user intent across multi-screen workflows
 * in ANY Android app (healthcare, utility payments, pension forms, messaging, search, etc.).
 *
 * Detects task stages universally based on screen control roles and semantics without hardcoding app-specific strings.
 */
class TaskPlanner {

    private var activeTaskState: TaskState? = null

    val currentTaskState: TaskState? get() = activeTaskState

    /** Reset active task memory */
    fun resetTask() {
        activeTaskState = null
    }

    /**
     * Updates or creates the [TaskState] based on new user input and screen state.
     */
    fun evaluateTask(
        userGoal: String,
        screenContext: ScreenContext,
        history: List<ConversationMessage>
    ): TaskState {
        val goalLower = userGoal.lowercase().trim()

        // 1. Detect if user is giving a correction (e.g., "No actually Dr Patel")
        val isCorrection = goalLower.startsWith("no") || goalLower.contains("actually") ||
                goalLower.contains("change to") || goalLower.contains("instead")

        // 2. Universal Task Type Detection
        val taskType = when {
            goalLower.contains("doctor") || goalLower.contains("appointment") || goalLower.contains("dr ") || goalLower.contains("hospital") ->
                TaskType.BOOK_DOCTOR
            goalLower.contains("bill") || goalLower.contains("electricity") || goalLower.contains("recharge") || goalLower.contains("pay") ->
                TaskType.PAY_BILL
            goalLower.contains("pension") || goalLower.contains("scheme") ->
                TaskType.FILL_PENSION_FORM
            goalLower.contains("form") || goalLower.contains("fill") || goalLower.contains("apply") ->
                TaskType.GENERAL_FORM
            goalLower.contains("explain") || goalLower.contains("what is this") || goalLower.contains("read screen") ->
                TaskType.SCREEN_EXPLANATION
            else -> activeTaskState?.currentTaskType ?: TaskType.GENERAL_GUIDANCE
        }

        // 3. Initialize or continue TaskState
        var state = activeTaskState
        if (state == null || (!isCorrection && isNewGoal(goalLower, state.originalGoal))) {
            state = TaskState(
                originalGoal = userGoal,
                currentTaskType = taskType,
                currentStage = TaskStage.DISCOVERY
            )
        }

        // 4. Inspect Screen Universally to Detect Current Stage
        val newStage = detectUniversalStageFromScreen(screenContext, state)
        val isSafetyPause = screenContext.hasSensitiveFields()

        val finalStage = if (isSafetyPause) {
            TaskStage.BANK_OTP_SAFETY_PAUSE
        } else {
            newStage
        }

        state = state.copy(
            currentTaskType = taskType,
            currentStage = finalStage,
            isBlockedBySafety = isSafetyPause
        )

        activeTaskState = state
        return state
    }

    private fun isNewGoal(newGoal: String, existingGoal: String): Boolean {
        if (existingGoal.isBlank()) return true
        if (newGoal.contains("what next") || newGoal.contains("next step") || newGoal.contains("what should i do")) return false
        val newLower = newGoal.lowercase()
        val oldLower = existingGoal.lowercase()
        // If user introduces a new main intent topic, treat as a new goal
        if ((newLower.contains("doctor") && !oldLower.contains("doctor")) ||
            (newLower.contains("bill") && !oldLower.contains("bill")) ||
            (newLower.contains("pension") && !oldLower.contains("pension"))
        ) {
            return true
        }
        return false
    }

    /**
     * Universal Stage Detector:
     * Inspects screen UI element types and semantic control patterns across ANY app.
     */
    private fun detectUniversalStageFromScreen(context: ScreenContext, currentState: TaskState): TaskStage {
        val labels = context.elements.map { it.label.lowercase() }
        val title = context.screenTitle.lowercase()

        // 1. Universal Completion Detection
        val completionKeywords = listOf("success", "successful", "booked", "submitted", "confirmed", "done", "reference", "receipt", "completed")
        if (labels.any { l -> completionKeywords.any { l.contains(it) } } || completionKeywords.any { title.contains(it) }) {
            return TaskStage.COMPLETED
        }

        // 2. Sensitive Security Field Detection
        if (context.hasSensitiveFields()) {
            return TaskStage.BANK_OTP_SAFETY_PAUSE
        }

        // 3. Input Form Stage
        if (context.elements.any { it.editable || it.role == "EDIT_TEXT" }) {
            return TaskStage.ENTER_PERSONAL_DETAILS
        }

        // 4. Confirmation / Action Stage
        val actionKeywords = listOf("continue", "proceed", "submit", "confirm", "pay now", "next", "book now", "finish")
        if (context.elements.any { el -> el.clickable && actionKeywords.any { el.label.lowercase().contains(it) } }) {
            return TaskStage.CONFIRMATION
        }

        // 5. Content Selection Stage (Radio buttons, choice cards, list items)
        if (context.elements.any { it.role == "RADIO" || it.role == "CHECKBOX" || it.role == "CARD" || (it.clickable && it.label.length > 3) }) {
            return TaskStage.SELECT_DATE // Use as generic selection stage
        }

        return TaskStage.DISCOVERY
    }

    /**
     * Builds a prompt section that embeds task context into LLM system/user prompts.
     */
    fun buildTaskContextPrompt(state: TaskState): String {
        val sb = StringBuilder()
        sb.append("ACTIVE TASK: ").append(state.currentTaskType.name).append("\n")
        sb.append("ORIGINAL GOAL: \"").append(state.originalGoal).append("\"\n")
        sb.append("TASK STAGE: ").append(state.currentStage.name).append("\n")
        if (state.completedSteps.isNotEmpty()) {
            sb.append("COMPLETED STEPS: ").append(state.completedSteps.joinToString(", ")).append("\n")
        }
        if (state.collectedInfo.isNotEmpty()) {
            sb.append("COLLECTED INFO: ").append(state.collectedInfo.entries.joinToString { "${it.key}=${it.value}" }).append("\n")
        }
        return sb.toString()
    }
}
