package com.phoneshim.android.data.repository.fake

import com.phoneshim.android.data.api.common.ApiException
import com.phoneshim.android.domain.model.CreateReminderCommand
import com.phoneshim.android.domain.model.ReminderRestrictionMode
import com.phoneshim.android.domain.model.UpdateReminderCommand
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeReminderRepositoryImplTest {
    private val repository = FakeReminderRepositoryImpl()

    @Test
    fun `생성 수정 삭제 결과를 날짜별 조회에 반영한다`() = runTest {
        val created = repository.createReminder(command(title = "운동")).getOrThrow()
        assertEquals("운동", repository.getReminders(DATE).getOrThrow().single().title)

        repository.updateReminder(created.id, UpdateReminderCommand(title = "과제")).getOrThrow()
        assertEquals("과제", repository.getReminders(DATE).getOrThrow().single().title)

        repository.deleteReminder(created.id).getOrThrow()
        assertTrue(repository.getReminders(DATE).getOrThrow().isEmpty())
    }

    @Test
    fun `겹치는 일정은 서버와 같은 오류 코드로 거절한다`() = runTest {
        repository.createReminder(command()).getOrThrow()

        val error = repository.createReminder(
            command(
                start = Instant.parse("2026-08-27T01:30:00Z"),
                end = Instant.parse("2026-08-27T02:30:00Z"),
            ),
        ).exceptionOrNull()

        assertTrue(error is ApiException.Server)
        assertEquals("REMINDER_TIME_OVERLAP", (error as ApiException.Server).error.code)
    }

    private fun command(
        title: String = "과제",
        start: Instant = Instant.parse("2026-08-27T01:00:00Z"),
        end: Instant = Instant.parse("2026-08-27T02:00:00Z"),
    ) = CreateReminderCommand(
        date = DATE,
        title = title,
        startTime = start,
        endTime = end,
        restrictionMode = ReminderRestrictionMode.NONE,
    )

    private companion object {
        val DATE: LocalDate = LocalDate.of(2026, 8, 27)
    }
}
