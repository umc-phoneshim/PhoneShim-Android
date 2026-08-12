package com.phoneshim.android.domain.model

import java.time.LocalDateTime

/**
 * 앱 사용 구간 한 건. GET /api/usage-sessions 응답입니다.
 *
 * 타임테이블 차트가 필요한 "몇 시부터 몇 시까지 썼는지" 정보가 여기 들어 있습니다.
 * 일별 합계만 주는 UsageLog 와 달리 시간 구간이 그대로 보존됩니다.
 */
data class UsageSession(
    val id: String,
    val monitoredAppId: String,
    val date: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
) {
    val durationMinutes: Int
        get() = java.time.Duration.between(startTime, endTime).toMinutes().toInt().coerceAtLeast(0)
}
