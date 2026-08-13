package com.example.elderhelpprototypev01.voice

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [WakeWordDetector].
 *
 * Verifies detection of "Hey Sahayak" and its common speech-to-text variants,
 * and confirms that unrelated phrases are correctly rejected.
 */
class WakeWordDetectorTest {

    // ---- Positive cases (should detect) ----

    @Test
    fun `detects exact hey sahayak`() {
        assertTrue(WakeWordDetector.isWakeWord("hey sahayak"))
    }

    @Test
    fun `detects case insensitive HEY SAHAYAK`() {
        assertTrue(WakeWordDetector.isWakeWord("HEY SAHAYAK"))
    }

    @Test
    fun `detects mixed case Hey Sahayak`() {
        assertTrue(WakeWordDetector.isWakeWord("Hey Sahayak"))
    }

    @Test
    fun `detects hey sahaay variant`() {
        assertTrue(WakeWordDetector.isWakeWord("hey sahaay"))
    }

    @Test
    fun `detects hey sahaayak variant`() {
        assertTrue(WakeWordDetector.isWakeWord("hey sahaayak"))
    }

    @Test
    fun `detects hey sahayk typo variant`() {
        assertTrue(WakeWordDetector.isWakeWord("hey sahayk"))
    }

    @Test
    fun `detects bare sahayak`() {
        assertTrue(WakeWordDetector.isWakeWord("sahayak"))
    }

    @Test
    fun `detects sahayak within a sentence`() {
        assertTrue(WakeWordDetector.isWakeWord("sahayak mujhe help karo"))
    }

    @Test
    fun `detects hey sahayak with trailing punctuation`() {
        assertTrue(WakeWordDetector.isWakeWord("hey sahayak!"))
    }

    // ---- Negative cases (should not detect) ----

    @Test
    fun `rejects blank string`() {
        assertFalse(WakeWordDetector.isWakeWord(""))
    }

    @Test
    fun `rejects whitespace only`() {
        assertFalse(WakeWordDetector.isWakeWord("   "))
    }

    @Test
    fun `rejects unrelated english phrase`() {
        assertFalse(WakeWordDetector.isWakeWord("Hello, how are you?"))
    }

    @Test
    fun `rejects partial match - only hey`() {
        assertFalse(WakeWordDetector.isWakeWord("hey there"))
    }

    @Test
    fun `rejects Hindi question without wake word`() {
        assertFalse(WakeWordDetector.isWakeWord("Mera BP check karne ka time kya hai?"))
    }
}
