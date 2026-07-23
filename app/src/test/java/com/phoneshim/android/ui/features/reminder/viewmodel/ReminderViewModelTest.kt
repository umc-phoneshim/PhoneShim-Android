package com.phoneshim.android.ui.features.reminder.viewmodel

import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReminderViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: ReminderViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = ReminderViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `parseTime converts valid time text to minutes`() {
        assertEquals(0, parseTime("00:00"))
        assertEquals(545, parseTime("9:05"))
        assertEquals(630, parseTime("10:30"))
        assertEquals(1439, parseTime("23:59"))
    }

    @Test
    fun `parseTime rejects invalid time text`() {
        listOf(
            "",
            "10",
            "10:30:20",
            "24:00",
            "10:60",
            "-1:00",
            "ab:cd",
        ).forEach { value ->
            assertNull("Expected '$value' to be invalid", parseTime(value))
        }
    }

    @Test
    fun `timeRangesOverlap detects every overlapping shape`() {
        assertTrue(timeRangesOverlap(570, 630, 600, 660))
        assertTrue(timeRangesOverlap(630, 690, 600, 660))
        assertTrue(timeRangesOverlap(615, 645, 600, 660))
        assertTrue(timeRangesOverlap(540, 720, 600, 660))
        assertTrue(timeRangesOverlap(600, 660, 600, 660))
    }

    @Test
    fun `timeRangesOverlap allows adjacent ranges`() {
        assertFalse(timeRangesOverlap(540, 600, 600, 660))
        assertFalse(timeRangesOverlap(660, 720, 600, 660))
    }

    @Test
    fun `saveTask rejects blank title and keeps popup open`() {
        prepareNewTask(title = "", start = "09:00", end = "10:00")
        val tasksBeforeSave = viewModel.uiState.value.selectedTasks

        viewModel.saveTask()

        val state = viewModel.uiState.value
        assertEquals("할 일 이름을 입력해 주세요", state.draft.titleError)
        assertNull(state.draft.timeError)
        assertEquals(tasksBeforeSave, state.selectedTasks)
        assertTrue(state.isTaskPopupVisible)
    }

    @Test
    fun `saveTask rejects title longer than twenty characters`() {
        prepareNewTask(title = "가".repeat(21), start = "09:00", end = "10:00")

        viewModel.saveTask()

        val state = viewModel.uiState.value
        assertEquals("이름은 20자 이내로 입력해 주세요", state.draft.titleError)
        assertTrue(state.selectedTasks.isEmpty())
        assertTrue(state.isTaskPopupVisible)
    }

    @Test
    fun `saveTask rejects malformed time`() {
        prepareNewTask(title = "운동", start = "invalid", end = "10:00")

        viewModel.saveTask()

        val state = viewModel.uiState.value
        assertEquals("시간을 HH:mm 형식으로 입력해 주세요", state.draft.timeError)
        assertTrue(state.selectedTasks.isEmpty())
        assertTrue(state.isTaskPopupVisible)
    }

    @Test
    fun `saveTask rejects end time equal to or earlier than start time`() {
        prepareNewTask(title = "운동", start = "10:00", end = "10:00")
        viewModel.saveTask()
        assertEquals(
            "종료 시간은 시작 시간보다 이후여야 합니다",
            viewModel.uiState.value.draft.timeError,
        )

        viewModel.updateEndTime("09:59")
        viewModel.saveTask()
        assertEquals(
            "종료 시간은 시작 시간보다 이후여야 합니다",
            viewModel.uiState.value.draft.timeError,
        )
    }

    @Test
    fun `saveTask rejects a task overlapping an existing task`() {
        viewModel.openAddPopup()
        viewModel.updateTitle("겹치는 일정")
        viewModel.updateStartTime("10:30")
        viewModel.updateEndTime("11:30")
        val tasksBeforeSave = viewModel.uiState.value.selectedTasks

        viewModel.saveTask()

        val state = viewModel.uiState.value
        assertEquals("이미 해당 시간에 등록된 할 일이 있습니다", state.draft.timeError)
        assertEquals(tasksBeforeSave, state.selectedTasks)
        assertTrue(state.isTaskPopupVisible)
    }

    @Test
    fun `saveTask adds a task to selected date and resets editor`() {
        prepareNewTask(
            title = "  운동  ",
            start = "09:00",
            end = "10:00",
            restrictionMode = RestrictionMode.SPECIFIC_APPS,
            restrictedAppId = "youtube",
        )

        viewModel.saveTask()

        val state = viewModel.uiState.value
        val task = state.selectedTasks.single()
        assertNotNull(task.id)
        assertTrue(task.id.isNotBlank())
        assertEquals(EMPTY_DATE, task.date)
        assertEquals("운동", task.title)
        assertEquals(540, task.startMinutes)
        assertEquals(600, task.endMinutes)
        assertEquals(RestrictionMode.SPECIFIC_APPS, task.restrictionMode)
        assertEquals(setOf("youtube"), task.restrictedAppIds)
        assertFalse(state.isTaskPopupVisible)
        assertNull(state.editingTask)
        assertEquals(ReminderDraft(), state.draft)
    }

    @Test
    fun `saveTask sorts added tasks by start time`() {
        prepareNewTask(title = "두 번째", start = "11:00", end = "12:00")
        viewModel.saveTask()
        prepareNewTask(title = "첫 번째", start = "09:00", end = "10:00")
        viewModel.saveTask()

        assertEquals(
            listOf("첫 번째", "두 번째"),
            viewModel.uiState.value.selectedTasks.map(ReminderTaskUiModel::title),
        )
    }

    @Test
    fun `saveTask updates an existing task without creating a duplicate`() {
        val original = viewModel.uiState.value.selectedTasks.first()
        viewModel.openEditPopup(original)
        viewModel.updateTitle("수정된 과제")
        viewModel.updateStartTime("10:15")
        viewModel.updateEndTime("11:15")

        viewModel.saveTask()

        val state = viewModel.uiState.value
        val updated = state.selectedTasks.single { it.id == original.id }
        assertEquals(2, state.selectedTasks.size)
        assertEquals("수정된 과제", updated.title)
        assertEquals(615, updated.startMinutes)
        assertEquals(675, updated.endMinutes)
        assertFalse(state.isTaskPopupVisible)
    }

    @Test
    fun `saveTask ignores the edited task itself during overlap validation`() {
        val original = viewModel.uiState.value.selectedTasks.first()
        viewModel.openEditPopup(original)

        viewModel.saveTask()

        val state = viewModel.uiState.value
        assertEquals(2, state.selectedTasks.size)
        assertEquals(original, state.selectedTasks.single { it.id == original.id })
        assertFalse(state.isTaskPopupVisible)
    }

    @Test
    fun `deleteTask removes only the edited task and resets editor`() {
        val originalTasks = viewModel.uiState.value.selectedTasks
        val taskToDelete = originalTasks.first()
        val untouchedTask = originalTasks.last()
        viewModel.openEditPopup(taskToDelete)

        viewModel.deleteTask()

        val state = viewModel.uiState.value
        assertEquals(listOf(untouchedTask), state.selectedTasks)
        assertFalse(state.isTaskPopupVisible)
        assertNull(state.editingTask)
        assertEquals(ReminderDraft(), state.draft)
    }

    @Test
    fun `deleteTask does nothing when adding a new task`() {
        val originalTasks = viewModel.uiState.value.selectedTasks
        viewModel.openAddPopup()

        viewModel.deleteTask()

        val state = viewModel.uiState.value
        assertEquals(originalTasks, state.selectedTasks)
        assertTrue(state.isTaskPopupVisible)
        assertNotEquals(null, state.draft)
    }

    @Test
    fun `editing tasks on one date does not change tasks on another date`() {
        val defaultDate = viewModel.uiState.value.selectedDate
        val defaultTasks = viewModel.uiState.value.selectedTasks
        prepareNewTask(title = "다른 날짜 일정", start = "09:00", end = "10:00")
        viewModel.saveTask()

        viewModel.selectDate(defaultDate)

        assertEquals(defaultTasks, viewModel.uiState.value.selectedTasks)
        assertEquals(
            "다른 날짜 일정",
            viewModel.uiState.value.tasksByDate.getValue(EMPTY_DATE).single().title,
        )
    }

    private fun prepareNewTask(
        title: String,
        start: String,
        end: String,
        restrictionMode: RestrictionMode = RestrictionMode.NONE,
        restrictedAppId: String? = null,
    ) {
        viewModel.selectDate(EMPTY_DATE)
        viewModel.openAddPopup()
        viewModel.updateTitle(title)
        viewModel.updateStartTime(start)
        viewModel.updateEndTime(end)
        viewModel.updateRestrictionMode(restrictionMode)
        restrictedAppId?.let(viewModel::toggleRestrictedApp)
    }

    private companion object {
        val EMPTY_DATE: LocalDate = LocalDate.of(2026, 7, 18)
    }
}
