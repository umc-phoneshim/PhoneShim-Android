package com.phoneshim.android.ui.features.reminder.viewmodel

import androidx.lifecycle.viewModelScope
import com.phoneshim.android.data.api.ReminderErrorCodes
import com.phoneshim.android.data.realtime.ReminderRealtimeUpdateSource
import com.phoneshim.android.domain.model.CreateReminderCommand
import com.phoneshim.android.domain.model.Reminder
import com.phoneshim.android.domain.model.ReminderDataSource
import com.phoneshim.android.domain.model.ReminderRestrictionMode
import com.phoneshim.android.domain.model.UpdateReminderCommand
import com.phoneshim.android.domain.usecase.CreateReminderUseCase
import com.phoneshim.android.domain.usecase.DeleteReminderUseCase
import com.phoneshim.android.domain.usecase.GetRemindersUseCase
import com.phoneshim.android.domain.usecase.GetMonitoredAppsUseCase
import com.phoneshim.android.domain.usecase.UpdateReminderUseCase
import com.phoneshim.android.ui.common.PhoneShimSnackbarType
import com.phoneshim.android.ui.common.base.BaseViewModel
import com.phoneshim.android.ui.common.base.UiError
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.YearMonth
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

@HiltViewModel
class ReminderViewModel @Inject constructor(
    private val getReminders: GetRemindersUseCase,
    private val getMonitoredApps: GetMonitoredAppsUseCase,
    private val createReminder: CreateReminderUseCase,
    private val updateReminder: UpdateReminderUseCase,
    private val deleteReminder: DeleteReminderUseCase,
    private val reminderRealtimeUpdateSource: ReminderRealtimeUpdateSource,
) : BaseViewModel<ReminderUiState, ReminderUiEvent, ReminderUiEffect>(
    ReminderUiState(todayDate = LocalDate.now(KOREA_ZONE_ID)),
) {
    private var selectedDateLoadJob: Job? = null
    private var monthLoadJob: Job? = null

    init {
        loadMonitoredApps()
        loadMonth(currentState.visibleMonth)
        observeRealtimeUpdates()
    }

    private fun observeRealtimeUpdates() {
        viewModelScope.launch {
            reminderRealtimeUpdateSource.updates.collect { result ->
                val today = LocalDate.now(KOREA_ZONE_ID)
                setState {
                    copy(
                        tasksByDate = tasksByDate + (today to result.reminders.map(Reminder::toUiModel)),
                        cachedDates = if (result.source == ReminderDataSource.CACHE) {
                            cachedDates + today
                        } else {
                            cachedDates - today
                        },
                        isShowingCachedData = if (selectedDate == today) {
                            result.source == ReminderDataSource.CACHE
                        } else {
                            isShowingCachedData
                        },
                    )
                }
            }
        }
    }

    override fun handleEvent(event: ReminderUiEvent) {
        when (event) {
            is ReminderUiEvent.DateSelected -> selectDate(event)
            is ReminderUiEvent.MonthMoved -> moveMonth(event)
            ReminderUiEvent.RetryClicked -> loadMonth(currentState.visibleMonth)
            ReminderUiEvent.AddTaskClicked -> openAddPopup()
            is ReminderUiEvent.EditTaskClicked -> openEditPopup(event)
            is ReminderUiEvent.TaskMoved -> moveTask(event)
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

    private fun selectDate(event: ReminderUiEvent.DateSelected) {
        if (event.date == currentState.selectedDate && currentState.loadErrorMessage == null) return
        setState {
            val cached = event.date in cachedDates
            copy(
                selectedDate = event.date,
                visibleMonth = YearMonth.from(event.date),
                isShowingCachedData = cached,
                syncWarningMessage = if (cached) CACHE_WARNING_MESSAGE else null,
                loadErrorMessage = null,
            )
        }
        loadSelectedDate()
    }

    private fun moveMonth(event: ReminderUiEvent.MonthMoved) {
        val month = currentState.visibleMonth.plusMonths(event.offset)
        setState { copy(visibleMonth = month) }
        loadMonth(month)
    }

    private fun openAddPopup() = setState {
        if (isShowingCachedData) {
            sendEffect(ReminderUiEffect.ShowMessage(OFFLINE_EDIT_MESSAGE))
            return@setState this
        }
        copy(draft = ReminderDraft(), isTaskPopupVisible = true)
    }

    private fun openEditPopup(event: ReminderUiEvent.EditTaskClicked) = setState {
        if (isShowingCachedData) {
            sendEffect(ReminderUiEffect.ShowMessage(OFFLINE_EDIT_MESSAGE))
            return@setState this
        }
        copy(
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
        copy(draft = ReminderDraft(), isTaskPopupVisible = false)
    }

    private fun moveTask(event: ReminderUiEvent.TaskMoved) = setState {
        val tasks = tasksByDate[selectedDate].orEmpty()
        if (event.fromIndex !in tasks.indices || event.toIndex !in tasks.indices || event.fromIndex == event.toIndex) {
            return@setState this
        }
        val reordered = tasks.toMutableList().apply {
            add(event.toIndex, removeAt(event.fromIndex))
        }
        copy(tasksByDate = tasksByDate + (selectedDate to reordered))
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
        if (state.isSubmitting) return
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
            overlaps(state, start, end, draft.editingTaskId) -> DUPLICATE_SCHEDULE_MESSAGE
            else -> null
        }
        if (draft.restrictionMode == RestrictionMode.SPECIFIC_APPS && draft.restrictedAppIds.isEmpty()) {
            sendEffect(ReminderUiEffect.ShowMessage("제한할 앱을 선택해 주세요"))
            return
        }
        if (titleError != null || timeError != null) {
            setState {
                copy(draft = draft.copy(titleError = titleError, timeError = timeError))
            }
            sendEffect(ReminderUiEffect.ShowMessage(requireNotNull(timeError ?: titleError)))
            return
        }
        val startInstant = state.selectedDate.atMinutes(requireNotNull(start))
        val endInstant = state.selectedDate.atMinutes(requireNotNull(end))
        setState { copy(isSubmitting = true) }
        viewModelScope.launch {
            val result = draft.editingTaskId?.let { id ->
                updateReminder(
                    id,
                    UpdateReminderCommand(
                        date = state.selectedDate,
                        title = draft.title.trim(),
                        startTime = startInstant,
                        endTime = endInstant,
                        restrictionMode = draft.restrictionMode.toDomain(),
                        restrictedAppIds = draft.restrictedAppIds,
                    ),
                )
            } ?: createReminder(
                CreateReminderCommand(
                    date = state.selectedDate,
                    title = draft.title.trim(),
                    startTime = startInstant,
                    endTime = endInstant,
                    restrictionMode = draft.restrictionMode.toDomain(),
                    restrictedAppIds = draft.restrictedAppIds,
                ),
            )
            result
                .onSuccess { saved -> applySavedReminder(state.selectedDate, saved) }
                .onFailure(::handleMutationFailure)
        }
    }

    private fun deleteTask() {
        if (currentState.isSubmitting) return
        val id = currentState.draft.editingTaskId ?: return
        val date = currentState.selectedDate
        setState { copy(isSubmitting = true) }
        viewModelScope.launch {
            deleteReminder(id)
                .onSuccess {
                    setState {
                        copy(
                            tasksByDate = tasksByDate + (date to tasksByDate[date].orEmpty().filterNot { it.id == id }),
                            isSubmitting = false,
                            draft = ReminderDraft(),
                            isTaskPopupVisible = false,
                        )
                    }
                    sendEffect(ReminderUiEffect.ShowMessage(REMINDER_DELETED_MESSAGE))
                }
                .onFailure(::handleMutationFailure)
        }
    }

    private fun loadSelectedDate() {
        val date = currentState.selectedDate
        selectedDateLoadJob?.cancel()
        selectedDateLoadJob = viewModelScope.launch {
            setState { copy(isLoading = true, loadErrorMessage = null) }
            getReminders(date)
                .onSuccess { result ->
                    val isCache = result.source == ReminderDataSource.CACHE
                    setState {
                        copy(
                            tasksByDate = tasksByDate + (date to result.reminders.map(Reminder::toUiModel)),
                            isLoading = false,
                            loadErrorMessage = null,
                            isShowingCachedData = isCache,
                            cachedDates = if (isCache) cachedDates + date else cachedDates - date,
                            syncWarningMessage = if (isCache) CACHE_WARNING_MESSAGE else null,
                        )
                    }
                }
                .onFailure { throwable ->
                    handleError(throwable) { error ->
                        setState {
                            copy(
                                isLoading = false,
                                loadErrorMessage = error.message,
                                isShowingCachedData = false,
                                syncWarningMessage = null,
                            )
                        }
                    }
                }
        }
    }

    private fun loadMonth(month: YearMonth) {
        monthLoadJob?.cancel()
        monthLoadJob = viewModelScope.launch {
            val selectedDate = currentState.selectedDate
            if (YearMonth.from(selectedDate) == month) {
                setState { copy(isLoading = true, loadErrorMessage = null) }
            }
            val semaphore = Semaphore(MONTH_REQUEST_CONCURRENCY)
            coroutineScope {
                (1..month.lengthOfMonth()).map { day ->
                    async {
                        val date = month.atDay(day)
                        val result = semaphore.withPermit { getReminders(date) }
                        if (currentState.visibleMonth != month) return@async
                        result.onSuccess { listResult ->
                            val isCache = listResult.source == ReminderDataSource.CACHE
                            setState {
                                copy(
                                    tasksByDate = tasksByDate +
                                        (date to listResult.reminders.map(Reminder::toUiModel)),
                                    cachedDates = if (isCache) cachedDates + date else cachedDates - date,
                                    isLoading = if (date == selectedDate) false else isLoading,
                                    loadErrorMessage = if (date == selectedDate) null else loadErrorMessage,
                                    isShowingCachedData = if (date == selectedDate) isCache else isShowingCachedData,
                                    syncWarningMessage = if (date == selectedDate && isCache) {
                                        CACHE_WARNING_MESSAGE
                                    } else if (date == selectedDate) {
                                        null
                                    } else {
                                        syncWarningMessage
                                    },
                                )
                            }
                        }.onFailure { throwable ->
                            if (date == selectedDate) {
                                handleError(throwable) { error ->
                                    setState {
                                        copy(
                                            isLoading = false,
                                            loadErrorMessage = error.message,
                                            isShowingCachedData = false,
                                            syncWarningMessage = null,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }.awaitAll()
            }
        }
    }

    private fun loadMonitoredApps() {
        viewModelScope.launch {
            setState { copy(isMonitoredAppsLoading = true) }
            getMonitoredApps()
                .onSuccess { apps ->
                    setState {
                        copy(
                            monitoredApps = apps.map { ReminderAppUiModel(it.id, it.packageName, it.appName) },
                            isMonitoredAppsLoading = false,
                        )
                    }
                }
                .onFailure {
                    setState { copy(monitoredApps = emptyList(), isMonitoredAppsLoading = false) }
                }
        }
    }

    private fun applySavedReminder(date: LocalDate, reminder: Reminder) = setState {
        val currentTasks = tasksByDate[date].orEmpty()
        val savedTask = reminder.toUiModel()
        val existingIndex = currentTasks.indexOfFirst { it.id == savedTask.id }
        val updated = if (existingIndex >= 0) {
            currentTasks.toMutableList().apply { this[existingIndex] = savedTask }
        } else {
            currentTasks + savedTask
        }.sortedWith(compareBy(ReminderTaskUiModel::startMinutes, ReminderTaskUiModel::endMinutes))
        sendEffect(
            ReminderUiEffect.ShowMessage(
                message = REMINDER_SAVED_MESSAGE,
                type = PhoneShimSnackbarType.Info,
            ),
        )
        copy(
            tasksByDate = tasksByDate + (date to updated),
            isSubmitting = false,
            isShowingCachedData = false,
            syncWarningMessage = null,
            draft = ReminderDraft(),
            isTaskPopupVisible = false,
        )
    }

    private fun handleMutationFailure(throwable: Throwable) {
        handleError(throwable) { error ->
            setState { copy(isSubmitting = false) }
            when (error.code) {
                ReminderErrorCodes.REMINDER_TIME_OVERLAP -> setState {
                    copy(draft = draft.copy(timeError = DUPLICATE_SCHEDULE_MESSAGE))
                }.also { sendEffect(ReminderUiEffect.ShowMessage(DUPLICATE_SCHEDULE_MESSAGE)) }
                ReminderErrorCodes.INVALID_TIME_RANGE -> setState {
                    copy(draft = draft.copy(timeError = "시간 범위를 다시 확인해 주세요"))
                }
                ReminderErrorCodes.REMINDER_NOT_FOUND -> {
                    setState { copy(isTaskPopupVisible = false, draft = ReminderDraft()) }
                    sendEffect(ReminderUiEffect.ShowMessage(error.message))
                    loadSelectedDate()
                }
                else -> sendEffect(ReminderUiEffect.ShowMessage(error.toReminderMessage()))
            }
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

private fun Reminder.toUiModel(): ReminderTaskUiModel = ReminderTaskUiModel(
    id = id,
    date = date,
    title = title,
    startMinutes = startTime.toKoreaMinutes(),
    endMinutes = endTime.toKoreaMinutes(),
    restrictionMode = restrictionMode.toUi(),
    restrictedAppIds = restrictedAppIds,
)

private fun LocalDate.atMinutes(minutes: Int): Instant =
    atStartOfDay(KOREA_ZONE_ID).plusMinutes(minutes.toLong()).toInstant()

private fun Instant.toKoreaMinutes(): Int = atZone(KOREA_ZONE_ID).let { it.hour * 60 + it.minute }

private fun RestrictionMode.toDomain(): ReminderRestrictionMode = when (this) {
    RestrictionMode.NONE -> ReminderRestrictionMode.NONE
    RestrictionMode.FULL_PHONE -> ReminderRestrictionMode.FULL_PHONE
    RestrictionMode.SPECIFIC_APPS -> ReminderRestrictionMode.SPECIFIC_APP
}

private fun ReminderRestrictionMode.toUi(): RestrictionMode = when (this) {
    ReminderRestrictionMode.NONE -> RestrictionMode.NONE
    ReminderRestrictionMode.FULL_PHONE -> RestrictionMode.FULL_PHONE
    ReminderRestrictionMode.SPECIFIC_APP -> RestrictionMode.SPECIFIC_APPS
}

private fun UiError.toReminderMessage(): String = when (code) {
    ReminderErrorCodes.INVALID_RESTRICT_MODE -> "제한 설정을 다시 확인해 주세요"
    ReminderErrorCodes.INVALID_RESTRICTED_APP_IDS -> "선택한 제한 앱을 다시 확인해 주세요"
    else -> message
}

private val KOREA_ZONE_ID: ZoneId = ZoneId.of("Asia/Seoul")
private const val CACHE_WARNING_MESSAGE = "네트워크에 연결할 수 없어 저장된 일정을 표시하고 있어요."
private const val OFFLINE_EDIT_MESSAGE = "네트워크 연결 후 일정을 변경해 주세요."
private const val REMINDER_SAVED_MESSAGE = "저장되었습니다."
private const val REMINDER_DELETED_MESSAGE = "삭제되었습니다."
private const val MONTH_REQUEST_CONCURRENCY = 4

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
