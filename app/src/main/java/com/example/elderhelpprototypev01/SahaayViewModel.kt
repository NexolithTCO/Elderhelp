package com.example.elderhelpprototypev01

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.elderhelpprototypev01.accessibility.SahaayAccessibilityService
import com.example.elderhelpprototypev01.ai.GeminiLlmService
import com.example.elderhelpprototypev01.ai.LlmService
import com.example.elderhelpprototypev01.highlight.HighlightData
import com.example.elderhelpprototypev01.highlight.HighlightManager
import com.example.elderhelpprototypev01.model.AssistantResponse
import com.example.elderhelpprototypev01.model.ConversationMessage
import com.example.elderhelpprototypev01.model.MessageRole
import com.example.elderhelpprototypev01.model.VoiceState
import com.example.elderhelpprototypev01.screen.GeminiScreenAnalysisService
import com.example.elderhelpprototypev01.screen.ScreenAnalysisResult
import com.example.elderhelpprototypev01.screen.ScreenAnalysisService
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
 * Single source of truth for the entire assistant pipeline:
 *   Voice Input → STT → LLM / Screen Analysis → Highlight Overlay → TTS
 *
 * Owned at Activity scope so state persists across tab switches.
 */
class SahaayViewModel(application: Application) : AndroidViewModel(application) {

    // ------------------------------------------------------------------
    // Dependencies
    // ------------------------------------------------------------------

    private val llmService: LlmService = GeminiLlmService()
    private val screenAnalysisService: ScreenAnalysisService = GeminiScreenAnalysisService()
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

    // Screen Highlighting State
    val activeHighlight: StateFlow<HighlightData?> = HighlightManager.activeHighlight

    private var speechCollectionJob: Job? = null
    private var lastTranscript: String = ""

    // ------------------------------------------------------------------
    // Initialization
    // ------------------------------------------------------------------

