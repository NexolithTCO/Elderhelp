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
import com.example.elderhelpprototypev01.model.VocalAnchorAction
import com.example.elderhelpprototypev01.model.VoiceInteractionState
import com.example.elderhelpprototypev01.model.VoiceState
import com.example.elderhelpprototypev01.voice.SpeechRecognizerManager
import com.example.elderhelpprototypev01.voice.TextToSpeechManager
import com.example.elderhelpprototypev01.voice.VocalAnchorProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * SahaayViewModel
 *
 * Single source of truth for the entire Voice Interaction Engine pipeline:
 *
 *   Wake Word Detection → Active STT → Vocal Anchor Check → LLM → TTS
 *
 * Voice Interaction Engine features added in this version:
 * - [engineState]       richer state machine covering wake-word, clarification, speaking
 * - [isWakeWordActive]  true while passively listening for "Hey Sahayak"
 * - [navigationEvent]   one-shot SharedFlow for GO_BACK vocal anchor
 * - Vocal anchor short-circuit in [processTranscript]: REPEAT / GO_BACK / STOP / NEXT_STEP
 *   are handled locally without hitting the LLM.
 * - [speakCurrentResponse] now uses speakRaw() to strip residual markdown.
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
    // Legacy State Flows (kept for backward-compat with existing UI)
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

    // ------------------------------------------------------------------
    // Voice Interaction Engine State Flows
    // ------------------------------------------------------------------

    /**
     * Rich engine state machine. UI screens that want full engine awareness
     * (e.g. VoiceScreen) should prefer this over [voiceState].
     */
    private val _engineState = MutableStateFlow<VoiceInteractionState>(VoiceInteractionState.Idle)
    val engineState: StateFlow<VoiceInteractionState> = _engineState.asStateFlow()

    /**
     * True while the engine is passively monitoring partial results for the
     * wake word "Hey Sahayak". Only active within the app (in-app scope).
     */
    private val _isWakeWordActive = MutableStateFlow(false)
    val isWakeWordActive: StateFlow<Boolean> = _isWakeWordActive.asStateFlow()

    /**
     * One-shot events for navigation actions triggered by vocal anchors.
     * Currently only emits [NavigationAction.NavigateBack] (GO_BACK anchor).
     * Consumed by the Activity / NavController.
     */
    private val _navigationEvent = MutableSharedFlow<NavigationAction>(extraBufferCapacity = 1)
    val navigationEvent: SharedFlow<NavigationAction> = _navigationEvent.asSharedFlow()

    /** Enum for navigation actions emitted through [navigationEvent]. */
    enum class NavigationAction { NavigateBack }

    // ------------------------------------------------------------------
    // Internal tracking
    // ------------------------------------------------------------------

    private var speechCollectionJob: Job? = null
    private var lastTranscript: String = ""

    /**
     * The full text most recently sent to TTS — used by the REPEAT vocal anchor
     * to replay the last utterance without re-querying the LLM.
     */
    private var lastSpokenText: String = ""

    /**
     * The last successful [AssistantResponse] — used by the NEXT_STEP anchor
     * to speak [AssistantResponse.suggestedNextStep] on demand.
     */
    private var lastSuccessfulResponse: AssistantResponse? = null

    // ------------------------------------------------------------------
    // Initialization
    // ------------------------------------------------------------------

    init {
        ttsManager.initialize(onReady = {})
        // Mirror TTS speaking state into our own flows
        viewModelScope.launch {
            ttsManager.isSpeaking.collect { speaking ->
                _isSpeaking.value = speaking
                // Keep engineState in sync: Speaking <-> Done
                if (speaking) {
                    if (_engineState.value !is VoiceInteractionState.Error) {
                        _engineState.value = VoiceInteractionState.Speaking
                    }
                } else {
                    if (_engineState.value is VoiceInteractionState.Speaking) {
                        _engineState.value = VoiceInteractionState.Done
                    }
                }
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
            val msg = "Voice recognition is not available on this device."
            _voiceState.value = VoiceState.Error(msg)
            _engineState.value = VoiceInteractionState.Error(msg)
            return
        }

        ttsManager.stop()
        _transcript.value = ""
        _voiceState.value = VoiceState.Listening
        _engineState.value = VoiceInteractionState.ActiveListening

        // Cancel any previous collection
        speechCollectionJob?.cancel()
        speechCollectionJob = viewModelScope.launch(Dispatchers.Main) {
            speechManager.events.collect { event ->
                when (event) {
                    is SpeechRecognizerManager.SpeechEvent.ReadyForSpeech -> {
                        _voiceState.value = VoiceState.Listening
                        _engineState.value = VoiceInteractionState.ActiveListening
                    }
                    is SpeechRecognizerManager.SpeechEvent.WakeWordDetected -> {
                        // Wake-word heard during passive monitoring — activate full session
                        _isWakeWordActive.value = true
                        _engineState.value = VoiceInteractionState.WakeWordListening
                    }
                    is SpeechRecognizerManager.SpeechEvent.PartialResult -> {
                        _transcript.value = event.text
                        _voiceState.value = VoiceState.PartialResult(event.text)
                        _engineState.value = VoiceInteractionState.PartialResult(event.text)
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
                        _engineState.value = VoiceInteractionState.Error(event.message)
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
            _engineState.value = VoiceInteractionState.Idle
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
        _engineState.value = VoiceInteractionState.Idle
        _transcript.value = ""
    }

    // ------------------------------------------------------------------
    // Wake Word
    // ------------------------------------------------------------------

    /**
     * Enable passive wake-word monitoring.
     * The mic stays open and WakeWordDetector screens partial results.
     * Called automatically when VoiceScreen becomes visible.
     */
    fun activateWakeWord() {
        if (_isWakeWordActive.value) return
        _isWakeWordActive.value = true
        _engineState.value = VoiceInteractionState.WakeWordListening
        // Start listening; WakeWordDetected events are emitted by SpeechRecognizerManager
        if (hasMicPermission() && speechManager.isAvailable()) {
            startListening()
        }
    }

    /**
     * Disable passive wake-word monitoring and return to idle.
     * Called when VoiceScreen is no longer visible.
     */
    fun deactivateWakeWord() {
        _isWakeWordActive.value = false
        speechManager.stopListening()
        _engineState.value = VoiceInteractionState.Idle
    }

    // ------------------------------------------------------------------
    // LLM Processing + Vocal Anchor Short-Circuit
    // ------------------------------------------------------------------

    /**
     * Core engine processing function.
     *
     * Step 1 — Vocal Anchor Detection: if [VocalAnchorProcessor] recognizes
     *   a navigation command, handle it locally (zero LLM latency).
     * Step 2 — LLM Analysis: for all other input, send to Gemini and build
     *   a structured [AssistantResponse].
     */
    private fun processTranscript(text: String) {
        // ---- Step 1: Vocal Anchor short-circuit ----
        val anchor = VocalAnchorProcessor.detect(text)
        if (anchor != null) {
            handleVocalAnchor(anchor)
            return
        }

        // ---- Step 2: LLM pipeline ----
        _voiceState.value = VoiceState.Processing
        _engineState.value = VoiceInteractionState.Processing
        _currentResponse.value = AssistantResponse.loading()

        // Add user message to conversation
        val userMessage = ConversationMessage(role = MessageRole.USER, text = text)
        _conversation.value = _conversation.value + userMessage

        viewModelScope.launch {
            val response = llmService.analyze(
                transcript = text,
                conversation = _conversation.value.dropLast(1),
                userLanguage = _currentLanguage.value
            )

            _currentResponse.value = response

            if (response.isError) {
                _voiceState.value = VoiceState.Error(response.errorMessage ?: response.response)
                _engineState.value = VoiceInteractionState.Error(
                    response.errorMessage ?: response.response
                )
            } else {
                // Check if LLM wants clarification → enter repair state
                if (response.needsClarification && response.clarifyingQuestion != null) {
                    _voiceState.value = VoiceState.Done
                    _engineState.value = VoiceInteractionState.WaitingForClarification(
                        response.clarifyingQuestion
                    )
                } else {
                    _voiceState.value = VoiceState.Done
                    _engineState.value = VoiceInteractionState.Done
                }

                // Add assistant message to conversation history
                val assistantMessage = ConversationMessage(
                    role = MessageRole.ASSISTANT,
                    text = response.response
                )
                _conversation.value = _conversation.value + assistantMessage
                lastSuccessfulResponse = response

                // Auto-speak using speakRaw() for guaranteed no-markdown output
                if (_ttsEnabled.value) {
                    val textToSpeak = buildSpeakableText(response)
                    lastSpokenText = textToSpeak
                    ttsManager.speakRaw(textToSpeak, force = false)
                }
            }
        }
    }

    /**
     * Handle a detected vocal anchor locally — no LLM call needed.
     *
     * REPEAT    → replay last spoken text via TTS
     * STOP      → halt TTS, return to idle
     * GO_BACK   → emit NavigateBack navigation event
     * NEXT_STEP → speak suggestedNextStep from last response, or prompt to ask a question
     */
    private fun handleVocalAnchor(action: VocalAnchorAction) {
        when (action) {
            VocalAnchorAction.REPEAT -> {
                if (lastSpokenText.isNotBlank()) {
                    ttsManager.speakRaw(lastSpokenText, force = true)
                } else {
                    val msg = "Koi pichli baat nahi mili. Aap apna sawaal phir se poochh sakte hain."
                    ttsManager.speakRaw(msg, force = true)
                    lastSpokenText = msg
                }
                val anchorResponse = AssistantResponse(
                    intent = "VOCAL_ANCHOR",
                    goal = "Repeat last response",
                    response = lastSpokenText,
                    isVocalAnchor = true,
                    vocalAnchorAction = action
                )
                _currentResponse.value = anchorResponse
                _voiceState.value = VoiceState.Done
                _engineState.value = VoiceInteractionState.Done
            }

            VocalAnchorAction.STOP -> {
                ttsManager.stop()
                _voiceState.value = VoiceState.Idle
                _engineState.value = VoiceInteractionState.Idle
                val anchorResponse = AssistantResponse(
                    intent = "VOCAL_ANCHOR",
                    goal = "Stop",
                    response = "Ruk gaya hoon.",
                    isVocalAnchor = true,
                    vocalAnchorAction = action
                )
                _currentResponse.value = anchorResponse
            }

            VocalAnchorAction.GO_BACK -> {
                ttsManager.stop()
                val anchorResponse = AssistantResponse(
                    intent = "VOCAL_ANCHOR",
                    goal = "Go back",
                    response = "Peeche ja raha hoon.",
                    isVocalAnchor = true,
                    vocalAnchorAction = action
                )
                _currentResponse.value = anchorResponse
                _voiceState.value = VoiceState.Done
                _engineState.value = VoiceInteractionState.Done
                // Emit navigation event for the Activity / NavController to consume
                viewModelScope.launch {
                    _navigationEvent.emit(NavigationAction.NavigateBack)
                }
            }

            VocalAnchorAction.NEXT_STEP -> {
                val nextStep = lastSuccessfulResponse?.suggestedNextStep
                val msg = if (!nextStep.isNullOrBlank()) {
                    nextStep
                } else {
                    "Abhi koi agle kadam ki jaankari nahi hai. Kya aap mujhse koi sawaal poochh sakte hain?"
                }
                lastSpokenText = msg
                ttsManager.speakRaw(msg, force = true)
                val anchorResponse = AssistantResponse(
                    intent = "VOCAL_ANCHOR",
                    goal = "Next step",
                    response = msg,
                    isVocalAnchor = true,
                    vocalAnchorAction = action
                )
                _currentResponse.value = anchorResponse
                _voiceState.value = VoiceState.Done
                _engineState.value = VoiceInteractionState.Done
            }
        }
    }

    private fun buildSpeakableText(response: AssistantResponse): String {
        val sb = StringBuilder(response.response)
        if (response.needsClarification && response.clarifyingQuestion != null) {
            sb.append(". ").append(response.clarifyingQuestion)
        } else if (response.suggestedNextStep != null) {
            sb.append(". ").append(response.suggestedNextStep)
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
        lastSpokenText = text
        ttsManager.speakRaw(text, force = true)
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
        _engineState.value = VoiceInteractionState.Idle
        lastTranscript = ""
        lastSpokenText = ""
        lastSuccessfulResponse = null
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
