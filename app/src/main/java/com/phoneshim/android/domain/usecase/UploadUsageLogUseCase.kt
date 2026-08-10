package com.phoneshim.android.domain.usecase

import com.phoneshim.android.domain.repository.PackageMonitoredAppResolver
import com.phoneshim.android.domain.repository.UsageLogRepository
import javax.inject.Inject

class UploadUsageLogUseCase @Inject constructor(
    private val resolver: PackageMonitoredAppResolver,
    private val usageLogRepository: UsageLogRepository,
) {
    suspend operator fun invoke(
        packageName: String,
        usedMinutes: Int,
        entryCount: Int,
        date: String? = null,
    ): Result<Unit> {
        val monitoredAppId = resolver.resolve(packageName)
            .getOrElse { return Result.failure(it) }
            ?: return Result.failure(IllegalStateException("등록된 주의 앱이 아닙니다: $packageName"))

        return usageLogRepository.uploadUsageLog(
            monitoredAppId = monitoredAppId,
            usedMinutes = usedMinutes,
            entryCount = entryCount,
            date = date,
        )
    }
}
