package com.phoneshim.android.data.repository

import com.phoneshim.android.data.api.UsageReasonApi
import com.phoneshim.android.data.api.UsageReasonRequest
import com.phoneshim.android.data.api.common.ApiCallExecutor
import com.phoneshim.android.domain.model.UsageReasonEntry
import com.phoneshim.android.domain.repository.ReportUsageReasonRepository
import javax.inject.Inject

class ReportUsageReasonRepositoryImpl @Inject constructor(
    private val usageReasonApi: UsageReasonApi,
    private val apiCallExecutor: ApiCallExecutor,
) : ReportUsageReasonRepository {

    override suspend fun submitUsageReason(entry: UsageReasonEntry): Result<Unit> =
        apiCallExecutor.executeAsResult {
            usageReasonApi.submitUsageReason(
                UsageReasonRequest(
                    monitoredAppId = entry.monitoredAppId,
                    date = entry.date,
                    timeRangeStart = entry.timeRangeStart,
                    timeRangeEnd = entry.timeRangeEnd,
                    reasonCodes = entry.reasonCodes.map { it.name },
                    usageLogId = entry.usageLogId,
                ),
            )
        }.map { }
}
