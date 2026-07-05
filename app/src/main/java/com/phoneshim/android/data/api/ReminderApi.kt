package com.phoneshim.android.data.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ReminderApi {
    @GET("reminders")
    suspend fun getReminders(): List<ReminderResponse>

    @POST("reminders")
    suspend fun addReminder(@Body request: ReminderResponse)
}

data class ReminderResponse(
    val id: String,
    val title: String,
    val scheduledAt: Long,
)
