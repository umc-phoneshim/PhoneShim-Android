package com.phoneshim.android.data.api

import com.phoneshim.android.data.api.common.ApiResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH

interface AlertSettingApi {
    @GET("api/alert-settings")
    suspend fun getAlertSetting(): ApiResponse<AlertSettingResponse>

    @PATCH("api/alert-settings")
    suspend fun updateAlertSetting(
        @Body request: UpdateAlertSettingRequest,
    ): ApiResponse<AlertSettingResponse>
}

data class UpdateAlertSettingRequest(
    val alertTimeMinutes: Int,
)

data class AlertSettingResponse(
    val id: String,
    val userId: String,
    val enabled: Boolean,
    val alertTimeMinutes: Int,
    val createdAt: String,
    val updatedAt: String,
)
