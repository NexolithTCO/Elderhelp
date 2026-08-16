package com.example.elderhelpprototypev01.ui.demo

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.elderhelpprototypev01.ui.theme.*

import com.example.elderhelpprototypev01.profile.ProfileRepository

/**
 * CareBookDemoScreen
 *
 * Interactive demo screen implementing the doctor appointment workflow.
 * Integrates with centralized BasicProfile for non-sensitive patient details.
 */
@Composable
fun CareBookDemoScreen(
    onVoiceCommandRequest: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val savedProfile by ProfileRepository.profile.collectAsState()

    var currentStep by remember { mutableIntStateOf(1) } // 1: Select Doctor, 2: Select Date, 3: Patient Form, 4: Confirmation
    var selectedDoctor by remember { mutableStateOf<String?>(null) }
    var selectedDate by remember { mutableStateOf("August 12, 2026") }
    var patientName by remember(savedProfile.fullName) { mutableStateOf(savedProfile.fullName) }
    var patientMobile by remember(savedProfile.mobileNumber) { mutableStateOf(savedProfile.mobileNumber) }

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
            // CareBook Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(SahaayCorners.large),
                color = MaterialTheme.colorScheme.surfaceContainer,
                shadowElevation = SahaayElevation.medium
            ) {
                Row(
                    modifier = Modifier.padding(SahaaySpacing.lg),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(SahaayDoctorBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalHospital,
                            contentDescription = "CareBook Hospital Icon",
                            tint = SahaayDoctorIcon,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(SahaaySpacing.md))
                    Column {
                        Text(
                            text = "CareBook App",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Doctor Appointment Portal",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = SahaayDoctorIcon
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(SahaaySpacing.xl))

            // Interactive Demo Test Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(SahaayCorners.medium),
                color = MaterialTheme.colorScheme.primaryContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(SahaaySpacing.md)) {
                    Text(
                        text = "Try Sample Voice Commands:",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(SahaaySpacing.sm))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(SahaaySpacing.sm),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = { onVoiceCommandRequest("I want to book Dr Sharma") },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(SahaayCorners.small),
                            contentPadding = PaddingValues(horizontal = SahaaySpacing.sm, vertical = SahaaySpacing.xs),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("“Book Dr Sharma”", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        Button(
                            onClick = { onVoiceCommandRequest("What should I do next?") },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            shape = RoundedCornerShape(SahaayCorners.small),
                            contentPadding = PaddingValues(horizontal = SahaaySpacing.sm, vertical = SahaaySpacing.xs),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("“What next?”", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(SahaaySpacing.xl))

            // STEP 1: Select Doctor
            if (currentStep == 1) {
                Text(
                    text = "Select a Doctor",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                Text(
                    text = "Tap a doctor below to book your appointment",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(SahaaySpacing.lg))

                val doctors = listOf(
                    Triple("Dr Sharma", "Cardiologist • 15 yrs exp", "Available Today"),
                    Triple("Dr Patel", "General Physician • 10 yrs exp", "Available Tomorrow"),
                    Triple("Dr Khan", "Orthopedic • 12 yrs exp", "Available Aug 14")
                )

                doctors.forEach { (name, spec, status) ->
                    val isSelected = selectedDoctor == name
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = SahaaySpacing.xs)
                            .clickable {
                                selectedDoctor = name
                                currentStep = 2
                                Toast.makeText(context, "Selected $name", Toast.LENGTH_SHORT).show()
                            },
                        shape = RoundedCornerShape(SahaayCorners.medium),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            1.5.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        ),
                        elevation = CardDefaults.cardElevation(SahaayElevation.low)
                    ) {
                        Row(
                            modifier = Modifier.padding(SahaaySpacing.lg),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = name,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(SahaaySpacing.md))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = spec,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                            Button(
                                onClick = {
                                    selectedDoctor = name
                                    currentStep = 2
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(SahaayCorners.small)
                            ) {
                                Text("Select", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }
            }

            // STEP 2: Select Date
            if (currentStep == 2) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { currentStep = 1 }) {
                        Text("← Back", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                    }
                }
                Text(
                    text = "Select Appointment Date",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                Text(
                    text = "Booking with ${selectedDoctor ?: "Doctor"}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.height(SahaaySpacing.lg))

                listOf("August 12, 2026", "August 13, 2026", "August 14, 2026").forEach { date ->
                    val isSelected = selectedDate == date
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = SahaaySpacing.xs)
                            .clickable { selectedDate = date },
                        shape = RoundedCornerShape(SahaayCorners.medium),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            1.5.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(SahaaySpacing.lg),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = date,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(SahaaySpacing.md))
                            Text(
                                text = date,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedDate = date },
                                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(SahaaySpacing.xl))

                Button(
                    onClick = {
                        if (patientName.isBlank() && savedProfile.hasName()) {
                            patientName = savedProfile.fullName
                        }
                        if (patientMobile.isBlank() && savedProfile.hasMobile()) {
                            patientMobile = savedProfile.mobileNumber
                        }
                        currentStep = 3
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(SahaayTouchTarget.preferred),
                    shape = RoundedCornerShape(SahaayCorners.medium),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Continue to Patient Details", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                }
            }

            // STEP 3: Patient Form Details
            if (currentStep == 3) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { currentStep = 2 }) {
                        Text("← Back", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                    }
                }
                Text(
                    text = "Patient Information",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )

                Spacer(modifier = Modifier.height(SahaaySpacing.sm))

                // Profile Autofill helper
                if (savedProfile.hasName() || savedProfile.hasMobile()) {
                    OutlinedButton(
                        onClick = {
                            if (savedProfile.hasName()) patientName = savedProfile.fullName
                            if (savedProfile.hasMobile()) patientMobile = savedProfile.mobileNumber
                            Toast.makeText(context, "Filled from saved profile", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(SahaayCorners.small)
                    ) {
                        Text("Use my saved details", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    }
                    Spacer(modifier = Modifier.height(SahaaySpacing.md))
                }

                OutlinedTextField(
                    value = patientName,
                    onValueChange = { patientName = it },
                    label = { Text("Full Name", style = MaterialTheme.typography.bodyMedium) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(SahaayCorners.medium)
                )

                Spacer(modifier = Modifier.height(SahaaySpacing.md))

                OutlinedTextField(
                    value = patientMobile,
                    onValueChange = { patientMobile = it },
                    label = { Text("Mobile Number", style = MaterialTheme.typography.bodyMedium) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(SahaayCorners.medium)
                )

                Spacer(modifier = Modifier.height(SahaaySpacing.xl))

                Button(
                    onClick = { currentStep = 4 },
                    enabled = patientName.isNotBlank() && patientMobile.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(SahaayTouchTarget.preferred),
                    shape = RoundedCornerShape(SahaayCorners.medium),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Continue to Summary", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                }
            }

            // STEP 4: Confirmation Summary
            if (currentStep == 4) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(SahaayCorners.large),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.5.dp, SahaaySuccess),
                    shadowElevation = SahaayElevation.medium
                ) {
                    Column(modifier = Modifier.padding(SahaaySpacing.xl)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Ready",
                                tint = SahaaySuccess,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(SahaaySpacing.sm))
                            Text(
                                text = "Appointment Ready",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(SahaaySpacing.md))
                        Text(
                            text = "Doctor: ${selectedDoctor ?: "Dr Sharma"}",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                        )
                        Text(
                            text = "Date: $selectedDate",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                        )
                        Text(
                            text = "Patient: $patientName ($patientMobile)",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                        )
                        Spacer(modifier = Modifier.height(SahaaySpacing.md))
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(SahaayCorners.small),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Sahaay is in guidance mode. Tap confirm yourself when ready.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                ),
                                modifier = Modifier.padding(SahaaySpacing.sm)
                            )
                        }
                        Spacer(modifier = Modifier.height(SahaaySpacing.lg))
                        Button(
                            onClick = {
                                Toast.makeText(context, "Appointment Confirmed for $patientName!", Toast.LENGTH_LONG).show()
                                currentStep = 1
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(SahaayTouchTarget.preferred),
                            shape = RoundedCornerShape(SahaayCorners.medium),
                            colors = ButtonDefaults.buttonColors(containerColor = SahaaySuccess)
                        ) {
                            Text("Confirm Booking", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }
    }
}
