package com.phoneshim.android.data.repository

import com.phoneshim.android.data.api.ReportApi
import com.phoneshim.android.data.api.UsageReasonRequest
import com.phoneshim.android.domain.model.DailyReport
import com.phoneshim.android.domain.model.TimetableEntry
import com.phoneshim.android.domain.repository.ReportRepository
import javax.inject.Inject

class ReportRepositoryImpl @Inject constructor(
    private val reportApi: ReportApi,
) : ReportRepository {
    override suspend fun getDailyReport(date: String): Result<DailyReport> = runCatching {
        val response = reportApi.getDailyReport(date)
        DailyReport(
            date = response.date,
            timetable = response.timetable.map {
                TimetableEntry(
                    id = it.id,
                    appName = it.appName,
                    startedAt = it.startedAt,
                    durationMinutes = it.durationMinutes,
                    usageReason = it.usageReason,
                )
            },
            aiSuggestion = response.aiSuggestion,
            summary = response.summary,
        )
    }

    override suspend fun submitUsageReason(entryId: String, reason: String): Result<Unit> = runCatching {
        reportApi.submitUsageReason(entryId, UsageReasonRequest(reason))
    }
}
