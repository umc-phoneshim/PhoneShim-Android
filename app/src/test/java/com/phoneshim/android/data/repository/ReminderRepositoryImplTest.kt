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
import com.phoneshim.android.data.database.dao.ReminderCacheEntry
import com.phoneshim.android.data.database.dao.ReminderDao
import com.phoneshim.android.data.database.dao.ReminderRestrictionDao
import com.phoneshim.android.data.database.dao.ReminderWithRestrictedApps
import com.phoneshim.android.data.database.entity.ReminderRestrictionEntity
import com.phoneshim.android.data.database.entity.ReminderSyncStateEntity
import com.phoneshim.android.domain.model.CreateReminderCommand
import com.phoneshim.android.domain.model.ReminderRestrictionMode
import com.phoneshim.android.domain.model.ReminderDataSource
import com.phoneshim.android.domain.model.MonitoredApp
import com.phoneshim.android.domain.model.ResolvedRestrictedApps
import com.phoneshim.android.domain.repository.MonitoredAppRepository
import com.phoneshim.android.domain.schedule.ReminderScheduleCoordinator
import com.phoneshim.android.domain.usecase.ResolveRestrictedPackageNamesUseCase
import java.io.IOException
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.flow.flowOf
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class ReminderRepositoryImplTest {
    private val api = FakeReminderApi()
    private val dao = FakeReminderDao()
    private val restrictionDao = FakeReminderRestrictionDao()
    private val coordinator = FakeReminderScheduleCoordinator(restrictionDao)
    private val monitoredAppRepository = FakeMonitoredAppRepository()
    private val repository = ReminderRepositoryImpl(
        api,
        ApiCallExecutor(Gson()),
        dao,
        restrictionDao,
        ResolveRestrictedPackageNamesUseCase(monitoredAppRepository),
        coordinator,
    )

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

    @Test
    fun `서버 조회 성공 시 날짜 캐시를 교체한다`() = runTest {
        val date = LocalDate.of(2026, 7, 16)

        val result = repository.getReminders(date).getOrThrow()

        assertEquals(ReminderDataSource.REMOTE, result.source)
        assertEquals("reminder-1", dao.getForDate(date.toEpochDay()).single().reminder.id)
        assertTrue(dao.getSyncState(date.toEpochDay()) != null)
        assertEquals("reminder-1", restrictionDao.getForDate(date.toEpochDay()).single().taskId)
        assertEquals(listOf("refresh:cached"), coordinator.calls)
    }

    @Test
    fun `서버 빈 목록 조회 성공 시 해당 날짜 엔진 캐시를 삭제한다`() = runTest {
        val date = LocalDate.of(2026, 7, 16)
        repository.getReminders(date).getOrThrow()
        api.getRemindersResult = ApiResponse(success = true, data = emptyList())

        repository.getReminders(date).getOrThrow()

        assertTrue(restrictionDao.getForDate(date.toEpochDay()).isEmpty())
        assertEquals("refresh:empty", coordinator.calls.last())
    }

    @Test
    fun `서버 재조회에서 사라진 제한 일정의 기존 알람을 취소한다`() = runTest {
        val date = LocalDate.of(2026, 7, 16)
        api.getRemindersResult = ApiResponse(
            success = true,
            data = listOf(reminderResponse(restrictMode = "FULL_PHONE")),
        )
        repository.getReminders(date).getOrThrow()
        coordinator.calls.clear()
        api.getRemindersResult = ApiResponse(success = true, data = emptyList())

        repository.getReminders(date).getOrThrow()

        assertEquals(
            listOf("cancel:reminder-1:empty", "refresh:empty"),
            coordinator.calls,
        )
    }

    @Test
    fun `네트워크 실패 시 동기화된 날짜 캐시를 반환한다`() = runTest {
        val date = LocalDate.of(2026, 7, 16)
        repository.getReminders(date).getOrThrow()
        api.getRemindersError = IOException("offline")

        val result = repository.getReminders(date).getOrThrow()

        assertEquals(ReminderDataSource.CACHE, result.source)
        assertEquals("reminder-1", result.reminders.single().id)
        assertEquals(1, coordinator.calls.size)
        assertEquals("reminder-1", restrictionDao.getForDate(date.toEpochDay()).single().taskId)
    }

    @Test
    fun `동기화 이력이 없는 날짜는 네트워크 오류를 유지한다`() = runTest {
        api.getRemindersError = IOException("offline")

        val error = repository.getReminders(LocalDate.of(2026, 7, 17)).exceptionOrNull()

        assertTrue(error is ApiException.Network)
    }

    @Test
    fun `생성 수정 삭제 성공을 캐시에 반영한다`() = runTest {
        val created = repository.createReminder(command()).getOrThrow()
        assertEquals(created.id, dao.getById(created.id)?.reminder?.id)
        assertEquals(created.id, restrictionDao.getForDate(created.date.toEpochDay()).single().taskId)
        assertEquals("schedule:${created.id}:cached", coordinator.calls.last())

        repository.updateReminder(created.id, com.phoneshim.android.domain.model.UpdateReminderCommand(title = "수정"))
            .getOrThrow()
        assertEquals(created.id, dao.getById(created.id)?.reminder?.id)
        assertEquals("reschedule:${created.id}:cached", coordinator.calls.last())

        repository.deleteReminder(created.id).getOrThrow()
        assertTrue(dao.getById(created.id) == null)
        assertTrue(restrictionDao.getForDate(created.date.toEpochDay()).isEmpty())
        assertEquals("cancel:${created.id}:empty", coordinator.calls.last())
    }

    @Test
    fun `특정 앱 제한 UUID를 packageName으로 변환해 엔진 캐시에 저장한다`() = runTest {
        api.createResult = ApiResponse(
            success = true,
            data = reminderResponse(
                restrictMode = "SPECIFIC_APP",
                restrictedAppIds = listOf("monitored-youtube"),
            ),
        )

        val created = repository.createReminder(command()).getOrThrow()

        val cached = restrictionDao.getForDate(created.date.toEpochDay()).single()
        assertEquals("SPECIFIC_APP", cached.restrictionMode)
        assertEquals("com.google.android.youtube", cached.restrictedPackages)
        assertEquals(600, cached.startMinutes)
        assertEquals(660, cached.endMinutes)
    }

    private fun command() = CreateReminderCommand(
        date = LocalDate.of(2026, 7, 16),
        title = "과제하기",
        startTime = Instant.parse("2026-07-16T01:00:00Z"),
        endTime = Instant.parse("2026-07-16T02:00:00Z"),
        restrictionMode = ReminderRestrictionMode.NONE,
    )
}

