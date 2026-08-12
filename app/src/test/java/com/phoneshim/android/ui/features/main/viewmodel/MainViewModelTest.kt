package com.phoneshim.android.ui.features.main.viewmodel

import com.phoneshim.android.domain.model.CreateReminderCommand
import com.phoneshim.android.domain.model.DashboardSummary
import com.phoneshim.android.domain.model.Goal
import com.phoneshim.android.domain.model.Reminder
import com.phoneshim.android.domain.model.ReminderRestrictionMode
import com.phoneshim.android.domain.model.UpdateReminderCommand
import com.phoneshim.android.domain.model.UsageStatus
import com.phoneshim.android.domain.model.User
import com.phoneshim.android.domain.model.WithdrawalResult
import com.phoneshim.android.domain.repository.DashboardRepository
import com.phoneshim.android.domain.repository.GoalRepository
import com.phoneshim.android.domain.repository.MyPageRepository
import com.phoneshim.android.domain.repository.ReminderRepository
import com.phoneshim.android.domain.repository.UsageLogRepository
import com.phoneshim.android.domain.usecase.GetDashboardSummaryUseCase
import com.phoneshim.android.domain.usecase.GetGoalUseCase
import com.phoneshim.android.domain.usecase.GetMyInfoUseCase
import com.phoneshim.android.domain.usecase.GetUsageStatusUseCase
import com.phoneshim.android.domain.usecase.ObserveRemindersUseCase
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var usageLogRepository: FakeUsageLogRepository
    private lateinit var dashboardRepository: FakeDashboardRepository
    private lateinit var goalRepository: FakeGoalRepository
    private lateinit var myPageRepository: FakeMyPageRepository
    private lateinit var reminderRepository: FakeReminderRepository
    private lateinit var viewModel: MainViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        usageLogRepository = FakeUsageLogRepository()
        dashboardRepository = FakeDashboardRepository()
        goalRepository = FakeGoalRepository()
        myPageRepository = FakeMyPageRepository()
        reminderRepository = FakeReminderRepository()
        viewModel = createViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `리마인더 캐시가 갱신되면 MainViewModel의 todayReminders도 자동으로 갱신된다`() = runTest(dispatcher) {
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.todayReminders.isEmpty())
        val usageStatusBefore = viewModel.uiState.value.usageStatus
        val dashboardSummaryBefore = viewModel.uiState.value.dashboardSummary
        val userNameBefore = viewModel.uiState.value.userName

        // 리마인더 화면에서 일정을 추가해 Room 캐시가 갱신된 상황을 흉내낸다.
        // GetRemindersUseCase 같은 재조회 호출 없이, 캐시 Flow가 새 값을 emit하는 것만으로 반영돼야 한다.
        reminderRepository.remindersFlow.value = listOf(reminder(id = "new-1", title = "새 일정"))
        advanceUntilIdle()

        assertEquals(listOf("새 일정"), viewModel.uiState.value.todayReminders.map { it.title })
        // 오늘 리마인더 캐시 갱신은 usageStatus/dashboardSummary/userName과 완전히 분리된 경로다.
        assertEquals(usageStatusBefore, viewModel.uiState.value.usageStatus)
        assertEquals(dashboardSummaryBefore, viewModel.uiState.value.dashboardSummary)
        assertEquals(userNameBefore, viewModel.uiState.value.userName)
    }

    @Test
    fun `리마인더 캐시가 여러 번 갱신되면 그때마다 todayReminders가 최신값을 따라간다`() = runTest(dispatcher) {
        advanceUntilIdle()

        reminderRepository.remindersFlow.value = listOf(reminder(id = "r-1", title = "첫 번째"))
        advanceUntilIdle()
        assertEquals(listOf("첫 번째"), viewModel.uiState.value.todayReminders.map { it.title })

        reminderRepository.remindersFlow.value = listOf(
            reminder(id = "r-1", title = "첫 번째"),
            reminder(id = "r-2", title = "두 번째"),
        )
        advanceUntilIdle()
        assertEquals(listOf("첫 번째", "두 번째"), viewModel.uiState.value.todayReminders.map { it.title })

        reminderRepository.remindersFlow.value = emptyList()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.todayReminders.isEmpty())
    }

    private fun createViewModel(): MainViewModel = MainViewModel(
        getUsageStatusUseCase = GetUsageStatusUseCase(usageLogRepository),
        getDashboardSummaryUseCase = GetDashboardSummaryUseCase(dashboardRepository),
        getGoalUseCase = GetGoalUseCase(goalRepository),
        getMyInfoUseCase = GetMyInfoUseCase(myPageRepository),
        observeRemindersUseCase = ObserveRemindersUseCase(reminderRepository),
    )

    private fun reminder(
        id: String = "reminder-1",
        title: String = "과제하기",
        date: LocalDate = LocalDate.now(KOREA_ZONE_ID),
    ): Reminder = Reminder(
        id = id,
        userId = "u-1",
        date = date,
        title = title,
        startTime = Instant.parse("${date}T01:00:00Z"),
        endTime = Instant.parse("${date}T02:00:00Z"),
        restrictionMode = ReminderRestrictionMode.NONE,
        restrictedAppIds = emptySet(),
        createdAt = Instant.parse("${date}T00:00:00Z"),
        updatedAt = Instant.parse("${date}T00:00:00Z"),
    )

    // ── 테스트 더블 ─────────────────────────────────────────────────

    private class FakeUsageLogRepository : UsageLogRepository {
        var statusResult: Result<List<UsageStatus>> = Result.success(
            listOf(UsageStatus(monitoredAppId = "m-1", appName = "카카오톡", packageName = "com.kakao.talk", usedMinutes = 10, entryCount = 1)),
        )

        override suspend fun getUsageLogs(date: String?) = throw UnsupportedOperationException()

        override suspend fun getUsageStatus(): Result<List<UsageStatus>> = statusResult

        override suspend fun uploadUsageLog(
            monitoredAppId: String,
            usedMinutes: Int,
            entryCount: Int,
            date: String?,
        ) = throw UnsupportedOperationException()
    }

    private class FakeDashboardRepository : DashboardRepository {
        var summaryResult: Result<DashboardSummary> = Result.success(
            DashboardSummary(date = "2026-08-13", targetMinutes = 210, usedMinutes = 90, remainingMinutes = 120, isExceeded = false),
        )

        override suspend fun getDailySummary(): Result<DashboardSummary> = summaryResult
    }

    private class FakeGoalRepository : GoalRepository {
        var goalResult: Result<Goal?> = Result.success(null)

        override suspend fun getGoal(): Result<Goal?> = goalResult

        override suspend fun saveGoal(goal: Goal) = throw UnsupportedOperationException()
    }

    private class FakeMyPageRepository : MyPageRepository {
        var infoResult: Result<User> = Result.success(User(email = "user@phoneshim.com", nickname = "유리"))

        override suspend fun getMyInfo(): Result<User> = infoResult

        override suspend fun updateMyInfo(name: String?, motivation: String?) = throw UnsupportedOperationException()

        override suspend fun withdraw(): Result<WithdrawalResult> = throw UnsupportedOperationException()
    }

    private class FakeReminderRepository : ReminderRepository {
        val remindersFlow = MutableStateFlow<List<Reminder>>(emptyList())
        val observedDates = mutableListOf<LocalDate>()

        override suspend fun getReminders(date: LocalDate) = throw UnsupportedOperationException()

        override suspend fun getReminder(id: String): Result<Reminder> = throw UnsupportedOperationException()

        override suspend fun createReminder(command: CreateReminderCommand): Result<Reminder> =
            throw UnsupportedOperationException()

        override suspend fun updateReminder(id: String, command: UpdateReminderCommand): Result<Reminder> =
            throw UnsupportedOperationException()

        override suspend fun deleteReminder(id: String): Result<Unit> = throw UnsupportedOperationException()

        override fun observeReminders(date: LocalDate): Flow<List<Reminder>> {
            observedDates += date
            return remindersFlow
        }
    }
}

private val KOREA_ZONE_ID = java.time.ZoneId.of("Asia/Seoul")
