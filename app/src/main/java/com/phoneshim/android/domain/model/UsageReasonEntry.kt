package com.phoneshim.android.domain.model

/**
 * 사용 사유 입력 항목. POST /api/usage-reasons 요청 모델.
 *
 * 입력/수정 가능 시간은 당일 22:00 ~ 익일 10:00 이며, 벗어나면 서버가
 * 403 USAGE_REASON_TIME_FORBIDDEN 을 내려줍니다.
 */
data class UsageReasonEntry(
    val monitoredAppId: String,
    /** 사용 날짜 YYYY-MM-DD */
    val date: String,
    /** 사용 시간 구간 시작 ISO 8601 */
    val timeRangeStart: String,
    /** 사용 시간 구간 종료 ISO 8601 */
    val timeRangeEnd: String,
    val reason: String,
    val usageLogId: String? = null,
) {
    init {
        require(monitoredAppId.isNotBlank()) { "주의 앱 ID가 비어 있습니다." }
        require(reason.isNotBlank()) { "사용 이유가 비어 있습니다." }
        require(reason.length <= MAX_REASON_LENGTH) {
            "사용 이유는 ${MAX_REASON_LENGTH}자 이내로 입력해 주세요."
        }
    }

    companion object {
        const val MAX_REASON_LENGTH = 100

        /** 입력 가능 시작 시각(시). */
        const val INPUT_WINDOW_START_HOUR = 22

        /** 입력 가능 종료 시각(익일, 시). */
        const val INPUT_WINDOW_END_HOUR = 10
    }
}

/** GET /api/usage-reasons/calendar?month= 응답 항목. 날짜별 사유 입력 여부. */
data class ReasonCalendarDay(
    val date: String,
    val hasReason: Boolean,
)
