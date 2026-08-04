package com.phoneshim.android.data.repository

import com.google.gson.Gson
import com.phoneshim.android.data.api.CreateReminderRequest
import com.phoneshim.android.data.api.ReminderApi
import com.phoneshim.android.data.api.ReminderResponse
import com.phoneshim.android.data.api.UpdateReminderRequest
import com.phoneshim.android.data.api.common.ApiCallExecutor
import com.phoneshim.android.data.api.common.ApiError
import com.phoneshim.android.data.api.common.ApiException
import com.phoneshim.android.data.api.common.ApiResponse
import com.phoneshim.android.domain.model.CreateReminderCommand
import com.phoneshim.android.domain.model.ReminderRestrictionMode
import java.io.IOException
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class ReminderRepositoryImplTest {
    private val api = FakeReminderApi()
    private val repository = ReminderRepositoryImpl(api, ApiCallExecutor(Gson()))

    @Test
    fun `비즈니스 오류 코드를 유지한다`() = runTest {
        api.createResult = ApiResponse(
            success = false,
            error = ApiError(
                code = "REMINDER_TIME_OVERLAP",
                message = "중복 일정",
            ),
        )

        val error = repository.createReminder(command()).exceptionOrNull()

        assertTrue(error is ApiException.Server)
        assertEquals("REMINDER_TIME_OVERLAP", (error as ApiException.Server).error.code)
    }

    @Test
    fun `삭제 HTTP 오류를 공통 오류로 변환한다`() = runTest {
        val body =
            """{"success":false,"error":{"code":"REMINDER_NOT_FOUND","message":"Not found"}}"""
                .toResponseBody("application/json".toMediaType())
        api.deleteResult = Response.error(404, body)

        val error = repository.deleteReminder("missing-id").exceptionOrNull()

        assertTrue(error is ApiException.Http)
        assertEquals(404, (error as ApiException.Http).statusCode)
        assertEquals("REMINDER_NOT_FOUND", error.error?.code)
    }

    @Test
    fun `네트워크 실패를 공통 네트워크 오류로 변환한다`() = runTest {
        api.getRemindersError = IOException("offline")

        val error = repository.getReminders(LocalDate.of(2026, 7, 16)).exceptionOrNull()

        assertTrue(error is ApiException.Network)
    }

    @Test
    fun `잘못된 서버 응답을 직렬화 오류로 변환한다`() = runTest {
        api.getRemindersResult = ApiResponse(
            success = true,
            data = listOf(reminderResponse(restrictMode = "UNKNOWN_MODE")),
        )

        val error = repository.getReminders(LocalDate.of(2026, 7, 16)).exceptionOrNull()

        assertTrue(error is ApiException.Serialization)
    }

    private fun command() = CreateReminderCommand(
        date = LocalDate.of(2026, 7, 16),
        title = "과제하기",
        startTime = Instant.parse("2026-07-16T01:00:00Z"),
        endTime = Instant.parse("2026-07-16T02:00:00Z"),
        restrictionMode = ReminderRestrictionMode.NONE,
    )
}

private class FakeReminderApi : ReminderApi {
    var getRemindersResult: ApiResponse<List<ReminderResponse>> = ApiResponse(
        success = true,
        data = listOf(reminderResponse()),
    )
    var getRemindersError: Throwable? = null
    var createResult: ApiResponse<ReminderResponse> = ApiResponse(
        success = true,
        data = reminderResponse(),
    )
    var deleteResult: Response<Unit> = Response.success(null)

    override suspend fun createReminder(request: CreateReminderRequest): ApiResponse<ReminderResponse> =
        createResult

    override suspend fun getReminders(date: String): ApiResponse<List<ReminderResponse>> {
        getRemindersError?.let { throw it }
        return getRemindersResult
    }

    override suspend fun getReminder(id: String): ApiResponse<ReminderResponse> =
        ApiResponse(success = true, data = reminderResponse())

    override suspend fun updateReminder(
        id: String,
        request: UpdateReminderRequest,
    ): ApiResponse<ReminderResponse> = ApiResponse(success = true, data = reminderResponse())

    override suspend fun deleteReminder(id: String): Response<Unit> = deleteResult
}

private fun reminderResponse(restrictMode: String = "NONE") = ReminderResponse(
    id = "reminder-1",
    userId = "user-1",
    date = "2026-07-16",
    title = "과제하기",
    startTime = "2026-07-16T01:00:00Z",
    endTime = "2026-07-16T02:00:00Z",
    restrictMode = restrictMode,
    createdAt = "2026-07-15T12:00:00Z",
    updatedAt = "2026-07-15T12:00:00Z",
)