private class FakeReminderRestrictionDao : ReminderRestrictionDao {
    private val entries = linkedMapOf<String, ReminderRestrictionEntity>()

    override suspend fun getForDate(epochDay: Long): List<ReminderRestrictionEntity> =
        entries.values.filter { it.date == epochDay }

    override fun observeForDate(epochDay: Long): Flow<List<ReminderRestrictionEntity>> =
        flowOf(entries.values.filter { it.date == epochDay })

    override suspend fun upsert(restriction: ReminderRestrictionEntity) {
        entries[restriction.taskId] = restriction
    }

    override suspend fun upsertAll(restrictions: List<ReminderRestrictionEntity>) {
        restrictions.forEach { entries[it.taskId] = it }
    }

    override suspend fun delete(taskId: String) {
        entries.remove(taskId)
    }

    override suspend fun deleteForDate(epochDay: Long) {
        entries.entries.removeAll { it.value.date == epochDay }
    }
}

private class FakeReminderScheduleCoordinator(
    private val dao: ReminderRestrictionDao,
) : ReminderScheduleCoordinator {
    val calls = mutableListOf<String>()

    override suspend fun schedule(taskId: String) {
        calls += "schedule:$taskId:${cacheState()}"
    }

    override suspend fun reschedule(taskId: String) {
        calls += "reschedule:$taskId:${cacheState()}"
    }

    override suspend fun cancel(taskId: String) {
        calls += "cancel:$taskId:${cacheState()}"
    }

    override suspend fun refreshToday() {
        calls += "refresh:${cacheState()}"
    }

    private suspend fun cacheState(): String =
        if (dao.getForDate(LocalDate.of(2026, 7, 16).toEpochDay()).isEmpty()) "empty" else "cached"
}

