package com.phoneshim.android.ui.common

import org.junit.Assert.assertEquals
import org.junit.Test

class InteractiveTimeSegmentInputTest {

    @Test
    fun `two digit input keeps the entered order`() {
        assertEquals("21", sanitizeTimeSegment("21", "2", 23))
    }

    @Test
    fun `value over the range keeps the previous input`() {
        assertEquals("23", sanitizeTimeSegment("29", "23", 23))
        assertEquals("59", sanitizeTimeSegment("60", "59", 59))
    }

    @Test
    fun `empty input remains empty so it can be retyped`() {
        assertEquals("", sanitizeTimeSegment("", "12", 23))
    }

    @Test
    fun `first digit replaces existing value even when touch moves the cursor`() {
        assertEquals("2", replaceOnFirstInput("00", "020"))
        assertEquals("2", replaceOnFirstInput("00", "002"))
        assertEquals("2", replaceOnFirstInput("00", "2"))
    }
}
