package com.phoneshim.android.domain.usecase

import com.phoneshim.android.domain.model.DailyUsageLog
import com.phoneshim.android.domain.repository.UsageLogRepository
import javax.inject.Inject

class GetUsageLogsUseCase @Inject constructor(
    private val usageLogRepository: UsageLogRepository,
) {
    suspend operator fun invoke(date: String? = null): Result<List<DailyUsageLog>> =
        usageLogRepository.getUsageLogs(date)
}
