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
 * - Elderly-friendly default speed (0.85f — slightly slower than normal)
 * - Language-aware locale
 * - Speaking state flow for UI reactivity
 * - Auto-skip for very long texts (user must tap Play)
 * - Clean lifecycle management
 *
 * Call [initialize] before using and [shutdown] when done.
 */
class TextToSpeechManager(private val context: Context) {

    companion object {
        /** Comfortable speech rate for elderly users (normal = 1.0f) */
        const val DEFAULT_SPEECH_RATE = 0.85f
        /**
         * Skip auto-play if text is longer than this many characters.
         * Raised from 250 → 400 to accommodate the engine's 2-3 sentence
         * TTS-optimized outputs while still gating very long LLM explanations.
         */
        const val AUTO_PLAY_MAX_CHARS = 400
    }

    private var tts: TextToSpeech? = null
    private var isReady = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private var speechRate = DEFAULT_SPEECH_RATE

    /** Initialize the TTS engine. Callback fires when ready. */
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
     * Speak the given text aloud.
     * @param text    The text to speak.
     * @param force   If true, speak even if text is long (user tapped Play).
     * @return true if speech was started, false if skipped or not ready.
     */
    fun speak(text: String, force: Boolean = false): Boolean {
        if (!isReady || text.isBlank()) return false
        if (!force && text.length > AUTO_PLAY_MAX_CHARS) return false

        stop()
        tts?.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "sahaay_response_${System.currentTimeMillis()}"
        )
        return true
    }

    /**
     * Speak the given text after stripping any residual markdown characters.
     *
     * The Voice Interaction Engine instructs Gemini to output clean text,
     * but this serves as a safety net in case the model adds formatting.
     * Strips: `*`, `**`, `#`, `- `, `> `.
     *
     * @param text  The response text (potentially with stray markdown).
     * @param force If true, speak even if text is long.
     * @return true if speech was started.
     */
    fun speakRaw(text: String, force: Boolean = false): Boolean {
        val cleaned = text
            .replace(Regex("\\*{1,2}"), "")           // * and **
            .replace(Regex("^#{1,6}\\s*", RegexOption.MULTILINE), "") // headings
            .replace(Regex("^[-*]\\s+", RegexOption.MULTILINE), "")   // bullets
            .replace(Regex("^>\\s+", RegexOption.MULTILINE), "")      // blockquotes
            .replace(Regex("\\[([^]]+)]\\([^)]+\\)"), "$1")          // [text](url)
            .replace(Regex("\\s{2,}"), " ")                           // extra spaces
            .trim()
        return speak(cleaned, force)
    }

    /** Stop any ongoing speech immediately. */
    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }

    /** Set speech rate. Normal = 1.0f, elderly-comfortable = 0.85f */
    fun setSpeechRate(rate: Float) {
        speechRate = rate.coerceIn(0.5f, 1.5f)
        tts?.setSpeechRate(speechRate)
    }

    fun getSpeechRate(): Float = speechRate

    /** Apply language-appropriate locale to TTS engine. */
    fun applyLanguage(language: String) {
        if (!isReady) return
        val locale = languageToLocale(language)
        val result = tts?.isLanguageAvailable(locale)
        if (result == TextToSpeech.LANG_AVAILABLE ||
            result == TextToSpeech.LANG_COUNTRY_AVAILABLE ||
            result == TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE) {
            tts?.language = locale
        } else {
            // Fall back to English if language not available
            tts?.language = Locale("en", "IN")
        }
    }

    /** Release TTS resources. Call in onDestroy. */
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
