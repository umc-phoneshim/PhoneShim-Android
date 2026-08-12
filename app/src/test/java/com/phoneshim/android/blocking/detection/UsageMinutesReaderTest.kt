package com.phoneshim.android.blocking.detection

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 진입 횟수 집계 규칙 검증.
 *
 * 기획 MAIN104: "진입 횟수는 1분 내 재진입 시 1회로 처리한다."
 * 서버 `PUT /api/usage-logs` 의 entryCount 에 그대로 올라가는 값이라
 * 경계값이 어긋나면 메인 화면과 리포트 숫자가 함께 틀어진다.
 */
class UsageMinutesReaderTest {

    private val base = 1_700_000_000_000L

    @Test
    fun `오늘 첫 진입은 새 진입으로 센다`() {
        assertTrue(isNewEntry(lastExitAt = null, enteredAt = base))
    }

    @Test
    fun `나가자마자 다시 들어오면 같은 진입으로 본다`() {
        assertFalse(isNewEntry(lastExitAt = base, enteredAt = base))
    }

    @Test
    fun `1분이 안 됐으면 같은 진입으로 본다`() {
        assertFalse(isNewEntry(lastExitAt = base, enteredAt = base + 59_999L))
    }

    @Test
    fun `정확히 1분이면 새 진입으로 센다`() {
        assertTrue(isNewEntry(lastExitAt = base, enteredAt = base + 60_000L))
    }

    @Test
    fun `1분을 넘겼으면 새 진입으로 센다`() {
        assertTrue(isNewEntry(lastExitAt = base, enteredAt = base + 60_001L))
    }

    @Test
    fun `한참 뒤에 들어오면 새 진입으로 센다`() {
        assertTrue(isNewEntry(lastExitAt = base, enteredAt = base + 3_600_000L))
    }

    @Test
    fun `재진입 판정 창은 1분이다`() {
        // 기획 값이 바뀌면 이 테스트가 먼저 깨지도록 상수 자체를 고정한다.
        assertTrue(REENTRY_WINDOW_MS == 60_000L)
    }
}