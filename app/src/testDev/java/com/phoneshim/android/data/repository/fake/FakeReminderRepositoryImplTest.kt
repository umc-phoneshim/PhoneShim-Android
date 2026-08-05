package com.phoneshim.android.data.repository.fake

import com.phoneshim.android.data.api.common.ApiException
import com.phoneshim.android.data.database.dao.ReminderCacheEntry
import com.phoneshim.android.data.database.dao.ReminderDao
import com.phoneshim.android.data.database.dao.ReminderWithRestrictedApps
import com.phoneshim.android.data.database.entity.ReminderEntity
import com.phoneshim.android.data.database.entity.ReminderRestrictedAppEntity
import com.phoneshim.android.data.database.entity.ReminderSyncStateEntity
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
    private val scenarioController = DevReminderScenarioController()
    private val dao = DevFakeReminderDao()
    private val repository = FakeReminderRepositoryImpl(scenarioController, dao)

    @Test
    fun `생성 수정 삭제 결과를 날짜별 조회에 반영한다`() = runTest {
        val created = repository.createReminder(command(title = "운동")).getOrThrow()
        assertEquals("운동", repository.getReminders(DATE).getOrThrow().reminders.single().title)

        repository.updateReminder(created.id, UpdateReminderCommand(title = "과제")).getOrThrow()
        assertEquals("과제", repository.getReminders(DATE).getOrThrow().reminders.single().title)

        repository.deleteReminder(created.id).getOrThrow()
        assertTrue(repository.getReminders(DATE).getOrThrow().reminders.isEmpty())
    }

    @Test
    fun `강제 오류는 다음 한 요청에만 적용된다`() = runTest {
        scenarioController.failNextWith(DevReminderFailure.NETWORK)

        assertTrue(repository.getReminders(DATE).exceptionOrNull() is ApiException.Network)
        assertTrue(repository.getReminders(DATE).getOrThrow().reminders.isEmpty())
    }

    @Test
    fun `동기화 이후 강제 네트워크 오류는 Room 캐시를 반환한다`() = runTest {
        repository.createReminder(command()).getOrThrow()
        repository.getReminders(DATE).getOrThrow()
        scenarioController.failNextWith(DevReminderFailure.NETWORK)

        val cached = repository.getReminders(DATE).getOrThrow()

        assertEquals(com.phoneshim.android.domain.model.ReminderDataSource.CACHE, cached.source)
        assertEquals("과제", cached.reminders.single().title)
    }

    @Test
    fun `401과 404 오류 시나리오를 재현한다`() = runTest {
        scenarioController.failNextWith(DevReminderFailure.UNAUTHORIZED)
        val unauthorized = repository.getReminders(DATE).exceptionOrNull()
        assertTrue(unauthorized is ApiException.Http && unauthorized.statusCode == 401)

        scenarioController.failNextWith(DevReminderFailure.NOT_FOUND)
        val notFound = repository.deleteReminder("missing").exceptionOrNull()
        assertTrue(notFound is ApiException.Server)
        assertEquals("REMINDER_NOT_FOUND", (notFound as ApiException.Server).error.code)
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

private class DevFakeReminderDao : ReminderDao {
    private val entries = linkedMapOf<String, ReminderCacheEntry>()
    private val syncStates = mutableMapOf<Long, ReminderSyncStateEntity>()

    override suspend fun getForDate(dateEpochDay: Long): List<ReminderWithRestrictedApps> =
        entries.values.filter { it.reminder.dateEpochDay == dateEpochDay }
            .map { ReminderWithRestrictedApps(it.reminder, it.restrictedApps) }

    override suspend fun getById(id: String): ReminderWithRestrictedApps? =
        entries[id]?.let { ReminderWithRestrictedApps(it.reminder, it.restrictedApps) }

    override suspend fun getSyncState(dateEpochDay: Long): ReminderSyncStateEntity? = syncStates[dateEpochDay]

    override suspend fun upsertReminders(reminders: List<ReminderEntity>) {
        reminders.forEach { reminder ->
            entries[reminder.id] = ReminderCacheEntry(reminder, entries[reminder.id]?.restrictedApps.orEmpty())
        }
    }

    override suspend fun upsertRestrictedApps(apps: List<ReminderRestrictedAppEntity>) {
        apps.groupBy(ReminderRestrictedAppEntity::reminderId).forEach { (id, values) ->
            entries[id]?.let { entries[id] = it.copy(restrictedApps = values) }
        }
    }

    override suspend fun upsertSyncState(state: ReminderSyncStateEntity) {
        syncStates[state.dateEpochDay] = state
    }

    override suspend fun deleteForDate(dateEpochDay: Long) {
        entries.entries.removeAll { it.value.reminder.dateEpochDay == dateEpochDay }
    }

    override suspend fun deleteRestrictedApps(reminderId: String) {
        entries[reminderId]?.let { entries[reminderId] = it.copy(restrictedApps = emptyList()) }
    }

    override suspend fun deleteById(id: String) {
        entries.remove(id)
    }
}
