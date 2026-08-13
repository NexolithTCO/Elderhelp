package com.example.elderhelpprototypev01

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.elderhelpprototypev01.ai.GeminiLlmService
import com.example.elderhelpprototypev01.ai.LlmService
import com.example.elderhelpprototypev01.model.AssistantResponse
import com.example.elderhelpprototypev01.model.ConversationMessage
import com.example.elderhelpprototypev01.model.MessageRole
import com.example.elderhelpprototypev01.model.VoiceState
import com.example.elderhelpprototypev01.voice.SpeechRecognizerManager
import com.example.elderhelpprototypev01.voice.TextToSpeechManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * SahaayViewModel
 *
 * Single source of truth for the entire voice assistant pipeline:
 *   Voice Input → STT → LLM → Response → TTS
 *
 * Owned at the Activity scope so state persists across tab switches.
 * The UI never talks directly to SpeechRecognizer, TTS, or the LLM.
 */
class SahaayViewModel(application: Application) : AndroidViewModel(application) {

    // ------------------------------------------------------------------
    // Dependencies
    // ------------------------------------------------------------------

    private val llmService: LlmService = GeminiLlmService()
    private val speechManager = SpeechRecognizerManager(application)
    private val ttsManager = TextToSpeechManager(application)

    // ------------------------------------------------------------------
    // State Flows
    // ------------------------------------------------------------------

    private val _voiceState = MutableStateFlow<VoiceState>(VoiceState.Idle)
    val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()

    private val _transcript = MutableStateFlow("")
    val transcript: StateFlow<String> = _transcript.asStateFlow()

    private val _conversation = MutableStateFlow<List<ConversationMessage>>(emptyList())
    val conversation: StateFlow<List<ConversationMessage>> = _conversation.asStateFlow()

    private val _currentResponse = MutableStateFlow<AssistantResponse?>(null)
    val currentResponse: StateFlow<AssistantResponse?> = _currentResponse.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _ttsEnabled = MutableStateFlow(true)
    val ttsEnabled: StateFlow<Boolean> = _ttsEnabled.asStateFlow()

    private val _speechRate = MutableStateFlow(TextToSpeechManager.DEFAULT_SPEECH_RATE)
    val speechRate: StateFlow<Float> = _speechRate.asStateFlow()

    private val _currentLanguage = MutableStateFlow("English (India)")
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    private var speechCollectionJob: Job? = null
    private var lastTranscript: String = ""

    // ------------------------------------------------------------------
    // Initialization
    // ------------------------------------------------------------------

    init {
        ttsManager.initialize(onReady = {})
        // Mirror TTS speaking state into our own flow
        viewModelScope.launch {
            ttsManager.isSpeaking.collect { speaking ->
                _isSpeaking.value = speaking
            }
        }
    }

    // ------------------------------------------------------------------
    // Language
    // ------------------------------------------------------------------

    fun setLanguage(language: String) {
        _currentLanguage.value = language
        ttsManager.applyLanguage(language)
    }

    // ------------------------------------------------------------------
    // Voice Input
    // ------------------------------------------------------------------

    /** Check if microphone permission is granted. */
    fun hasMicPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            getApplication(),
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    /** Start listening for speech. Call only after permission is granted. */
    fun startListening() {
        if (!hasMicPermission()) {
            _voiceState.value = VoiceState.RequestingPermission
            return
        }
        if (!speechManager.isAvailable()) {
            _voiceState.value = VoiceState.Error(
                "Voice recognition is not available on this device."
            )
            return
        }

        ttsManager.stop()
        _transcript.value = ""
        _voiceState.value = VoiceState.Listening

        // Cancel any previous collection
        speechCollectionJob?.cancel()
        speechCollectionJob = viewModelScope.launch(Dispatchers.Main) {
            speechManager.events.collect { event ->
                when (event) {
                    is SpeechRecognizerManager.SpeechEvent.ReadyForSpeech -> {
                        _voiceState.value = VoiceState.Listening
                    }
                    is SpeechRecognizerManager.SpeechEvent.PartialResult -> {
                        _transcript.value = event.text
                        _voiceState.value = VoiceState.PartialResult(event.text)
                    }
                    is SpeechRecognizerManager.SpeechEvent.FinalResult -> {
                        val text = event.text
                        _transcript.value = text
                        lastTranscript = text
                        speechCollectionJob?.cancel()
                        processTranscript(text)
                    }
                    is SpeechRecognizerManager.SpeechEvent.Error -> {
                        _voiceState.value = VoiceState.Error(event.message)
                        speechCollectionJob?.cancel()
                    }
                    is SpeechRecognizerManager.SpeechEvent.Stopped -> {
                        // Stopped by user — do nothing, wait for result or already processed
                    }
                }
            }
        }

        speechManager.startListening(_currentLanguage.value)
    }

