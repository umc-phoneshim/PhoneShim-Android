package com.phoneshim.android.ui.features.reminder.viewmodel

import com.phoneshim.android.ui.common.base.UiEffect
import com.phoneshim.android.ui.common.base.UiEvent
import com.phoneshim.android.ui.common.base.UiState
import java.time.LocalDate
import java.time.YearMonth

enum class RestrictionMode { NONE, FULL_PHONE, SPECIFIC_APPS }

internal const val DUPLICATE_SCHEDULE_MESSAGE = "중복된 일정은 등록할 수 없어요!"

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

data class ReminderUiState(
    val todayDate: LocalDate,
    val selectedDate: LocalDate = todayDate,
    val visibleMonth: YearMonth = YearMonth.from(selectedDate),
    val tasksByDate: Map<LocalDate, List<ReminderTaskUiModel>> = emptyMap(),
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val loadErrorMessage: String? = null,
    val isTaskPopupVisible: Boolean = false,
    val draft: ReminderDraft = ReminderDraft(),
) : UiState {
    val selectedTasks: List<ReminderTaskUiModel>
        get() = tasksByDate[selectedDate].orEmpty()
}

sealed interface ReminderUiEvent : UiEvent {
    data class DateSelected(val date: LocalDate) : ReminderUiEvent
    data class MonthMoved(val offset: Long) : ReminderUiEvent
    data object RetryClicked : ReminderUiEvent
    data object AddTaskClicked : ReminderUiEvent
    data class EditTaskClicked(val task: ReminderTaskUiModel) : ReminderUiEvent
    data class TaskMoved(val fromIndex: Int, val toIndex: Int) : ReminderUiEvent
    data object PopupDismissed : ReminderUiEvent
    data class TitleChanged(val value: String) : ReminderUiEvent
    data class StartTimeChanged(val value: String) : ReminderUiEvent
    data class EndTimeChanged(val value: String) : ReminderUiEvent
    data class RestrictionModeChanged(val mode: RestrictionMode) : ReminderUiEvent
    data class RestrictedAppToggled(val appId: String) : ReminderUiEvent
    data object SaveTaskClicked : ReminderUiEvent
    data object DeleteTaskClicked : ReminderUiEvent
}

sealed interface ReminderUiEffect : UiEffect {
    data class ShowMessage(val message: String) : ReminderUiEffect
}
