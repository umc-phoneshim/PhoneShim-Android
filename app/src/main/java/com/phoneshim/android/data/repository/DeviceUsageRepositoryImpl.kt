package com.phoneshim.android.data.repository

import com.phoneshim.android.data.api.DeviceUsageApi
import com.phoneshim.android.data.api.DeviceUsageUpsertRequest
import com.phoneshim.android.data.api.common.ApiCallExecutor
import com.phoneshim.android.domain.repository.DeviceUsageRepository
import javax.inject.Inject

class DeviceUsageRepositoryImpl @Inject constructor(
    private val deviceUsageApi: DeviceUsageApi,
    private val apiCallExecutor: ApiCallExecutor,
) : DeviceUsageRepository {

    override suspend fun uploadDeviceUsage(totalUsedMinutes: Int, date: String?): Result<Unit> =
        apiCallExecutor.executeAsResult {
            deviceUsageApi.putDeviceUsage(
                DeviceUsageUpsertRequest(date = date, totalUsedMinutes = totalUsedMinutes),
            )
        }.map { Unit }
}
