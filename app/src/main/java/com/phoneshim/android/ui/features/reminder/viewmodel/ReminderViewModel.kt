package com.phoneshim.android.ui.features.reminder.viewmodel

import com.phoneshim.android.ui.common.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.YearMonth
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ReminderViewModel @Inject constructor() :
    BaseViewModel<ReminderUiState, ReminderUiEvent, ReminderUiEffect>(ReminderUiState()) {

    override fun handleEvent(event: ReminderUiEvent) {
        when (event) {
            is ReminderUiEvent.DateSelected -> selectDate(event)
            is ReminderUiEvent.MonthMoved -> moveMonth(event)
            ReminderUiEvent.AddTaskClicked -> openAddPopup()
            is ReminderUiEvent.EditTaskClicked -> openEditPopup(event)
            ReminderUiEvent.PopupDismissed -> dismissPopup()
            is ReminderUiEvent.TitleChanged -> updateTitle(event)
            is ReminderUiEvent.StartTimeChanged -> updateStartTime(event)
            is ReminderUiEvent.EndTimeChanged -> updateEndTime(event)
            is ReminderUiEvent.RestrictionModeChanged -> updateRestrictionMode(event)
            is ReminderUiEvent.RestrictedAppToggled -> toggleRestrictedApp(event)
            ReminderUiEvent.SaveTaskClicked -> saveTask()
            ReminderUiEvent.DeleteTaskClicked -> deleteTask()
        }
    }

    private fun selectDate(event: ReminderUiEvent.DateSelected) = setState {
        copy(selectedDate = event.date, visibleMonth = YearMonth.from(event.date))
    }

    private fun moveMonth(event: ReminderUiEvent.MonthMoved) = setState {
        copy(visibleMonth = visibleMonth.plusMonths(event.offset))
    }

    private fun openAddPopup() = setState {
        copy(editingTask = null, draft = ReminderDraft(title = "과제"), isTaskPopupVisible = true)
    }

    private fun openEditPopup(event: ReminderUiEvent.EditTaskClicked) = setState {
        copy(
            editingTask = event.task,
            draft = ReminderDraft(
                editingTaskId = event.task.id,
                title = event.task.title,
                startTimeText = formatMinutes(event.task.startMinutes),
                endTimeText = formatMinutes(event.task.endMinutes),
                restrictionMode = event.task.restrictionMode,
                restrictedAppIds = event.task.restrictedAppIds,
            ),
            isTaskPopupVisible = true,
        )
    }

    private fun dismissPopup() = setState {
        copy(editingTask = null, draft = ReminderDraft(), isTaskPopupVisible = false)
    }

    private fun updateTitle(event: ReminderUiEvent.TitleChanged) = updateDraft {
        copy(
            title = event.value,
            titleError = if (event.value.length > 20) "이름은 20자 이내로 입력해 주세요" else null,
        )
    }

    private fun updateStartTime(event: ReminderUiEvent.StartTimeChanged) = updateDraft {
        copy(startTimeText = event.value.take(5), timeError = null)
    }

    private fun updateEndTime(event: ReminderUiEvent.EndTimeChanged) = updateDraft {
        copy(endTimeText = event.value.take(5), timeError = null)
    }

    private fun updateRestrictionMode(event: ReminderUiEvent.RestrictionModeChanged) = updateDraft {
        copy(
            restrictionMode = event.mode,
            restrictedAppIds = if (event.mode == RestrictionMode.SPECIFIC_APPS) {
                restrictedAppIds
            } else {
                emptySet()
            },
        )
    }

    private fun toggleRestrictedApp(event: ReminderUiEvent.RestrictedAppToggled) = updateDraft {
        copy(
            restrictedAppIds = if (event.appId in restrictedAppIds) {
                restrictedAppIds - event.appId
            } else {
                restrictedAppIds + event.appId
            },
        )
    }

    private fun saveTask() {
        val state = currentState
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
            setState {
                copy(draft = draft.copy(titleError = titleError, timeError = timeError))
            }
            sendEffect(ReminderUiEffect.ShowMessage(requireNotNull(timeError ?: titleError)))
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
        setState {
            copy(
                tasksByDate = tasksByDate + (state.selectedDate to updated.sortedBy(ReminderTaskUiModel::startMinutes)),
                editingTask = null,
                draft = ReminderDraft(),
                isTaskPopupVisible = false,
            )
        }
        // TODO: 실제 제한 엔진 연동 시 저장된 restrictionMode와 restrictedAppIds를 전달합니다.
    }

    private fun deleteTask() {
        val id = currentState.draft.editingTaskId ?: return
        setState {
            val updated = tasksByDate[selectedDate].orEmpty().filterNot { it.id == id }
            copy(
                tasksByDate = tasksByDate + (selectedDate to updated),
                editingTask = null,
                draft = ReminderDraft(),
                isTaskPopupVisible = false,
            )
        }
    }

    private fun updateDraft(transform: ReminderDraft.() -> ReminderDraft) = setState {
        copy(draft = draft.transform())
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
