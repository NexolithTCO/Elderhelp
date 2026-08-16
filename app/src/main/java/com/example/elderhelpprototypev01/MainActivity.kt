package com.example.elderhelpprototypev01

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.elderhelpprototypev01.overlay.OverlayPermissionManager
import com.example.elderhelpprototypev01.overlay.SahaayOverlayService
import com.example.elderhelpprototypev01.overlay.SahaayPreferences
import com.example.elderhelpprototypev01.ui.screens.SahaayHomeScreen
import com.example.elderhelpprototypev01.ui.theme.ElderHelpPrototypeV01Theme

class MainActivity : ComponentActivity() {

    companion object {
        /** Intent extra: open the voice tab directly (used by overlay Voice button) */
        const val EXTRA_OPEN_VOICE_TAB = "open_voice_tab"
        /** Intent extra: start voice listening immediately in activity context */
        const val EXTRA_START_VOICE_LISTENING = "start_voice_listening"
        /** Intent extra: trigger immediate screen analysis (used by overlay Screen/Explain/Help buttons) */
        const val EXTRA_ANALYZE_SCREEN = "analyze_screen"
        /** Intent extra: trigger emergency SOS button redirect directly from overlay */
        const val EXTRA_TRIGGER_SOS = "trigger_sos"
    }

    // ViewModel owned at Activity scope — survives tab switches
    private val sahaayViewModel: SahaayViewModel by viewModels()

    // Incremented every onResume so Compose re-checks overlay permission & state
    private var overlayRefreshTick by mutableIntStateOf(0)

    // Which tab to open (can be set by overlay intent)
    private var initialTab by mutableIntStateOf(0)

    // Track whether overlay requested immediate SOS modal trigger
    private var openSosModalOnLaunch by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize centralized ProfileRepository
        com.example.elderhelpprototypev01.profile.ProfileRepository.init(applicationContext)

        // Handle intent extras from overlay
        handleIncomingIntents(intent)

        setContent {
            ElderHelpPrototypeV01Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SahaayHomeScreen(
                        modifier = Modifier.padding(innerPadding),
                        overlayRefreshTick = overlayRefreshTick,
                        viewModel = sahaayViewModel,
                        initialTab = initialTab,
                        openSosModalOnLaunch = openSosModalOnLaunch
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntents(intent)
    }

    override fun onResume() {
        super.onResume()
        overlayRefreshTick++

        if (OverlayPermissionManager.canDrawOverlays(this) &&
            SahaayPreferences.isOverlayEnabled(this)
        ) {
            startOverlayService()
        }
    }

    private fun handleIncomingIntents(intent: Intent?) {
        if (intent == null) return

        if (intent.getBooleanExtra(EXTRA_TRIGGER_SOS, false)) {
            initialTab = 0
            openSosModalOnLaunch = true
        }

        if (intent.getBooleanExtra(EXTRA_OPEN_VOICE_TAB, false) || intent.getBooleanExtra(EXTRA_START_VOICE_LISTENING, false)) {
            initialTab = 1
            if (intent.getBooleanExtra(EXTRA_START_VOICE_LISTENING, false)) {
                sahaayViewModel.startListening()
            }
        }

        val analyzeCmd = intent.getStringExtra(EXTRA_ANALYZE_SCREEN)
        if (!analyzeCmd.isNullOrBlank()) {
            initialTab = 1 // Switch to Voice/Screen tab to show response
            sahaayViewModel.analyzeCurrentScreenAndHighlight(analyzeCmd)
        }
    }

    private fun startOverlayService() {
        val serviceIntent = SahaayOverlayService.startIntent(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SahaayHomeScreenMainPreview() {
    ElderHelpPrototypeV01Theme {
        SahaayHomeScreen()
    }
}