package com.example.elderhelpprototypev01.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.elderhelpprototypev01.SahaayViewModel
import com.example.elderhelpprototypev01.ui.theme.*
import kotlinx.coroutines.delay

enum class PaymentMvpStep {
    FORM,
    CONFIRMATION,
    PROCESSING,
    SUCCESS
}

enum class PaymentBillType(
    val displayName: String,
    val provider: String,
    val amount: String,
    val dueDate: String
) {
    ELECTRICITY(
        displayName = "Electricity",
        provider = "Maharashtra Electricity Demo",
        amount = "₹850",
        dueDate = "20 Aug 2026"
    ),
    WATER(
        displayName = "Water",
        provider = "Mumbai Water Demo",
        amount = "₹620",
        dueDate = "22 Aug 2026"
    ),
    GAS(
        displayName = "Gas",
        provider = "Maharashtra Gas Demo",
        amount = "₹950",
        dueDate = "25 Aug 2026"
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentAssistanceMvpScreen(
    viewModel: SahaayViewModel,
    currentLanguage: String = "English (India)",
    modifier: Modifier = Modifier,
    onBackToHome: () -> Unit = {}
) {
    var currentStep by remember { mutableStateOf(PaymentMvpStep.FORM) }
    var selectedBill by remember { mutableStateOf(PaymentBillType.ELECTRICITY) }
    var consumerNumber by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(SahaaySpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    when (currentStep) {
                        PaymentMvpStep.FORM -> onBackToHome()
                        PaymentMvpStep.CONFIRMATION -> currentStep = PaymentMvpStep.FORM
                        PaymentMvpStep.PROCESSING -> {} // Cannot go back during processing
                        PaymentMvpStep.SUCCESS -> onBackToHome()
                    }
                },
                enabled = currentStep != PaymentMvpStep.PROCESSING
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.width(SahaaySpacing.sm))

            Text(
                text = "Payment Assistance",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(SahaaySpacing.lg))

        // Body
        when (currentStep) {
            PaymentMvpStep.FORM -> {
                // Assistant Trigger Banner
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(SahaayCorners.medium),
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(SahaaySpacing.md)) {
                        Text(
                            text = "Try Assistant Triggers:",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(SahaaySpacing.sm))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(SahaaySpacing.sm),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { viewModel.analyzeCurrentScreenAndHighlight("Where do I enter consumer number?") },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                                shape = RoundedCornerShape(SahaayCorners.small),
                                contentPadding = PaddingValues(horizontal = SahaaySpacing.sm, vertical = SahaaySpacing.xs),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("“Where to type?”", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                            }
                            Button(
                                onClick = { viewModel.analyzeCurrentScreenAndHighlight("Help me pay electricity bill") },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(SahaayCorners.small),
                                contentPadding = PaddingValues(horizontal = SahaaySpacing.sm, vertical = SahaaySpacing.xs),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("“Guide bill pay”", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(SahaaySpacing.lg))

                Text(
                    text = "Select Bill Type",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.height(SahaaySpacing.md))

                // Bill Type Selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(SahaaySpacing.sm)
                ) {
                    PaymentBillType.entries.forEach { billType ->
                        val isSelected = selectedBill == billType
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedBill = billType },
                            shape = RoundedCornerShape(SahaayCorners.medium),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            border = if (isSelected) null else borderStrokeForUnselected()
                        ) {
                            Text(
                                text = billType.displayName,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(vertical = SahaaySpacing.md),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(SahaaySpacing.lg))

                // Details Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(SahaayCorners.large),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = SahaayElevation.low
                ) {
                    Column(modifier = Modifier.padding(SahaaySpacing.lg)) {
                        Text(
                            text = selectedBill.provider,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(SahaaySpacing.sm))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Amount Due:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(selectedBill.amount, style = MaterialTheme.typography.bodyLarge, color = SahaaySuccess)
                        }
                        Spacer(modifier = Modifier.height(SahaaySpacing.xs))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Due Date:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(selectedBill.dueDate, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(SahaaySpacing.lg))

                OutlinedTextField(
                    value = consumerNumber,
                    onValueChange = { consumerNumber = it },
                    label = { Text("Consumer Number", style = MaterialTheme.typography.bodyMedium) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(SahaayCorners.medium)
                )

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = { currentStep = PaymentMvpStep.CONFIRMATION },
                    enabled = consumerNumber.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(SahaayTouchTarget.preferred),
                    shape = RoundedCornerShape(SahaayCorners.medium),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Review Payment", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                }
            }

            PaymentMvpStep.CONFIRMATION -> {
                Text(
                    text = "Confirm Payment Details",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.height(SahaaySpacing.md))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(SahaayCorners.large),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = SahaayElevation.low
                ) {
                    Column(modifier = Modifier.padding(SahaaySpacing.lg)) {
                        ConfirmationRow("Bill Type", selectedBill.displayName)
                        Spacer(modifier = Modifier.height(SahaaySpacing.sm))
                        ConfirmationRow("Provider", selectedBill.provider)
                        Spacer(modifier = Modifier.height(SahaaySpacing.sm))
                        ConfirmationRow("Consumer No", maskConsumerNumber(consumerNumber))
                        Spacer(modifier = Modifier.height(SahaaySpacing.sm))
                        ConfirmationRow("Due Date", selectedBill.dueDate)
                        Spacer(modifier = Modifier.height(SahaaySpacing.sm))
                        ConfirmationRow("Payment Method", "Saved UPI (Demo)")
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = SahaaySpacing.md))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Amount", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                            Text(selectedBill.amount, style = MaterialTheme.typography.headlineMedium, color = SahaaySuccess)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(SahaaySpacing.md))

                // Safety Messaging
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(SahaayCorners.medium),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "Safety Notice: ElderHelp will never ask for or store your OTP, UPI PIN, CVV, or password.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(SahaaySpacing.md)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = { currentStep = PaymentMvpStep.PROCESSING },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(SahaayTouchTarget.preferred),
                    shape = RoundedCornerShape(SahaayCorners.medium),
                    colors = ButtonDefaults.buttonColors(containerColor = SahaaySuccess)
                ) {
                    Text("Confirm & Pay", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                }

                Spacer(modifier = Modifier.height(SahaaySpacing.sm))

                OutlinedButton(
                    onClick = { currentStep = PaymentMvpStep.FORM },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(SahaayTouchTarget.preferred),
                    shape = RoundedCornerShape(SahaayCorners.medium)
                ) {
                    Text("Go Back", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                }
            }

            PaymentMvpStep.PROCESSING -> {
                LaunchedEffect(Unit) {
                    delay(2500)
                    currentStep = PaymentMvpStep.SUCCESS
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(SahaaySpacing.xl))
                    Text(
                        text = "Processing simulated payment...",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            PaymentMvpStep.SUCCESS -> {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center,
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
                        text = "Payment Successful",
                        style = MaterialTheme.typography.headlineMedium,
                        color = SahaaySuccess
                    )
                    
                    Spacer(modifier = Modifier.height(SahaaySpacing.xl))
                    
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(SahaayCorners.large),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = SahaayElevation.low
                    ) {
                        Column(modifier = Modifier.padding(SahaaySpacing.lg)) {
                            ConfirmationRow("Amount Paid", selectedBill.amount)
                            Spacer(modifier = Modifier.height(SahaaySpacing.sm))
                            ConfirmationRow("Bill Type", selectedBill.displayName)
                            Spacer(modifier = Modifier.height(SahaaySpacing.sm))
                            ConfirmationRow("Provider", selectedBill.provider)
                            Spacer(modifier = Modifier.height(SahaaySpacing.sm))
                            ConfirmationRow("Consumer No", maskConsumerNumber(consumerNumber))
                            Spacer(modifier = Modifier.height(SahaaySpacing.sm))
                            ConfirmationRow("Ref Number", "DEMO-REF-987654")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(SahaaySpacing.xl))
                    
                    Text(
                        text = "Note: This was a simulated payment for the MVP demo. No real transaction occurred.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                Button(
                    onClick = { onBackToHome() },
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

@Composable
private fun ConfirmationRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun borderStrokeForUnselected() = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E5EA))

fun maskConsumerNumber(consumerNumber: String): String {
    if (consumerNumber.isBlank()) return "Not entered"
    if (consumerNumber.length <= 4) return consumerNumber
    return "••••" + consumerNumber.takeLast(4)
}