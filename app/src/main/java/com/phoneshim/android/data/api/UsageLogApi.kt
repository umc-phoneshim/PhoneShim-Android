package com.phoneshim.android.data.api

import com.phoneshim.android.data.api.common.ApiResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Query

/**
 * 사용량 로그 API. usageLogRouter 구현 기준입니다.
 *
 * 리포트 화면(ReportApi)에도 같은 경로가 중복 선언돼 있었는데, 3단계 교차연동에서
 * 이 파일로 일원화했습니다. 리포트는 [com.phoneshim.android.data.repository.ReportRepositoryImpl]
 * 이 이 API 를 직접 호출합니다. 리포트 전용 집계(calendar/sessions/summary/suggestion)만
 * ReportApi 에 남아 있습니다.
 */
interface UsageLogApi {

    /** 구현완료. 앱별 일별 사용량. date 생략 시 KST 오늘. */
    @GET("api/usage-logs")
    suspend fun getUsageLogs(
        @Query("date") date: String? = null,
    ): ApiResponse<List<UsageLogResponse>>

    /** 구현완료. 오늘 사용 현황. 앱 이름/패키지명/목표까지 함께 내려옵니다. */
    @GET("api/usage-logs/status")
    suspend fun getUsageStatus(): ApiResponse<List<UsageStatusResponse>>

    @PUT("api/usage-logs")
    suspend fun putUsageLog(
        @Body request: UsageLogUpsertRequest,
    ): ApiResponse<UsageLogResponse>
}

// Gson 은 Kotlin 기본값을 적용하지 않아 응답에 없는 필드가 null 로 들어옵니다.
// 그래서 아래 DTO 는 전부 nullable 로 받고 도메인 변환에서 보정합니다.

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

/** date 생략 시 KST 오늘 기준. */
data class UsageLogUpsertRequest(
    val monitoredAppId: String,
    val date: String? = null,
    val usedMinutes: Int,
    val entryCount: Int,
)
