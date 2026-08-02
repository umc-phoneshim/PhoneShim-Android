package com.phoneshim.android.domain.usecase

import com.phoneshim.android.domain.model.UsageStatus
import com.phoneshim.android.domain.repository.UsageLogRepository
import javax.inject.Inject

class GetUsageStatusUseCase @Inject constructor(
    private val usageLogRepository: UsageLogRepository,
) {
    suspend operator fun invoke(): Result<List<UsageStatus>> = usageLogRepository.getUsageStatus()
}
