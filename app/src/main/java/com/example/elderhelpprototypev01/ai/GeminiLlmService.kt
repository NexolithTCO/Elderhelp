package com.example.elderhelpprototypev01.ai

import android.util.Log
import com.example.elderhelpprototypev01.BuildConfig
import com.example.elderhelpprototypev01.model.AssistantResponse
import com.example.elderhelpprototypev01.model.ConversationMessage
import com.example.elderhelpprototypev01.model.MessageRole
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/**
 * GeminiLlmService
 *
 * Implements [LlmService] using Gemini REST API with multi-endpoint fallback
 * and elder-focused conversation intelligence.
 */
class GeminiLlmService : LlmService {

    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(12, TimeUnit.SECONDS)
        .build()

    private val apiKey: String get() = BuildConfig.GEMINI_API_KEY.trim()

    private val modelEndpoints = listOf(
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent",
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent"
    )

    override suspend fun analyze(
        transcript: String,
        conversation: List<ConversationMessage>,
        userLanguage: String
    ): AssistantResponse = withContext(Dispatchers.IO) {
        if (transcript.isBlank()) {
            return@withContext AssistantResponse.error(
                "I didn't catch that. Could you please try speaking again?"
            )
        }

        if (apiKey == "REPLACE_WITH_YOUR_GEMINI_API_KEY" || apiKey.isBlank()) {
            return@withContext AssistantResponse.error(
                "Sahaay AI is not configured yet. Please check your Gemini API key in local.properties."
            )
        }

        val requestBodyJson = buildRequestBody(transcript, conversation, userLanguage)

        for (endpoint in modelEndpoints) {
            try {
                val request = Request.Builder()
                    .url("$endpoint?key=$apiKey")
                    .post(requestBodyJson.toRequestBody("application/json".toMediaType()))
                    .build()

                val response = client.newCall(request).execute()
                val responseCode = response.code
                val responseBodyStr = response.body?.string() ?: ""

                if (response.isSuccessful && responseBodyStr.isNotBlank()) {
                    val result = parseGeminiResponse(responseBodyStr)
                    if (!result.isError) {
                        return@withContext result
                    }
                } else {
                    Log.e("SahaayGemini", "LlmService Endpoint $endpoint failed with code $responseCode: $responseBodyStr")
                }
            } catch (e: Exception) {
                Log.e("SahaayGemini", "LlmService Error calling $endpoint: ${e.localizedMessage}")
            }
        }

        return@withContext AssistantResponse(
            intent = "GENERAL",
            goal = transcript,
            response = "I am Sahaay, your voice assistant. I am here to help you navigate digital services and understand your screen.",
            needsClarification = false,
            clarifyingQuestion = null,
            suggestedNextStep = "Tell me what you would like to do.",
            helpfulTip = "You can tap Voice or Read Screen at any time."
        )
    }

    private fun buildRequestBody(
        transcript: String,
        conversation: List<ConversationMessage>,
        userLanguage: String
    ): String {
        val systemInstruction = buildSystemPrompt(userLanguage)
        val historyParts = mutableListOf<Map<String, Any>>()

        val recentHistory = conversation.takeLast(6)
        for (msg in recentHistory) {
            val role = if (msg.role == MessageRole.USER) "user" else "model"
            historyParts.add(
                mapOf(
                    "role" to role,
                    "parts" to listOf(mapOf("text" to msg.text))
                )
            )
        }

        historyParts.add(
            mapOf(
                "role" to "user",
                "parts" to listOf(mapOf("text" to transcript))
            )
        )

        val requestMap = mapOf(
            "system_instruction" to mapOf(
                "parts" to listOf(mapOf("text" to systemInstruction))
            ),
            "contents" to historyParts,
            "generationConfig" to mapOf(
                "temperature" to 0.5,
                "maxOutputTokens" to 450,
                "responseMimeType" to "application/json"
            )
        )

        return gson.toJson(requestMap)
    }

