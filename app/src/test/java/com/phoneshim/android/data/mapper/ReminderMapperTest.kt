package com.phoneshim.android.data.mapper

import com.phoneshim.android.data.api.ReminderResponse
import com.phoneshim.android.domain.model.CreateReminderCommand
import com.phoneshim.android.domain.model.ReminderRestrictionMode
import com.phoneshim.android.domain.model.UpdateReminderCommand
import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderMapperTest {
    @Test
    fun `서버 응답을 도메인 모델로 변환한다`() {
        val response = ReminderResponse(
            id = "reminder-1",
            userId = "user-1",
            date = "2026-07-16T00:00:00.000Z",
            title = "과제하기",
            startTime = "2026-07-16T01:00:00.000Z",
            endTime = "2026-07-16T02:00:00.000Z",
            restrictMode = "SPECIFIC_APP",
            restrictedAppIds = listOf("app-2", "app-1"),
            createdAt = "2026-07-15T12:00:00.000Z",
            updatedAt = "2026-07-15T12:30:00.000Z",
        )

        val result = response.toDomain()

        assertEquals(LocalDate.of(2026, 7, 16), result.date)
        assertEquals(Instant.parse("2026-07-16T01:00:00Z"), result.startTime)
        assertEquals(ReminderRestrictionMode.SPECIFIC_APP, result.restrictionMode)
        assertEquals(setOf("app-1", "app-2"), result.restrictedAppIds)
    }

    @Test
    fun `생성 명령을 API 요청 형식으로 변환한다`() {
        val command = CreateReminderCommand(
            date = LocalDate.of(2026, 7, 16),
            title = "과제하기",
            startTime = Instant.parse("2026-07-16T01:00:00Z"),
            endTime = Instant.parse("2026-07-16T02:00:00Z"),
            restrictionMode = ReminderRestrictionMode.SPECIFIC_APP,
            restrictedAppIds = setOf("app-2", "app-1"),
        )

        val request = command.toRequest()

        assertEquals("2026-07-16", request.date)
        assertEquals("2026-07-16T10:00+09:00", request.startTime)
        assertEquals("SPECIFIC_APP", request.restrictMode)
        assertEquals(listOf("app-1", "app-2"), request.restrictedAppIds)
    }

    @Test
    fun `전체 폰 제한에는 특정 앱 id를 보내지 않는다`() {
        val command = CreateReminderCommand(
            date = LocalDate.of(2026, 7, 16),
            title = "집중하기",
            startTime = Instant.parse("2026-07-16T01:00:00Z"),
            endTime = Instant.parse("2026-07-16T02:00:00Z"),
            restrictionMode = ReminderRestrictionMode.FULL_PHONE,
            restrictedAppIds = setOf("잘못 남은 앱 id"),
        )

        assertTrue(command.toRequest().restrictedAppIds.isEmpty())
    }

    @Test
    fun `수정 명령의 null 필드는 변경하지 않는 값으로 유지한다`() {
        val request = UpdateReminderCommand(title = "수정된 할 일").toRequest()

        assertEquals("수정된 할 일", request.title)
        assertEquals(null, request.date)
        assertEquals(null, request.startTime)
        assertEquals(null, request.restrictMode)
        assertEquals(null, request.restrictedAppIds)
    }

    @Test(expected = ReminderMappingException::class)
    fun `알 수 없는 제한 모드는 매핑 오류로 처리한다`() {
        ReminderResponse(
            id = "reminder-1",
            userId = "user-1",
            date = "2026-07-16",
            title = "과제하기",
            startTime = "2026-07-16T01:00:00Z",
            endTime = "2026-07-16T02:00:00Z",
            restrictMode = "UNKNOWN_MODE",
            createdAt = "2026-07-15T12:00:00Z",
            updatedAt = "2026-07-15T12:00:00Z",
        ).toDomain()
    }

    @Test(expected = ReminderMappingException::class)
    fun `잘못된 서버 시간은 매핑 오류로 처리한다`() {
        ReminderResponse(
            id = "reminder-1",
            userId = "user-1",
            date = "2026-07-16",
            title = "과제하기",
            startTime = "invalid-time",
            endTime = "2026-07-16T02:00:00Z",
            restrictMode = "NONE",
            createdAt = "2026-07-15T12:00:00Z",
            updatedAt = "2026-07-15T12:00:00Z",
        ).toDomain()
    }
}
