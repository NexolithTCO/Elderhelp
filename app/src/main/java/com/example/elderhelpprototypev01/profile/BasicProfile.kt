package com.example.elderhelpprototypev01.profile

data class BasicProfile(
    val fullName: String = "",
    val mobileNumber: String = "",
    val email: String = "",
    val address: String = "",
    val dateOfBirth: String = "",
    val preferredLanguage: String = "English (India)"
) {
    fun isEmpty(): Boolean = fullName.isBlank() && mobileNumber.isBlank() && email.isBlank()
    fun hasName(): Boolean = fullName.isNotBlank()
    fun hasMobile(): Boolean = mobileNumber.isNotBlank()
    fun hasEmail(): Boolean = email.isNotBlank()
    fun hasAddress(): Boolean = address.isNotBlank()
}
