package com.phoneshim.android.data.repository

import com.google.gson.Gson
import com.phoneshim.android.data.api.AlertSettingApi
import com.phoneshim.android.data.api.AlertSettingResponse
import com.phoneshim.android.data.api.UpdateAlertSettingRequest
import com.phoneshim.android.data.api.common.ApiCallExecutor
import com.phoneshim.android.data.api.common.ApiError
import com.phoneshim.android.data.api.common.ApiException
import com.phoneshim.android.data.api.common.ApiResponse
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AlertSettingRepositoryImplTest {
    private val api = FakeAlertSettingApi()
    private val repository = AlertSettingRepositoryImpl(api, ApiCallExecutor(Gson()))

    @Test
    fun `GET 응답을 도메인 모델로 변환한다`() = runTest {
        val setting = repository.getAlertSetting().getOrThrow()

        assertEquals("alert-1", setting.id)
        assertEquals(true, setting.enabled)
        assertEquals(22, setting.hour)
        assertEquals("00", setting.minuteLabel)
    }

    @Test
    fun `PATCH 성공 응답 전체 객체를 반환한다`() = runTest {
        val setting = repository.updateAlertSetting(1439).getOrThrow()

        assertEquals(1439, api.lastRequest?.alertTimeMinutes)
        assertEquals(1439, setting.alertTimeMinutes)
        assertEquals("23", setting.hourLabel)
        assertEquals("59", setting.minuteLabel)
    }

    @Test
    fun `서버 오류 코드를 보존한다`() = runTest {
        api.error = ApiError("INVALID_ALERT_TIME", "Invalid alert time")

        val result = repository.updateAlertSetting(1319)

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull() as ApiException.Server
        assertEquals("INVALID_ALERT_TIME", exception.error.code)
    }

    @Test
    fun `잘못된 시간 응답을 직렬화 오류로 변환한다`() = runTest {
        api.responseMinutes = 0

        val result = repository.getAlertSetting()

        assertTrue(result.exceptionOrNull() is ApiException.Serialization)
    }

    private class FakeAlertSettingApi : AlertSettingApi {
        var responseMinutes = 1320
        var error: ApiError? = null
        var lastRequest: UpdateAlertSettingRequest? = null

        override suspend fun getAlertSetting(): ApiResponse<AlertSettingResponse> = response()

        override suspend fun updateAlertSetting(
            request: UpdateAlertSettingRequest,
        ): ApiResponse<AlertSettingResponse> {
            lastRequest = request
            responseMinutes = request.alertTimeMinutes
            return response()
        }

        private fun response(): ApiResponse<AlertSettingResponse> = error?.let {
            ApiResponse(success = false, error = it)
        } ?: ApiResponse(
            success = true,
            data = AlertSettingResponse(
                id = "alert-1",
                userId = "user-1",
                enabled = true,
                alertTimeMinutes = responseMinutes,
                createdAt = "2026-08-13T00:00:00Z",
                updatedAt = "2026-08-13T01:00:00Z",
            ),
        )
    }
}
