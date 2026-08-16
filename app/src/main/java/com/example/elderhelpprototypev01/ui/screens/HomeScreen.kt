package com.example.elderhelpprototypev01.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.elderhelpprototypev01.R
import com.example.elderhelpprototypev01.SahaayViewModel
import com.example.elderhelpprototypev01.ui.components.*
import com.example.elderhelpprototypev01.ui.demo.CareBookDemoScreen
import com.example.elderhelpprototypev01.ui.demo.PayBillsDemoScreen
import com.example.elderhelpprototypev01.ui.localization.Localization
import com.example.elderhelpprototypev01.ui.theme.*
import com.example.elderhelpprototypev01.ui.voice.VoiceScreen
import java.util.Calendar

@Composable
fun SahaayHomeScreen(
    modifier: Modifier = Modifier,
    overlayRefreshTick: Int = 0,
    viewModel: SahaayViewModel? = null,
    initialTab: Int = 0,
    openSosModalOnLaunch: Boolean = false
) {
    val context = LocalContext.current

    var currentLanguage by rememberSaveable { mutableStateOf("English (India)") }
    var selectedTab by rememberSaveable { mutableIntStateOf(initialTab) }
    var isSosModalOpen by remember { mutableStateOf(openSosModalOnLaunch) }
    var activeServiceDemo by rememberSaveable { mutableIntStateOf(0) } // 0: CareBook Doctor, 1: Pay Bills
    var showProfile by rememberSaveable { mutableStateOf(false) }
    var showHelp by rememberSaveable { mutableStateOf(false) }
    var showForms by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(openSosModalOnLaunch) {
        if (openSosModalOnLaunch) {
            selectedTab = 0
            isSosModalOpen = true
        }
    }

    LaunchedEffect(currentLanguage) {
        viewModel?.setLanguage(currentLanguage)
    }

    val scrollState = rememberScrollState()
    val strings = Localization.getStrings(currentLanguage)

    // Dynamic greeting based on time of day
    val greetingText = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 4..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..21 -> "Good evening"
            else -> "Hello"
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedTab = selectedTab,
                currentLanguage = currentLanguage,
                onTabSelected = { index ->
                    selectedTab = index
                    showProfile = false
                    showHelp = false
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
                    // Voice Assistant Tab
                    if (viewModel != null) {
                        VoiceScreen(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Voice Assistant",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
                2 -> {
                    // Services Tab (Doctor Booking, Pension Form, Pay Bills)
                    Column(modifier = Modifier.fillMaxSize()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            tonalElevation = SahaayElevation.low
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = SahaaySpacing.md, vertical = SahaaySpacing.sm),
                                horizontalArrangement = Arrangement.spacedBy(SahaaySpacing.xs)
                            ) {
                                FilterChip(
                                    selected = activeServiceDemo == 0,
                                    onClick = { activeServiceDemo = 0 },
                                    label = {
                                        Text(
                                            "Doctor",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = activeServiceDemo == 1,
                                    onClick = { activeServiceDemo = 1 },
                                    label = {
                                        Text(
                                            "Pension Form",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.tertiary,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = activeServiceDemo == 2,
                                    onClick = { activeServiceDemo = 2 },
                                    label = {
                                        Text(
                                            "Pay Bills",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SahaaySuccess,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        when (activeServiceDemo) {
                            0 -> {
                                CareBookDemoScreen(
                                    onVoiceCommandRequest = { cmd ->
                                        viewModel?.analyzeCurrentScreenAndHighlight(cmd)
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            1 -> {
                                PensionFormScreen(
                                    onBack = { selectedTab = 0 },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            else -> {
                                if (viewModel != null) {
                                    PaymentAssistanceMvpScreen(
                                        viewModel = viewModel,
                                        currentLanguage = currentLanguage,
                                        onBackToHome = { selectedTab = 0 },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    PayBillsDemoScreen(
                                        onVoiceCommandRequest = { cmd ->
                                            viewModel?.analyzeCurrentScreenAndHighlight(cmd)
                                        },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                }
                3 -> {
                    // Settings Tab
                    SettingsScreen(
                        currentLanguage = currentLanguage,
                        onLanguageChange = { newLang ->
                            currentLanguage = newLang
                        }
                    )
                }
                else -> {
                    // Home Screen
                    if (showProfile) {
                        BackHandler { showProfile = false }
                        ProfileScreen(modifier = Modifier.fillMaxSize())
                    } else if (showHelp) {
                        BackHandler { showHelp = false }
                        HelpScreen(
                            onNavigateToPayments = { selectedTab = 2; activeServiceDemo = 2; showHelp = false },
                            onNavigateToForms = { showForms = true; showHelp = false },
                            onNavigateToDoctor = { selectedTab = 2; activeServiceDemo = 0; showHelp = false },
                            onNavigateToProfile = { showProfile = true; showHelp = false },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else if (showForms) {
                        BackHandler { showForms = false }
                        PensionFormScreen(
                            onBack = { showForms = false },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(scrollState)
                                    .padding(horizontal = SahaaySpacing.lg)
                                    .padding(top = SahaaySpacing.lg, bottom = 96.dp),
                                horizontalAlignment = Alignment.Start,
                                verticalArrangement = Arrangement.Top
                            ) {
                            // Top Header: Pixel-aligned Greeting & Brand Logo
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = greetingText,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "I'm Sahaay",
                                        style = MaterialTheme.typography.displayLarge.copy(
                                            color = MaterialTheme.colorScheme.onBackground,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "How can I help you today?",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }

                                Image(
                                    painter = painterResource(id = R.drawable.ic_sahaay_logo),
                                    contentDescription = "Sahaay Logo",
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .size(48.dp)
                                        .clip(CircleShape)
                                )

                                Surface(
                                    onClick = { showProfile = true },
                                    shape = RoundedCornerShape(SahaayCorners.medium),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = SahaaySpacing.sm, vertical = SahaaySpacing.xs),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = "My Profile",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Profile",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(SahaaySpacing.xl))

                            // Emergency SOS Button
                            EmergencySosButton(
                                onClick = { isSosModalOpen = true },
                                currentLanguage = currentLanguage,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(SahaaySpacing.xl))

                            // Hero Microphone Button
                            MicrophoneButton(
                                isListening = false,
                                currentLanguage = currentLanguage,
                                onClick = {
                                    selectedTab = 1 // Switch to Voice tab
                                },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(SahaaySpacing.xl))

                            // Quick Tasks Section (Doctor, Bills, Forms, Help)
                            QuickActionsSection(
                                currentLanguage = currentLanguage,
                                onActionClick = { action ->
                                    val id = action.id.lowercase()
                                    val title = action.title.lowercase()
                                    if (id == "bills" || title.contains("bill") || title.contains("electricity") || title.contains("बिल")) {
                                        selectedTab = 2
                                        activeServiceDemo = 1
                                    } else if (id == "doctor" || title.contains("doctor") || title.contains("appointment") || title.contains("डाक्टर")) {
                                        selectedTab = 2
                                        activeServiceDemo = 0
                                    } else if (id == "forms" || title.contains("form") || title.contains("फॉर्म")) {
                                        showForms = true
                                    } else if (id == "help" || title.contains("help") || title.contains("मदद")) {
                                        showHelp = true
                                    } else {
                                        viewModel?.analyzeCurrentScreenAndHighlight(action.title)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(SahaaySpacing.xl))

                            // Sahaay Overlay Assistant Card
                            OverlayToggleCard(
                                modifier = Modifier.fillMaxWidth(),
                                refreshTick = overlayRefreshTick
                            )
                        }

                        if (isSosModalOpen) {
                            EmergencySosModal(
                                onDismiss = { isSosModalOpen = false },
                                onEmergencyTriggered = { isSosModalOpen = false },
                                currentLanguage = currentLanguage
                            )
                        }
                    }
                }
            }
        }
    }
}
}
