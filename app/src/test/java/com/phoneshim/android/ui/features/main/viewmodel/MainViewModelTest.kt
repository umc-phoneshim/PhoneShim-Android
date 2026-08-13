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
import com.phoneshim.android.domain.usecase.ObserveRemindersUseCase
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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
    fun `최초 진입 시 GetRemindersUseCase로 오늘 날짜를 서버에서 조회해 캐시를 채운다`() = runTest(dispatcher) {
        advanceUntilIdle()

        // 캐시가 완전히 비어있는 최초 설치 시나리오(타로 리뷰) — observeTodayReminders()의
        // Flow만으로는 서버 데이터를 알 수 없어 fetchDashboard()가 최초 1회 서버를 불러야 한다.
        assertEquals(listOf(LocalDate.now(KOREA_ZONE_ID)), reminderRepository.getRemindersCalledDates)
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

    @Test
    fun `RefreshUsageAndDashboard 이벤트가 오면 usageStatus,dashboardSummary만 다시 조회하고 userName,todayReminders,isGoalSet은 그대로 유지한다`() = runTest(dispatcher) {
        advanceUntilIdle()
        val userNameBefore = viewModel.uiState.value.userName
        val todayRemindersBefore = viewModel.uiState.value.todayReminders
        val isGoalSetBefore = viewModel.uiState.value.isGoalSet
        assertEquals(1, usageLogRepository.getUsageStatusCallCount)
        assertEquals(1, dashboardRepository.getDailySummaryCallCount)
        assertEquals(1, myPageRepository.getMyInfoCallCount)
        assertEquals(1, goalRepository.getGoalCallCount)
        assertEquals(1, reminderRepository.getRemindersCalledDates.size)

        // 메인 탭을 벗어났다 돌아온 상황을 흉내낸다 — 그 사이 서버 값이 바뀌었다고 가정.
        usageLogRepository.statusResult = Result.success(
            listOf(UsageStatus(monitoredAppId = "m-2", appName = "유튜브", packageName = "com.google.android.youtube", usedMinutes = 40, entryCount = 4)),
        )
        dashboardRepository.summaryResult = Result.success(
            DashboardSummary(date = "2026-08-13", targetMinutes = 210, usedMinutes = 150, remainingMinutes = 60, isExceeded = false),
        )

        viewModel.onEvent(MainUiEvent.RefreshUsageAndDashboard)
        advanceUntilIdle()

        assertEquals(listOf("m-2"), viewModel.uiState.value.usageStatus.map { it.monitoredAppId })
        assertEquals(150, viewModel.uiState.value.dashboardSummary?.usedMinutes)
        // isGoalSet/userName/todayReminders는 이 이벤트가 손대지 않으므로 값도, 호출 횟수도 그대로다.
        assertEquals(isGoalSetBefore, viewModel.uiState.value.isGoalSet)
        assertEquals(userNameBefore, viewModel.uiState.value.userName)
        assertEquals(todayRemindersBefore, viewModel.uiState.value.todayReminders)
        assertEquals(1, myPageRepository.getMyInfoCallCount)
        assertEquals(1, goalRepository.getGoalCallCount)
        assertEquals(1, reminderRepository.getRemindersCalledDates.size)
        // usageStatus/dashboardSummary만 정확히 한 번씩 더 불렸다(초기 1회 + 재조회 1회).
        assertEquals(2, usageLogRepository.getUsageStatusCallCount)
        assertEquals(2, dashboardRepository.getDailySummaryCallCount)
    }

    @Test
    fun `LoadDashboard가 아직 isLoading 상태일 때 RefreshUsageAndDashboard가 오면 무시된다`() = runTest(dispatcher) {
        // init의 fetchDashboard()가 setState { isLoading = true }를 동기로 실행한 직후, 아직
        // viewModelScope.launch 코루틴이 끝나기 전 시점 — MainScreen의 ON_RESUME 구독이 라이프사이클
        // 등록 시점에 바로 한 번 더 발화하는 동기 catch-up 호출이 이 타이밍에 해당한다.
        assertTrue(viewModel.uiState.value.isLoading)

        viewModel.onEvent(MainUiEvent.RefreshUsageAndDashboard)
        advanceUntilIdle()

        // 가드에 걸려 무시됐다면 usageStatus/dashboardSummary는 fetchDashboard()가 부른 1번씩만 있다.
        assertEquals(1, usageLogRepository.getUsageStatusCallCount)
        assertEquals(1, dashboardRepository.getDailySummaryCallCount)
    }

    @Test
    fun `RefreshUsageAndDashboard를 반복 호출해도(탭 반복 전환) 매번 usageStatus,dashboardSummary가 재조회된다`() = runTest(dispatcher) {
        advanceUntilIdle()
        assertEquals(1, usageLogRepository.getUsageStatusCallCount)
        assertEquals(1, dashboardRepository.getDailySummaryCallCount)

        // 탭을 여러 번 왔다갔다 하며 ON_RESUME이 반복 발생하는 상황을 흉내낸다.
        repeat(3) {
            viewModel.onEvent(MainUiEvent.RefreshUsageAndDashboard)
            advanceUntilIdle()
        }

        // 초기 1회 + 반복 3회 = 총 4회 — isLoading 가드는 최초의 동기 catch-up 호출만 무시하고,
        // 그 이후의 진짜 재진입 호출은 매번 정상적으로 재조회를 트리거해야 한다.
        assertEquals(4, usageLogRepository.getUsageStatusCallCount)
        assertEquals(4, dashboardRepository.getDailySummaryCallCount)
    }

    @Test
    fun `RefreshUsageAndDashboard 조회가 실패하면 기존 usageStatus,dashboardSummary를 유지하고 ShowMessage effect를 보낸다`() = runTest(dispatcher) {
        advanceUntilIdle()
        val usageStatusBefore = viewModel.uiState.value.usageStatus
        val dashboardSummaryBefore = viewModel.uiState.value.dashboardSummary

        usageLogRepository.statusResult = Result.failure(IllegalStateException("network"))
        val effect = async { viewModel.effect.first() }

        viewModel.onEvent(MainUiEvent.RefreshUsageAndDashboard)
        advanceUntilIdle()

        assertTrue(effect.await() is MainUiEffect.ShowMessage)
        assertEquals(usageStatusBefore, viewModel.uiState.value.usageStatus)
        assertEquals(dashboardSummaryBefore, viewModel.uiState.value.dashboardSummary)
    }

    private fun createViewModel(): MainViewModel = MainViewModel(
        getUsageStatusUseCase = GetUsageStatusUseCase(usageLogRepository),
        getDashboardSummaryUseCase = GetDashboardSummaryUseCase(dashboardRepository),
        getGoalUseCase = GetGoalUseCase(goalRepository),
        getMyInfoUseCase = GetMyInfoUseCase(myPageRepository),
        getRemindersUseCase = GetRemindersUseCase(reminderRepository),
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
        var getGoalCallCount = 0

        override suspend fun getGoal(): Result<Goal?> {
            getGoalCallCount++
            return goalResult
        }

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

        override suspend fun updateUserProfile(gender: String, ageGroup: String) =
            throw UnsupportedOperationException()

        override suspend fun withdraw(): Result<WithdrawalResult> = throw UnsupportedOperationException()
    }

    private class FakeReminderRepository : ReminderRepository {
        val remindersFlow = MutableStateFlow<List<Reminder>>(emptyList())
        val observedDates = mutableListOf<LocalDate>()
        var getRemindersResult: Result<ReminderListResult> = Result.success(
            ReminderListResult(emptyList(), ReminderDataSource.REMOTE),
        )
        val getRemindersCalledDates = mutableListOf<LocalDate>()

        override suspend fun getReminders(date: LocalDate): Result<ReminderListResult> {
            getRemindersCalledDates += date
            return getRemindersResult
        }

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
