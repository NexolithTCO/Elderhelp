package com.example.elderhelpprototypev01.ui.screens

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.elderhelpprototypev01.SahaayViewModel
import com.example.elderhelpprototypev01.model.FormSchema
import com.example.elderhelpprototypev01.model.VoiceState
import com.example.elderhelpprototypev01.ui.components.FormFieldCard
import com.example.elderhelpprototypev01.ui.theme.*

/**
 * FormFillingScreen
 *
 * Elderly-friendly Voice-to-Form Filling screen:
 * - Displays active form title
 * - Renders FormFieldCard for every field in activeForm
 * - Connects to SahaayViewModel STT + Gemini form field extraction
 * - Provides large microphone trigger, clear button, and submit action
 */
@Composable
fun FormFillingScreen(
    viewModel: SahaayViewModel,
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val activeForm by viewModel.activeForm.collectAsStateWithLifecycle()
    val formValues by viewModel.formValues.collectAsStateWithLifecycle()
    val isExtractingForm by viewModel.isExtractingForm.collectAsStateWithLifecycle()
    val voiceState by viewModel.voiceState.collectAsStateWithLifecycle()
    val transcript by viewModel.transcript.collectAsStateWithLifecycle()

    val isListening = voiceState is VoiceState.Listening || voiceState is VoiceState.PartialResult

    // Permission launcher for microphone access
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.startListening()
        } else {
            Toast.makeText(context, "Microphone permission is required for voice filling.", Toast.LENGTH_LONG).show()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppleCanvasBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp)
                .padding(top = 16.dp, bottom = 24.dp)
        ) {
            // ---- Top Header Row ----
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        viewModel.clearForm()
                        onBackClick()
                    },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = AppleTextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = activeForm?.title ?: "Government Form",
                        style = Typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = AppleTextPrimary
                        )
                    )
                    Text(
                        text = "Speak or type details to fill the form",
                        style = Typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            color = AppleTextMuted
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ---- Voice Input Trigger Section ----
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = AppleSurfaceWhite,
                shadowElevation = 3.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = when {
                            isExtractingForm -> "Analyzing speech..."
                            isListening -> "Listening carefully... Speak now"
                            else -> "Tap microphone to speak details"
                        },
                        style = Typography.bodyLarge.copy(
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isListening) AppleBlue else if (isExtractingForm) Color(0xFFFF9500) else AppleTextPrimary,
                            textAlign = TextAlign.Center
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Large Elderly Mic Button
                    Button(
                        onClick = {
                            if (isListening) {
                                viewModel.stopListening()
                            } else {
                                if (viewModel.hasMicPermission()) {
                                    viewModel.startListening()
                                } else {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                        },
                        modifier = Modifier.size(80.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isListening) Color(0xFFFF3B30) else AppleBlue
                        ),
                        elevation = ButtonDefaults.buttonElevation(6.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        if (isExtractingForm) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                color = Color.White,
                                strokeWidth = 3.dp
                            )
                        } else {
                            Icon(
                                imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = "Voice Fill",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    // Spoken transcript preview
                    if (transcript.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "\u201C$transcript\u201D",
                            style = Typography.bodyMedium.copy(
                                fontSize = 15.sp,
                                color = AppleTextSecondary,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                }
            }

            // ---- Loading / Extracting Banner ----
            AnimatedVisibility(
                visible = isExtractingForm,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFFFF4E5)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color(0xFFFF9500),
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Sahaay is extracting form fields from your speech...",
                                style = Typography.bodyMedium.copy(
                                    color = Color(0xFFB76E00),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ---- Form Fields List ----
            val fields = activeForm?.fields ?: emptyList()

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(fields, key = { it.id }) { field ->
                    val value = formValues[field.id] ?: ""
                    FormFieldCard(
                        field = field,
                        value = value,
                        onValueChange = { newValue ->
                            viewModel.updateFormField(field.id, newValue)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ---- Action Buttons Row ----
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Clear Form Button
                OutlinedButton(
                    onClick = {
                        viewModel.clearForm()
                        Toast.makeText(context, "Form cleared", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, AppleBorderSubtle)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Clear",
                        tint = AppleTextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Clear",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppleTextMuted
                    )
                }

                // Submit Form Button
                Button(
                    onClick = {
                        Toast.makeText(context, "Form Submitted Successfully! 🎉", Toast.LENGTH_LONG).show()
                        viewModel.clearForm()
                        onBackClick()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppleBlue)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Submit",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Submit",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
