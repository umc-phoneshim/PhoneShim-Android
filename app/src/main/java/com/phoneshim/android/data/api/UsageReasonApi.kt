package com.phoneshim.android.data.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/** UsageReason 도메인 API. 명세서 8_UsageLog_UsageReason 기준. */
interface UsageReasonApi {

    /** 상태: 구현완료. 입력 가능 시간대(당일 22:00~익일 10:00)를 벗어나면 403. */
    @POST("api/usage-reasons")
    suspend fun submitUsageReason(
        @Body request: UsageReasonRequest,
    ): ApiResponse<UsageReasonResponse>

    /** 상태: 예정. 월 단위 사유 입력 여부. */
    @GET("api/usage-reasons/calendar")
    suspend fun getReasonCalendar(
        @Query("month") month: String,
    ): ApiResponse<List<ReasonCalendarResponse>>
}

data class UsageReasonRequest(
    val monitoredAppId: String,
    val date: String,
    val timeRangeStart: String,
    val timeRangeEnd: String,
    val reason: String,
    val usageLogId: String? = null,
)

data class UsageReasonResponse(
    val id: String,
    val monitoredAppId: String,
    val date: String,
    val timeRangeStart: String,
    val timeRangeEnd: String,
    val reason: String,
)

data class ReasonCalendarResponse(
    val date: String,
    val hasReason: Boolean,
)
