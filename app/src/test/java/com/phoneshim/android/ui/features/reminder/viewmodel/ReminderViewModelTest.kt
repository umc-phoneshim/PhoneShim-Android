package com.phoneshim.android.ui.features.reminder.viewmodel

import com.phoneshim.android.data.api.common.ApiError
import com.phoneshim.android.data.api.common.ApiException
import com.phoneshim.android.domain.model.CreateReminderCommand
import com.phoneshim.android.domain.model.Reminder
import com.phoneshim.android.domain.model.ReminderRestrictionMode
import com.phoneshim.android.domain.model.UpdateReminderCommand
import com.phoneshim.android.domain.repository.ReminderRepository
import com.phoneshim.android.domain.usecase.CreateReminderUseCase
import com.phoneshim.android.domain.usecase.DeleteReminderUseCase
import com.phoneshim.android.domain.usecase.GetRemindersUseCase
import com.phoneshim.android.domain.usecase.UpdateReminderUseCase
import com.phoneshim.android.ui.common.base.CommonUiEffect
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReminderViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeReminderRepository
    private lateinit var viewModel: ReminderViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repository = FakeReminderRepository()
        viewModel = createViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `화면 진입 시 오늘 리마인더를 조회한다`() = runTest(dispatcher) {
        advanceUntilIdle()

        assertEquals(listOf(viewModel.uiState.value.todayDate), repository.requestedDates)
        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.selectedTasks.isEmpty())
    }

    @Test
    fun `날짜 선택 시 해당 날짜의 서버 일정을 표시한다`() = runTest(dispatcher) {
        advanceUntilIdle()
        val date = LocalDate.of(2026, 7, 18)
        repository.remindersByDate[date] = listOf(reminder(date = date, title = "운동"))

        viewModel.onEvent(ReminderUiEvent.DateSelected(date))
        advanceUntilIdle()

        assertEquals(date, viewModel.uiState.value.selectedDate)
        assertEquals("운동", viewModel.uiState.value.selectedTasks.single().title)
        assertEquals(600, viewModel.uiState.value.selectedTasks.single().startMinutes)
    }

    @Test
    fun `생성 성공 시 서버 id를 가진 일정을 목록에 추가한다`() = runTest(dispatcher) {
        advanceUntilIdle()
        val date = viewModel.uiState.value.selectedDate
        repository.createResult = Result.success(reminder(id = "server-id", date = date, title = "운동"))
        prepareNewTask("운동", "10:00", "11:00")

        viewModel.onEvent(ReminderUiEvent.SaveTaskClicked)
        advanceUntilIdle()

        val command = requireNotNull(repository.lastCreateCommand)
        assertEquals(Instant.parse("${date}T01:00:00Z"), command.startTime)
        assertEquals("server-id", viewModel.uiState.value.selectedTasks.single().id)
        assertFalse(viewModel.uiState.value.isTaskPopupVisible)
        assertFalse(viewModel.uiState.value.isSubmitting)
    }

    @Test
    fun `수정 성공 시 기존 일정을 서버 응답으로 교체한다`() = runTest(dispatcher) {
        val configuredRepository = FakeReminderRepository()
        val date = LocalDate.now(java.time.ZoneId.of("Asia/Seoul"))
        configuredRepository.remindersByDate[date] = listOf(reminder(date = date))
        viewModel = createViewModel(configuredRepository)
        advanceUntilIdle()
        val original = viewModel.uiState.value.selectedTasks.single()
        configuredRepository.updateResult = Result.success(reminder(date = date, title = "수정 완료"))
        viewModel.onEvent(ReminderUiEvent.EditTaskClicked(original))
        viewModel.onEvent(ReminderUiEvent.TitleChanged("수정 완료"))

        viewModel.onEvent(ReminderUiEvent.SaveTaskClicked)
        advanceUntilIdle()

        assertEquals("reminder-1", configuredRepository.lastUpdateId)
        assertEquals("수정 완료", viewModel.uiState.value.selectedTasks.single().title)
        assertEquals(1, viewModel.uiState.value.selectedTasks.size)
    }

    @Test
    fun `삭제 성공 시 목록에서 제거한다`() = runTest(dispatcher) {
        val configuredRepository = FakeReminderRepository()
        val date = LocalDate.now(java.time.ZoneId.of("Asia/Seoul"))
        configuredRepository.remindersByDate[date] = listOf(reminder(date = date))
        viewModel = createViewModel(configuredRepository)
        advanceUntilIdle()
        viewModel.onEvent(ReminderUiEvent.EditTaskClicked(viewModel.uiState.value.selectedTasks.single()))

        viewModel.onEvent(ReminderUiEvent.DeleteTaskClicked)
        advanceUntilIdle()

        assertEquals("reminder-1", configuredRepository.lastDeleteId)
        assertTrue(viewModel.uiState.value.selectedTasks.isEmpty())
        assertFalse(viewModel.uiState.value.isTaskPopupVisible)
    }

    @Test
    fun `서버 중복 오류는 팝업의 중복 일정 오류로 표시한다`() = runTest(dispatcher) {
        advanceUntilIdle()
        repository.createResult = Result.failure(
            ApiException.Server(ApiError("REMINDER_TIME_OVERLAP", "overlap")),
        )
        prepareNewTask("운동", "10:00", "11:00")

        viewModel.onEvent(ReminderUiEvent.SaveTaskClicked)
        advanceUntilIdle()

        assertEquals(DUPLICATE_SCHEDULE_MESSAGE, viewModel.uiState.value.draft.timeError)
        assertTrue(viewModel.uiState.value.isTaskPopupVisible)
        assertFalse(viewModel.uiState.value.isSubmitting)
    }

    @Test
    fun `조회 네트워크 오류 후 재시도할 수 있다`() = runTest(dispatcher) {
        val failingRepository = FakeReminderRepository().apply {
            getResultOverride = Result.failure(ApiException.Network(IOException("offline")))
        }
        viewModel = createViewModel(failingRepository)
        advanceUntilIdle()
        assertEquals("네트워크에 연결할 수 없어요. 연결 상태를 확인해 주세요.", viewModel.uiState.value.loadErrorMessage)

        failingRepository.getResultOverride = null
        viewModel.onEvent(ReminderUiEvent.RetryClicked)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.loadErrorMessage)
        assertEquals(2, failingRepository.requestedDates.size)
    }

    @Test
    fun `401 조회 오류는 인증 만료 공통 효과를 보낸다`() = runTest(dispatcher) {
        val unauthorizedRepository = FakeReminderRepository().apply {
            getResultOverride = Result.failure(
                ApiException.Http(401, ApiError("INVALID_TOKEN", "expired"), RuntimeException()),
            )
        }
        viewModel = createViewModel(unauthorizedRepository)
        val effect = async { viewModel.commonEffect.first() }

        advanceUntilIdle()

        assertEquals(CommonUiEffect.AuthExpired, effect.await())
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `로컬 입력 검증은 API 호출 전에 수행한다`() = runTest(dispatcher) {
        advanceUntilIdle()
        prepareNewTask("", "10:00", "09:00")

        viewModel.onEvent(ReminderUiEvent.SaveTaskClicked)
        advanceUntilIdle()

        assertNull(repository.lastCreateCommand)
        assertEquals("할 일 이름을 입력해 주세요", viewModel.uiState.value.draft.titleError)
        assertEquals("종료 시간은 시작 시간보다 이후여야 합니다", viewModel.uiState.value.draft.timeError)
    }

    @Test
    fun `인접한 시간은 겹침으로 판단하지 않는다`() {
        assertFalse(timeRangesOverlap(540, 600, 600, 660))
        assertFalse(timeRangesOverlap(660, 720, 600, 660))
        assertTrue(timeRangesOverlap(570, 630, 600, 660))
    }

    private fun createViewModel(repository: ReminderRepository) = ReminderViewModel(
        GetRemindersUseCase(repository),
        CreateReminderUseCase(repository),
        UpdateReminderUseCase(repository),
        DeleteReminderUseCase(repository),
    )

    private fun prepareNewTask(title: String, start: String, end: String) {
        viewModel.onEvent(ReminderUiEvent.AddTaskClicked)
        viewModel.onEvent(ReminderUiEvent.TitleChanged(title))
        viewModel.onEvent(ReminderUiEvent.StartTimeChanged(start))
        viewModel.onEvent(ReminderUiEvent.EndTimeChanged(end))
    }
}

