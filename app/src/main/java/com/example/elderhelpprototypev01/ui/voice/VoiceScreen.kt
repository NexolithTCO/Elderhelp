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
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Warning
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
import com.example.elderhelpprototypev01.model.VoiceState
import com.example.elderhelpprototypev01.ui.components.ClarificationCard
import com.example.elderhelpprototypev01.ui.theme.*

@Composable
fun VoiceScreen(
    viewModel: SahaayViewModel,
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val voiceState by viewModel.voiceState.collectAsStateWithLifecycle()
    val transcript by viewModel.transcript.collectAsStateWithLifecycle()
    val conversation by viewModel.conversation.collectAsStateWithLifecycle()
    val currentResponse by viewModel.currentResponse.collectAsStateWithLifecycle()
    val isSpeaking by viewModel.isSpeaking.collectAsStateWithLifecycle()
    val ttsEnabled by viewModel.ttsEnabled.collectAsStateWithLifecycle()
    val speechRate by viewModel.speechRate.collectAsStateWithLifecycle()

    val scrollState = rememberScrollState()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.startListening()
        }
    }

    val systemSpeechLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK && result.data != null) {
            val matches = result.data?.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS)
            val text = matches?.firstOrNull()
            if (!text.isNullOrBlank()) {
                viewModel.processTranscript(text)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(horizontal = SahaaySpacing.lg)
            .padding(top = SahaaySpacing.lg, bottom = SahaaySpacing.xxxl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Voice Assistant",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                Text(
                    text = "Ask Sahaay anything using your voice",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            if (conversation.isNotEmpty()) {
                IconButton(
                    onClick = { viewModel.clearConversation() },
                    modifier = Modifier.size(SahaayTouchTarget.minimum)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Clear conversation",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(SahaaySpacing.md))

        // Permission Banner
        if (voiceState is VoiceState.RequestingPermission) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(SahaayCorners.medium),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Column(modifier = Modifier.padding(SahaaySpacing.lg)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(SahaaySpacing.sm))
                        Text(
                            text = "Microphone permission needed",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(SahaaySpacing.xs))
                    Text(
                        text = "Sahaay needs to hear your voice to help you. Please allow microphone access.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    )
                    Spacer(modifier = Modifier.height(SahaaySpacing.md))
                    Button(
                        onClick = {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        ),
                        shape = RoundedCornerShape(SahaayCorners.small),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Allow Microphone Access",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(SahaaySpacing.xl))
        }

        // Voice Input Panel
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

        // Clarification Card
        AnimatedVisibility(
            visible = currentResponse?.needsClarification == true,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut()
        ) {
            currentResponse?.clarifyingQuestion?.let { question ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(SahaaySpacing.xl))
                    ClarificationCard(
                        question = question,
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

        // Conversation History
        AnimatedVisibility(
            visible = conversation.isNotEmpty(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.height(SahaaySpacing.xxl))
                Text(
                    text = "Conversation",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    modifier = Modifier.padding(start = SahaaySpacing.xs, bottom = SahaaySpacing.xs)
                )
                ConversationPanel(
                    messages = conversation,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                )
            }
        }

        // Response Card
        AnimatedVisibility(
            visible = currentResponse != null && !currentResponse!!.isError
                    && currentResponse?.intent != "LOADING",
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut()
        ) {
            currentResponse?.let { response ->
                if (!response.isVocalAnchor) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Spacer(modifier = Modifier.height(SahaaySpacing.xl))
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

        // Error Card
        AnimatedVisibility(
            visible = currentResponse?.isError == true || voiceState is VoiceState.Error,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            val errorMsg = (voiceState as? VoiceState.Error)?.message
                ?: currentResponse?.errorMessage
                ?: "Something went wrong."
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.height(SahaaySpacing.lg))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(SahaayCorners.medium),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Row(
                        modifier = Modifier.padding(SahaaySpacing.lg),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(SahaaySpacing.md))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = errorMsg,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            )
                            Spacer(modifier = Modifier.height(SahaaySpacing.sm))
                            OutlinedButton(
                                onClick = {
                                    viewModel.resetVoiceState()
                                    if (viewModel.hasMicPermission()) {
                                        val intent = android.content.Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                            putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                            putExtra(android.speech.RecognizerIntent.EXTRA_PROMPT, "Speak to Sahaay...")
                                        }
                                        try {
                                            systemSpeechLauncher.launch(intent)
                                        } catch (e: Exception) {
                                            viewModel.startListening()
                                        }
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                },
                                shape = RoundedCornerShape(SahaayCorners.small)
                            ) {
                                Text("Try again", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }
            }
        }

        // Loading Card
        AnimatedVisibility(
            visible = voiceState is VoiceState.Processing,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.height(SahaaySpacing.lg))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(SahaayCorners.medium),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(SahaaySpacing.lg),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(SahaaySpacing.md))
                        Text(
                            text = "Sahaay is thinking...",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }
    }
}
