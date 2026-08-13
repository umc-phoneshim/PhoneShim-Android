package com.phoneshim.android.data.repository

import com.phoneshim.android.data.api.UsageLogApi
import com.phoneshim.android.data.api.UsageLogResponse
import com.phoneshim.android.data.api.UsageLogUpsertRequest
import com.phoneshim.android.data.api.UsageStatusResponse
import com.phoneshim.android.data.api.common.ApiCallExecutor
import com.phoneshim.android.domain.model.DailyUsageLog
import com.phoneshim.android.domain.model.UsageStatus
import com.phoneshim.android.domain.repository.UsageLogRepository
import javax.inject.Inject

class UsageLogRepositoryImpl @Inject constructor(
    private val usageLogApi: UsageLogApi,
    private val apiCallExecutor: ApiCallExecutor,
) : UsageLogRepository {

    override suspend fun getUsageLogs(date: String?): Result<List<DailyUsageLog>> =
        apiCallExecutor.executeAsResult { usageLogApi.getUsageLogs(date) }
            .map { entries -> entries.map { it.toDomain() } }

    override suspend fun getUsageStatus(): Result<List<UsageStatus>> =
        apiCallExecutor.executeAsResult { usageLogApi.getUsageStatus() }
            .map { statuses -> statuses.map { it.toDomain() } }

    override suspend fun uploadUsageLog(
        monitoredAppId: String,
        usedMinutes: Int,
        entryCount: Int,
        date: String?,
    ): Result<Unit> = apiCallExecutor.executeAsResult {
        usageLogApi.putUsageLog(
            UsageLogUpsertRequest(
                monitoredAppId = monitoredAppId,
                date = date,
                usedMinutes = usedMinutes,
                entryCount = entryCount,
            ),
        )
    }.map { Unit }

    // DTO 가 전부 nullable 이라(Gson 이 Kotlin 기본값을 무시함) 여기서 기본값으로 보정합니다.
    private fun UsageLogResponse.toDomain(): DailyUsageLog = DailyUsageLog(
        id = id.orEmpty(),
        monitoredAppId = monitoredAppId.orEmpty(),
        date = date.orEmpty(),
        usedMinutes = usedMinutes ?: 0,
        entryCount = entryCount ?: 0,
    )

    private fun UsageStatusResponse.toDomain(): UsageStatus = UsageStatus(
        monitoredAppId = monitoredAppId.orEmpty(),
        appName = appName.orEmpty(),
        packageName = packageName.orEmpty(),
        appIcon = appIcon,
        sortOrder = sortOrder ?: 0,
        targetMinutes = targetMinutes,
        targetCount = targetCount,
        usedMinutes = usedMinutes ?: 0,
        entryCount = entryCount ?: 0,
    )
}
