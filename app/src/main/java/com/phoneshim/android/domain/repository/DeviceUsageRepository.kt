package com.phoneshim.android.domain.repository

interface DeviceUsageRepository {
    suspend fun uploadDeviceUsage(totalUsedMinutes: Int, date: String? = null): Result<Unit>
}
