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
import androidx.compose.ui.tooling.preview.Preview
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
            .background(AppleCanvasBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 18.dp)
                .padding(top = 20.dp, bottom = 32.dp)
        ) {
            // Settings Title Header
            Text(
                text = strings.settingsTitle,
                style = Typography.headlineLarge.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppleTextPrimary
                )
            )
            Text(
                text = strings.settingsSubtitle,
                style = Typography.bodyMedium.copy(
                    fontSize = 15.sp,
                    color = AppleTextMuted
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Section 1: Accessibility & Assistance
            SettingsSectionTitle(title = "ACCESSIBILITY & VOICE")

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AppleBorderSubtle, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                color = AppleSurfaceWhite,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
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
                    HorizontalDivider(color = AppleBorderSubtle, thickness = 0.5.dp)
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
                    HorizontalDivider(color = AppleBorderSubtle, thickness = 0.5.dp)
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
                    HorizontalDivider(color = AppleBorderSubtle, thickness = 0.5.dp)
                    val isAccEnabled = remember { com.example.elderhelpprototypev01.accessibility.SahaayAccessibilityService.isServiceEnabled(context) }
                    SettingsNavigationRow(
                        title = "Sahaay Screen Inspector",
                        value = if (isAccEnabled) "ON • Ready to highlight options" else "OFF • Tap to enable in Settings",
                        icon = Icons.Default.Visibility,
                        iconTint = if (isAccEnabled) Color(0xFF34C759) else Color(0xFFFF9500),
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

            Spacer(modifier = Modifier.height(24.dp))

            // Section 2: Personal & Emergency Contacts
            SettingsSectionTitle(title = "PERSONAL & EMERGENCY")

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AppleBorderSubtle, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                color = AppleSurfaceWhite,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    SettingsNavigationRow(
                        title = "Emergency Contact",
                        value = "Rahul • +91 98765 43210",
                        icon = Icons.Default.ContactPhone,
                        iconTint = Color(0xFFFF3B30),
                        onClick = {
                            Toast.makeText(context, "Emergency Contact clicked", Toast.LENGTH_SHORT).show()
                        }
                    )
                    HorizontalDivider(color = AppleBorderSubtle, thickness = 0.5.dp)
                    SettingsNavigationRow(
                        title = strings.prefLanguageTitle,
                        value = currentLanguage,
                        icon = Icons.Default.Language,
                        iconTint = AppleBlue,
                        onClick = {
                            showLanguageDialog = true
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section 3: App Information & Support
            SettingsSectionTitle(title = "SUPPORT & ABOUT")

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AppleBorderSubtle, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                color = AppleSurfaceWhite,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    SettingsNavigationRow(
                        title = "How to Use (Tutorial Video)",
                        value = "Play quick 1-min guide",
                        icon = Icons.AutoMirrored.Filled.Help,
                        iconTint = Color(0xFFAF52DE),
                        onClick = {
                            Toast.makeText(context, "Tutorial video clicked", Toast.LENGTH_SHORT).show()
                        }
                    )
                    HorizontalDivider(color = AppleBorderSubtle, thickness = 0.5.dp)
                    SettingsNavigationRow(
                        title = "About ElderhelpV0.1",
                        value = "V0.1.0 • Hackathon Prototype",
                        icon = Icons.Default.Info,
                        iconTint = Color(0xFF34C759),
                        onClick = {
                            Toast.makeText(context, "ElderhelpV0.1 Hackathon Prototype", Toast.LENGTH_SHORT).show()
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
                        style = Typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = AppleTextPrimary
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
                                    .height(48.dp)
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
                                    colors = RadioButtonDefaults.colors(selectedColor = AppleBlue)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = language,
                                    style = Typography.bodyLarge.copy(
                                        fontSize = 17.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) AppleBlue else AppleTextPrimary
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
                            color = AppleBlue,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                shape = RoundedCornerShape(24.dp),
                containerColor = AppleSurfaceWhite
            )
        }
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = Typography.labelMedium.copy(
            color = AppleTextMuted,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            fontSize = 12.sp
        ),
        modifier = Modifier.padding(start = 6.dp, bottom = 8.dp)
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
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(AppleBlueLight)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppleBlue,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = Typography.titleMedium.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppleTextPrimary
                )
            )
            Text(
                text = subtitle,
                style = Typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    color = AppleTextMuted
                )
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = AppleBlue,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFE5E5EA)
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
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconTint.copy(alpha = 0.12f))
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = Typography.titleMedium.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppleTextPrimary
                )
            )
            Text(
                text = value,
                style = Typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    color = AppleTextMuted
                )
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = AppleTextMuted,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    ElderHelpPrototypeV01Theme {
        SettingsScreen(
            currentLanguage = "Hindi (हिंदी)",
            onLanguageChange = {}
        )
    }
}
