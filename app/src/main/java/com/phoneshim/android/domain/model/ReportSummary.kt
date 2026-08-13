package com.phoneshim.android.domain.model

/**
 * 기간별 사용 사유 요약. GET /api/reports/summary 응답입니다.
 *
 * 사유별 총 사용 시간과, 그 사유 안에서 앱별로 얼마나 썼는지를 담습니다.
 * "어플 사용 요약" 막대 차트가 이 데이터를 그대로 사용합니다.
 */
data class ReportSummary(
    val range: ReportRange,
    val from: String,
    val to: String,
    val reasons: List<ReasonSummary>,
) {
    val totalMinutes: Int get() = reasons.sumOf { it.totalMinutes }
    val isEmpty: Boolean get() = reasons.isEmpty()
}

data class ReasonSummary(
    val reason: UsageReasonCode,
    val totalMinutes: Int,
    val apps: List<ReasonAppUsage>,
)

data class ReasonAppUsage(
    val monitoredAppId: String,
    val appName: String,
    val minutes: Int,
)

/** 서버 range 파라미터. day = 당일, week = 최근 7일, month = 최근 30일. */
enum class ReportRange(val value: String) {
    DAY("day"),
    WEEK("week"),
    MONTH("month"),
    ;

    companion object {
        fun from(raw: String?): ReportRange? = entries.firstOrNull { it.value == raw }
    }
}
