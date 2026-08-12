package com.phoneshim.android.ui.features.main.viewmodel

import com.phoneshim.android.domain.model.CreateReminderCommand
import com.phoneshim.android.domain.model.DashboardSummary
import com.phoneshim.android.domain.model.Goal
import com.phoneshim.android.domain.model.Reminder
import com.phoneshim.android.domain.model.ReminderDataSource
import com.phoneshim.android.domain.model.ReminderListResult
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
import com.phoneshim.android.domain.usecase.GetRemindersUseCase
import com.phoneshim.android.domain.usecase.GetUsageStatusUseCase
import java.io.IOException
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
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
    fun `RefreshTodayReminders 이벤트가 오면 오늘 리마인더만 다시 조회하고 usageStatus,dashboardSummary,userName은 그대로 유지한다`() = runTest(dispatcher) {
        advanceUntilIdle()
        val usageStatusBefore = viewModel.uiState.value.usageStatus
        val dashboardSummaryBefore = viewModel.uiState.value.dashboardSummary
        val userNameBefore = viewModel.uiState.value.userName
        assertEquals(1, usageLogRepository.getUsageStatusCallCount)
        assertEquals(1, dashboardRepository.getDailySummaryCallCount)
        assertEquals(1, myPageRepository.getMyInfoCallCount)

        // 리마인더 탭에서 새 일정을 추가한 상황을 흉내낸다.
        reminderRepository.remindersResult = Result.success(
            ReminderListResult(listOf(reminder(id = "new-1", title = "새 일정")), ReminderDataSource.REMOTE),
        )

        viewModel.onEvent(MainUiEvent.RefreshTodayReminders)
        advanceUntilIdle()

        assertEquals(listOf("새 일정"), viewModel.uiState.value.todayReminders.map { it.title })
        // usageStatus/dashboardSummary/userName은 손대지 않았으므로 값도, 호출 횟수도 그대로다.
        assertEquals(usageStatusBefore, viewModel.uiState.value.usageStatus)
        assertEquals(dashboardSummaryBefore, viewModel.uiState.value.dashboardSummary)
        assertEquals(userNameBefore, viewModel.uiState.value.userName)
        assertEquals(1, usageLogRepository.getUsageStatusCallCount)
        assertEquals(1, dashboardRepository.getDailySummaryCallCount)
        assertEquals(1, myPageRepository.getMyInfoCallCount)
    }

    @Test
    fun `RefreshTodayReminders 조회가 실패하면 기존 todayReminders를 유지하고 ShowMessage effect를 보낸다`() = runTest(dispatcher) {
        reminderRepository.remindersResult = Result.success(
            ReminderListResult(listOf(reminder(id = "old-1", title = "기존 일정")), ReminderDataSource.REMOTE),
        )
        viewModel = createViewModel()
        advanceUntilIdle()
        val todayRemindersBefore = viewModel.uiState.value.todayReminders

        reminderRepository.remindersResult = Result.failure(IOException("network"))
        val effect = async { viewModel.effect.first() }

        viewModel.onEvent(MainUiEvent.RefreshTodayReminders)
        advanceUntilIdle()

        assertTrue(effect.await() is MainUiEffect.ShowMessage)
        assertEquals(todayRemindersBefore, viewModel.uiState.value.todayReminders)
    }

    private fun createViewModel(): MainViewModel = MainViewModel(
        getUsageStatusUseCase = GetUsageStatusUseCase(usageLogRepository),
        getDashboardSummaryUseCase = GetDashboardSummaryUseCase(dashboardRepository),
        getGoalUseCase = GetGoalUseCase(goalRepository),
        getMyInfoUseCase = GetMyInfoUseCase(myPageRepository),
        getRemindersUseCase = GetRemindersUseCase(reminderRepository),
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
        var getUsageStatusCallCount = 0

        override suspend fun getUsageLogs(date: String?) = throw UnsupportedOperationException()

        override suspend fun getUsageStatus(): Result<List<UsageStatus>> {
            getUsageStatusCallCount++
            return statusResult
        }

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
        var getDailySummaryCallCount = 0

        override suspend fun getDailySummary(): Result<DashboardSummary> {
            getDailySummaryCallCount++
            return summaryResult
        }
    }

    private class FakeGoalRepository : GoalRepository {
        var goalResult: Result<Goal?> = Result.success(null)

        override suspend fun getGoal(): Result<Goal?> = goalResult

        override suspend fun saveGoal(goal: Goal) = throw UnsupportedOperationException()
    }

    private class FakeMyPageRepository : MyPageRepository {
        var infoResult: Result<User> = Result.success(User(email = "user@phoneshim.com", nickname = "유리"))
        var getMyInfoCallCount = 0

        override suspend fun getMyInfo(): Result<User> {
            getMyInfoCallCount++
            return infoResult
        }

        override suspend fun updateMyInfo(name: String?, motivation: String?) = throw UnsupportedOperationException()

        override suspend fun withdraw(): Result<WithdrawalResult> = throw UnsupportedOperationException()
    }

    private class FakeReminderRepository : ReminderRepository {
        var remindersResult: Result<ReminderListResult> = Result.success(
            ReminderListResult(emptyList(), ReminderDataSource.REMOTE),
        )
        val requestedDates = mutableListOf<LocalDate>()

        override suspend fun getReminders(date: LocalDate): Result<ReminderListResult> {
            requestedDates += date
            return remindersResult
        }

        override suspend fun getReminder(id: String): Result<Reminder> = throw UnsupportedOperationException()

        override suspend fun createReminder(command: CreateReminderCommand): Result<Reminder> =
            throw UnsupportedOperationException()

        override suspend fun updateReminder(id: String, command: UpdateReminderCommand): Result<Reminder> =
            throw UnsupportedOperationException()

        override suspend fun deleteReminder(id: String): Result<Unit> = throw UnsupportedOperationException()
    }
}

private val KOREA_ZONE_ID = java.time.ZoneId.of("Asia/Seoul")
