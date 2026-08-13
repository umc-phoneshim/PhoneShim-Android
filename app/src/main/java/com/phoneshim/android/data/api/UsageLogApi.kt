package com.phoneshim.android.data.api

import com.phoneshim.android.data.api.common.ApiResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Query

interface UsageLogApi {

    @GET("api/usage-logs")
    suspend fun getUsageLogs(
        @Query("date") date: String? = null,
    ): ApiResponse<List<UsageLogResponse>>

    @GET("api/usage-logs/status")
    suspend fun getUsageStatus(): ApiResponse<List<UsageStatusResponse>>

    @PUT("api/usage-logs")
    suspend fun putUsageLog(
        @Body request: UsageLogUpsertRequest,
    ): ApiResponse<UsageLogResponse>
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

/** date 생략 시 KST 오늘 기준. */
data class UsageLogUpsertRequest(
    val monitoredAppId: String,
    val date: String? = null,
    val usedMinutes: Int,
    val entryCount: Int,
)
