package com.example.elderhelpprototypev01.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.elderhelpprototypev01.ui.theme.SahaayCorners
import com.example.elderhelpprototypev01.ui.theme.SahaayElevation
import com.example.elderhelpprototypev01.ui.theme.SahaaySpacing

@Composable
fun StatusCard(
    isListening: Boolean = false,
    statusText: String = "I'm here to help.",
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "statusDotPulse")
    val dotScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotScale"
    )

    val indicatorColor by animateColorAsState(
        targetValue = if (isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary,
        animationSpec = tween(300),
        label = "indicatorColor"
    )

    Surface(
        modifier = modifier
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(SahaayCorners.full)
            ),
        shape = RoundedCornerShape(SahaayCorners.full),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = SahaayElevation.low
    ) {
        Row(
            modifier = Modifier.padding(horizontal = SahaaySpacing.lg, vertical = SahaaySpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .scale(dotScale)
                        .clip(CircleShape)
                        .background(indicatorColor.copy(alpha = 0.2f))
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(indicatorColor)
                )
            }

            Spacer(modifier = Modifier.width(SahaaySpacing.sm))

            Text(
                text = if (isListening) "Listening now..." else statusText,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}
