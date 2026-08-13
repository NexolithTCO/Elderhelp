package com.example.elderhelpprototypev01.ui.voice

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.elderhelpprototypev01.SahaayViewModel
import com.example.elderhelpprototypev01.model.VoiceInteractionState
import com.example.elderhelpprototypev01.model.VoiceState
import com.example.elderhelpprototypev01.ui.components.ClarificationCard
import com.example.elderhelpprototypev01.ui.components.WakeWordBadge
import com.example.elderhelpprototypev01.ui.theme.*

/**
 * VoiceScreen
 *
 * The full-page voice assistant screen.
 * Updated for the Voice Interaction Engine to show:
 *  1. [WakeWordBadge]      — pulsing badge while engine monitors for "Hey Sahayak"
 *  2. [VoiceInputPanel]    — mic button + live transcript
 *  3. [ClarificationCard]  — warm amber card when LLM requests a repair clarification
 *  4. [ConversationPanel]  — scrollable chat history
 *  5. [ResponseCard]       — latest full response with TTS controls
 *
 * Engine state machine transitions are observed from [SahaayViewModel.engineState].
 * Navigation events (GO_BACK anchor) are consumed via [SahaayViewModel.navigationEvent].
 */
@Composable
fun VoiceScreen(
    viewModel: SahaayViewModel,
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val voiceState by viewModel.voiceState.collectAsStateWithLifecycle()
    val engineState by viewModel.engineState.collectAsStateWithLifecycle()
    val transcript by viewModel.transcript.collectAsStateWithLifecycle()
    val conversation by viewModel.conversation.collectAsStateWithLifecycle()
    val currentResponse by viewModel.currentResponse.collectAsStateWithLifecycle()
    val isSpeaking by viewModel.isSpeaking.collectAsStateWithLifecycle()
    val ttsEnabled by viewModel.ttsEnabled.collectAsStateWithLifecycle()
    val speechRate by viewModel.speechRate.collectAsStateWithLifecycle()
    val isWakeWordActive by viewModel.isWakeWordActive.collectAsStateWithLifecycle()

    val scrollState = rememberScrollState()

    // Consume GO_BACK navigation events from the ViewModel
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { action ->
            when (action) {
                SahaayViewModel.NavigationAction.NavigateBack -> onNavigateBack()
            }
        }
    }

    // Microphone permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.startListening()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppleCanvasBg)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // ---- Header ----
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Sahaay",
                    style = Typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = AppleTextPrimary,
                        fontSize = 28.sp
                    )
                )
                Text(
                    text = "Your voice assistant",
                    style = Typography.bodyMedium.copy(
                        color = AppleTextMuted,
                        fontSize = 14.sp
                    )
                )
            }

            // Clear conversation button
            if (conversation.isNotEmpty()) {
                IconButton(
                    onClick = { viewModel.clearConversation() },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Clear conversation",
                        tint = AppleTextMuted,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ---- Wake Word Badge ----
        // Shown when the engine is passively listening for "Hey Sahayak"
        AnimatedVisibility(
            visible = isWakeWordActive || engineState is VoiceInteractionState.WakeWordListening,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column {
                WakeWordBadge(
                    isActive = true,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // ---- Permission Banner (shown when not granted) ----
        if (voiceState is VoiceState.RequestingPermission) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFFFF3E0)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🎙️ Microphone permission needed",
                        style = Typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE65100),
                            fontSize = 17.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Sahaay needs to hear your voice to help you. Please allow microphone access.",
                        style = Typography.bodyMedium.copy(
                            color = AppleTextSecondary,
                            fontSize = 15.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE65100),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Allow Microphone Access", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // ---- Voice Input Panel ----
        VoiceInputPanel(
            voiceState = voiceState,
            transcript = transcript,
            onMicClick = {
                if (viewModel.hasMicPermission()) {
                    viewModel.startListening()
                } else {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            },
            onStopClick = {
                viewModel.stopListening()
            },
            modifier = Modifier.fillMaxWidth()
        )

        // ---- Clarification Card (Conversational Repair) ----
        // Displayed when the LLM needs one more piece of information.
        AnimatedVisibility(
            visible = engineState is VoiceInteractionState.WaitingForClarification,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut()
        ) {
            val clarificationState = engineState as? VoiceInteractionState.WaitingForClarification
            clarificationState?.let { state ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(20.dp))
                    ClarificationCard(
                        question = state.question,
                        onMicClick = {
                            if (viewModel.hasMicPermission()) {
                                viewModel.startListening()
                            } else {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // ---- Conversation History ----
        AnimatedVisibility(
            visible = conversation.isNotEmpty(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Conversation",
                    style = Typography.labelMedium.copy(
                        color = AppleTextMuted,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    ),
                    modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                )
                ConversationPanel(
                    messages = conversation,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                )
            }
        }

        // ---- Response Card ----
        AnimatedVisibility(
            visible = currentResponse != null && !currentResponse!!.isError
                    && currentResponse?.intent != "LOADING",
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut()
        ) {
            currentResponse?.let { response ->
                // Do not show a dedicated ResponseCard for vocal-anchor short-circuits —
                // they are TTS-only feedback and don't need a full card.
                if (!response.isVocalAnchor) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Spacer(modifier = Modifier.height(20.dp))
                        ResponseCard(
                            response = response,
                            isSpeaking = isSpeaking,
                            ttsEnabled = ttsEnabled,
                            speechRate = speechRate,
                            onPlayClick = { viewModel.speakCurrentResponse() },
                            onStopClick = { viewModel.stopSpeaking() },
                            onRetryClick = { viewModel.retryLastTranscript() },
                            onToggleTts = { viewModel.toggleTts() },
                            onSpeechRateChange = { viewModel.setSpeechRate(it) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // ---- Error Card ----
        AnimatedVisibility(
            visible = currentResponse?.isError == true || voiceState is VoiceState.Error,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            val errorMsg = (voiceState as? VoiceState.Error)?.message
                ?: currentResponse?.errorMessage
                ?: "Something went wrong."
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFFFF0F0)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("⚠️", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = errorMsg,
                                style = Typography.bodyLarge.copy(
                                    color = Color(0xFFCC0000),
                                    fontSize = 17.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = {
                                    viewModel.resetVoiceState()
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Try again", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // ---- Loading Card ----
        AnimatedVisibility(
            visible = voiceState is VoiceState.Processing,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = AppleBlueLight
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = AppleBlue,
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = "Sahaay is thinking...",
                            style = Typography.bodyLarge.copy(
                                color = AppleBlue,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }
    }
}
