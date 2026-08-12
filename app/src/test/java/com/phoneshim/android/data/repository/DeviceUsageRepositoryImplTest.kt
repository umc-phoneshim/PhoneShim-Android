package com.phoneshim.android.data.repository

import com.google.gson.Gson
import com.phoneshim.android.data.api.DeviceUsageApi
import com.phoneshim.android.data.api.DeviceUsageResponse
import com.phoneshim.android.data.api.DeviceUsageUpsertRequest
import com.phoneshim.android.data.api.common.ApiCallExecutor
import com.phoneshim.android.data.api.common.ApiError
import com.phoneshim.android.data.api.common.ApiException
import com.phoneshim.android.data.api.common.ApiResponse
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeviceUsageRepositoryImplTest {

    private val api = FakeDeviceUsageApi()
    private val repository = DeviceUsageRepositoryImpl(
        deviceUsageApi = api,
        apiCallExecutor = ApiCallExecutor(Gson()),
    )

    @Test
    fun `기기 사용량 업로드가 성공하면 성공을 돌려준다`() = runTest {
        api.response = ApiResponse(
            success = true,
            data = DeviceUsageResponse(
                id = "d-1",
                userId = "u-1",
                date = "2026-08-12",
                totalUsedMinutes = 120,
                createdAt = "",
                updatedAt = "",
            ),
        )

        val result = repository.uploadDeviceUsage(totalUsedMinutes = 120, date = "2026-08-12")

        assertTrue(result.isSuccess)
        assertEquals(1, api.receivedRequests.size)
        assertEquals(120, api.receivedRequests[0].totalUsedMinutes)
    }

    @Test
    fun `서버 오류 응답은 실패로 전달한다`() = runTest {
        api.response = ApiResponse(
            success = false,
            error = ApiError(code = "SERVICE_UNAVAILABLE", message = "Try again later"),
        )

        val result = repository.uploadDeviceUsage(totalUsedMinutes = 120, date = "2026-08-12")

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull() as ApiException.Server
        assertEquals("SERVICE_UNAVAILABLE", error.error.code)
    }

    private class FakeDeviceUsageApi : DeviceUsageApi {
        lateinit var response: ApiResponse<DeviceUsageResponse>
        val receivedRequests = mutableListOf<DeviceUsageUpsertRequest>()

        override suspend fun putDeviceUsage(
            request: DeviceUsageUpsertRequest,
        ): ApiResponse<DeviceUsageResponse> {
            receivedRequests += request
            return response
        }
    }
}
