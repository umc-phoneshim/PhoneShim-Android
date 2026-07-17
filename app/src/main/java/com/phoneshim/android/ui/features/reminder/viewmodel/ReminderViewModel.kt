package com.phoneshim.android.ui.features.reminder.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class RestrictionMode { NONE, FULL_PHONE, SPECIFIC_APPS }

data class ReminderTaskUiModel(
    val id: String,
    val date: LocalDate,
    val title: String,
    val startMinutes: Int,
    val endMinutes: Int,
    val restrictionMode: RestrictionMode = RestrictionMode.NONE,
    val restrictedAppIds: Set<String> = emptySet(),
)

data class ReminderDraft(
    val editingTaskId: String? = null,
    val title: String = "",
    val startTimeText: String = "",
    val endTimeText: String = "",
    val restrictionMode: RestrictionMode = RestrictionMode.NONE,
    val restrictedAppIds: Set<String> = emptySet(),
    val titleError: String? = null,
    val timeError: String? = null,
)

data class MockRestrictedApp(val id: String, val name: String)

data class ReminderUiState(
    val todayDate: LocalDate = LocalDate.of(2026, 7, 11),
    val selectedDate: LocalDate = LocalDate.of(2026, 7, 17),
    val visibleMonth: YearMonth = YearMonth.of(2026, 7),
    val tasksByDate: Map<LocalDate, List<ReminderTaskUiModel>> = defaultTasks(),
    val editingTask: ReminderTaskUiModel? = null,
    val isTaskPopupVisible: Boolean = false,
    val isDatePickerVisible: Boolean = false,
    val draft: ReminderDraft = ReminderDraft(),
    val mockApps: List<MockRestrictedApp> = listOf(
        MockRestrictedApp("kakao", "카카오톡"),
        MockRestrictedApp("youtube", "YouTube"),
        MockRestrictedApp("instagram", "Instagram"),
    ),
    val message: String? = null,
) {
    val selectedTasks: List<ReminderTaskUiModel>
        get() = tasksByDate[selectedDate].orEmpty().sortedBy { it.startMinutes }
}

