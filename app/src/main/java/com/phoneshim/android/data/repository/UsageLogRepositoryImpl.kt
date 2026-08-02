package com.phoneshim.android.data.repository

import com.phoneshim.android.data.api.UsageAppStatusResponse
import com.phoneshim.android.data.api.UsageLogApi
import com.phoneshim.android.data.api.UsageLogEntryResponse
import com.phoneshim.android.data.api.UsageLogUpsertRequest
import com.phoneshim.android.data.api.runCatchingApi
import com.phoneshim.android.data.api.unwrap
import com.phoneshim.android.domain.model.DailyUsageLog
import com.phoneshim.android.domain.model.UsageStatus
import com.phoneshim.android.domain.repository.UsageLogRepository
import javax.inject.Inject

class UsageLogRepositoryImpl @Inject constructor(
    private val usageLogApi: UsageLogApi,
) : UsageLogRepository {

    override suspend fun getUsageLogs(date: String?): Result<List<DailyUsageLog>> = runCatchingApi {
        usageLogApi.getUsageLogs(date).unwrap().map { it.toDomain() }
    }

    override suspend fun getUsageStatus(): Result<List<UsageStatus>> = runCatchingApi {
        usageLogApi.getUsageStatus().unwrap().map { it.toDomain() }
    }

    override suspend fun uploadUsageLog(
        monitoredAppId: String,
        usedMinutes: Int,
        entryCount: Int,
        date: String?,
    ): Result<Unit> = runCatchingApi {
        usageLogApi.putUsageLog(
            UsageLogUpsertRequest(
                monitoredAppId = monitoredAppId,
                date = date,
                usedMinutes = usedMinutes,
                entryCount = entryCount,
            ),
        ).unwrap()
        Unit
    }

    private fun UsageLogEntryResponse.toDomain(): DailyUsageLog = DailyUsageLog(
        id = id,
        monitoredAppId = monitoredAppId,
        date = date,
        usedMinutes = usedMinutes,
        entryCount = entryCount,
    )

    private fun UsageAppStatusResponse.toDomain(): UsageStatus = UsageStatus(
        monitoredAppId = monitoredAppId,
        appName = appName,
        packageName = packageName,
        appIcon = appIcon,
        sortOrder = sortOrder,
        targetMinutes = targetMinutes,
        targetCount = targetCount,
        usedMinutes = usedMinutes,
        entryCount = entryCount,
    )
}
