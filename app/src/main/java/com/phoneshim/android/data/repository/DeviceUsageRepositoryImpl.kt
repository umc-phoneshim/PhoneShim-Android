package com.phoneshim.android.data.repository

import com.phoneshim.android.data.api.DeviceUsageApi
import com.phoneshim.android.data.api.DeviceUsageUpsertRequest
import com.phoneshim.android.data.api.runCatchingApi
import com.phoneshim.android.data.api.unwrap
import com.phoneshim.android.domain.repository.DeviceUsageRepository
import javax.inject.Inject

class DeviceUsageRepositoryImpl @Inject constructor(
    private val deviceUsageApi: DeviceUsageApi,
) : DeviceUsageRepository {

    override suspend fun uploadDeviceUsage(totalUsedMinutes: Int, date: String?): Result<Unit> =
        runCatchingApi {
            deviceUsageApi.putDeviceUsage(
                DeviceUsageUpsertRequest(date = date, totalUsedMinutes = totalUsedMinutes),
            ).unwrap()
            Unit
        }
}
