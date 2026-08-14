package com.example.elderhelpprototypev01.ui.voice

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.elderhelpprototypev01.model.AssistantResponse
import com.example.elderhelpprototypev01.ui.theme.*

@Composable
fun ResponseCard(
    response: AssistantResponse,
    isSpeaking: Boolean,
    ttsEnabled: Boolean,
    speechRate: Float,
    onPlayClick: () -> Unit,
    onStopClick: () -> Unit,
    onRetryClick: () -> Unit,
    onToggleTts: () -> Unit,
    onSpeechRateChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SahaayCorners.large),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = SahaayElevation.medium,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(SahaaySpacing.xl)) {

            // Header: Sahaay Assistant Indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(SahaayCorners.small))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = "Sahaay Assistant",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(SahaaySpacing.md))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Sahaay",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    if (response.intent.isNotBlank() && response.intent != "GENERAL" &&
                        response.intent != "ERROR" && response.intent != "LOADING") {
                        IntentBadge(response.intent)
                    }
                }
            }

            Spacer(modifier = Modifier.height(SahaaySpacing.md))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(SahaaySpacing.md))

            // Main Response Text
            Text(
                text = response.response,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Normal
                )
            )

            // Clarifying Question Section
            if (response.needsClarification && response.clarifyingQuestion != null) {
                Spacer(modifier = Modifier.height(SahaaySpacing.md))
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(SahaayCorners.small),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(SahaaySpacing.md),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(SahaaySpacing.sm))
                        Text(
                            text = response.clarifyingQuestion,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        )
                    }
                }
            }

            // Suggested Next Step Section
            if (!response.needsClarification && response.suggestedNextStep != null) {
                Spacer(modifier = Modifier.height(SahaaySpacing.md))
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(SahaayCorners.small),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(SahaaySpacing.md)) {
                        Text(
                            text = "Next step",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(SahaaySpacing.xs))
                        Text(
                            text = response.suggestedNextStep,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }

            // Helpful Tip Section
            if (response.helpfulTip != null) {
                Spacer(modifier = Modifier.height(SahaaySpacing.md))
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = RoundedCornerShape(SahaayCorners.small),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(SahaaySpacing.md),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(SahaaySpacing.sm))
                        Column {
                            Text(
                                text = "Helpful tip",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = MaterialTheme.colorScheme.tertiary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = response.helpfulTip,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(SahaaySpacing.lg))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(SahaaySpacing.md))

            // Action Control Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SahaaySpacing.sm)
            ) {
                if (ttsEnabled) {
                    Button(
                        onClick = if (isSpeaking) onStopClick else onPlayClick,
                        modifier = Modifier.height(SahaayTouchTarget.minimum),
                        shape = RoundedCornerShape(SahaayCorners.small),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSpeaking) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = if (isSpeaking) Icons.Default.Stop else Icons.Default.VolumeUp,
                            contentDescription = if (isSpeaking) "Stop" else "Listen",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(SahaaySpacing.xs))
                        Text(
                            text = if (isSpeaking) "Stop" else "Listen",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                OutlinedButton(
                    onClick = onRetryClick,
                    modifier = Modifier.height(SahaayTouchTarget.minimum),
                    shape = RoundedCornerShape(SahaayCorners.small),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Retry",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(SahaaySpacing.xs))
                    Text(
                        text = "Retry",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                IconButton(
                    onClick = onToggleTts,
                    modifier = Modifier
                        .height(SahaayTouchTarget.minimum)
                        .width(SahaayTouchTarget.minimum)
                ) {
                    Icon(
                        imageVector = if (ttsEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = "Toggle voice",
                        tint = if (ttsEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Speech Speed Slider
            if (ttsEnabled) {
                Spacer(modifier = Modifier.height(SahaaySpacing.sm))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.SlowMotionVideo,
                        contentDescription = "Speed",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Slider(
                        value = speechRate,
                        onValueChange = onSpeechRateChange,
                        valueRange = 0.5f..1.5f,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = SahaaySpacing.sm),
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                    Text(
                        text = "%.1fx".format(speechRate),
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.width(36.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun IntentBadge(intent: String) {
    val (label, color) = when (intent) {
        "BOOK_APPOINTMENT" -> "Appointment" to SahaayDoctorIcon
        "PAY_BILL" -> "Bill Payment" to SahaayBillsIcon
        "FILL_FORM" -> "Form Guidance" to SahaayFormsIcon
        "EXPLAIN_TERM" -> "Explanation" to SahaayHelpIcon
        "EMERGENCY_HELP" -> "Emergency" to MaterialTheme.colorScheme.error
        "ASK_QUESTION" -> "Question" to SahaayPrimary
        else -> return
    }

    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(SahaayCorners.small)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = color,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(horizontal = SahaaySpacing.sm, vertical = 3.dp)
        )
    }
}
