package com.phoneshim.android.data.repository

import com.phoneshim.android.data.api.DeviceUsageApi
import com.phoneshim.android.data.api.DeviceUsageUpsertRequest
import com.phoneshim.android.data.api.common.ApiCallExecutor
import com.phoneshim.android.domain.repository.DeviceUsageRepository
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

class DeviceUsageRepositoryImpl @Inject constructor(
    private val deviceUsageApi: DeviceUsageApi,
    private val apiCallExecutor: ApiCallExecutor,
) : DeviceUsageRepository {

    override suspend fun uploadDeviceUsage(totalUsedMinutes: Int, date: String?): Result<Unit> =
        runCatchingApiCall {
            apiCallExecutor.execute {
                deviceUsageApi.putDeviceUsage(
                    DeviceUsageUpsertRequest(date = date, totalUsedMinutes = totalUsedMinutes),
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
}
