package com.example.elderhelpprototypev01.profile

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Centralized repository for user's basic non-sensitive profile.
 * Uses SharedPreferences for lightweight local persistence.
 * NEVER stores OTP, PIN, password, CVV, or any authentication credentials.
 */
object ProfileRepository {
    private const val PREFS_NAME = "sahaay_basic_profile"
    private const val KEY_FULL_NAME = "full_name"
    private const val KEY_MOBILE = "mobile_number"
    private const val KEY_EMAIL = "email"
    private const val KEY_ADDRESS = "address"
    private const val KEY_DOB = "date_of_birth"
    private const val KEY_LANGUAGE = "preferred_language"

    private var prefs: SharedPreferences? = null
    private val _profile = MutableStateFlow(BasicProfile())
    val profile: StateFlow<BasicProfile> = _profile.asStateFlow()

    fun init(context: Context) {
        if (prefs != null) return
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _profile.value = load()
    }

    private fun load(): BasicProfile {
        val p = prefs ?: return BasicProfile()
        return BasicProfile(
            fullName = p.getString(KEY_FULL_NAME, "") ?: "",
            mobileNumber = p.getString(KEY_MOBILE, "") ?: "",
            email = p.getString(KEY_EMAIL, "") ?: "",
            address = p.getString(KEY_ADDRESS, "") ?: "",
            dateOfBirth = p.getString(KEY_DOB, "") ?: "",
            preferredLanguage = p.getString(KEY_LANGUAGE, "English (India)") ?: "English (India)"
        )
    }

    fun save(profile: BasicProfile) {
        prefs?.edit()?.apply {
            putString(KEY_FULL_NAME, profile.fullName)
            putString(KEY_MOBILE, profile.mobileNumber)
            putString(KEY_EMAIL, profile.email)
            putString(KEY_ADDRESS, profile.address)
            putString(KEY_DOB, profile.dateOfBirth)
            putString(KEY_LANGUAGE, profile.preferredLanguage)
            apply()
        }
        _profile.value = profile
    }

    fun clear() {
        prefs?.edit()?.clear()?.apply()
        _profile.value = BasicProfile()
    }

    fun getProfile(): BasicProfile = _profile.value
}
