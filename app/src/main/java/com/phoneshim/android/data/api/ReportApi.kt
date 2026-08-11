package com.phoneshim.android.data.api

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 리포트 화면이 사용하는 API. 백엔드 src/domains/{usageLog,usageSession,report} 구현 기준입니다.
 *
 * 주의: "쉼이의 제안"(AI 피드백)은 백엔드에 AI 도메인 자체가 없어 아직 호출할 수 없습니다.
 * 엔드포인트가 생기면 여기에 추가하세요.
 */
interface ReportApi {

    /** usageLogRouter: GET / — 구현완료. 앱별 일별 사용량. date 생략 시 KST 오늘. */
    @GET("api/usage-logs")
    suspend fun getUsageLogs(
        @Query("date") date: String? = null,
    ): ApiResponse<List<UsageLogResponse>>

    /** usageLogRouter: GET /status — 구현완료. 오늘 사용 현황. 앱 이름/패키지명/목표 포함. */
    @GET("api/usage-logs/status")
    suspend fun getUsageStatus(): ApiResponse<List<UsageStatusResponse>>

    /**
     * usageSessionRouter: GET / — 구현완료.
     * 앱 사용 구간(startTime~endTime)을 그대로 반환합니다. 타임테이블 차트의 원본 데이터입니다.
     */
    @GET("api/usage-sessions")
    suspend fun getUsageSessions(
        @Query("date") date: String? = null,
    ): ApiResponse<List<UsageSessionResponse>>

    /**
     * reportRouter: GET /summary — 구현완료.
     * range 는 day | week | month 이고 각각 당일 / 최근 7일 / 최근 30일입니다.
     * 사용 사유별 집계와 사유 안에서의 앱별 구성비를 함께 반환합니다.
     */
    @GET("api/reports/summary")
    suspend fun getReportSummary(
        @Query("range") range: String,
        @Query("date") date: String? = null,
    ): ApiResponse<ReportSummaryResponse>

    /** usageLogRouter: GET /calendar — 구현완료. 그 달에 목표를 달성한 날짜 목록. */
    @GET("api/usage-logs/calendar")
    suspend fun getUsageCalendar(
        @Query("month") month: String,
    ): ApiResponse<UsageCalendarResponse>
}

data class UsageLogResponse(
    val id: String? = null,
    val userId: String? = null,
    val monitoredAppId: String? = null,
    val date: String? = null,
    val usedMinutes: Int? = null,
    val entryCount: Int? = null,
)

data class UsageStatusResponse(
    val monitoredAppId: String? = null,
    val appName: String? = null,
    val packageName: String? = null,
    val appIcon: String? = null,
    val sortOrder: Int? = null,
    val targetMinutes: Int? = null,
    val targetCount: Int? = null,
    val usedMinutes: Int? = null,
    val entryCount: Int? = null,
)

/** startTime / endTime 은 ISO 8601 문자열입니다. */
data class UsageSessionResponse(
    val id: String? = null,
    val monitoredAppId: String? = null,
    val date: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
)

data class ReportSummaryResponse(
    val range: String? = null,
    val from: String? = null,
    val to: String? = null,
    val reasons: List<ReasonSummaryResponse>? = null,
)

/** reason 은 LEISURE / COMMUTE / HABIT / INFO / OTHER 중 하나입니다. */
data class ReasonSummaryResponse(
    val reason: String? = null,
    val totalMinutes: Int? = null,
    val apps: List<ReasonAppUsageResponse>? = null,
)

data class ReasonAppUsageResponse(
    val monitoredAppId: String? = null,
    val appName: String? = null,
    val minutes: Int? = null,
)

data class UsageCalendarResponse(
    val month: String? = null,
    val achievedDates: List<String>? = null,
)
