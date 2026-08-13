package com.phoneshim.android.data.repository

import com.phoneshim.android.data.api.DashboardApi
import com.phoneshim.android.data.api.DashboardSummaryResponse
import com.phoneshim.android.data.api.common.ApiCallExecutor
import com.phoneshim.android.domain.model.DashboardSummary
import com.phoneshim.android.domain.repository.DashboardRepository
import javax.inject.Inject

class DashboardRepositoryImpl @Inject constructor(
    private val dashboardApi: DashboardApi,
    private val apiCallExecutor: ApiCallExecutor,
) : DashboardRepository {

    override suspend fun getDailySummary(): Result<DashboardSummary> =
        apiCallExecutor.executeAsResult { dashboardApi.getDailySummary() }
            .map { it.toDomain() }

    private fun DashboardSummaryResponse.toDomain(): DashboardSummary = DashboardSummary(
        date = date,
        targetMinutes = targetMinutes,
        usedMinutes = usedMinutes,
        remainingMinutes = remainingMinutes,
        isExceeded = isExceeded,
    )
}
