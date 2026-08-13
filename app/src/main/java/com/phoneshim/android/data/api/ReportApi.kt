package com.phoneshim.android.data.api

import com.phoneshim.android.data.api.common.ApiResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * 리포트 화면 전용 집계 API. 백엔드 src/domains/{usageLog,usageSession,report} 구현을 직접 확인해 맞췄습니다.
 *
 * 앱별 사용량 조회(GET /api/usage-logs, /status)는 [UsageLogApi] 로 일원화했습니다.
 * 여기에는 리포트에서만 쓰는 집계 엔드포인트만 둡니다.
 */
interface ReportApi {

    /** usageLogRouter: GET /calendar — 구현완료. 그 달에 전체 목표를 달성한 날짜 목록. */
    @GET("api/usage-logs/calendar")
    suspend fun getUsageCalendar(
        @Query("month") month: String,
    ): ApiResponse<UsageCalendarResponse>

    /**
     * usageSessionRouter: GET / — 구현완료.
     * 앱 사용 구간(startTime~endTime)을 그대로 반환합니다. 타임테이블 차트의 원본 데이터입니다.
     */
    @GET("api/usage-sessions")
    suspend fun getUsageSessions(
        @Query("date") date: String? = null,
    ): ApiResponse<List<UsageSessionResponse>>

    /**
     * usageSessionRouter: POST / — 사용 구간 저장.
     * 차단 엔진이 UsageEvents 로 잡은 구간을 그대로 올립니다.
     * 같은 monitoredAppId + startTime 이면 서버가 덮어씁니다(아직 안 끝난 세션이 길어지는 경우).
     */
    @POST("api/usage-sessions")
    suspend fun postUsageSession(
        @Body request: UsageSessionCreateRequest,
    ): ApiResponse<UsageSessionResponse>

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

    /**
     * reportRouter: GET /suggestion — "쉼이의 제안".
     * AI가 아니라 목표 대비 사용량으로 정해진 문구 템플릿을 골라 내려줍니다.
     */
    @GET("api/reports/suggestion")
    suspend fun getReportSuggestion(
        @Query("date") date: String? = null,
    ): ApiResponse<ReportSuggestionResponse>
}

// Gson 은 Kotlin 기본값을 적용하지 않아 응답에 없는 필드가 null 로 들어옵니다.
// 그래서 아래 DTO 는 전부 nullable 로 받고 도메인 변환에서 보정합니다.

data class UsageCalendarResponse(
    val month: String? = null,
    val achievedDates: List<String>? = null,
)

/** startTime / endTime 은 ISO 8601(오프셋 포함) 문자열입니다. */
data class UsageSessionCreateRequest(
    val monitoredAppId: String,
    val startTime: String,
    val endTime: String,
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

/** suggestionType 은 TOTAL_EXCEEDED / APP_EXCEEDED / ACHIEVED / NO_GOAL 중 하나입니다. */
data class ReportSuggestionResponse(
    val suggestionType: String? = null,
    val message: String? = null,
    val excessMinutes: Int? = null,
    val appName: String? = null,
)
