package com.phoneshim.android.data.api

import com.phoneshim.android.data.api.common.ApiResponse
import retrofit2.http.GET

interface DashboardApi {

    @GET("api/dashboard/daily-summary")
    suspend fun getDailySummary(): ApiResponse<DashboardSummaryResponse>
}

data class DashboardSummaryResponse(
    val date: String,
    val targetMinutes: Int? = null,
    val usedMinutes: Int,
    val remainingMinutes: Int? = null,
    val isExceeded: Boolean,
)
