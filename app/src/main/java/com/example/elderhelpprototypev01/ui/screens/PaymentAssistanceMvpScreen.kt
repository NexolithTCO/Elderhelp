package com.example.elderhelpprototypev01.ui.screens

import androidx.compose.animation.*
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
import com.example.elderhelpprototypev01.ui.theme.AppleCanvasBg
import com.example.elderhelpprototypev01.ui.theme.Typography
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
            .background(AppleCanvasBg)
            .padding(24.dp),
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
                    tint = Color(0xFF1C1C1E)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Payment Assistance",
                style = Typography.titleLarge,
                color = Color(0xFF1C1C1E)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Body
        when (currentStep) {
            PaymentMvpStep.FORM -> {
                Text(
                    text = "Select Bill Type",
                    style = Typography.titleMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Bill Type Selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PaymentBillType.entries.forEach { billType ->
                        val isSelected = selectedBill == billType
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedBill = billType },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Color(0xFF007AFF) else Color.White,
                            border = if (isSelected) null else borderStrokeForUnselected()
                        ) {
                            Text(
                                text = billType.displayName,
                                style = Typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) Color.White else Color(0xFF1C1C1E),
                                modifier = Modifier.padding(vertical = 12.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Details Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = selectedBill.provider,
                            style = Typography.bodyLarge,
                            color = Color(0xFF1C1C1E)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Amount Due:", style = Typography.bodyMedium, color = Color(0xFF8E8E93))
                            Text(selectedBill.amount, style = Typography.bodyLarge, color = Color(0xFF34C759))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Due Date:", style = Typography.bodyMedium, color = Color(0xFF8E8E93))
                            Text(selectedBill.dueDate, style = Typography.bodyMedium, color = Color(0xFF1C1C1E))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = consumerNumber,
                    onValueChange = { consumerNumber = it },
                    label = { Text("Consumer Number", style = Typography.bodyMedium) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = { currentStep = PaymentMvpStep.CONFIRMATION },
                    enabled = consumerNumber.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF))
                ) {
                    Text("Review Payment", style = Typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                }
            }

            PaymentMvpStep.CONFIRMATION -> {
                Text(
                    text = "Confirm Payment Details",
                    style = Typography.titleMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        ConfirmationRow("Bill Type", selectedBill.displayName)
                        Spacer(modifier = Modifier.height(8.dp))
                        ConfirmationRow("Provider", selectedBill.provider)
                        Spacer(modifier = Modifier.height(8.dp))
                        ConfirmationRow("Consumer No", maskConsumerNumber(consumerNumber))
                        Spacer(modifier = Modifier.height(8.dp))
                        ConfirmationRow("Due Date", selectedBill.dueDate)
                        Spacer(modifier = Modifier.height(8.dp))
                        ConfirmationRow("Payment Method", "Saved UPI (Demo)")
                        
                        Divider(modifier = Modifier.padding(vertical = 12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Amount", style = Typography.titleMedium, color = Color(0xFF1C1C1E))
                            Text(selectedBill.amount, style = Typography.headlineMedium, color = Color(0xFF34C759))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Safety Messaging
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFF4E5)
                ) {
                    Text(
                        text = "Safety Notice: ElderHelp will never ask for or store your OTP, UPI PIN, CVV, or password.",
                        style = Typography.bodyMedium,
                        color = Color(0xFFFF9500),
                        modifier = Modifier.padding(16.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = { currentStep = PaymentMvpStep.PROCESSING },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34C759))
                ) {
                    Text("Confirm & Pay", style = Typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { currentStep = PaymentMvpStep.FORM },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Go Back", style = Typography.bodyLarge, color = Color(0xFF007AFF))
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
                    CircularProgressIndicator(color = Color(0xFF007AFF))
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Processing simulated payment...",
                        style = Typography.titleMedium,
                        color = Color(0xFF1C1C1E)
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
                        tint = Color(0xFF34C759),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Payment Successful",
                        style = Typography.headlineMedium,
                        color = Color(0xFF34C759)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            ConfirmationRow("Amount Paid", selectedBill.amount)
                            Spacer(modifier = Modifier.height(8.dp))
                            ConfirmationRow("Bill Type", selectedBill.displayName)
                            Spacer(modifier = Modifier.height(8.dp))
                            ConfirmationRow("Provider", selectedBill.provider)
                            Spacer(modifier = Modifier.height(8.dp))
                            ConfirmationRow("Consumer No", maskConsumerNumber(consumerNumber))
                            Spacer(modifier = Modifier.height(8.dp))
                            ConfirmationRow("Ref Number", "DEMO-REF-987654")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "Note: This was a simulated payment for the MVP demo. No real transaction occurred.",
                        style = Typography.bodyMedium,
                        color = Color(0xFF8E8E93),
                        textAlign = TextAlign.Center
                    )
                }

                Button(
                    onClick = { onBackToHome() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF))
                ) {
                    Text("Back to Home", style = Typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
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
        Text(label, style = Typography.bodyMedium, color = Color(0xFF8E8E93))
        Text(value, style = Typography.bodyLarge, color = Color(0xFF1C1C1E))
    }
}

@Composable
private fun borderStrokeForUnselected() = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E5EA))

fun maskConsumerNumber(consumerNumber: String): String {
    if (consumerNumber.isBlank()) return "Not entered"
    if (consumerNumber.length <= 4) return consumerNumber
    return "••••" + consumerNumber.takeLast(4)
}