package com.phoneshim.android.ui.features.setgoal.component

import org.junit.Assert.assertEquals
import org.junit.Test

class TimeInputTest {

    @Test
    fun `숫자가 아닌 문자는 걸러진다`() {
        assertEquals("12", sanitizeTimeInput("1a2", current = "00", maxValue = MAX_MINUTE_VALUE))
        assertEquals("", sanitizeTimeInput("abc", current = "00", maxValue = MAX_MINUTE_VALUE))
    }

    @Test
    fun `두 자리까지만 받는다`() {
        assertEquals("12", sanitizeTimeInput("123", current = "12", maxValue = MAX_MINUTE_VALUE))
    }

    @Test
    fun `다 지우면 빈 문자열이 된다`() {
        assertEquals("", sanitizeTimeInput("", current = "07", maxValue = MAX_HOUR_VALUE))
    }

    @Test
    fun `시는 23을 넘기면 직전 값을 유지한다`() {
        assertEquals("23", sanitizeTimeInput("23", current = "2", maxValue = MAX_HOUR_VALUE))
        assertEquals("9", sanitizeTimeInput("95", current = "9", maxValue = MAX_HOUR_VALUE))
        assertEquals("2", sanitizeTimeInput("24", current = "2", maxValue = MAX_HOUR_VALUE))
    }

    @Test
    fun `분은 59까지 받고 넘기면 직전 값을 유지한다`() {
        assertEquals("59", sanitizeTimeInput("59", current = "5", maxValue = MAX_MINUTE_VALUE))
        assertEquals("5", sanitizeTimeInput("60", current = "5", maxValue = MAX_MINUTE_VALUE))
    }

    @Test
    fun `0과 선행 0 입력을 허용한다`() {
        assertEquals("0", sanitizeTimeInput("0", current = "", maxValue = MAX_HOUR_VALUE))
        assertEquals("00", sanitizeTimeInput("00", current = "0", maxValue = MAX_HOUR_VALUE))
        assertEquals("05", sanitizeTimeInput("05", current = "0", maxValue = MAX_HOUR_VALUE))
    }
}
