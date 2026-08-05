package com.phoneshim.android.domain.usecase

import com.phoneshim.android.domain.repository.DeviceUsageRepository
import javax.inject.Inject

class UploadDeviceUsageUseCase @Inject constructor(
    private val deviceUsageRepository: DeviceUsageRepository,
) {
    suspend operator fun invoke(totalUsedMinutes: Int, date: String? = null): Result<Unit> =
        deviceUsageRepository.uploadDeviceUsage(totalUsedMinutes, date)
}
