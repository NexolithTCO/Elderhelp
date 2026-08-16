package com.example.elderhelpprototypev01.ui.theme

import androidx.compose.ui.unit.dp

// ============================================================
// SAHAAY DESIGN TOKENS
// ============================================================
// Centralized spacing, shape, and elevation constants.
// Use these instead of arbitrary dp values.
// ============================================================

/** Consistent spacing rhythm based on 4dp base unit */
object SahaaySpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val xxxl = 32.dp
    val huge = 40.dp
    val section = 48.dp
}

/** Consistent corner radius hierarchy */
object SahaayCorners {
    /** Small controls, badges, chips */
    val small = 12.dp
    /** Cards, containers, inputs */
    val medium = 16.dp
    /** Hero surfaces, modals */
    val large = 20.dp
    /** Dialogs, sheets */
    val extraLarge = 28.dp
    /** Fully rounded (pills, FABs) */
    val full = 100.dp
}

/** Restrained elevation hierarchy */
object SahaayElevation {
    val none = 0.dp
    val low = 1.dp
    val medium = 2.dp
    val high = 4.dp
}

/** Minimum touch target sizes for elderly accessibility */
object SahaayTouchTarget {
    /** Absolute minimum per WCAG */
    val minimum = 48.dp
    /** Preferred for primary actions */
    val preferred = 56.dp
    /** Hero interactions (mic button) */
    val hero = 80.dp
}
