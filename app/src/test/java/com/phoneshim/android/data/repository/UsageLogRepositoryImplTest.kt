package com.phoneshim.android.data.repository

import com.google.gson.Gson
import com.phoneshim.android.data.api.UsageLogApi
import com.phoneshim.android.data.api.UsageLogResponse
import com.phoneshim.android.data.api.UsageLogUpsertRequest
import com.phoneshim.android.data.api.UsageStatusResponse
import com.phoneshim.android.data.api.common.ApiCallExecutor
import com.phoneshim.android.data.api.common.ApiError
import com.phoneshim.android.data.api.common.ApiException
import com.phoneshim.android.data.api.common.ApiResponse
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UsageLogRepositoryImplTest {

    private val api = FakeUsageLogApi()
    private val repository = UsageLogRepositoryImpl(
        usageLogApi = api,
        apiCallExecutor = ApiCallExecutor(Gson()),
    )

    @Test
    fun `사용 현황을 도메인 모델로 돌려준다`() = runTest {
        api.statusResponse = ApiResponse(
            success = true,
            data = listOf(
                UsageStatusResponse(
                    monitoredAppId = "m-1",
                    appName = "카카오톡",
                    packageName = "com.kakao.talk",
                    usedMinutes = 30,
                    entryCount = 3,
                ),
            ),
        )

        val statuses = repository.getUsageStatus().getOrThrow()

        assertEquals(1, statuses.size)
        assertEquals("m-1", statuses[0].monitoredAppId)
        assertEquals("com.kakao.talk", statuses[0].packageName)
        assertEquals(30, statuses[0].usedMinutes)
    }

    @Test
    fun `사용 현황 조회 실패 시 결과에 오류를 담는다`() = runTest {
        api.statusResponse = ApiResponse(
            success = false,
            error = ApiError(code = "SERVICE_UNAVAILABLE", message = "Try again later"),
        )

        val result = repository.getUsageStatus()

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull() as ApiException.Server
        assertEquals("SERVICE_UNAVAILABLE", error.error.code)
    }

    private class FakeUsageLogApi : UsageLogApi {
        lateinit var statusResponse: ApiResponse<List<UsageStatusResponse>>

        override suspend fun getUsageLogs(date: String?): ApiResponse<List<UsageLogResponse>> =
            throw UnsupportedOperationException()

        override suspend fun getUsageStatus(): ApiResponse<List<UsageStatusResponse>> = statusResponse

        override suspend fun putUsageLog(
            request: UsageLogUpsertRequest,
        ): ApiResponse<UsageLogResponse> = throw UnsupportedOperationException()
    }
}
