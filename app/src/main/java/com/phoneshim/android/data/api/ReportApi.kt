package com.phoneshim.android.data.api

import com.phoneshim.android.data.api.common.ApiResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * 리포트 화면이 사용하는 API. 명세서 8_UsageLog_UsageReason / 9_AlertSetting_Report_AI 기준.
 *
 * 기존 코드에 있던 reports/{date}, reports/timetable/{entryId}/reason 은
 * 서버에 존재하지 않는 경로라 전부 교체했습니다.
 */
interface ReportApi {

    /** 상태: 구현완료. 일별 주의 앱 사용량 (폴 담당). date 생략 시 KST 오늘. */
    @GET("api/usage-logs")
    suspend fun getUsageLogs(
        @Query("date") date: String? = null,
    ): ApiResponse<List<UsageLogResponse>>

    /** 상태: 구현완료. 오늘 사용 현황. 앱 이름/패키지명/목표가 함께 내려옵니다. */
    @GET("api/usage-logs/status")
    suspend fun getUsageStatus(): ApiResponse<List<UsageStatusResponse>>

    /** 상태: 예정. 주간/월간 사용 사유 요약. 데이터 부족 시 422 INSUFFICIENT_REPORT_DATA. */
    @GET("api/reports/summary")
    suspend fun getReportSummary(
        @Query("range") range: String,
        @Query("date") date: String? = null,
    ): ApiResponse<ReportSummaryResponse>

    /**
     * 상태: 예정. "쉼이의 제안".
     * 백엔드가 사용 로그와 사용 사유를 분석해 완성된 문구를 내려주고, 화면은 출력만 합니다.
     * 데이터 부족 시 422 INSUFFICIENT_AI_FEEDBACK_DATA.
     */
    @POST("api/ai/daily-feedback")
    suspend fun getDailyFeedback(
        @Body request: DailyFeedbackRequest,
    ): ApiResponse<DailyFeedbackResponse>
}

data class UsageLogResponse(
    val id: String,
    val userId: String? = null,
    val monitoredAppId: String,
    val date: String,
    val usedMinutes: Int,
    val entryCount: Int,
)

data class UsageStatusResponse(
    val monitoredAppId: String,
    val appName: String,
    val packageName: String,
    val appIcon: String? = null,
    val sortOrder: Int = 0,
    val targetMinutes: Int? = null,
    val targetCount: Int? = null,
    val usedMinutes: Int,
    val entryCount: Int,
)

data class ReportSummaryResponse(
    val range: String,
    val from: String,
    val to: String,
    val keywords: List<ReasonKeywordResponse> = emptyList(),
    val summary: String,
)

data class ReasonKeywordResponse(
    val text: String,
    val count: Int,
)

/** date 생략 시 KST 오늘 기준. */
data class DailyFeedbackRequest(
    val date: String? = null,
)

data class DailyFeedbackResponse(
    val date: String,
    val feedback: String,
)
