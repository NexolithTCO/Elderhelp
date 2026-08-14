package com.example.elderhelpprototypev01.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.elderhelpprototypev01.ui.localization.Localization
import com.example.elderhelpprototypev01.ui.theme.SahaayCorners
import com.example.elderhelpprototypev01.ui.theme.SahaayElevation
import com.example.elderhelpprototypev01.ui.theme.SahaaySpacing
import com.example.elderhelpprototypev01.ui.theme.SahaayTouchTarget

@Composable
fun MicrophoneButton(
    isListening: Boolean = false,
    currentLanguage: String = "English (India)",
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val strings = Localization.getStrings(currentLanguage)

    // Controlled pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulseRing")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = if (isListening) 1.0f else 0.98f,
        targetValue = if (isListening) 1.15f else 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isListening) 800 else 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "buttonScale"
    )

    val buttonColor by animateColorAsState(
        targetValue = if (isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
        animationSpec = tween(300),
        label = "buttonColor"
    )

    val auraColor by animateColorAsState(
        targetValue = if (isListening) MaterialTheme.colorScheme.error.copy(alpha = 0.18f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
        animationSpec = tween(300),
        label = "auraColor"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(SahaayCorners.large)),
        shape = RoundedCornerShape(SahaayCorners.large),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shadowElevation = SahaayElevation.low
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = SahaaySpacing.xl, horizontal = SahaaySpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Voice Trigger Button Area — 148dp container guarantees zero clipping when pulse reaches 138dp
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(148.dp)
            ) {
                // Pulsing Aura Ring (120dp * 1.15f = max 138dp)
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(auraColor)
                )

                // Core Microphone Surface Circle (80dp)
                Surface(
                    modifier = Modifier
                        .size(SahaayTouchTarget.hero)
                        .scale(buttonScale)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = onClick
                        ),
                    shape = CircleShape,
                    color = buttonColor,
                    shadowElevation = SahaayElevation.medium
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.GraphicEq else Icons.Default.Mic,
                            contentDescription = "Microphone Trigger",
                            tint = Color.White,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(SahaaySpacing.xs))

            // Text Label
            Text(
                text = if (isListening) strings.tapToStop else strings.tapToSpeak,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = if (isListening) strings.listeningText else strings.micSubtitle,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}
