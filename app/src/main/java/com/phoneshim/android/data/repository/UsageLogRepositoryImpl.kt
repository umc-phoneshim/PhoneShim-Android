package com.phoneshim.android.data.repository

import com.phoneshim.android.data.api.UsageAppStatusResponse
import com.phoneshim.android.data.api.UsageLogApi
import com.phoneshim.android.data.api.UsageLogEntryResponse
import com.phoneshim.android.data.api.UsageLogUpsertRequest
import com.phoneshim.android.data.api.common.ApiCallExecutor
import com.phoneshim.android.domain.model.DailyUsageLog
import com.phoneshim.android.domain.model.UsageStatus
import com.phoneshim.android.domain.repository.UsageLogRepository
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

class UsageLogRepositoryImpl @Inject constructor(
    private val usageLogApi: UsageLogApi,
    private val apiCallExecutor: ApiCallExecutor,
) : UsageLogRepository {

    override suspend fun getUsageLogs(date: String?): Result<List<DailyUsageLog>> =
        runCatchingApiCall { apiCallExecutor.execute { usageLogApi.getUsageLogs(date) } }
            .map { entries -> entries.map { it.toDomain() } }

    override suspend fun getUsageStatus(): Result<List<UsageStatus>> =
        runCatchingApiCall { apiCallExecutor.execute { usageLogApi.getUsageStatus() } }
            .map { statuses -> statuses.map { it.toDomain() } }

    override suspend fun uploadUsageLog(
        monitoredAppId: String,
        usedMinutes: Int,
        entryCount: Int,
        date: String?,
    ): Result<Unit> = runCatchingApiCall {
        apiCallExecutor.execute {
            usageLogApi.putUsageLog(
                UsageLogUpsertRequest(
                    monitoredAppId = monitoredAppId,
                    date = date,
                    usedMinutes = usedMinutes,
                    entryCount = entryCount,
                ),
            )
        }
    }.map { Unit }

    // TODO(#53 머지 후): apiCallExecutor.executeAsResult { ... } 로 교체하고 이 헬퍼는 제거.
    private suspend inline fun <T> runCatchingApiCall(crossinline block: suspend () -> T): Result<T> =
        try {
            Result.success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            Result.failure(e)
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
