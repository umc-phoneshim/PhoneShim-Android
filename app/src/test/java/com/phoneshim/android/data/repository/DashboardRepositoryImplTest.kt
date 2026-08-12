package com.phoneshim.android.data.repository

import com.google.gson.Gson
import com.phoneshim.android.data.api.DashboardApi
import com.phoneshim.android.data.api.DashboardSummaryResponse
import com.phoneshim.android.data.api.common.ApiCallExecutor
import com.phoneshim.android.data.api.common.ApiError
import com.phoneshim.android.data.api.common.ApiException
import com.phoneshim.android.data.api.common.ApiResponse
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DashboardRepositoryImplTest {

    private val api = FakeDashboardApi()
    private val repository = DashboardRepositoryImpl(
        dashboardApi = api,
        apiCallExecutor = ApiCallExecutor(Gson()),
    )

    @Test
    fun `일일 요약을 도메인 모델로 돌려준다`() = runTest {
        api.response = ApiResponse(
            success = true,
            data = DashboardSummaryResponse(
                date = "2026-08-12",
                targetMinutes = 210,
                usedMinutes = 90,
                remainingMinutes = 120,
                isExceeded = false,
            ),
        )

        val summary = repository.getDailySummary().getOrThrow()

        assertEquals("2026-08-12", summary.date)
        assertEquals(210, summary.targetMinutes)
        assertEquals(90, summary.usedMinutes)
        assertEquals(120, summary.remainingMinutes)
        assertEquals(false, summary.isExceeded)
    }

    @Test
    fun `서버 오류 응답은 실패로 전달한다`() = runTest {
        api.response = ApiResponse(
            success = false,
            error = ApiError(code = "SERVICE_UNAVAILABLE", message = "Try again later"),
        )

        val result = repository.getDailySummary()

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull() as ApiException.Server
        assertEquals("SERVICE_UNAVAILABLE", error.error.code)
    }

    private class FakeDashboardApi : DashboardApi {
        lateinit var response: ApiResponse<DashboardSummaryResponse>

        override suspend fun getDailySummary(): ApiResponse<DashboardSummaryResponse> = response
    }
}
