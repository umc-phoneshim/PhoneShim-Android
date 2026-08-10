package com.phoneshim.android.data.repository

import com.phoneshim.android.data.api.DashboardApi
import com.phoneshim.android.data.api.DashboardSummaryResponse
import com.phoneshim.android.data.api.common.ApiCallExecutor
import com.phoneshim.android.domain.model.DashboardSummary
import com.phoneshim.android.domain.repository.DashboardRepository
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

class DashboardRepositoryImpl @Inject constructor(
    private val dashboardApi: DashboardApi,
    private val apiCallExecutor: ApiCallExecutor,
) : DashboardRepository {

    override suspend fun getDailySummary(): Result<DashboardSummary> =
        runCatchingApiCall { apiCallExecutor.execute { dashboardApi.getDailySummary() } }
            .map { it.toDomain() }

    // TODO(#53 머지 후): apiCallExecutor.executeAsResult { ... } 로 교체하고 이 헬퍼는 제거.
    private suspend inline fun <T> runCatchingApiCall(crossinline block: suspend () -> T): Result<T> =
        try {
            Result.success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            Result.failure(e)
        }

    private fun DashboardSummaryResponse.toDomain(): DashboardSummary = DashboardSummary(
        date = date,
        targetMinutes = targetMinutes,
        usedMinutes = usedMinutes,
        remainingMinutes = remainingMinutes,
        isExceeded = isExceeded,
    )
}
