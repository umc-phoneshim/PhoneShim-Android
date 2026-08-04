package com.phoneshim.android.data.api

import com.phoneshim.android.data.api.common.ApiResponse
import retrofit2.http.Body
import retrofit2.http.PUT

interface DeviceUsageApi {

    @PUT("api/device-usage")
    suspend fun putDeviceUsage(
        @Body request: DeviceUsageUpsertRequest,
    ): ApiResponse<DeviceUsageResponse>
}

/** date 생략 시 KST 오늘 기준. */
data class DeviceUsageUpsertRequest(
    val date: String? = null,
    val totalUsedMinutes: Int,
)

data class DeviceUsageResponse(
    val id: String,
    val userId: String,
    val date: String,
    val totalUsedMinutes: Int,
    val createdAt: String,
    val updatedAt: String,
)
