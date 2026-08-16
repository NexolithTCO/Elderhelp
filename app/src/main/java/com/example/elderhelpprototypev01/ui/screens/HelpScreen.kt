package com.example.elderhelpprototypev01.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.elderhelpprototypev01.ui.theme.*

data class HelpCategory(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val description: String,
    val details: String,
    val actionButtonText: String? = null
)

@Composable
fun HelpScreen(
    onNavigateToPayments: () -> Unit = {},
    onNavigateToForms: () -> Unit = {},
    onNavigateToDoctor: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val categories = remember {
        listOf(
            HelpCategory(
                id = "payments",
                title = "Payments & Bills",
                icon = Icons.Default.Payment,
                description = "Get step-by-step help paying utility bills",
                details = "I can guide you through bill payments step by step. I will explain the amount, due date, and payment method clearly. I will never ask you to tell me your OTP or PIN. You always enter those yourself.",
                actionButtonText = "Go to Bill Payments"
            ),
            HelpCategory(
                id = "pension",
                title = "Pension Application Form",
                icon = Icons.Default.AccountBalance,
                description = "Apply for national or state pension scheme",
                details = "You can fill out your pension application form with Sahaay. We help you fill your name, mobile, and disbursement bank details cleanly. Your PIN, OTP, and passwords remain 100% private.",
                actionButtonText = "Open Pension Form"
            ),
            HelpCategory(
                id = "forms",
                title = "General Forms Assistance",
                icon = Icons.Default.Description,
                description = "Help filling forms and official applications",
                details = "I can help you understand online forms and fill in your saved personal details like name, phone number, and address with your permission. I will read each field name aloud before filling it.",
                actionButtonText = "Open Forms & Applications"
            ),
            HelpCategory(
                id = "doctor",
                title = "Doctor Appointments",
                icon = Icons.Default.LocalHospital,
                description = "Book and manage medical visits",
                details = "I can help you find doctors, choose appointment dates, and fill in patient booking forms. I will guide you through each step clearly.",
                actionButtonText = "Go to Doctor Booking"
            ),
            HelpCategory(
                id = "profile",
                title = "My Basic Profile",
                icon = Icons.Default.Person,
                description = "Manage saved name, address, and phone number",
                details = "Save your basic non-sensitive personal details here once. Sahaay will use them to help you pre-fill common forms quickly.",
                actionButtonText = "Edit My Saved Details"
            ),
            HelpCategory(
                id = "screen",
                title = "Reading Your Screen",
                icon = Icons.Default.Visibility,
                description = "Understand what is on your screen",
                details = "I can read and explain what is currently shown on your screen. I will highlight the next step and tell you exactly where to tap."
            ),
            HelpCategory(
                id = "words",
                title = "Difficult Technical Words",
                icon = Icons.Default.Translate,
                description = "Explain technical terms simply",
                details = "If you see a word you do not understand, ask me and I will explain it in simple language. For example, IFSC means bank branch code, and CVV is the number on the back of your card."
            ),
            HelpCategory(
                id = "voice",
                title = "Voice Commands Guide",
                icon = Icons.Default.Mic,
                description = "What you can say to Sahaay",
                details = "You can say things like: 'Pay my electricity bill', 'Open pension form', 'Book a doctor', 'What should I do next?', 'Read this screen', 'Go back', 'Repeat that', or 'Help me'. I understand both English and Hindi."
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(horizontal = SahaaySpacing.lg)
            .padding(top = SahaaySpacing.lg, bottom = SahaaySpacing.xxxl)
    ) {
        Text(
            text = "How can Sahaay help you?",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        )

        Spacer(modifier = Modifier.height(SahaaySpacing.xs))

        Text(
            text = "Tap any topic below to learn more or open the service.",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        Spacer(modifier = Modifier.height(SahaaySpacing.xl))

        categories.forEach { category ->
            var expanded by remember { mutableStateOf(false) }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = SahaaySpacing.xs),
                shape = RoundedCornerShape(SahaayCorners.medium),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shadowElevation = SahaayElevation.low
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = !expanded }
                            .padding(SahaaySpacing.lg),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(SahaaySpacing.md))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = category.title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = category.description,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                        Icon(
                            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (expanded) "Collapse" else "Expand",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    AnimatedVisibility(visible = expanded) {
                        Column(
                            modifier = Modifier.padding(
                                start = SahaaySpacing.lg + 28.dp + SahaaySpacing.md,
                                end = SahaaySpacing.lg,
                                bottom = SahaaySpacing.lg
                            )
                        ) {
                            Text(
                                text = category.details,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.3f
                                )
                            )

                            if (category.actionButtonText != null) {
                                Spacer(modifier = Modifier.height(SahaaySpacing.md))
                                Button(
                                    onClick = {
                                        when (category.id) {
                                            "payments" -> onNavigateToPayments()
                                            "pension" -> onNavigateToForms()
                                            "forms" -> onNavigateToForms()
                                            "doctor" -> onNavigateToDoctor()
                                            "profile" -> onNavigateToProfile()
                                        }
                                    },
                                    shape = RoundedCornerShape(SahaayCorners.small),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text(
                                        text = category.actionButtonText,
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
