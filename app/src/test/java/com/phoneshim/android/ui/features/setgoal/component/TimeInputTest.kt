package com.phoneshim.android.ui.features.setgoal.component

import com.phoneshim.android.ui.common.sanitizeTimeSegment
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * setgoal 이 공용 시간 입력에 넘기는 상한이 맞는지 확인한다.
 * 정제 규칙 자체는 InteractiveTimeSegmentInputTest 가 검증하고,
 * 여기서는 "시는 23까지, 분은 59까지"라는 화면 계약만 고정한다.
 */
class TimeInputTest {

    @Test
    fun `시는 23까지 받고 넘기면 직전 값을 유지한다`() {
        assertEquals("23", sanitizeTimeSegment("23", "2", MAX_HOUR_VALUE))
        assertEquals("2", sanitizeTimeSegment("24", "2", MAX_HOUR_VALUE))
        assertEquals("9", sanitizeTimeSegment("95", "9", MAX_HOUR_VALUE))
    }

    @Test
    fun `분은 59까지 받고 넘기면 직전 값을 유지한다`() {
        assertEquals("59", sanitizeTimeSegment("59", "5", MAX_MINUTE_VALUE))
        assertEquals("5", sanitizeTimeSegment("60", "5", MAX_MINUTE_VALUE))
    }

    @Test
    fun `숫자가 아닌 문자는 걸러지고 두 자리까지만 받는다`() {
        assertEquals("12", sanitizeTimeSegment("1a2", "00", MAX_MINUTE_VALUE))
        assertEquals("12", sanitizeTimeSegment("123", "12", MAX_MINUTE_VALUE))
        assertEquals("", sanitizeTimeSegment("abc", "00", MAX_MINUTE_VALUE))
    }

    @Test
    fun `선행 0 입력을 허용한다`() {
        assertEquals("00", sanitizeTimeSegment("00", "0", MAX_HOUR_VALUE))
        assertEquals("05", sanitizeTimeSegment("05", "0", MAX_HOUR_VALUE))
    }
}
