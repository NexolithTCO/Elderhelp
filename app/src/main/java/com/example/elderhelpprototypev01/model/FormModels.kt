package com.example.elderhelpprototypev01.model

data class FormField(
    val id: String,
    val label: String,
    val value: String = "",
    val required: Boolean = true
)

data class FormSchema(
    val id: String,
    val title: String,
    val fields: List<FormField>
)

data class ExtractedFieldValue(
    val fieldId: String,
    val value: String,
    val confidence: Float = 1.0f
)