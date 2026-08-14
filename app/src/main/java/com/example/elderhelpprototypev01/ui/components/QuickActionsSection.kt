package com.example.elderhelpprototypev01.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import com.example.elderhelpprototypev01.ui.localization.Localization
import com.example.elderhelpprototypev01.ui.theme.*

data class QuickActionItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val buttonText: String,
    val icon: ImageVector,
    val iconTint: Color,
    val iconBgColor: Color
)

@Composable
fun QuickActionsSection(
    currentLanguage: String = "English (India)",
    onActionClick: (QuickActionItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = Localization.getStrings(currentLanguage)

    val actions = listOf(
        QuickActionItem(
            id = "doctor",
            title = strings.doctorTitle,
            subtitle = strings.doctorSubtitle,
            buttonText = strings.doctorBtn,
            icon = Icons.Default.MedicalServices,
            iconTint = SahaayDoctorIcon,
            iconBgColor = SahaayDoctorBg
        ),
        QuickActionItem(
            id = "bills",
            title = strings.billsTitle,
            subtitle = strings.billsSubtitle,
            buttonText = strings.billsBtn,
            icon = Icons.AutoMirrored.Filled.ReceiptLong,
            iconTint = SahaayBillsIcon,
            iconBgColor = SahaayBillsBg
        ),
        QuickActionItem(
            id = "forms",
            title = strings.formsTitle,
            subtitle = strings.formsSubtitle,
            buttonText = strings.formsBtn,
            icon = Icons.Default.Description,
            iconTint = SahaayFormsIcon,
            iconBgColor = SahaayFormsBg
        ),
        QuickActionItem(
            id = "help",
            title = strings.helpTitle,
            subtitle = strings.helpSubtitle,
            buttonText = strings.helpBtn,
            icon = Icons.AutoMirrored.Filled.HelpOutline,
            iconTint = SahaayHelpIcon,
            iconBgColor = SahaayHelpBg
        )
    )

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = strings.exploreTasks,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
        }

        Spacer(modifier = Modifier.height(SahaaySpacing.md))

        // 2x2 Grid of Action Cards
        Column(
            verticalArrangement = Arrangement.spacedBy(SahaaySpacing.md)
        ) {
            actions.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(SahaaySpacing.md)
                ) {
                    rowItems.forEach { item ->
                        QuickActionCard(
                            title = item.title,
                            subtitle = item.subtitle,
                            actionButtonText = item.buttonText,
                            icon = item.icon,
                            iconTint = item.iconTint,
                            iconBgColor = item.iconBgColor,
                            onClick = { onActionClick(item) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