    private fun buildSystemPrompt(userLanguage: String): String {
        val languageInstruction = when {
            userLanguage.contains("Hindi") -> "Respond in simple, warm Hindi or natural Hinglish."
            userLanguage.contains("Marathi") -> "Respond in simple, warm Marathi."
            userLanguage.contains("Tamil") -> "Respond in simple, warm Tamil."
            userLanguage.contains("Telugu") -> "Respond in simple, warm Telugu."
            userLanguage.contains("Bengali") -> "Respond in simple, warm Bengali."
            else -> "Respond in simple, warm, clear English."
        }

        return """
You are Sahaay, a patient, intelligent digital companion for elderly users in India.
$languageInstruction

CONVERSATION INTELLIGENCE:
- Understand natural speech, incomplete sentences, and follow-ups.
- Track the current goal across turns. If user says "no not that" or "the other one", use context.
- Handle corrections gracefully: "Actually I want to book a doctor" updates the task.
- "Repeat" means repeat your last important instruction.
- "Go back" means navigate backward.
- "What should I do next?" means analyze current state and guide.
- Handle Hindi/Hinglish naturally: "Mujhe bill pay karna hai" = PAY_BILL intent.
- "Ye kya hai?" = EXPLAIN_TERM, "Agla step?" = next step guidance.

SAFETY RULES (CRITICAL):
- NEVER read, store, repeat, or ask for: OTP, UPI PIN, password, CVV, bank PIN.
- If user mentions OTP/PIN/password, say: "That is private. Please enter it yourself."
- NEVER send OTP/PIN values to this conversation.
- For OTP fields: "This is your private OTP field. Please type the code yourself."

INTENT CLASSIFICATION:
Classify as: BOOK_APPOINTMENT, PAY_BILL, FILL_FORM, EXPLAIN_TERM, EMERGENCY_HELP, ASK_QUESTION, NAVIGATE_BACK, REPEAT, READ_SCREEN, GENERAL

RESPONSE STYLE:
- Calm, patient, concise (1-2 sentences max).
- No preamble ("As an AI", "I am Sahaay").
- No markdown, asterisks, underscores.
- Give practical next step in suggested_next_step.
- Include safety tip in helpful_tip when relevant.

Respond ONLY with this JSON:
{
  "intent": "PAY_BILL",
  "goal": "User's current goal",
  "response": "Simple direct response.",
  "needs_clarification": false,
  "clarifying_question": null,
  "suggested_next_step": "Clear next action.",
  "helpful_tip": "Useful tip."
}
        """.trimIndent()
    }

    private fun parseGeminiResponse(responseBody: String): AssistantResponse {
        return try {
            val root = gson.fromJson(responseBody, JsonObject::class.java)
            val candidates = root.getAsJsonArray("candidates") ?: return AssistantResponse.error("No candidates")
            val text = candidates[0].asJsonObject.getAsJsonObject("content")
                .getAsJsonArray("parts")[0].asJsonObject.get("text").asString.trim()

            val cleaned = text.removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            val obj = gson.fromJson(cleaned, JsonObject::class.java)

            AssistantResponse(
                intent = obj.get("intent")?.asString ?: "GENERAL",
                goal = obj.get("goal")?.asString ?: "",
                response = obj.get("response")?.asString ?: "I am here to help you.",
                needsClarification = obj.get("needs_clarification")?.asBoolean ?: false,
                clarifyingQuestion = obj.get("clarifying_question")?.takeIf { !it.isJsonNull }?.asString,
                suggestedNextStep = obj.get("suggested_next_step")?.takeIf { !it.isJsonNull }?.asString,
                helpfulTip = obj.get("helpful_tip")?.takeIf { !it.isJsonNull }?.asString
            )
        } catch (e: Exception) {
            AssistantResponse(
                intent = "GENERAL",
                goal = "",
                response = "I am here to help you step by step."
            )
        }
    }
}
