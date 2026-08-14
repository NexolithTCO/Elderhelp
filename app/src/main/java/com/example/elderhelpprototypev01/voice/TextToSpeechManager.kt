package com.example.elderhelpprototypev01.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * TextToSpeechManager
 *
 * Wraps Android [TextToSpeech] with:
 * - Human-like speech sanitization (strips `_`, `__`, `*`, `#`, markdown, JSON keys)
 * - Elderly-friendly default speed (0.85f — slightly slower than normal)
 * - Language-aware locale
 * - Speaking state flow for UI reactivity
 * - Clean lifecycle management
 */
class TextToSpeechManager(private val context: Context) {

    companion object {
        const val DEFAULT_SPEECH_RATE = 0.85f
        const val AUTO_PLAY_MAX_CHARS = 400

        /**
         * Cleans raw AI response text into natural, spoken human prose.
         * Removes underscores, markdown formatting, JSON tokens, and special symbols
         * so the Android TTS engine never pronounces "underscore underscore" or formatting characters.
         */
        fun sanitizeForSpeech(text: String): String {
            return text
                .replace(Regex("_+"), " ")                               // Replace all _ or __ with space
                .replace(Regex("\\*{1,2}"), "")                          // Replace * and **
                .replace(Regex("`{1,3}"), "")                            // Replace backticks
                .replace(Regex("^#{1,6}\\s*", RegexOption.MULTILINE), "")// Remove markdown headers
                .replace(Regex("^[-*]\\s+", RegexOption.MULTILINE), "")  // Remove bullets
                .replace(Regex("^>\\s+", RegexOption.MULTILINE), "")     // Remove blockquotes
                .replace(Regex("\\[([^]]+)]\\([^)]+\\)"), "$1")         // Simplify markdown links
                .replace(Regex("[{}\\[\\]\\\"\\']"), "")                 // Remove brackets and quotes
                .replace(Regex("\\s{2,}"), " ")                          // Double spaces -> single space
                .trim()
        }
    }

    private var tts: TextToSpeech? = null
    private var isReady = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private var speechRate = DEFAULT_SPEECH_RATE

    fun initialize(language: String = "English (India)", onReady: () -> Unit = {}) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isReady = true
                applyLanguage(language)
                tts?.setSpeechRate(speechRate)
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                    }
                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                    }
                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                    }
                })
                onReady()
            }
        }
    }

    /**
     * Speak the given text aloud after sanitizing it into clean human prose.
     * @param text  The text to speak.
     * @param force If true, speak even if text is long.
     * @return true if speech was started.
     */
    fun speak(text: String, force: Boolean = false): Boolean {
        if (!isReady || text.isBlank()) return false
        val sanitized = sanitizeForSpeech(text)
        if (sanitized.isBlank()) return false
        if (!force && sanitized.length > AUTO_PLAY_MAX_CHARS) return false

        stop()
        tts?.speak(
            sanitized,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "sahaay_response_${System.currentTimeMillis()}"
        )
        return true
    }

    fun speakRaw(text: String, force: Boolean = false): Boolean {
        return speak(text, force)
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun setSpeechRate(rate: Float) {
        speechRate = rate.coerceIn(0.5f, 1.5f)
        tts?.setSpeechRate(speechRate)
    }

    fun getSpeechRate(): Float = speechRate

    fun applyLanguage(language: String) {
        if (!isReady) return
        val locale = languageToLocale(language)
        val result = tts?.isLanguageAvailable(locale)
        if (result == TextToSpeech.LANG_AVAILABLE ||
            result == TextToSpeech.LANG_COUNTRY_AVAILABLE ||
            result == TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE) {
            tts?.language = locale
        } else {
            tts?.language = Locale("en", "IN")
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isReady = false
    }

    private fun languageToLocale(language: String): Locale = when {
        language.contains("Hindi") -> Locale("hi", "IN")
        language.contains("Marathi") -> Locale("mr", "IN")
        language.contains("Tamil") -> Locale("ta", "IN")
        language.contains("Telugu") -> Locale("te", "IN")
        language.contains("Bengali") -> Locale("bn", "IN")
        else -> Locale("en", "IN")
    }
}