private class FakeMonitoredAppRepository : MonitoredAppRepository {
    private val apps = listOf(
        MonitoredApp("monitored-youtube", "com.google.android.youtube", "YouTube"),
    )

    override suspend fun getMonitoredApps(): Result<List<MonitoredApp>> = Result.success(apps)
    override suspend fun refreshMonitoredApps(): Result<List<MonitoredApp>> = Result.success(apps)
    override suspend fun resolveMonitoredAppId(packageName: String): Result<String?> =
        Result.success(apps.firstOrNull { it.packageName == packageName }?.id)
    override suspend fun resolvePackageNames(monitoredAppIds: List<String>): Result<ResolvedRestrictedApps> {
        val byId = apps.associateBy(MonitoredApp::id)
        return Result.success(
            ResolvedRestrictedApps(
                packageNames = monitoredAppIds.mapNotNull { byId[it]?.packageName },
                unresolvedIds = monitoredAppIds.filterNot(byId::containsKey),
            ),
        )
    }
}

private class FakeReminderDao : ReminderDao {
    private val entries = linkedMapOf<String, ReminderCacheEntry>()
    private val syncStates = mutableMapOf<Long, ReminderSyncStateEntity>()

    override suspend fun getForDate(dateEpochDay: Long): List<ReminderWithRestrictedApps> =
        entries.values
            .filter { it.reminder.dateEpochDay == dateEpochDay }
            .sortedBy { it.reminder.startTimeEpochMillis }
            .map { ReminderWithRestrictedApps(it.reminder, it.restrictedApps) }

    // 이 테스트는 suspend 조회 경로만 검증하므로, 현재 스냅샷을 한 번 emit하는 것으로 충분하다.
    override fun observeForDate(dateEpochDay: Long): Flow<List<ReminderWithRestrictedApps>> =
        flow { emit(getForDate(dateEpochDay)) }

    override suspend fun getById(id: String): ReminderWithRestrictedApps? =
        entries[id]?.let { ReminderWithRestrictedApps(it.reminder, it.restrictedApps) }

    override suspend fun getSyncState(dateEpochDay: Long): ReminderSyncStateEntity? = syncStates[dateEpochDay]

    override suspend fun upsertReminders(reminders: List<com.phoneshim.android.data.database.entity.ReminderEntity>) {
        reminders.forEach { reminder ->
            val previousApps = entries[reminder.id]?.restrictedApps.orEmpty()
            entries[reminder.id] = ReminderCacheEntry(reminder, previousApps)
        }
    }

    override suspend fun upsertRestrictedApps(
        apps: List<com.phoneshim.android.data.database.entity.ReminderRestrictedAppEntity>,
    ) {
        apps.groupBy { it.reminderId }.forEach { (id, restrictedApps) ->
            entries[id]?.let { entries[id] = it.copy(restrictedApps = restrictedApps) }
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

private fun reminderResponse(
    restrictMode: String = "NONE",
    restrictedAppIds: List<String> = emptyList(),
) = ReminderResponse(
    id = "reminder-1",
    userId = "user-1",
    date = "2026-07-16",
    title = "과제하기",
    startTime = "2026-07-16T01:00:00Z",
    endTime = "2026-07-16T02:00:00Z",
    restrictMode = restrictMode,
    restrictedAppIds = restrictedAppIds,
    createdAt = "2026-07-15T12:00:00Z",
    updatedAt = "2026-07-15T12:00:00Z",
)
