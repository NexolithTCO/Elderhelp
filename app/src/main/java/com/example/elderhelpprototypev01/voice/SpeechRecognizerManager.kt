package com.example.elderhelpprototypev01.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import java.util.Locale

/**
 * SpeechRecognizerManager
 *
 * Wraps Android's [SpeechRecognizer] in a clean, coroutine-friendly API.
 * Emits [SpeechEvent] objects via a Flow that the ViewModel observes.
 *
 * Must be created and destroyed on the main thread.
 * Call [destroy] when done to release the recognizer.
 */
class SpeechRecognizerManager(private val context: Context) {

    sealed class SpeechEvent {
        object ReadyForSpeech : SpeechEvent()
        data class PartialResult(val text: String) : SpeechEvent()
        data class FinalResult(val text: String) : SpeechEvent()
        data class Error(val message: String) : SpeechEvent()
        object Stopped : SpeechEvent()
        /**
         * Emitted when a partial result contains a recognized wake-word
         * pattern (e.g. "Hey Sahayak"). The ViewModel can transition to
         * full active listening without waiting for a final result.
         */
        object WakeWordDetected : SpeechEvent()
    }

    private val _events = Channel<SpeechEvent>(Channel.BUFFERED)
    val events: Flow<SpeechEvent> = _events.receiveAsFlow()

    private var recognizer: SpeechRecognizer? = null
    private var isListening = false

    /** Returns true if device supports speech recognition. */
    fun isAvailable(): Boolean =
        SpeechRecognizer.isRecognitionAvailable(context)

    /**
     * Start listening for speech.
     * @param language e.g. "English (India)", "Hindi (हिंदी)"
     */
    fun startListening(language: String = "English (India)") {
        if (!isAvailable()) {
            _events.trySend(SpeechEvent.Error(
                "Voice recognition is not available on this device."
            ))
            return
        }

        // Release any existing recognizer
        recognizer?.destroy()

        recognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    isListening = true
                    _events.trySend(SpeechEvent.ReadyForSpeech)
                }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    isListening = false
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val results = partialResults
                        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val partial = results?.firstOrNull() ?: return
                    if (partial.isNotBlank()) {
                        // Check for wake-word before emitting partial result
                        if (WakeWordDetector.isWakeWord(partial)) {
                            _events.trySend(SpeechEvent.WakeWordDetected)
                        }
                        _events.trySend(SpeechEvent.PartialResult(partial))
                    }
                }

                override fun onResults(results: Bundle?) {
                    isListening = false
                    val matches = results
                        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val bestResult = matches?.firstOrNull()
                    if (bestResult.isNullOrBlank()) {
                        _events.trySend(SpeechEvent.Error(
                            "I couldn't hear you clearly. Please try again."
                        ))
                    } else {
                        _events.trySend(SpeechEvent.FinalResult(bestResult))
                    }
                }

                override fun onError(error: Int) {
                    isListening = false
                    val message = when (error) {
                        SpeechRecognizer.ERROR_AUDIO ->
                            "There was a problem with the microphone. Please try again."
                        SpeechRecognizer.ERROR_CLIENT ->
                            "Something went wrong. Please try again."
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
                            "Microphone permission is needed to listen."
                        SpeechRecognizer.ERROR_NETWORK ->
                            "No internet connection. Voice recognition needs the internet."
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
                            "The connection timed out. Please check your internet and try again."
                        SpeechRecognizer.ERROR_NO_MATCH ->
                            "I didn't catch that. Please speak a little more clearly and try again."
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY ->
                            "I'm already listening. Please wait a moment and try again."
                        SpeechRecognizer.ERROR_SERVER ->
                            "There was a server error. Please try again in a moment."
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
                            "I didn't hear anything. Please tap the microphone and try speaking."
                        else ->
                            "Something went wrong. Please try again."
                    }
                    _events.trySend(SpeechEvent.Error(message))
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }

        val locale = languageToLocale(language)
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale.toString())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L)
        }

        recognizer?.startListening(intent)
    }

    /** Stop listening immediately. */
    fun stopListening() {
        recognizer?.stopListening()
        isListening = false
        _events.trySend(SpeechEvent.Stopped)
    }

    /** Release all resources. Must be called on main thread. */
    fun destroy() {
        recognizer?.destroy()
        recognizer = null
        _events.close()
    }

    // ------------------------------------------------------------------
    // Language → Locale mapping
    // ------------------------------------------------------------------

    private fun languageToLocale(language: String): Locale = when {
        language.contains("Hindi") -> Locale("hi", "IN")
        language.contains("Marathi") -> Locale("mr", "IN")
        language.contains("Tamil") -> Locale("ta", "IN")
        language.contains("Telugu") -> Locale("te", "IN")
        language.contains("Bengali") -> Locale("bn", "IN")
        else -> Locale("en", "IN")
    }
}
