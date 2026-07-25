package com.phoneshim.android.ui.features.reminder.viewmodel

import com.phoneshim.android.ui.common.base.UiEffect
import com.phoneshim.android.ui.common.base.UiEvent
import com.phoneshim.android.ui.common.base.UiState
import java.time.LocalDate
import java.time.YearMonth

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
) : UiState {
    val selectedTasks: List<ReminderTaskUiModel>
        get() = tasksByDate[selectedDate].orEmpty().sortedBy { it.startMinutes }
}

sealed interface ReminderUiEvent : UiEvent {
    data class DateSelected(val date: LocalDate) : ReminderUiEvent
    data class MonthMoved(val offset: Long) : ReminderUiEvent
    data object AddTaskClicked : ReminderUiEvent
    data class EditTaskClicked(val task: ReminderTaskUiModel) : ReminderUiEvent
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

private fun defaultTasks(): Map<LocalDate, List<ReminderTaskUiModel>> {
    val date = LocalDate.of(2026, 7, 17)
    return mapOf(
        date to listOf(
            ReminderTaskUiModel("mock-1", date, "과제하기", 600, 660),
            ReminderTaskUiModel("mock-2", date, "과제하기", 780, 840),
        ),
    )
}
