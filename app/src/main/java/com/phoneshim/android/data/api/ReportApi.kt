package com.phoneshim.android.data.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ReportApi {
    @GET("reports/{date}")
    suspend fun getDailyReport(@Path("date") date: String): DailyReportResponse

    @POST("reports/timetable/{entryId}/reason")
    suspend fun submitUsageReason(@Path("entryId") entryId: String, @Body reason: UsageReasonRequest)
}

data class DailyReportResponse(
    val date: String,
    val timetable: List<TimetableEntryResponse>,
    val aiSuggestion: String,
    val summary: String,
)

data class TimetableEntryResponse(
    val id: String,
    val appName: String,
    val startedAt: Long,
    val durationMinutes: Int,
    val usageReason: String?,
)

data class UsageReasonRequest(val reason: String)
