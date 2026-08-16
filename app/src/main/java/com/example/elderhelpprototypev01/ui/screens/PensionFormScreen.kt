package com.example.elderhelpprototypev01.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.elderhelpprototypev01.profile.ProfileRepository
import com.example.elderhelpprototypev01.ui.theme.*

enum class PensionStep {
    PERSONAL_DETAILS,
    BANK_DETAILS,
    REVIEW,
    SUBMITTED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PensionFormScreen(
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val savedProfile by ProfileRepository.profile.collectAsState()

    var currentStep by remember { mutableStateOf(PensionStep.PERSONAL_DETAILS) }

    // Form fields automatically pre-filled from saved profile
    var applicantName by remember(savedProfile.fullName) { mutableStateOf(savedProfile.fullName) }
    var mobileNumber by remember(savedProfile.mobileNumber) { mutableStateOf(savedProfile.mobileNumber) }
    var dateOfBirth by remember(savedProfile.dateOfBirth) { mutableStateOf(savedProfile.dateOfBirth) }
    var address by remember(savedProfile.address) { mutableStateOf(savedProfile.address) }

    var accountNumber by remember { mutableStateOf("") }
    var bankName by remember { mutableStateOf("") }
    var ifscCode by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(SahaaySpacing.lg)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    when (currentStep) {
                        PensionStep.PERSONAL_DETAILS -> onBack()
                        PensionStep.BANK_DETAILS -> currentStep = PensionStep.PERSONAL_DETAILS
                        PensionStep.REVIEW -> currentStep = PensionStep.BANK_DETAILS
                        PensionStep.SUBMITTED -> onBack()
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.width(SahaaySpacing.sm))
            Text(
                text = "Pension Application Form",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(SahaaySpacing.md))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            when (currentStep) {
                PensionStep.PERSONAL_DETAILS -> {
                    Text(
                        text = "Step 1: Personal Information",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(SahaaySpacing.sm))

                    // Saved Profile Autofill Chip
                    if (savedProfile.hasName() || savedProfile.hasMobile()) {
                        OutlinedButton(
                            onClick = {
                                if (savedProfile.hasName()) applicantName = savedProfile.fullName
                                if (savedProfile.hasMobile()) mobileNumber = savedProfile.mobileNumber
                                if (savedProfile.address.isNotBlank()) address = savedProfile.address
                                if (savedProfile.dateOfBirth.isNotBlank()) dateOfBirth = savedProfile.dateOfBirth
                                Toast.makeText(context, "Filled using saved profile", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(SahaayCorners.small)
                        ) {
                            Text("Use my saved details", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        Spacer(modifier = Modifier.height(SahaaySpacing.md))
                    }

                    OutlinedTextField(
                        value = applicantName,
                        onValueChange = { applicantName = it },
                        label = { Text("Full Applicant Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(SahaayCorners.medium)
                    )

                    Spacer(modifier = Modifier.height(SahaaySpacing.md))

                    OutlinedTextField(
                        value = mobileNumber,
                        onValueChange = { mobileNumber = it },
                        label = { Text("Mobile / Phone Number") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        shape = RoundedCornerShape(SahaayCorners.medium)
                    )

                    Spacer(modifier = Modifier.height(SahaaySpacing.md))

                    OutlinedTextField(
                        value = dateOfBirth,
                        onValueChange = { dateOfBirth = it },
                        label = { Text("Date of Birth (DD/MM/YYYY)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(SahaayCorners.medium)
                    )

                    Spacer(modifier = Modifier.height(SahaaySpacing.md))

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Permanent Residential Address") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(SahaayCorners.medium)
                    )

                    Spacer(modifier = Modifier.height(SahaaySpacing.xl))

                    Button(
                        onClick = { currentStep = PensionStep.BANK_DETAILS },
                        enabled = applicantName.isNotBlank() && mobileNumber.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(SahaayTouchTarget.preferred),
                        shape = RoundedCornerShape(SahaayCorners.medium),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Next: Bank Account Details", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                    }
                }

                PensionStep.BANK_DETAILS -> {
                    Text(
                        text = "Step 2: Pension Bank Account",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(SahaaySpacing.sm))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        shape = RoundedCornerShape(SahaayCorners.medium),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "This bank account will be registered to receive monthly pension credits directly.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(SahaaySpacing.md)
                        )
                    }

                    Spacer(modifier = Modifier.height(SahaaySpacing.md))

                    OutlinedTextField(
                        value = bankName,
                        onValueChange = { bankName = it },
                        label = { Text("Bank Name (e.g. State Bank of India)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(SahaayCorners.medium)
                    )

                    Spacer(modifier = Modifier.height(SahaaySpacing.md))

                    OutlinedTextField(
                        value = accountNumber,
                        onValueChange = { accountNumber = it },
                        label = { Text("Savings Bank Account Number") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(SahaayCorners.medium)
                    )

                    Spacer(modifier = Modifier.height(SahaaySpacing.md))

                    OutlinedTextField(
                        value = ifscCode,
                        onValueChange = { ifscCode = it.uppercase() },
                        label = { Text("IFSC Code (Bank Branch Code)") },
                        supportingText = { Text("IFSC is the 11-digit code printed on your chequebook or passbook.") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(SahaayCorners.medium)
                    )

                    Spacer(modifier = Modifier.height(SahaaySpacing.xl))

                    Button(
                        onClick = { currentStep = PensionStep.REVIEW },
                        enabled = accountNumber.isNotBlank() && bankName.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(SahaayTouchTarget.preferred),
                        shape = RoundedCornerShape(SahaayCorners.medium),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Next: Review Application", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                    }
                }

                PensionStep.REVIEW -> {
                    Text(
                        text = "Step 3: Review Pension Application",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(SahaaySpacing.md))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(SahaayCorners.large),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = SahaayElevation.medium
                    ) {
                        Column(modifier = Modifier.padding(SahaaySpacing.lg)) {
                            Text("Personal Details", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary))
                            Spacer(modifier = Modifier.height(SahaaySpacing.xs))
                            Text("Name: $applicantName", style = MaterialTheme.typography.bodyLarge)
                            Text("Mobile: $mobileNumber", style = MaterialTheme.typography.bodyLarge)
                            if (dateOfBirth.isNotBlank()) Text("DoB: $dateOfBirth", style = MaterialTheme.typography.bodyMedium)
                            if (address.isNotBlank()) Text("Address: $address", style = MaterialTheme.typography.bodyMedium)

                            HorizontalDivider(modifier = Modifier.padding(vertical = SahaaySpacing.md))

                            Text("Disbursement Bank Account", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary))
                            Spacer(modifier = Modifier.height(SahaaySpacing.xs))
                            Text("Bank: $bankName", style = MaterialTheme.typography.bodyLarge)
                            Text("Account No: ••••" + accountNumber.takeLast(4), style = MaterialTheme.typography.bodyLarge)
                            if (ifscCode.isNotBlank()) Text("IFSC Code: $ifscCode", style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    Spacer(modifier = Modifier.height(SahaaySpacing.md))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(SahaayCorners.medium),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = "Safety Notice: Sahaay helps you fill this form. We never store or transmit bank PINs, passwords, or OTPs.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(SahaaySpacing.md)
                        )
                    }

                    Spacer(modifier = Modifier.height(SahaaySpacing.xl))

                    Button(
                        onClick = { currentStep = PensionStep.SUBMITTED },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(SahaayTouchTarget.preferred),
                        shape = RoundedCornerShape(SahaayCorners.medium),
                        colors = ButtonDefaults.buttonColors(containerColor = SahaaySuccess)
                    ) {
                        Text("Submit Pension Application", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                    }
                }

                PensionStep.SUBMITTED -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Success",
                            tint = SahaaySuccess,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(SahaaySpacing.md))
                        Text(
                            text = "Pension Application Submitted",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = SahaaySuccess,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(SahaaySpacing.xs))
                        Text(
                            text = "Your application has been registered successfully.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(SahaaySpacing.xl))

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(SahaayCorners.large),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = SahaayElevation.low
                        ) {
                            Column(modifier = Modifier.padding(SahaaySpacing.lg)) {
                                Text("Application Reference", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("PEN-SAHAAY-2026-9481", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                                Spacer(modifier = Modifier.height(SahaaySpacing.sm))
                                Text("Applicant: $applicantName", style = MaterialTheme.typography.bodyMedium)
                                Text("Bank Account: $bankName (••••${accountNumber.takeLast(4)})", style = MaterialTheme.typography.bodyMedium)
                            }
                        }

                        Spacer(modifier = Modifier.height(SahaaySpacing.lg))

                        OutlinedButton(
                            onClick = {
                                Toast.makeText(context, "Reference PEN-SAHAAY-2026-9481 copied", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(SahaayCorners.small)
                        ) {
                            Text("Copy Reference Number", style = MaterialTheme.typography.bodyMedium)
                        }

                        Spacer(modifier = Modifier.height(SahaaySpacing.md))

                        Button(
                            onClick = onBack,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(SahaayTouchTarget.preferred),
                            shape = RoundedCornerShape(SahaayCorners.medium),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Back to Home", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }
    }
}