    /** Stop listening early (user tapped Stop). */
    fun stopListening() {
        speechManager.stopListening()
        if (_voiceState.value is VoiceState.Listening) {
            _voiceState.value = VoiceState.Idle
        }
    }

    /** Retry the last recognized transcript through the LLM again. */
    fun retryLastTranscript() {
        if (lastTranscript.isNotBlank()) {
            processTranscript(lastTranscript)
        } else {
            startListening()
        }
    }

    /** Reset voice state so user can start fresh. */
    fun resetVoiceState() {
        _voiceState.value = VoiceState.Idle
        _transcript.value = ""
    }

    // ------------------------------------------------------------------
    // LLM Processing
    // ------------------------------------------------------------------

    private fun processTranscript(text: String) {
        _voiceState.value = VoiceState.Processing
        _currentResponse.value = AssistantResponse.loading()

        // Add user message to conversation
        val userMessage = ConversationMessage(
            role = MessageRole.USER,
            text = text
        )
        _conversation.value = _conversation.value + userMessage

        viewModelScope.launch {
            val response = llmService.analyze(
                transcript = text,
                conversation = _conversation.value.dropLast(1), // don't include the just-added message
                userLanguage = _currentLanguage.value
            )

            _currentResponse.value = response
            _voiceState.value = if (response.isError) {
                VoiceState.Error(response.errorMessage ?: response.response)
            } else {
                VoiceState.Done
            }

            // Add assistant message to conversation
            if (!response.isError) {
                val assistantMessage = ConversationMessage(
                    role = MessageRole.ASSISTANT,
                    text = response.response
                )
                _conversation.value = _conversation.value + assistantMessage

                // Auto-speak short responses if TTS is enabled
                if (_ttsEnabled.value) {
                    val textToSpeak = buildSpeakableText(response)
                    ttsManager.speak(textToSpeak, force = false)
                }
            }
        }
    }

    private fun buildSpeakableText(response: AssistantResponse): String {
        val sb = StringBuilder(response.response)
        if (response.needsClarification && response.clarifyingQuestion != null) {
            sb.append(". ").append(response.clarifyingQuestion)
        } else if (response.suggestedNextStep != null) {
            sb.append(". Next step: ").append(response.suggestedNextStep)
        }
        return sb.toString()
    }

    // ------------------------------------------------------------------
    // TTS Controls
    // ------------------------------------------------------------------

    /** Play/replay the current response aloud (user-forced, ignores length limit). */
    fun speakCurrentResponse() {
        val response = _currentResponse.value ?: return
        val text = buildSpeakableText(response)
        ttsManager.speak(text, force = true)
    }

    /** Stop TTS immediately. */
    fun stopSpeaking() {
        ttsManager.stop()
    }

    /** Toggle TTS on/off. */
    fun toggleTts() {
        _ttsEnabled.value = !_ttsEnabled.value
        if (!_ttsEnabled.value) {
            ttsManager.stop()
        }
    }

    /** Update speech rate (0.5–1.5). */
    fun setSpeechRate(rate: Float) {
        _speechRate.value = rate
        ttsManager.setSpeechRate(rate)
    }

    // ------------------------------------------------------------------
    // Conversation
    // ------------------------------------------------------------------

    /** Clear all conversation history and reset to idle. */
    fun clearConversation() {
        _conversation.value = emptyList()
        _currentResponse.value = null
        _transcript.value = ""
        _voiceState.value = VoiceState.Idle
        lastTranscript = ""
        ttsManager.stop()
    }

    // ------------------------------------------------------------------
    // Lifecycle
    // ------------------------------------------------------------------

    override fun onCleared() {
        super.onCleared()
        speechManager.destroy()
        ttsManager.shutdown()
    }
}
