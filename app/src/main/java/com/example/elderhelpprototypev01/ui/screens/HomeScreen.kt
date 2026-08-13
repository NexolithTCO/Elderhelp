package com.example.elderhelpprototypev01.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.elderhelpprototypev01.SahaayViewModel
import com.example.elderhelpprototypev01.ui.components.*
import com.example.elderhelpprototypev01.ui.localization.Localization
import com.example.elderhelpprototypev01.ui.theme.*
import com.example.elderhelpprototypev01.ui.voice.VoiceScreen

@Composable
fun SahaayHomeScreen(
    modifier: Modifier = Modifier,
    overlayRefreshTick: Int = 0,
    viewModel: SahaayViewModel? = null,
    initialTab: Int = 0
) {
    val context = LocalContext.current

    // Hoisted persistent state across tab switches and recompositions
    // Default language is English — user can change in Settings
    var currentLanguage by rememberSaveable { mutableStateOf("English (India)") }
    var isListening by remember { mutableStateOf(false) }
    var activeMessage by remember { mutableStateOf<String?>(null) }
    var selectedTab by rememberSaveable { mutableIntStateOf(initialTab) }
    var isSosModalOpen by remember { mutableStateOf(false) }

    // Sync language preference to ViewModel whenever it changes
    LaunchedEffect(currentLanguage) {
        viewModel?.setLanguage(currentLanguage)
    }

    val scrollState = rememberScrollState()
    val strings = Localization.getStrings(currentLanguage)

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedTab = selectedTab,
                currentLanguage = currentLanguage,
                onTabSelected = { index ->
                    selectedTab = index
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Crossfade(
            targetState = selectedTab,
            label = "tabCrossfade",
            modifier = Modifier.padding(innerPadding)
        ) { tabIndex ->
            when (tabIndex) {
                1 -> {
                    // Tab Index 1: Voice Assistant Screen
                    if (viewModel != null) {
                        VoiceScreen(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Preview fallback
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🎙️ Voice Assistant",
                                style = Typography.headlineMedium.copy(color = AppleTextMuted)
                            )
                        }
                    }
                }
                3 -> {
                    // Tab Index 3: Settings Screen (uses hoisted currentLanguage & callback)
                    SettingsScreen(
                        currentLanguage = currentLanguage,
                        onLanguageChange = { newLang ->
                            currentLanguage = newLang
                        }
                    )
                }
                else -> {
                    // Main Home Screen (Uses localized strings everywhere)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(AppleCanvasBg)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState)
                                .padding(horizontal = 18.dp)
                                .padding(top = 16.dp, bottom = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Top
                        ) {
                            // Top Header Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Profile Avatar + App Name & Localized Subtitle
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(AppleBlueLight)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = "Profile",
                                            tint = AppleBlue,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = "ElderhelpV0.1",
                                            style = Typography.titleLarge.copy(
                                                fontSize = 22.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = AppleTextPrimary
                                            )
                                        )
                                        Text(
                                            text = strings.appSubtitle,
                                            style = Typography.bodyMedium.copy(
                                                fontSize = 13.sp,
                                                color = AppleTextMuted
                                            )
                                        )
                                    }
                                }

                                // Search & Notifications
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = {
                                        Toast.makeText(context, "Search clicked", Toast.LENGTH_SHORT).show()
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = "Search",
                                            tint = AppleTextPrimary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    IconButton(onClick = {
                                        Toast.makeText(context, "Notifications clicked", Toast.LENGTH_SHORT).show()
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Notifications,
                                            contentDescription = "Notifications",
                                            tint = AppleTextPrimary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Localized Status Tag Chip
                            StatusCard(
                                isListening = isListening,
                                statusText = strings.statusText,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            // Localized Hero Circular Voice Assistant Banner
                            MicrophoneButton(
                                isListening = isListening,
                                currentLanguage = currentLanguage,
                                onClick = {
                                    isListening = !isListening
                                    activeMessage = if (isListening) {
                                        "${strings.listeningText}"
                                    } else {
                                        "Voice mode paused."
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            // High-Contrast Emergency SOS Pill Button
                            EmergencySosButton(
                                onClick = {
                                    isSosModalOpen = true
                                },
                                currentLanguage = currentLanguage,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(22.dp))

                            // Localized Quick Services Grid
                            QuickActionsSection(
                                currentLanguage = currentLanguage,
                                onActionClick = { action ->
                                    activeMessage = "${action.title} selected"
                                    Toast.makeText(context, "${action.title} clicked", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Sahaay Floating Overlay Toggle Card
                            OverlayToggleCard(
                                modifier = Modifier.fillMaxWidth(),
                                refreshTick = overlayRefreshTick
                            )
                        }

                        // Active Feedback Toast / Banner
                        AnimatedVisibility(
                            visible = activeMessage != null,
                            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                            exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 16.dp, start = 20.dp, end = 20.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = AppleTextPrimary,
                                shadowElevation = 8.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = activeMessage ?: "",
                                        style = Typography.bodyLarge.copy(
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    TextButton(onClick = { activeMessage = null }) {
                                        Text(
                                            text = "OK",
                                            color = Color(0xFF64D2FF),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Emergency SOS Guardrail Countdown Modal Overlay
                        if (isSosModalOpen) {
                            EmergencySosModal(
                                onDismiss = { isSosModalOpen = false },
                                onEmergencyTriggered = {
                                    activeMessage = "Emergency call initiated to Rahul (+91 98765 43210)"
                                },
                                contactName = "Rahul",
                                contactNumber = "+91 98765 43210",
                                currentLanguage = currentLanguage
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SahaayHomeScreenPreview() {
    ElderHelpPrototypeV01Theme {
        SahaayHomeScreen()
    }
}
