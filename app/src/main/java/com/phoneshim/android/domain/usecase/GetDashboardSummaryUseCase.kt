package com.phoneshim.android.domain.usecase

import com.phoneshim.android.domain.model.DashboardSummary
import com.phoneshim.android.domain.repository.DashboardRepository
import javax.inject.Inject

class GetDashboardSummaryUseCase @Inject constructor(
    private val dashboardRepository: DashboardRepository,
) {
    suspend operator fun invoke(): Result<DashboardSummary> = dashboardRepository.getDailySummary()
}
