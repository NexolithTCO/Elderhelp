package com.example.elderhelpprototypev01.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * WakeWordBadge
 *
 * A small, animated status badge displayed when the Voice Interaction Engine
 * is passively monitoring for the wake word "Hey Sahayak".
 *
 * Visual design:
 * - Soft green pill with a pulsing dot — indicates the mic is warm and listening.
 * - Alpha pulse animation draws the elderly user's eye gently without being jarring.
 *
 * @param isActive Show the badge. When false the badge is invisible (takes no space).
 * @param modifier Optional layout modifier.
 */
@Composable
fun WakeWordBadge(
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    if (!isActive) return

    // Infinite pulsing animation for the indicator dot
    val infiniteTransition = rememberInfiniteTransition(label = "wake_word_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Surface(
        shape = RoundedCornerShape(50),
        color = Color(0xFFE8F5E9),  // soft green background
        shadowElevation = 2.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pulsing green dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .scale(pulseScale)
                    .alpha(pulseAlpha)
                    .background(Color(0xFF43A047), shape = CircleShape)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Hey Sahayak — ready",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2E7D32)
            )
        }
    }
}
