package com.phoneshim.android.domain.model

/** 주간/월간 사용 사유 요약. GET /api/reports/summary?range=week|month (예정). */
data class ReportSummary(
    val range: ReportRange,
    val from: String,
    val to: String,
    val keywords: List<ReasonKeyword>,
    val summary: String,
)

data class ReasonKeyword(
    val text: String,
    val count: Int,
)

enum class ReportRange(val value: String) {
    WEEK("week"),
    MONTH("month"),
}
