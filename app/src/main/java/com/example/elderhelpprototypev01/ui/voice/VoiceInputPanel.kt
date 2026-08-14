package com.example.elderhelpprototypev01.ui.voice

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.elderhelpprototypev01.model.VoiceState
import com.example.elderhelpprototypev01.ui.theme.*

@Composable
fun VoiceInputPanel(
    voiceState: VoiceState,
    transcript: String,
    onMicClick: () -> Unit,
    onStopClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isListening = voiceState is VoiceState.Listening || voiceState is VoiceState.PartialResult
    val isProcessing = voiceState is VoiceState.Processing

    val pulseAnim = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulseAnim.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val stateText = when (voiceState) {
            is VoiceState.Idle -> "Tap the microphone to speak"
            is VoiceState.RequestingPermission -> "Microphone permission needed"
            is VoiceState.Listening -> "I'm listening..."
            is VoiceState.PartialResult -> "I'm listening..."
            is VoiceState.Processing -> "Understanding what you need..."
            is VoiceState.Done -> "Tap again to ask something else"
            is VoiceState.Error -> voiceState.message
        }

        val labelColor = when (voiceState) {
            is VoiceState.Error -> MaterialTheme.colorScheme.error
            is VoiceState.Listening, is VoiceState.PartialResult -> MaterialTheme.colorScheme.primary
            is VoiceState.Processing -> MaterialTheme.colorScheme.secondary
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }

        Text(
            text = stateText,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Medium,
                color = labelColor,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SahaaySpacing.xxl)
        )

        Spacer(modifier = Modifier.height(SahaaySpacing.xl))

        // Microphone Action Button Area
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(120.dp)
        ) {
            if (isListening) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f))
                )
            }

            if (isListening || isProcessing) {
                Button(
                    onClick = onStopClick,
                    modifier = Modifier.size(SahaayTouchTarget.hero),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = SahaayElevation.medium),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            color = Color.White,
                            strokeWidth = 3.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop listening",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            } else {
                Button(
                    onClick = onMicClick,
                    modifier = Modifier.size(SahaayTouchTarget.hero),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = SahaayElevation.medium),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Start listening",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(SahaaySpacing.lg))

        // Live Transcript Display
        if (transcript.isNotBlank()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SahaaySpacing.lg),
                shape = RoundedCornerShape(SahaayCorners.medium),
                color = MaterialTheme.colorScheme.primaryContainer,
                shadowElevation = SahaayElevation.low
            ) {
                Column(modifier = Modifier.padding(SahaaySpacing.lg)) {
                    Text(
                        text = "You said:",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(SahaaySpacing.xs))
                    Text(
                        text = "\u201C$transcript\u201D",
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
