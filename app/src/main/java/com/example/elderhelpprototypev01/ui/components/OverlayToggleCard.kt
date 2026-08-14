package com.example.elderhelpprototypev01.ui.components

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.ShieldMoon
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.elderhelpprototypev01.overlay.OverlayPermissionManager
import com.example.elderhelpprototypev01.overlay.SahaayOverlayService
import com.example.elderhelpprototypev01.overlay.SahaayPreferences
import com.example.elderhelpprototypev01.ui.theme.*

@Composable
fun OverlayToggleCard(
    modifier: Modifier = Modifier,
    refreshTick: Int = 0
) {
    val context = LocalContext.current

    val hasPermission by remember(refreshTick) {
        mutableStateOf(OverlayPermissionManager.canDrawOverlays(context))
    }
    var overlayEnabled by remember(refreshTick) {
        mutableStateOf(
            hasPermission && SahaayPreferences.isOverlayEnabled(context)
        )
    }

    val accentColor by animateColorAsState(
        targetValue = if (overlayEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
        animationSpec = spring(),
        label = "accentColor"
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SahaayCorners.medium),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shadowElevation = SahaayElevation.low
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SahaaySpacing.lg)
        ) {
            // Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Sahaay Floating Assistant",
                        tint = accentColor,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(SahaaySpacing.md))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Sahaay Floating Assistant",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (overlayEnabled) "Floating assistant is ON" else "Floating assistant is OFF",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = accentColor,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (hasPermission) {
                    Switch(
                        checked = overlayEnabled,
                        onCheckedChange = { enabled ->
                            overlayEnabled = enabled
                            SahaayPreferences.setOverlayEnabled(context, enabled)
                            if (enabled) {
                                startOverlayService(context)
                            } else {
                                stopOverlayService(context)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(SahaaySpacing.md))

            if (!hasPermission) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(SahaayCorners.medium),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(SahaaySpacing.lg)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ShieldMoon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(SahaaySpacing.sm))
                            Text(
                                text = "Permission required for floating button",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(SahaaySpacing.xs))
                        Text(
                            text = "To show the floating Sahaay assistant on top of other apps, tap below and allow \"Display over other apps\".",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        )
                        Spacer(modifier = Modifier.height(SahaaySpacing.md))
                        Button(
                            onClick = {
                                val intent = OverlayPermissionManager.buildPermissionSettingsIntent(context)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor = MaterialTheme.colorScheme.onSecondary
                            ),
                            shape = RoundedCornerShape(SahaayCorners.medium),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(SahaayTouchTarget.preferred)
                        ) {
                            Text(
                                "Grant Permission in Settings",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            } else {
                // Feature hint pills with clear, senior-readable typography
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(SahaaySpacing.sm)
                ) {
                    val hints = listOf(
                        HintItem("Voice", Icons.Default.Mic),
                        HintItem("Screen", Icons.Default.Visibility),
                        HintItem("Explain", Icons.Default.Lightbulb),
                        HintItem("Help", Icons.AutoMirrored.Filled.HelpOutline)
                    )
                    hints.forEach { hint ->
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(SahaayCorners.small),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = SahaaySpacing.xs, vertical = SahaaySpacing.sm),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = hint.icon,
                                    contentDescription = hint.label,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = hint.label,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class HintItem(val label: String, val icon: ImageVector)

private fun startOverlayService(context: Context) {
    val intent = SahaayOverlayService.startIntent(context)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(intent)
    } else {
        context.startService(intent)
    }
}

private fun stopOverlayService(context: Context) {
    context.stopService(SahaayOverlayService.stopIntent(context))
}
