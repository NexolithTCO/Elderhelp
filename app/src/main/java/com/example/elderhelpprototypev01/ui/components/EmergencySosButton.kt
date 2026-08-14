package com.example.elderhelpprototypev01.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.elderhelpprototypev01.ui.localization.Localization
import com.example.elderhelpprototypev01.ui.theme.SahaayCorners
import com.example.elderhelpprototypev01.ui.theme.SahaayElevation
import com.example.elderhelpprototypev01.ui.theme.SahaaySpacing
import com.example.elderhelpprototypev01.ui.theme.SahaayTouchTarget
import kotlinx.coroutines.delay

/**
 * Emergency SOS Button
 * High-contrast emergency action control for elderly safety.
 */
@Composable
fun EmergencySosButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    currentLanguage: String = "English (India)"
) {
    val strings = Localization.getStrings(currentLanguage)

    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(SahaayTouchTarget.preferred)
            .shadow(elevation = SahaayElevation.medium, shape = RoundedCornerShape(SahaayCorners.medium)),
        shape = RoundedCornerShape(SahaayCorners.medium),
        color = MaterialTheme.colorScheme.error,
        contentColor = MaterialTheme.colorScheme.onError
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = SahaaySpacing.xl),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Emergency Siren",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(SahaaySpacing.md))

            Text(
                text = strings.emergencySos.uppercase(),
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            )
        }
    }
}

/**
 * Emergency SOS Safety Guardrail Modal
 */
@Composable
fun EmergencySosModal(
    onDismiss: () -> Unit,
    onEmergencyTriggered: () -> Unit,
    contactName: String = "Rahul",
    contactNumber: String = "+91 98765 43210",
    currentLanguage: String = "English (India)"
) {
    val context = LocalContext.current
    var countdown by remember { mutableIntStateOf(3) }
    val strings = Localization.getStrings(currentLanguage)

    LaunchedEffect(countdown) {
        if (countdown > 0) {
            delay(1000L)
            countdown -= 1
        } else {
            triggerEmergencyCall(context, contactNumber)
            onEmergencyTriggered()
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f))
                .padding(SahaaySpacing.xxl),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .border(2.dp, MaterialTheme.colorScheme.error, RoundedCornerShape(SahaayCorners.extraLarge)),
                shape = RoundedCornerShape(SahaayCorners.extraLarge),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = SahaayElevation.high
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(SahaaySpacing.xxl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Emergency Alert",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(SahaaySpacing.lg))

                    Text(
                        text = "EMERGENCY ALERT",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(SahaaySpacing.sm))

                    Surface(
                        shape = RoundedCornerShape(SahaayCorners.small),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = SahaaySpacing.lg, vertical = SahaaySpacing.md),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhoneInTalk,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(SahaaySpacing.sm))
                            Text(
                                text = "$contactName • $contactNumber",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(SahaaySpacing.xl))

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(88.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.error)
                    ) {
                        Text(
                            text = "$countdown",
                            style = MaterialTheme.typography.displayLarge.copy(
                                color = MaterialTheme.colorScheme.onError,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(SahaaySpacing.md))

                    Text(
                        text = "Calling in $countdown...",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(SahaaySpacing.xxl))

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(SahaayTouchTarget.preferred),
                        shape = RoundedCornerShape(SahaayCorners.medium),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel",
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(SahaaySpacing.sm))
                            Text(
                                text = strings.cancel.uppercase(),
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun triggerEmergencyCall(context: Context, phoneNumber: String) {
    try {
        val cleanNumber = phoneNumber.replace(" ", "").replace("-", "")
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$cleanNumber")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
        Toast.makeText(context, "Dialing Emergency Contact ($cleanNumber)...", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Unable to launch dialer: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
