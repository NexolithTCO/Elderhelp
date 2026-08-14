package com.example.elderhelpprototypev01.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.elderhelpprototypev01.ui.localization.Localization
import com.example.elderhelpprototypev01.ui.theme.*

@Composable
fun SettingsScreen(
    currentLanguage: String,
    onLanguageChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val strings = Localization.getStrings(currentLanguage)

    var highContrastText by remember { mutableStateOf(true) }
    var voiceFeedback by remember { mutableStateOf(true) }
    var simpleMode by remember { mutableStateOf(false) }

    var showLanguageDialog by remember { mutableStateOf(false) }

    val languages = listOf(
        "English (India)",
        "Hindi (हिंदी)",
        "Marathi (मराठी)",
        "Tamil (தமிழ்)",
        "Telugu (తెలుగు)",
        "Bengali (বাংলা)"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = SahaaySpacing.lg)
                .padding(top = SahaaySpacing.lg, bottom = SahaaySpacing.xxxl)
        ) {
            // Settings Title Header
            Text(
                text = strings.settingsTitle,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
            Text(
                text = strings.settingsSubtitle,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(SahaaySpacing.xl))

            // Section 1: Accessibility & Voice
            SettingsSectionTitle(title = "ACCESSIBILITY & VOICE")

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(SahaayCorners.medium)),
                shape = RoundedCornerShape(SahaayCorners.medium),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = SahaayElevation.low
            ) {
                Column(modifier = Modifier.padding(horizontal = SahaaySpacing.lg, vertical = SahaaySpacing.xs)) {
                    SettingsSwitchRow(
                        title = "Large Readability Fonts",
                        subtitle = "Increased font contrast for easy reading",
                        icon = Icons.Default.FormatSize,
                        checked = highContrastText,
                        onCheckedChange = {
                            highContrastText = it
                            Toast.makeText(context, "Readability updated", Toast.LENGTH_SHORT).show()
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                    SettingsSwitchRow(
                        title = "Voice Speech Feedback",
                        subtitle = "Speak aloud button actions & confirmations",
                        icon = Icons.AutoMirrored.Filled.VolumeUp,
                        checked = voiceFeedback,
                        onCheckedChange = {
                            voiceFeedback = it
                            Toast.makeText(context, "Voice feedback updated", Toast.LENGTH_SHORT).show()
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                    SettingsSwitchRow(
                        title = "Simplified Easy Mode",
                        subtitle = "Hide extra options & enlarge touch icons",
                        icon = Icons.Default.TouchApp,
                        checked = simpleMode,
                        onCheckedChange = {
                            simpleMode = it
                            Toast.makeText(context, "Simple mode toggled", Toast.LENGTH_SHORT).show()
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                    val isAccEnabled = remember { com.example.elderhelpprototypev01.accessibility.SahaayAccessibilityService.isServiceEnabled(context) }
                    SettingsNavigationRow(
                        title = "Sahaay Screen Inspector",
                        value = if (isAccEnabled) "ON • Ready to highlight options" else "OFF • Tap to enable in Settings",
                        icon = Icons.Default.Visibility,
                        iconTint = if (isAccEnabled) SahaaySuccess else SahaayWarning,
                        onClick = {
                            try {
                                val intent = android.content.Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Please open Android Settings -> Accessibility", Toast.LENGTH_LONG).show()
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(SahaaySpacing.xxl))

            // Section 2: Personal & Emergency Contacts
            SettingsSectionTitle(title = "PERSONAL & EMERGENCY")

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(SahaayCorners.medium)),
                shape = RoundedCornerShape(SahaayCorners.medium),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = SahaayElevation.low
            ) {
                Column(modifier = Modifier.padding(horizontal = SahaaySpacing.lg, vertical = SahaaySpacing.xs)) {
                    SettingsNavigationRow(
                        title = "Emergency Contact",
                        value = "Rahul • +91 98765 43210",
                        icon = Icons.Default.ContactPhone,
                        iconTint = MaterialTheme.colorScheme.error,
                        onClick = {
                            Toast.makeText(context, "Emergency Contact clicked", Toast.LENGTH_SHORT).show()
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                    SettingsNavigationRow(
                        title = strings.prefLanguageTitle,
                        value = currentLanguage,
                        icon = Icons.Default.Language,
                        iconTint = MaterialTheme.colorScheme.primary,
                        onClick = {
                            showLanguageDialog = true
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(SahaaySpacing.xxl))

            // Section 3: App Information & Support
            SettingsSectionTitle(title = "SUPPORT & ABOUT")

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(SahaayCorners.medium)),
                shape = RoundedCornerShape(SahaayCorners.medium),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = SahaayElevation.low
            ) {
                Column(modifier = Modifier.padding(horizontal = SahaaySpacing.lg, vertical = SahaaySpacing.xs)) {
                    SettingsNavigationRow(
                        title = "How to Use (Guide Video)",
                        value = "Play quick 1-min guide",
                        icon = Icons.AutoMirrored.Filled.Help,
                        iconTint = SahaayHelpIcon,
                        onClick = {
                            Toast.makeText(context, "Tutorial guide clicked", Toast.LENGTH_SHORT).show()
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                    SettingsNavigationRow(
                        title = "About Sahaay Companion",
                        value = "v1.0 • Accessibility Companion",
                        icon = Icons.Default.Info,
                        iconTint = SahaaySuccess,
                        onClick = {
                            Toast.makeText(context, "Sahaay Accessibility Companion v1.0", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }

        // Language Selection Dialog
        if (showLanguageDialog) {
            AlertDialog(
                onDismissRequest = { showLanguageDialog = false },
                title = {
                    Text(
                        text = "Select Language",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                },
                text = {
                    Column(
                        modifier = Modifier
                            .selectableGroup()
                            .fillMaxWidth()
                    ) {
                        languages.forEach { language ->
                            val isSelected = language == currentLanguage
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(SahaayTouchTarget.minimum)
                                    .selectable(
                                        selected = isSelected,
                                        onClick = {
                                            onLanguageChange(language)
                                            showLanguageDialog = false
                                            Toast.makeText(
                                                context,
                                                "Language set to $language",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        role = Role.RadioButton
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = null,
                                    colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                                )
                                Spacer(modifier = Modifier.width(SahaaySpacing.md))
                                Text(
                                    text = language,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showLanguageDialog = false }) {
                        Text(
                            text = "Cancel",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                },
                shape = RoundedCornerShape(SahaayCorners.large),
                containerColor = MaterialTheme.colorScheme.surface
            )
        }
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium.copy(
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        ),
        modifier = Modifier.padding(start = SahaaySpacing.xs, bottom = SahaaySpacing.sm)
    )
}

@Composable
fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = SahaaySpacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(SahaayCorners.small))
                .background(MaterialTheme.colorScheme.primaryContainer)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(SahaaySpacing.md))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        Spacer(modifier = Modifier.width(SahaaySpacing.sm))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = MaterialTheme.colorScheme.outlineVariant
            )
        )
    }
}

@Composable
fun SettingsNavigationRow(
    title: String,
    value: String,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = SahaaySpacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(SahaayCorners.small))
                .background(iconTint.copy(alpha = 0.12f))
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(SahaaySpacing.md))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}
