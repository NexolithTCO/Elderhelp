package com.example.elderhelpprototypev01.ai

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
 * Implements [LlmService] using the Gemini REST API (gemini-1.5-flash model).
 * Uses OkHttp for HTTP + Gson for JSON. No official SDK needed.
 *
 * Security: API key is read from BuildConfig.GEMINI_API_KEY which is
 * injected at compile time from local.properties (never in source control).
 *
 * Safety: The system prompt explicitly forbids the model from:
 * - Claiming it performed payments or device actions
 * - Asking for passwords or OTPs
 * - Making financial decisions
 */
class GeminiLlmService : LlmService {

    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val apiKey: String get() = BuildConfig.GEMINI_API_KEY
    private val endpoint =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent"

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
                "Sahaay AI is not configured yet. Please add your Gemini API key to local.properties."
            )
        }

        try {
            val requestBody = buildRequestBody(transcript, conversation, userLanguage)
            val request = Request.Builder()
                .url("$endpoint?key=$apiKey")
                .post(requestBody.toRequestBody("application/json".toMediaType()))
                .build()

            val responseBody = client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val code = response.code
                    return@withContext when (code) {
                        429 -> AssistantResponse.error(
                            "I'm a little busy right now. Please try again in a moment."
                        )
                        401, 403 -> AssistantResponse.error(
                            "There is a configuration issue. Please check the API key."
                        )
                        else -> AssistantResponse.error(
                            "I'm having trouble connecting right now. Please try again."
                        )
                    }
                }
                response.body?.string()
                    ?: return@withContext AssistantResponse.error(
                        "I received an empty response. Please try again."
                    )
            }

            parseGeminiResponse(responseBody)
        } catch (e: java.net.UnknownHostException) {
            AssistantResponse.error("No internet connection. Please check your network and try again.")
        } catch (e: java.net.SocketTimeoutException) {
            AssistantResponse.error("The connection timed out. Please try again.")
        } catch (e: Exception) {
            AssistantResponse.error("I'm having trouble right now. Please try again in a moment.")
        }
    }

    // ------------------------------------------------------------------
    // Request Building
    // ------------------------------------------------------------------

    private fun buildRequestBody(
        transcript: String,
        conversation: List<ConversationMessage>,
        userLanguage: String
    ): String {
        val systemInstruction = buildSystemPrompt(userLanguage)

        // Build conversation history for context
        val historyParts = mutableListOf<Map<String, Any>>()

        // Add previous conversation for context (last 10 messages max)
        val recentHistory = conversation.takeLast(10)
        for (msg in recentHistory) {
            val role = if (msg.role == MessageRole.USER) "user" else "model"
            historyParts.add(
                mapOf(
                    "role" to role,
                    "parts" to listOf(mapOf("text" to msg.text))
                )
            )
        }

        // Add current user message
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
                "temperature" to 0.7,
                "maxOutputTokens" to 500,
                "responseMimeType" to "application/json"
            )
        )

        return gson.toJson(requestMap)
    }

    private fun buildSystemPrompt(userLanguage: String): String {
        val languageInstruction = when {
            userLanguage.contains("Hindi") ->
                """The user prefers Hindi or Hinglish. Respond in the SAME language mix the user used.
                   If they spoke in Hinglish (mixed Hindi-English), reply in natural Hinglish.
                   If they spoke in pure Hindi (Devanagari), reply in simple Hindi.
                   Keep words short and common — avoid literary or formal Hindi."""
            userLanguage.contains("Marathi") ->
                "Respond in simple Marathi (मराठी). Use easy, everyday words only."
            userLanguage.contains("Tamil") ->
                "Respond in simple Tamil (தமிழ்). Use easy, everyday words only."
            userLanguage.contains("Telugu") ->
                "Respond in simple Telugu (తెలుగు). Use easy, everyday words only."
            userLanguage.contains("Bengali") ->
                "Respond in simple Bengali (বাংলা). Use easy, everyday words only."
            else ->
                "Respond in very simple, clear English. Short sentences only."
        }

        return """
You are Sahaay, a calm, patient, and helpful digital assistant for elderly users in India.
Your responses are read aloud by a Text-to-Speech (TTS) engine. The users may have low literacy.
Your job is to GUIDE and EXPLAIN — NOT to perform actions on behalf of the user.

$languageInstruction

CRITICAL SAFETY RULES — NEVER VIOLATE:
1. NEVER claim you made a payment, sent a message, or clicked anything in another app.
2. NEVER ask the user for their password, PIN, or OTP.
3. NEVER tell the user to share an OTP with anyone.
4. NEVER pretend to have read the screen or accessed another application.
5. If the user asks "Did you pay my bill?", respond: "Main aapka payment nahi kar sakta. Main aapko guide kar sakta hoon."
6. Always distinguish clearly between GUIDANCE (what you do) and ACTION (what the user must do).

OUTPUT FORMAT RULES — CRITICAL FOR TTS:
1. NEVER use markdown formatting. No asterisks (*), no bold (**text**), no bullet points (-), no headers (#), no backticks.
2. Output only plain, raw sentences separated by commas and periods. No lists, no numbering.
3. Keep the 'response' field to 2 or 3 sentences maximum. Short sentences are better.
4. Use commas within sentences to create natural pauses for the TTS engine.
5. Do NOT use emojis in the 'response', 'clarifying_question', or 'suggested_next_step' fields.

CONVERSATIONAL REPAIR RULES:
1. If the user's input is ambiguous, fragmented, or missing a key detail, set 'needs_clarification' to true.
2. The 'clarifying_question' must be a SINGLE, SHORT sentence (15 words or fewer).
3. The clarifying question must be in the same language or code-mix as the user's input.
4. GOOD example: "Aap kisko call karna chahte hain?" (Who would you like to call?)
5. BAD example: "Error: Contact not specified. Please provide a valid contact name."
6. Never produce a generic error message. Always ask a simple, friendly question instead.

VOCAL ANCHOR AWARENESS:
If the user's input is exactly a navigation command (repeat, go back, stop, next step, or their Hindi equivalents),
set intent to 'VOCAL_ANCHOR'. These are handled locally; just acknowledge in 'response'.

BEHAVIOR GUIDELINES:
- Be patient and encouraging. Never make the user feel rushed or confused.
- Provide one step at a time. Do not overwhelm with many instructions at once.
- For tasks like booking appointments or paying bills, give step-by-step verbal guidance only.
- Always add a helpful tip or gentle reminder when relevant.

KNOWN INTENTS (use exactly one):
BOOK_APPOINTMENT, PAY_BILL, FILL_FORM, EXPLAIN_TERM, ASK_QUESTION, EMERGENCY_HELP,
VOCAL_ANCHOR, REPAIR, GENERAL, UNKNOWN

You MUST respond ONLY with this exact JSON structure (no extra text, no markdown):
{
  "intent": "INTENT_NAME",
  "goal": "Short description of what the user wants",
  "response": "Your main response in 2 to 3 plain sentences. No markdown. Use commas for natural pauses.",
  "needs_clarification": false,
  "clarifying_question": null,
  "suggested_next_step": "The very next simple thing the user should do, as one plain sentence. Or null.",
  "helpful_tip": "A brief helpful tip or safety note as one plain sentence. Or null."
}
        """.trimIndent()
    }

    // ------------------------------------------------------------------
    // Response Parsing
    // ------------------------------------------------------------------

    private fun parseGeminiResponse(responseBody: String): AssistantResponse {
        return try {
            val root = gson.fromJson(responseBody, JsonObject::class.java)

            // Extract the text content from Gemini's response structure
            val candidates = root.getAsJsonArray("candidates")
                ?: return AssistantResponse.error("I received an unexpected response. Please try again.")

            val firstCandidate = candidates.get(0)?.asJsonObject
                ?: return AssistantResponse.error("No response received. Please try again.")

            val content = firstCandidate.getAsJsonObject("content")
            val parts = content.getAsJsonArray("parts")
            val text = parts.get(0)?.asJsonObject?.get("text")?.asString
                ?: return AssistantResponse.error("The response was empty. Please try again.")

            // Parse the JSON embedded in the text
            parseStructuredResponse(text.trim())

        } catch (e: Exception) {
            AssistantResponse.error("I had trouble understanding the response. Please try again.")
        }
    }

    private fun parseStructuredResponse(jsonText: String): AssistantResponse {
        return try {
            // Strip markdown code fences if the model added them
            val cleaned = jsonText
                .removePrefix("```json").removePrefix("```")
                .removeSuffix("```").trim()

            val obj = gson.fromJson(cleaned, JsonObject::class.java)

            AssistantResponse(
                intent = obj.get("intent")?.asString ?: "GENERAL",
                goal = obj.get("goal")?.asString ?: "",
                response = obj.get("response")?.asString
                    ?: "I'm here to help. Could you tell me more?",
                needsClarification = obj.get("needs_clarification")?.asBoolean ?: false,
                clarifyingQuestion = obj.get("clarifying_question")
                    ?.takeIf { !it.isJsonNull }?.asString,
                suggestedNextStep = obj.get("suggested_next_step")
                    ?.takeIf { !it.isJsonNull }?.asString,
                helpfulTip = obj.get("helpful_tip")
                    ?.takeIf { !it.isJsonNull }?.asString
            )
        } catch (e: Exception) {
            // If JSON parsing fails, use the raw text as the response
            // (Gemini sometimes adds prose before/after the JSON)
            AssistantResponse(
                intent = "GENERAL",
                goal = "",
                response = jsonText.take(500).ifBlank {
                    "I'm here to help. Could you tell me what you need?"
                }
            )
        }
    }
}
