package com.example.elderhelpprototypev01.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.elderhelpprototypev01.ui.theme.SahaayCorners
import com.example.elderhelpprototypev01.ui.theme.SahaayElevation
import com.example.elderhelpprototypev01.ui.theme.SahaaySpacing
import com.example.elderhelpprototypev01.ui.theme.SahaayTouchTarget

@Composable
fun ClarificationCard(
    question: String,
    onMicClick: () -> Unit,
    modifier: Modifier = Modifier,
    visible: Boolean = true
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 })
    ) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(SahaayCorners.large),
            color = MaterialTheme.colorScheme.secondaryContainer,
            shadowElevation = SahaayElevation.medium
        ) {
            Column(modifier = Modifier.padding(SahaaySpacing.xl)) {

                // Label row with Material Icon
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                        contentDescription = "Clarification needed",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(SahaaySpacing.sm))
                    Text(
                        text = "Quick Question",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(SahaaySpacing.md))

                // Question text
                Text(
                    text = question,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                )

                Spacer(modifier = Modifier.height(SahaaySpacing.xl))

                // Mic action button to answer
                Button(
                    onClick = onMicClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(SahaayTouchTarget.preferred),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    ),
                    shape = RoundedCornerShape(SahaayCorners.small)
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Answer",
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(SahaaySpacing.sm))
                    Text(
                        text = "Tap to answer",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = Modifier.height(SahaaySpacing.xs))

                Text(
                    text = "Press the button and speak your answer.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )
                )
            }
        }
    }
}
