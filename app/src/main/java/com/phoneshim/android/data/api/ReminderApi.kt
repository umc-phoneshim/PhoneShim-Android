package com.phoneshim.android.data.api

import com.phoneshim.android.data.api.common.ApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/** API 명세서 7_Reminder_API 기준의 Reminder CRUD 계약. */
interface ReminderApi {
    @POST("api/reminders")
    suspend fun createReminder(
        @Body request: CreateReminderRequest,
    ): ApiResponse<ReminderResponse>

    @GET("api/reminders")
    suspend fun getReminders(
        @Query("date") date: String,
    ): ApiResponse<List<ReminderResponse>>

    @GET("api/reminders/{id}")
    suspend fun getReminder(
        @Path("id") id: String,
    ): ApiResponse<ReminderResponse>

    @PATCH("api/reminders/{id}")
    suspend fun updateReminder(
        @Path("id") id: String,
        @Body request: UpdateReminderRequest,
    ): ApiResponse<ReminderResponse>

    /** 성공 응답이 204 No Content이므로 공통 envelope를 사용하지 않는다. */
    @DELETE("api/reminders/{id}")
    suspend fun deleteReminder(
        @Path("id") id: String,
    ): Response<Unit>
}

data class CreateReminderRequest(
    val date: String,
    val title: String,
    val startTime: String,
    val endTime: String,
    val restrictMode: String = ReminderRestrictModeValue.NONE,
    val restrictedAppIds: List<String> = emptyList(),
)

data class UpdateReminderRequest(
    val date: String? = null,
    val title: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val restrictMode: String? = null,
    val restrictedAppIds: List<String>? = null,
)

data class ReminderResponse(
    val id: String,
    val userId: String,
    val date: String,
    val title: String,
    val startTime: String,
    val endTime: String,
    val restrictMode: String,
    val restrictedAppIds: List<String> = emptyList(),
    val createdAt: String,
    val updatedAt: String,
)

object ReminderRestrictModeValue {
    const val NONE = "NONE"
    const val FULL_PHONE = "FULL_PHONE"
    const val SPECIFIC_APP = "SPECIFIC_APP"
}
