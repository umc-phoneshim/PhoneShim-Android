package com.phoneshim.android.domain.repository

import com.phoneshim.android.domain.model.DailyUsageLog
import com.phoneshim.android.domain.model.UsageStatus

interface UsageLogRepository {
    suspend fun getUsageLogs(date: String? = null): Result<List<DailyUsageLog>>
    suspend fun getUsageStatus(): Result<List<UsageStatus>>
    suspend fun uploadUsageLog(
        monitoredAppId: String,
        usedMinutes: Int,
        entryCount: Int,
        date: String? = null,
    ): Result<Unit>
}
