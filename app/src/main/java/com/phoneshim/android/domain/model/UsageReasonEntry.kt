package com.phoneshim.android.domain.model

/**
 * 사용 사유 입력 항목. POST /api/usage-reasons 요청 모델.
 *
 * 사유는 자유 입력이 아니라 [UsageReasonCode] 5개 중에서 고르며, 한 시간 블록에
 * 여러 개를 선택할 수 있습니다. 서버는 고른 코드마다 행을 하나씩 저장합니다.
 *
 * 입력 가능 시간은 KST 기준 당일 22:00 ~ 익일 10:00 이며, 벗어나면 서버가
 * USAGE_REASON_TIME_FORBIDDEN 을 내려줍니다.
 */
data class UsageReasonEntry(
    val monitoredAppId: String,
    /** 사용 날짜 YYYY-MM-DD */
    val date: String,
    /** 사용 시간 구간 시작 ISO 8601 */
    val timeRangeStart: String,
    /** 사용 시간 구간 종료 ISO 8601 */
    val timeRangeEnd: String,
    val reasonCodes: List<UsageReasonCode>,
    val usageLogId: String? = null,
) {
    init {
        require(monitoredAppId.isNotBlank()) { "주의 앱 ID가 비어 있습니다." }
        require(reasonCodes.isNotEmpty()) { "사용 이유를 하나 이상 선택해 주세요." }
        require(timeRangeStart.isNotBlank() && timeRangeEnd.isNotBlank()) {
            "사용 시간 구간이 비어 있습니다."
        }
    }

    companion object {
        /** 입력 가능 시작 시각(KST, 시). */
        const val INPUT_WINDOW_START_HOUR = 22

        /** 입력 가능 종료 시각(익일 KST, 시). */
        const val INPUT_WINDOW_END_HOUR = 10
    }
}
