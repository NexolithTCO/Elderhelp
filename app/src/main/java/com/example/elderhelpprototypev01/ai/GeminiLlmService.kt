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
 * Implements [LlmService] using the Gemini REST API.
 *
 * Handles:
 * 1. Normal voice assistant conversations
 * 2. Voice-to-form field extraction
 */
class GeminiLlmService : LlmService {

    private val gson = Gson()

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val apiKey: String
        get() = BuildConfig.GEMINI_API_KEY

    private val endpoint =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent"


    // ================================================================
    // NORMAL VOICE ASSISTANT
    // ================================================================

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

            val requestBody =
                buildRequestBody(
                    transcript,
                    conversation,
                    userLanguage
                )

            val request = Request.Builder()
                .url("$endpoint?key=$apiKey")
                .post(
                    requestBody.toRequestBody(
                        "application/json".toMediaType()
                    )
                )
                .build()

            val responseBody =
                client.newCall(request).execute().use { response ->

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

            AssistantResponse.error(
                "No internet connection. Please check your network and try again."
            )

        } catch (e: java.net.SocketTimeoutException) {

            AssistantResponse.error(
                "The connection timed out. Please try again."
            )

        } catch (e: Exception) {

            AssistantResponse.error(
                "I'm having trouble right now. Please try again in a moment."
            )
        }
    }


    // ================================================================
    // VOICE-TO-FORM FIELD EXTRACTION
    // ================================================================

    override suspend fun extractFormFields(
        transcript: String,
        targetFields: List<String>,
        userLanguage: String
    ): Map<String, String> = withContext(Dispatchers.IO) {

        if (transcript.isBlank() || targetFields.isEmpty()) {
            return@withContext emptyMap()
        }

        if (apiKey == "REPLACE_WITH_YOUR_GEMINI_API_KEY" || apiKey.isBlank()) {
            return@withContext emptyMap()
        }

        try {

            val prompt = """
                You are a voice-to-form filling assistant for elderly users in India.

                Your task is to extract information from the user's spoken sentence
                and map it ONLY to the available form fields.

                User language:
                $userLanguage

                User said:
                "$transcript"

                Available form fields:
                ${targetFields.joinToString(", ")}

                IMPORTANT RULES:

                1. Extract only information that the user actually provided.
                2. Never guess missing information.
                3. Do not create new field names.
                4. Use only the field IDs provided above.
                5. Convert spoken numbers into normal numeric values when appropriate.
                6. Keep names and addresses as spoken, with minor formatting if needed.
                7. Return ONLY valid JSON.
                8. Do not add explanations.

                Example:

                User:
                "Mera naam Ramesh hai aur meri age 65 saal hai"

                Available fields:
                full_name, age

                Output:
                {
                  "full_name": "Ramesh",
                  "age": "65"
                }

                Another example:

                User:
                "Mera address Navi Mumbai hai"

                Available fields:
                address, phone_number

                Output:
                {
                  "address": "Navi Mumbai"
                }
            """.trimIndent()


            val requestMap = mapOf(

                "contents" to listOf(

                    mapOf(
                        "role" to "user",

                        "parts" to listOf(
                            mapOf(
                                "text" to prompt
                            )
                        )
                    )
                ),

                "generationConfig" to mapOf(
                    "temperature" to 0.1,
                    "maxOutputTokens" to 300,
                    "responseMimeType" to "application/json"
                )
            )


            val requestBody = gson.toJson(requestMap)


            val request = Request.Builder()
                .url("$endpoint?key=$apiKey")
                .post(
                    requestBody.toRequestBody(
                        "application/json".toMediaType()
                    )
                )
                .build()


            val responseBody =
                client.newCall(request).execute().use { response ->

                    if (!response.isSuccessful) {
                        return@withContext emptyMap()
                    }

                    response.body?.string()
                        ?: return@withContext emptyMap()
                }


            val root =
                gson.fromJson(
                    responseBody,
                    JsonObject::class.java
                )


            val candidates =
                root.getAsJsonArray("candidates")
                    ?: return@withContext emptyMap()


            if (candidates.size() == 0) {
                return@withContext emptyMap()
            }


            val firstCandidate =
                candidates.get(0)?.asJsonObject
                    ?: return@withContext emptyMap()


            val content =
                firstCandidate.getAsJsonObject("content")
                    ?: return@withContext emptyMap()


            val parts =
                content.getAsJsonArray("parts")
                    ?: return@withContext emptyMap()


            if (parts.size() == 0) {
                return@withContext emptyMap()
            }


            val text =
                parts.get(0)
                    ?.asJsonObject
                    ?.get("text")
                    ?.asString
                    ?: return@withContext emptyMap()


            val cleaned =
                text
                    .removePrefix("```json")
                    .removePrefix("```")
                    .removeSuffix("```")
                    .trim()


            val json =
                gson.fromJson(
                    cleaned,
                    JsonObject::class.java
                )


            val result =
                mutableMapOf<String, String>()


            // Only accept fields that actually exist
            // in targetFields.
            for (field in targetFields) {

                if (
                    json.has(field) &&
                    !json.get(field).isJsonNull
                ) {

                    val value =
                        json.get(field).asString.trim()

                    if (value.isNotBlank()) {
                        result[field] = value
                    }
                }
            }


            result

        } catch (e: Exception) {

            emptyMap()
        }
    }


    // ================================================================
    // REQUEST BUILDING FOR NORMAL ASSISTANT
    // ================================================================

    private fun buildRequestBody(
        transcript: String,
        conversation: List<ConversationMessage>,
        userLanguage: String
    ): String {

        val systemInstruction =
            buildSystemPrompt(userLanguage)


        val historyParts =
            mutableListOf<Map<String, Any>>()


        val recentHistory =
            conversation.takeLast(10)


        for (msg in recentHistory) {

            val role =
                if (msg.role == MessageRole.USER) {
                    "user"
                } else {
                    "model"
                }


            historyParts.add(
                mapOf(
                    "role" to role,
                    "parts" to listOf(
                        mapOf(
                            "text" to msg.text
                        )
                    )
                )
            )
        }


        historyParts.add(
            mapOf(
                "role" to "user",
                "parts" to listOf(
                    mapOf(
                        "text" to transcript
                    )
                )
            )
        )


        val requestMap =
            mapOf(

                "system_instruction" to mapOf(
                    "parts" to listOf(
                        mapOf(
                            "text" to systemInstruction
                        )
                    )
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


    // ================================================================
    // NORMAL ASSISTANT SYSTEM PROMPT
    // ================================================================

    private fun buildSystemPrompt(
        userLanguage: String
    ): String {

        val languageInstruction =
            when {

                userLanguage.contains("Hindi") ->
                    "Respond in simple Hindi (हिंदी). Use easy words."

                userLanguage.contains("Marathi") ->
                    "Respond in simple Marathi (मराठी). Use easy words."

                userLanguage.contains("Tamil") ->
                    "Respond in simple Tamil (தமிழ்). Use easy words."

                userLanguage.contains("Telugu") ->
                    "Respond in simple Telugu (తెలుగు). Use easy words."

                userLanguage.contains("Bengali") ->
                    "Respond in simple Bengali (বাংলা). Use easy words."

                else ->
                    "Respond in simple, clear English."
            }


        return """
            You are Sahaay, a calm, patient, and helpful digital assistant
            for elderly users in India.

            Your job is to GUIDE and EXPLAIN —
            NOT to perform actions on behalf of the user.

            $languageInstruction

            CRITICAL SAFETY RULES — NEVER VIOLATE:

            1. NEVER claim you made a payment, sent a message,
               or clicked anything in another app.

            2. NEVER ask the user for their password, PIN, or OTP.

            3. NEVER tell the user to share an OTP with anyone.

            4. NEVER pretend to have read the screen
               or accessed another application.

            5. If the user asks "Did you pay my bill?",
               respond:
               "I haven't made a payment. I can guide you through the process."

            6. Always distinguish clearly between GUIDANCE
               and ACTION.

            BEHAVIOR GUIDELINES:

            - Use very simple language.
            - Avoid technical jargon.
            - Be patient and encouraging.
            - Never make the user feel rushed or confused.
            - Provide one step at a time.
            - When something is unclear, ask ONE clarifying question.
            - For tasks like booking appointments or paying bills,
              give step-by-step verbal guidance only.
            - Keep responses concise.
            - Always add a helpful tip when relevant.

            KNOWN INTENTS:

            BOOK_APPOINTMENT,
            PAY_BILL,
            FILL_FORM,
            EXPLAIN_TERM,
            ASK_QUESTION,
            EMERGENCY_HELP,
            GENERAL,
            UNKNOWN

            You MUST respond ONLY with this exact JSON structure:

            {
              "intent": "INTENT_NAME",
              "goal": "Short description of what the user wants",
              "response": "Your main response in 2–4 sentences",
              "needs_clarification": false,
              "clarifying_question": null,
              "suggested_next_step": "The very next simple thing the user should do (or null)",
              "helpful_tip": "A brief helpful tip or safety note (or null)"
            }

        """.trimIndent()
    }


    // ================================================================
    // RESPONSE PARSING
    // ================================================================

    private fun parseGeminiResponse(
        responseBody: String
    ): AssistantResponse {

        return try {

            val root =
                gson.fromJson(
                    responseBody,
                    JsonObject::class.java
                )


            val candidates =
                root.getAsJsonArray("candidates")
                    ?: return AssistantResponse.error(
                        "I received an unexpected response. Please try again."
                    )


            if (candidates.size() == 0) {
                return AssistantResponse.error(
                    "No response received. Please try again."
                )
            }


            val firstCandidate =
                candidates.get(0)?.asJsonObject
                    ?: return AssistantResponse.error(
                        "No response received. Please try again."
                    )


            val content =
                firstCandidate.getAsJsonObject("content")
                    ?: return AssistantResponse.error(
                        "The response was empty. Please try again."
                    )


            val parts =
                content.getAsJsonArray("parts")
                    ?: return AssistantResponse.error(
                        "The response was empty. Please try again."
                    )


            if (parts.size() == 0) {
                return AssistantResponse.error(
                    "The response was empty. Please try again."
                )
            }


            val text =
                parts.get(0)
                    ?.asJsonObject
                    ?.get("text")
                    ?.asString
                    ?: return AssistantResponse.error(
                        "The response was empty. Please try again."
                    )


            parseStructuredResponse(text.trim())

        } catch (e: Exception) {

            AssistantResponse.error(
                "I had trouble understanding the response. Please try again."
            )
        }
    }


    // ================================================================
    // STRUCTURED RESPONSE PARSING
    // ================================================================

    private fun parseStructuredResponse(
        jsonText: String
    ): AssistantResponse {

        return try {

            val cleaned =
                jsonText
                    .removePrefix("```json")
                    .removePrefix("```")
                    .removeSuffix("```")
                    .trim()


            val obj =
                gson.fromJson(
                    cleaned,
                    JsonObject::class.java
                )


            AssistantResponse(

                intent =
                    obj.get("intent")?.asString
                        ?: "GENERAL",

                goal =
                    obj.get("goal")?.asString
                        ?: "",

                response =
                    obj.get("response")?.asString
                        ?: "I'm here to help. Could you tell me more?",

                needsClarification =
                    obj.get("needs_clarification")?.asBoolean
                        ?: false,

                clarifyingQuestion =
                    obj.get("clarifying_question")
                        ?.takeIf { !it.isJsonNull }
                        ?.asString,

                suggestedNextStep =
                    obj.get("suggested_next_step")
                        ?.takeIf { !it.isJsonNull }
                        ?.asString,

                helpfulTip =
                    obj.get("helpful_tip")
                        ?.takeIf { !it.isJsonNull }
                        ?.asString
            )

        } catch (e: Exception) {

            AssistantResponse(

                intent = "GENERAL",

                goal = "",

                response =
                    jsonText.take(500).ifBlank {
                        "I'm here to help. Could you tell me what you need?"
                    }
            )
        }
    }
}