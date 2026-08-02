package com.phoneshim.android.data.repository

import com.phoneshim.android.data.api.DashboardApi
import com.phoneshim.android.data.api.DashboardSummaryResponse
import com.phoneshim.android.data.api.runCatchingApi
import com.phoneshim.android.data.api.unwrap
import com.phoneshim.android.domain.model.DashboardSummary
import com.phoneshim.android.domain.repository.DashboardRepository
import javax.inject.Inject

class DashboardRepositoryImpl @Inject constructor(
    private val dashboardApi: DashboardApi,
) : DashboardRepository {

    override suspend fun getDailySummary(): Result<DashboardSummary> = runCatchingApi {
        dashboardApi.getDailySummary().unwrap().toDomain()
    }

    private fun DashboardSummaryResponse.toDomain(): DashboardSummary = DashboardSummary(
        date = date,
        targetMinutes = targetMinutes,
        usedMinutes = usedMinutes,
        remainingMinutes = remainingMinutes,
        isExceeded = isExceeded,
    )
}