@HiltViewModel
class ReminderViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(ReminderUiState())
    val uiState: StateFlow<ReminderUiState> = _uiState.asStateFlow()

    fun selectDate(date: LocalDate) = _uiState.update {
        it.copy(selectedDate = date, visibleMonth = YearMonth.from(date))
    }

    fun moveMonth(monthOffset: Long) = _uiState.update {
        it.copy(visibleMonth = it.visibleMonth.plusMonths(monthOffset))
    }

    fun openAddPopup() = _uiState.update {
        it.copy(editingTask = null, draft = ReminderDraft(title = "과제"), isTaskPopupVisible = true)
    }

    fun openEditPopup(task: ReminderTaskUiModel) = _uiState.update {
        it.copy(
            editingTask = task,
            draft = ReminderDraft(
                editingTaskId = task.id,
                title = task.title,
                startTimeText = formatMinutes(task.startMinutes),
                endTimeText = formatMinutes(task.endMinutes),
                restrictionMode = task.restrictionMode,
                restrictedAppIds = task.restrictedAppIds,
            ),
            isTaskPopupVisible = true,
        )
    }

    fun dismissPopup() = _uiState.update {
        it.copy(editingTask = null, draft = ReminderDraft(), isTaskPopupVisible = false)
    }

    fun updateTitle(value: String) = updateDraft {
        copy(title = value, titleError = if (value.length > 20) "이름은 20자 이내로 입력해 주세요" else null)
    }

    fun updateStartTime(value: String) = updateDraft { copy(startTimeText = value.take(5), timeError = null) }
    fun updateEndTime(value: String) = updateDraft { copy(endTimeText = value.take(5), timeError = null) }
    fun updateRestrictionMode(mode: RestrictionMode) = updateDraft {
        copy(restrictionMode = mode, restrictedAppIds = if (mode == RestrictionMode.SPECIFIC_APPS) restrictedAppIds else emptySet())
    }

    fun toggleRestrictedApp(appId: String) = updateDraft {
        copy(restrictedAppIds = if (appId in restrictedAppIds) restrictedAppIds - appId else restrictedAppIds + appId)
    }

    fun saveTask() {
        val state = _uiState.value
        val draft = state.draft
        val start = parseTime(draft.startTimeText)
        val end = parseTime(draft.endTimeText)
        val titleError = when {
            draft.title.isBlank() -> "할 일 이름을 입력해 주세요"
            draft.title.length > 20 -> "이름은 20자 이내로 입력해 주세요"
            else -> null
        }
        val timeError = when {
            start == null || end == null -> "시간을 HH:mm 형식으로 입력해 주세요"
            end <= start -> "종료 시간은 시작 시간보다 이후여야 합니다"
            overlaps(state, start, end, draft.editingTaskId) -> "이미 해당 시간에 등록된 할 일이 있습니다"
            else -> null
        }
        if (titleError != null || timeError != null) {
            _uiState.update { it.copy(draft = draft.copy(titleError = titleError, timeError = timeError), message = timeError ?: titleError) }
            return
        }
        val task = ReminderTaskUiModel(
            id = draft.editingTaskId ?: UUID.randomUUID().toString(),
            date = state.selectedDate,
            title = draft.title.trim(),
            startMinutes = requireNotNull(start),
            endMinutes = requireNotNull(end),
            restrictionMode = draft.restrictionMode,
            restrictedAppIds = draft.restrictedAppIds,
        )
        val updated = state.tasksByDate[state.selectedDate].orEmpty().filterNot { it.id == task.id } + task
        _uiState.update {
            it.copy(
                tasksByDate = it.tasksByDate + (state.selectedDate to updated.sortedBy(ReminderTaskUiModel::startMinutes)),
                editingTask = null,
                draft = ReminderDraft(),
                isTaskPopupVisible = false,
                message = null,
            )
        }
        // TODO: 실제 제한 엔진 연동 시 저장된 restrictionMode와 restrictedAppIds를 전달합니다.
    }

    fun deleteTask() {
        val id = _uiState.value.draft.editingTaskId ?: return
        _uiState.update { state ->
            val updated = state.tasksByDate[state.selectedDate].orEmpty().filterNot { it.id == id }
            state.copy(
                tasksByDate = state.tasksByDate + (state.selectedDate to updated),
                editingTask = null,
                draft = ReminderDraft(),
                isTaskPopupVisible = false,
            )
        }
    }

    fun clearMessage() = _uiState.update { it.copy(message = null) }

    private fun updateDraft(transform: ReminderDraft.() -> ReminderDraft) = _uiState.update {
        it.copy(draft = it.draft.transform())
    }

    private fun overlaps(state: ReminderUiState, start: Int, end: Int, editingId: String?): Boolean =
        state.tasksByDate[state.selectedDate].orEmpty().any {
            it.id != editingId && timeRangesOverlap(start, end, it.startMinutes, it.endMinutes)
        }
}

internal fun timeRangesOverlap(newStart: Int, newEnd: Int, existingStart: Int, existingEnd: Int): Boolean =
    newStart < existingEnd && existingStart < newEnd

fun parseTime(value: String): Int? {
    val parts = value.split(':')
    if (parts.size != 2) return null
    val hour = parts[0].toIntOrNull() ?: return null
    val minute = parts[1].toIntOrNull() ?: return null
    if (hour !in 0..23 || minute !in 0..59) return null
    return hour * 60 + minute
}

fun formatMinutes(minutes: Int): String = "%02d:%02d".format(minutes / 60, minutes % 60)

private fun defaultTasks(): Map<LocalDate, List<ReminderTaskUiModel>> {
    val date = LocalDate.of(2026, 7, 17)
    return mapOf(
        date to listOf(
            ReminderTaskUiModel("mock-1", date, "과제하기", 600, 660),
            ReminderTaskUiModel("mock-2", date, "과제하기", 780, 840),
        ),
    )
}
