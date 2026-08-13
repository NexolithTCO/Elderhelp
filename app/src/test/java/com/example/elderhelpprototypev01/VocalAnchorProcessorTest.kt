package com.example.elderhelpprototypev01.voice

import com.example.elderhelpprototypev01.model.VocalAnchorAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for [VocalAnchorProcessor].
 *
 * Verifies that all four anchors are detected correctly for:
 * - English phrases
 * - Hindi Devanagari phrases
 * - Hinglish transliterations
 * - Mixed-case and punctuated inputs
 * - Non-anchor inputs return null
 */
class VocalAnchorProcessorTest {

    // ---- REPEAT ----

    @Test
    fun `repeat - English exact`() {
        assertEquals(VocalAnchorAction.REPEAT, VocalAnchorProcessor.detect("repeat"))
    }

    @Test
    fun `repeat - Hindi Devanagari`() {
        assertEquals(VocalAnchorAction.REPEAT, VocalAnchorProcessor.detect("फिर से बोलो"))
    }

    @Test
    fun `repeat - Hinglish phir se bolo`() {
        assertEquals(VocalAnchorAction.REPEAT, VocalAnchorProcessor.detect("phir se bolo"))
    }

    @Test
    fun `repeat - mixed case`() {
        assertEquals(VocalAnchorAction.REPEAT, VocalAnchorProcessor.detect("REPEAT please"))
    }

    // ---- GO_BACK ----

    @Test
    fun `go back - English`() {
        assertEquals(VocalAnchorAction.GO_BACK, VocalAnchorProcessor.detect("go back"))
    }

    @Test
    fun `go back - Hindi Devanagari`() {
        assertEquals(VocalAnchorAction.GO_BACK, VocalAnchorProcessor.detect("पीछे जाओ"))
    }

    @Test
    fun `go back - Hinglish peeche jao`() {
        assertEquals(VocalAnchorAction.GO_BACK, VocalAnchorProcessor.detect("peeche jao"))
    }

    @Test
    fun `go back - wapas`() {
        assertEquals(VocalAnchorAction.GO_BACK, VocalAnchorProcessor.detect("wapas"))
    }

    // ---- STOP ----

    @Test
    fun `stop - English`() {
        assertEquals(VocalAnchorAction.STOP, VocalAnchorProcessor.detect("stop"))
    }

    @Test
    fun `stop - Hindi Devanagari ruko`() {
        assertEquals(VocalAnchorAction.STOP, VocalAnchorProcessor.detect("रुको"))
    }

    @Test
    fun `stop - Hinglish ruko`() {
        assertEquals(VocalAnchorAction.STOP, VocalAnchorProcessor.detect("ruko"))
    }

    @Test
    fun `stop - bas`() {
        assertEquals(VocalAnchorAction.STOP, VocalAnchorProcessor.detect("bas"))
    }

    // ---- NEXT_STEP ----

    @Test
    fun `next step - English`() {
        assertEquals(VocalAnchorAction.NEXT_STEP, VocalAnchorProcessor.detect("what should i do next"))
    }

    @Test
    fun `next step - Hindi Devanagari`() {
        assertEquals(VocalAnchorAction.NEXT_STEP, VocalAnchorProcessor.detect("अब क्या करना है"))
    }

    @Test
    fun `next step - Hinglish ab kya karna hai`() {
        assertEquals(VocalAnchorAction.NEXT_STEP, VocalAnchorProcessor.detect("ab kya karna hai"))
    }

    @Test
    fun `next step - short aage kya`() {
        assertEquals(VocalAnchorAction.NEXT_STEP, VocalAnchorProcessor.detect("aage kya"))
    }

    // ---- No anchor ----

    @Test
    fun `no anchor - regular question English`() {
        assertNull(VocalAnchorProcessor.detect("How do I pay my electricity bill?"))
    }

    @Test
    fun `no anchor - Hinglish request`() {
        assertNull(VocalAnchorProcessor.detect("Mera BP check karne ka time kya hai?"))
    }

    @Test
    fun `no anchor - blank input`() {
        assertNull(VocalAnchorProcessor.detect(""))
    }

    @Test
    fun `no anchor - whitespace only`() {
        assertNull(VocalAnchorProcessor.detect("   "))
    }
}
