package com.phoneshim.android.data.mapper

import com.phoneshim.android.data.database.dao.ReminderWithRestrictedApps
import com.phoneshim.android.domain.model.Reminder
import com.phoneshim.android.domain.model.ReminderRestrictionMode
import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class ReminderEntityMapperTest {
    @Test
    fun `도메인 모델을 캐시에 저장하고 손실 없이 복원한다`() {
        val reminder = Reminder(
            id = "reminder-1",
            userId = "user-1",
            date = LocalDate.of(2026, 8, 27),
            title = "과제하기",
            startTime = Instant.parse("2026-08-27T01:00:00Z"),
            endTime = Instant.parse("2026-08-27T02:00:00Z"),
            restrictionMode = ReminderRestrictionMode.SPECIFIC_APP,
            restrictedAppIds = setOf("app-2", "app-1"),
            createdAt = Instant.parse("2026-08-26T12:00:00Z"),
            updatedAt = Instant.parse("2026-08-26T13:00:00Z"),
        )

        val entry = reminder.toCacheEntry()
        val restored = ReminderWithRestrictedApps(entry.reminder, entry.restrictedApps).toDomain()

        assertEquals(reminder, restored)
    }

    @Test(expected = ReminderMappingException::class)
    fun `알 수 없는 캐시 제한 모드는 매핑 오류로 처리한다`() {
        val reminder = Reminder(
            id = "reminder-1",
            userId = "user-1",
            date = LocalDate.of(2026, 8, 27),
            title = "과제하기",
            startTime = Instant.parse("2026-08-27T01:00:00Z"),
            endTime = Instant.parse("2026-08-27T02:00:00Z"),
            restrictionMode = ReminderRestrictionMode.NONE,
            restrictedAppIds = emptySet(),
            createdAt = Instant.parse("2026-08-26T12:00:00Z"),
            updatedAt = Instant.parse("2026-08-26T13:00:00Z"),
        )
        val entry = reminder.toCacheEntry()

        ReminderWithRestrictedApps(
            reminder = entry.reminder.copy(restrictionMode = "UNKNOWN"),
            restrictedApps = entry.restrictedApps,
        ).toDomain()
    }
}
