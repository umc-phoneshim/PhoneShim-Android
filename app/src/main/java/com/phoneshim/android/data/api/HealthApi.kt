package com.phoneshim.android.data.api

import com.phoneshim.android.data.api.common.ApiResponse
import retrofit2.http.GET

interface HealthApi {
    @GET("health")
    suspend fun getHealth(): ApiResponse<HealthResponse>
}

data class HealthResponse(
    val status: String,
)
