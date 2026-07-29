package com.phoneshim.android.data.repository

import com.phoneshim.android.data.api.UsageReasonApi
import com.phoneshim.android.data.api.UsageReasonRequest
import com.phoneshim.android.data.api.runCatchingApi
import com.phoneshim.android.data.api.unwrap
import com.phoneshim.android.domain.model.ReasonCalendarDay
import com.phoneshim.android.domain.model.UsageReasonEntry
import com.phoneshim.android.domain.repository.ReportUsageReasonRepository
import javax.inject.Inject

class ReportUsageReasonRepositoryImpl @Inject constructor(
    private val usageReasonApi: UsageReasonApi,
) : ReportUsageReasonRepository {

    override suspend fun submitUsageReason(entry: UsageReasonEntry): Result<Unit> = runCatchingApi {
        usageReasonApi.submitUsageReason(
            UsageReasonRequest(
                monitoredAppId = entry.monitoredAppId,
                date = entry.date,
                timeRangeStart = entry.timeRangeStart,
                timeRangeEnd = entry.timeRangeEnd,
                reason = entry.reason,
                usageLogId = entry.usageLogId,
            ),
        ).unwrap()
        Unit
    }

    override suspend fun getReasonCalendar(month: String): Result<List<ReasonCalendarDay>> =
        runCatchingApi {
            usageReasonApi.getReasonCalendar(month).unwrap().map {
                ReasonCalendarDay(date = it.date, hasReason = it.hasReason)
            }
        }
}
