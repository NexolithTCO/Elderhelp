package com.example.elderhelpprototypev01.overlay

import android.content.Context

/**
 * Lightweight SharedPreferences wrapper for Sahaay overlay state.
 * No database required – a simple key/value store is sufficient.
 */
object SahaayPreferences {

    private const val PREFS_NAME = "sahaay_prefs"
    private const val KEY_OVERLAY_ENABLED = "overlay_enabled"

    fun setOverlayEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_OVERLAY_ENABLED, enabled)
            .apply()
    }

    fun isOverlayEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_OVERLAY_ENABLED, true)
    }
}
