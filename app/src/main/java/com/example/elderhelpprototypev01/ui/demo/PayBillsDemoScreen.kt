package com.example.elderhelpprototypev01.ui.demo

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.elderhelpprototypev01.ui.theme.*

/**
 * PayBillsDemoScreen
 *
 * Interactive Indian Bill Payment Portal designed for senior citizens.
 */
@Composable
fun PayBillsDemoScreen(
    onVoiceCommandRequest: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var selectedBiller by remember { mutableStateOf("Tata Power Electricity") }
    var consumerNumber by remember { mutableStateOf("") }
    var selectedPaymentMode by remember { mutableStateOf("UPI (Google Pay / PhonePe)") }
    var isBillPaid by remember { mutableStateOf(false) }

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
            // Pay Bills Portal Header
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
                            .background(SahaayBillsBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ElectricBolt,
                            contentDescription = "Electricity Bill Icon",
                            tint = SahaayBillsIcon,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(SahaaySpacing.md))
                    Column {
                        Text(
                            text = "Sahaay Bill Pay",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Electricity & Utility Payments",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = SahaayBillsIcon
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(SahaaySpacing.xl))

            // Sample Assistant Triggers
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
                            onClick = { onVoiceCommandRequest("Where do I enter consumer number?") },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                            shape = RoundedCornerShape(SahaayCorners.small),
                            contentPadding = PaddingValues(horizontal = SahaaySpacing.sm, vertical = SahaaySpacing.xs),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("“Where to type?”", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        Button(
                            onClick = { onVoiceCommandRequest("Help me pay electricity bill") },
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

            Spacer(modifier = Modifier.height(SahaaySpacing.xl))

            if (!isBillPaid) {
                // STEP 1: Select Biller
                Text(
                    text = "1. Select Provider",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                Spacer(modifier = Modifier.height(SahaaySpacing.sm))

                listOf("Tata Power Electricity", "BSES Rajdhani", "MSEB Mumbai").forEach { biller ->
                    val isSelected = selectedBiller == biller
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = SahaaySpacing.xs)
                            .clickable { selectedBiller = biller },
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
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedBiller = biller },
                                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.width(SahaaySpacing.md))
                            Text(
                                text = biller,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(SahaaySpacing.xl))

                // STEP 2: Consumer Number Entry
                Text(
                    text = "2. Enter Consumer Number",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                Text(
                    text = "Located on top right of your paper bill (e.g. 900012345)",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Spacer(modifier = Modifier.height(SahaaySpacing.md))

                OutlinedTextField(
                    value = consumerNumber,
                    onValueChange = { consumerNumber = it },
                    label = { Text("Consumer / Account Number", style = MaterialTheme.typography.bodyMedium) },
                    placeholder = { Text("e.g. 900012345678", style = MaterialTheme.typography.bodyMedium) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(SahaayCorners.medium),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(modifier = Modifier.height(SahaaySpacing.xl))

                // STEP 3: Bill Amount Summary Card
                Text(
                    text = "3. Bill Amount Summary",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                Spacer(modifier = Modifier.height(SahaaySpacing.sm))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(SahaayCorners.medium),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(SahaaySpacing.lg)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Provider", style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                            Text(text = selectedBiller, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                        }
                        Spacer(modifier = Modifier.height(SahaaySpacing.xs))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Consumer No.", style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                            Text(
                                text = if (consumerNumber.isNotBlank()) consumerNumber else "900012345678",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Spacer(modifier = Modifier.height(SahaaySpacing.xs))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Due Date", style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                            Text(text = "Aug 20, 2026", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = SahaayWarning))
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = SahaaySpacing.md), color = MaterialTheme.colorScheme.outlineVariant)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Total Payable Amount", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text(
                                text = "₹1,842.00",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(SahaaySpacing.xl))

                // STEP 4: Select Payment Mode
                Text(
                    text = "4. Payment Mode",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                Spacer(modifier = Modifier.height(SahaaySpacing.sm))

                listOf("UPI (Google Pay / PhonePe)", "Debit Card / NetBanking").forEach { mode ->
                    val isSelected = selectedPaymentMode == mode
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = SahaaySpacing.xs)
                            .clickable { selectedPaymentMode = mode },
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
                                imageVector = Icons.Default.CreditCard,
                                contentDescription = mode,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(SahaaySpacing.md))
                            Text(
                                text = mode,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedPaymentMode = mode },
                                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(SahaaySpacing.xxl))

                // Pay Bill Primary Button
                Button(
                    onClick = {
                        isBillPaid = true
                        Toast.makeText(context, "Payment Successful! ₹1,842.00 paid.", Toast.LENGTH_LONG).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(SahaayTouchTarget.preferred),
                    shape = RoundedCornerShape(SahaayCorners.medium),
                    colors = ButtonDefaults.buttonColors(containerColor = SahaaySuccess)
                ) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = "Secure", modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(SahaaySpacing.sm))
                    Text("Pay ₹1,842.00 Securely", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                }
            } else {
                // Payment Success Screen
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(SahaayCorners.large),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.5.dp, SahaaySuccess),
                    shadowElevation = SahaayElevation.medium
                ) {
                    Column(
                        modifier = Modifier.padding(SahaaySpacing.xxl),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = SahaaySuccess,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(SahaaySpacing.md))
                        Text(
                            text = "Bill Payment Successful!",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Spacer(modifier = Modifier.height(SahaaySpacing.xs))
                        Text(
                            text = "₹1,842.00 paid to $selectedBiller",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Text(
                            text = "Transaction Ref: SHY987654321",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Spacer(modifier = Modifier.height(SahaaySpacing.xl))
                        Button(
                            onClick = {
                                isBillPaid = false
                                consumerNumber = ""
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(SahaayTouchTarget.preferred),
                            shape = RoundedCornerShape(SahaayCorners.medium),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Pay Another Bill", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }
    }
}