private class FakeReminderRepository : ReminderRepository {
    val remindersByDate = mutableMapOf<LocalDate, List<Reminder>>()
    val requestedDates = mutableListOf<LocalDate>()
    var getResultOverride: Result<List<Reminder>>? = null
    var createResult: Result<Reminder>? = null
    var updateResult: Result<Reminder>? = null
    var deleteResult: Result<Unit> = Result.success(Unit)
    var lastCreateCommand: CreateReminderCommand? = null
    var lastUpdateId: String? = null
    var lastDeleteId: String? = null

    override suspend fun getReminders(date: LocalDate): Result<List<Reminder>> {
        requestedDates += date
        return getResultOverride ?: Result.success(remindersByDate[date].orEmpty())
    }

    override suspend fun getReminder(id: String): Result<Reminder> = error("Not used")

    override suspend fun createReminder(command: CreateReminderCommand): Result<Reminder> {
        lastCreateCommand = command
        return createResult ?: Result.success(reminder(date = command.date, title = command.title))
    }

    override suspend fun updateReminder(id: String, command: UpdateReminderCommand): Result<Reminder> {
        lastUpdateId = id
        return updateResult ?: error("Update result was not configured")
    }

    override suspend fun deleteReminder(id: String): Result<Unit> {
        lastDeleteId = id
        return deleteResult
    }
}

private fun reminder(
    id: String = "reminder-1",
    date: LocalDate,
    title: String = "과제하기",
) = Reminder(
    id = id,
    userId = "user-1",
    date = date,
    title = title,
    startTime = Instant.parse("${date}T01:00:00Z"),
    endTime = Instant.parse("${date}T02:00:00Z"),
    restrictionMode = ReminderRestrictionMode.NONE,
    restrictedAppIds = emptySet(),
    createdAt = Instant.parse("2026-07-15T12:00:00Z"),
    updatedAt = Instant.parse("2026-07-15T12:00:00Z"),
)
