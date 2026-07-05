package com.phoneshim.android.data.api

import retrofit2.http.GET

interface MainApi {
    @GET("usage/today")
    suspend fun getTodayUsage(): List<AppUsageResponse>
}

data class AppUsageResponse(
    val packageName: String,
    val appName: String,
    val usageMinutes: Int,
)
