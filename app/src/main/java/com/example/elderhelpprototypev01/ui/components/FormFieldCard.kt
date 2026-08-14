package com.example.elderhelpprototypev01.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.elderhelpprototypev01.model.FormField
import com.example.elderhelpprototypev01.ui.theme.*

/**
 * FormFieldCard
 *
 * An elderly-friendly form field card displaying:
 * - Field label in large, clear text
 * - Required badge indicator (*)
 * - Large, touch-friendly text input field
 * - Filled status badge (green checkmark)
 */
@Composable
fun FormFieldCard(
    field: FormField,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isFilled = value.isNotBlank()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = if (isFilled) 1.5.dp else 1.dp,
                color = if (isFilled) Color(0xFF34C759) else AppleBorderSubtle,
                shape = RoundedCornerShape(18.dp)
            ),
        shape = RoundedCornerShape(18.dp),
        color = AppleSurfaceWhite,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Label Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = field.label,
                        style = Typography.titleMedium.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppleTextPrimary
                        )
                    )
                    if (field.required) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "*",
                            style = Typography.titleMedium.copy(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF3B30)
                            )
                        )
                    }
                }

                // Status Badge
                if (isFilled) {
                    Surface(
                        color = Color(0xFFEAF9EC),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Filled",
                                tint = Color(0xFF34C759),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Filled",
                                style = Typography.labelMedium.copy(
                                    color = Color(0xFF34C759),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Text Input Field
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "Tap to edit or speak above...",
                        style = Typography.bodyLarge.copy(
                            fontSize = 16.sp,
                            color = AppleTextMuted
                        )
                    )
                },
                textStyle = Typography.bodyLarge.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppleTextPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppleBlue,
                    unfocusedBorderColor = AppleBorderSubtle,
                    focusedContainerColor = AppleBlueLight.copy(alpha = 0.3f),
                    unfocusedContainerColor = Color(0xFFF9F9FB)
                ),
                singleLine = true
            )
        }
    }
}