    init {
        ttsManager.initialize(onReady = {})
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

    fun hasMicPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            getApplication(),
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

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
                    is SpeechRecognizerManager.SpeechEvent.Stopped -> {}
                }
            }
        }

        speechManager.startListening(_currentLanguage.value)
    }

    fun stopListening() {
        speechManager.stopListening()
        if (_voiceState.value is VoiceState.Listening) {
            _voiceState.value = VoiceState.Idle
        }
    }

    fun retryLastTranscript() {
        if (lastTranscript.isNotBlank()) {
            processTranscript(lastTranscript)
        } else {
            startListening()
        }
    }

    fun resetVoiceState() {
        _voiceState.value = VoiceState.Idle
        _transcript.value = ""
    }

    // ------------------------------------------------------------------
    // Screen Analysis & Highlighting Pipeline
    // ------------------------------------------------------------------

    /**
     * Inspects the current screen using [SahaayAccessibilityService], sends context to Gemini,
     * draws a visual highlight overlay over the target UI element, and speaks the guidance.
     */
    fun analyzeCurrentScreenAndHighlight(userGoal: String = "What should I do next?") {
        _voiceState.value = VoiceState.Processing
        _currentResponse.value = AssistantResponse.loading()

        val isServiceEnabled = SahaayAccessibilityService.isServiceEnabled(getApplication())
        val screenContext = SahaayAccessibilityService.instance?.captureCurrentScreenContext()

        if (!isServiceEnabled || screenContext == null || screenContext.elements.isEmpty()) {
            // Service not enabled or no nodes -> Friendly guidance prompt
            val response = AssistantResponse(
                intent = "ACCESSIBILITY_NEEDED",
                goal = userGoal,
                response = "Please enable Sahaay Accessibility Service in System Settings so I can inspect and highlight options on your screen.",
                suggestedNextStep = "Open Settings → Accessibility → Sahaay Assistant Service → Turn ON."
            )
            _currentResponse.value = response
            _voiceState.value = VoiceState.Done
            if (_ttsEnabled.value) {
                ttsManager.speak(response.response, force = false)
            }
            return
        }

        // Add to conversation
        val userMsg = ConversationMessage(role = MessageRole.USER, text = userGoal)
        _conversation.value = _conversation.value + userMsg

        viewModelScope.launch {
            val result = screenAnalysisService.analyzeScreen(
                screenContext = screenContext,
                userGoal = userGoal,
                conversationHistory = _conversation.value,
                userLanguage = _currentLanguage.value
            )

            val assistantResponse = AssistantResponse(
                intent = result.actionType,
                goal = userGoal,
                response = result.explanation,
                suggestedNextStep = result.targetElementText?.let { "Tap the highlighted '$it' option." },
                helpfulTip = result.reason,
                isError = result.isError,
                errorMessage = result.errorMessage
            )

            _currentResponse.value = assistantResponse
            _voiceState.value = if (result.isError) VoiceState.Error(result.explanation) else VoiceState.Done

            if (!result.isError) {
                _conversation.value = _conversation.value + ConversationMessage(
                    role = MessageRole.ASSISTANT,
                    text = result.explanation
                )

                // If target bounds found -> Show visual highlight overlay!
                if (result.targetElementBounds != null && result.targetElementBounds.width() > 0) {
                    HighlightManager.showHighlight(
                        context = getApplication(),
                        bounds = result.targetElementBounds,
                        targetText = result.targetElementText ?: "",
                        explanation = result.explanation
                    )
                }

                // Speak explanation via TTS
                if (_ttsEnabled.value) {
                    val textToSpeak = buildSpeakableText(assistantResponse)
                    ttsManager.speak(textToSpeak, force = false)
                }
            }
        }
    }

    /** Clear active visual highlight box. */
    fun clearHighlight() {
        HighlightManager.clearHighlight(getApplication())
    }

    // ------------------------------------------------------------------
    // LLM Processing Dispatcher
    // ------------------------------------------------------------------

    fun processTranscript(text: String) {
        val lower = text.lowercase()

        // Check if user is asking for screen analysis / highlighting
        val isScreenCommand = lower.contains("next") || lower.contains("what should i do") ||
                lower.contains("explain screen") || lower.contains("read screen") ||
                lower.contains("read this") || lower.contains("doctor") ||
                lower.contains("sharma") || lower.contains("book") || lower.contains("bill")

        val hasAccessibility = SahaayAccessibilityService.isServiceEnabled(getApplication()) &&
                SahaayAccessibilityService.instance?.captureCurrentScreenContext()?.elements?.isNotEmpty() == true

        if (isScreenCommand && hasAccessibility) {
            analyzeCurrentScreenAndHighlight(text)
            return
        }

        // Standard LLM processing
        _voiceState.value = VoiceState.Processing
        _currentResponse.value = AssistantResponse.loading()

        val userMessage = ConversationMessage(role = MessageRole.USER, text = text)
        _conversation.value = _conversation.value + userMessage

        viewModelScope.launch {
            val response = llmService.analyze(
                transcript = text,
                conversation = _conversation.value.dropLast(1),
                userLanguage = _currentLanguage.value
            )

            _currentResponse.value = response
            _voiceState.value = if (response.isError) {
                VoiceState.Error(response.errorMessage ?: response.response)
            } else {
                VoiceState.Done
            }

            if (!response.isError) {
                val assistantMessage = ConversationMessage(
                    role = MessageRole.ASSISTANT,
                    text = response.response
                )
                _conversation.value = _conversation.value + assistantMessage

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
            sb.append(". ").append(response.suggestedNextStep)
        }
        return sb.toString()
    }

    // ------------------------------------------------------------------
    // TTS Controls
    // ------------------------------------------------------------------

    fun speakCurrentResponse() {
        val response = _currentResponse.value ?: return
        val text = buildSpeakableText(response)
        ttsManager.speak(text, force = true)
    }

    fun stopSpeaking() {
        ttsManager.stop()
    }

    fun toggleTts() {
        _ttsEnabled.value = !_ttsEnabled.value
        if (!_ttsEnabled.value) {
            ttsManager.stop()
        }
    }

    fun setSpeechRate(rate: Float) {
        _speechRate.value = rate
        ttsManager.setSpeechRate(rate)
    }

    // ------------------------------------------------------------------
    // Conversation
    // ------------------------------------------------------------------

    fun clearConversation() {
        _conversation.value = emptyList()
        _currentResponse.value = null
        _transcript.value = ""
        _voiceState.value = VoiceState.Idle
        lastTranscript = ""
        clearHighlight()
        ttsManager.stop()
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.destroy()
        ttsManager.shutdown()
        clearHighlight()
    }
}
