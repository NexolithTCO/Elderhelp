package com.example.elderhelpprototypev01.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * ClarificationCard
 *
 * Displayed when the Voice Interaction Engine enters a [WaitingForClarification]
 * state — i.e., the LLM determined that the user's request was too ambiguous
 * to answer and needs a single follow-up question.
 *
 * Design choices for elderly/low-literacy users:
 * - Large question text (20sp) for readability.
 * - Warm amber background — visually distinct from the main response card
 *   but not alarming like a red error card.
 * - Prominent mic button inviting the user to answer immediately.
 * - "Tap the mic to answer" helper text in smaller type below.
 *
 * @param question      The clarifying question to display and speak.
 * @param onMicClick    Called when the user taps the mic button to respond.
 * @param modifier      Optional layout modifier.
 */
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
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFFFFF8E1),   // warm amber tint
            shadowElevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {

                // Label row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🤔",
                        fontSize = 22.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ek sawaal",   // "One question" in Hindi/Hinglish
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE65100),
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // The clarifying question — large for readability
                Text(
                    text = question,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF4E342E),
                    lineHeight = 28.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Mic button to respond
                Button(
                    onClick = onMicClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE65100),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Answer",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Jawab do   (Tap to answer)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Mic button dabao aur bolo.",
                    fontSize = 13.sp,
                    color = Color(0xFF795548)
                )
            }
        }
    }
}
