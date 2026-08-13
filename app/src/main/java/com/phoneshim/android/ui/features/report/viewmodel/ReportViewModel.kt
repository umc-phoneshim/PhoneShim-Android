package com.phoneshim.android.ui.features.report.viewmodel

import androidx.lifecycle.viewModelScope
import com.phoneshim.android.data.api.common.ApiErrorCodes
import com.phoneshim.android.domain.model.AlertSettingPolicy
import com.phoneshim.android.domain.repository.ReportPreferencesRepository
import com.phoneshim.android.domain.usecase.GetAchievedDatesUseCase
import com.phoneshim.android.domain.usecase.GetAlertSettingUseCase
import com.phoneshim.android.domain.usecase.GetDailyReportUseCase
import com.phoneshim.android.domain.usecase.GetReportSummaryUseCase
import com.phoneshim.android.domain.usecase.GetRestSuggestionUseCase
import com.phoneshim.android.domain.usecase.GetUsageSessionsUseCase
import com.phoneshim.android.domain.usecase.UpdateAlertSettingUseCase
import com.phoneshim.android.ui.common.base.BaseViewModel
import com.phoneshim.android.ui.common.base.toSnackbarMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val getDailyReportUseCase: GetDailyReportUseCase,
    private val getUsageSessionsUseCase: GetUsageSessionsUseCase,
    private val getReportSummaryUseCase: GetReportSummaryUseCase,
    private val getRestSuggestionUseCase: GetRestSuggestionUseCase,
    private val getAchievedDatesUseCase: GetAchievedDatesUseCase,
    private val reportPreferencesRepository: ReportPreferencesRepository,
    private val getAlertSettingUseCase: GetAlertSettingUseCase,
    private val updateAlertSettingUseCase: UpdateAlertSettingUseCase,
) : BaseViewModel<ReportUiState, ReportUiEvent, ReportUiEffect>(ReportUiState()) {

    /** 달력 달성일 조회. 월을 빠르게 넘길 때 이전 요청을 취소하려고 들고 있습니다. */
    private var achievedDatesJob: Job? = null

    override fun handleEvent(event: ReportUiEvent) {
        when (event) {
            is ReportUiEvent.ScreenEntered -> enterScreen(event)
            ReportUiEvent.PreviousDateClicked -> moveDate(-1)
            ReportUiEvent.NextDateClicked -> moveDate(1)
            ReportUiEvent.DatePickerOpened -> openDatePicker()
            ReportUiEvent.DatePickerDismissed -> setState { copy(isDatePickerVisible = false) }
            ReportUiEvent.CalendarTooltipDismissed -> dismissCalendarTooltip()
            is ReportUiEvent.DatePicked -> pickDate(event)
            is ReportUiEvent.PickerMonthMoved -> movePickerMonth(event)
            is ReportUiEvent.TabSelected -> selectTab(event)
            is ReportUiEvent.PeriodSelected -> selectPeriod(event)
            is ReportUiEvent.TimetableEntryClicked -> openUsageReasonInput(event)
            ReportUiEvent.RestSuggestionClicked -> sendEffect(ReportUiEffect.NavigateToRestSuggestion)
            ReportUiEvent.RestSuggestionRequested -> loadRestSuggestion()

            ReportUiEvent.AlarmSettingsClicked -> openAlarmDialog()
            ReportUiEvent.AlarmDialogDismissed -> setState {
                copy(isAlarmDialogVisible = false, alarmInputError = null)
            }
            is ReportUiEvent.AlarmHourChanged -> setState {
                copy(alarmHourDraft = event.value, alarmInputError = null)
            }
            is ReportUiEvent.AlarmMinuteChanged -> setState {
                copy(alarmMinuteDraft = event.value, alarmInputError = null)
            }
            ReportUiEvent.AlarmConfirmed -> confirmAlarm()

            ReportUiEvent.Retry -> load()
        }
    }

    private fun enterScreen(event: ReportUiEvent.ScreenEntered) {
        setState { copy(selectedTab = event.tab) }
        loadPreferences()
        load()
    }

    // ---------------------------------------------------------------- UI 로컬 설정 + 서버 AlertSetting

    /**
     * 달력 툴팁은 기기에 저장하고, 알림 시각은 계정별 서버 설정을 원본으로 사용합니다.
     * 신규 사용자는 GET 시 서버가 22:00 기본 설정을 자동 생성합니다.
     */
    private fun loadPreferences() {
        viewModelScope.launch {
            val isTooltipDismissed = reportPreferencesRepository.isCalendarTooltipDismissed()
            setState {
                copy(
                    isCalendarTooltipVisible = !isTooltipDismissed,
                    isAlertSettingLoading = true,
                )
            }
            getAlertSettingUseCase()
                .onSuccess { setting ->
                    setState {
                        copy(
                            alertSetting = setting,
                            alarmHourDraft = setting.hourLabel,
                            alarmMinuteDraft = setting.minuteLabel,
                            isAlertSettingLoading = false,
                        )
                    }
                }
                .onFailure { error ->
                    setState { copy(isAlertSettingLoading = false) }
                    handleAlertSettingFailure(error, "알림 설정을 불러오지 못했습니다.")
                }
        }
    }

    private fun dismissCalendarTooltip() {
        setState { copy(isCalendarTooltipVisible = false) }
        viewModelScope.launch { reportPreferencesRepository.dismissCalendarTooltip() }
    }

    /** 팝업은 저장된 값에서 시작해야 사용자가 지금 설정을 확인하고 고칠 수 있습니다. */
    private fun openAlarmDialog() = setState {
        copy(
            isAlarmDialogVisible = true,
            alarmHourDraft = alertSetting?.hourLabel ?: DEFAULT_ALERT_HOUR,
            alarmMinuteDraft = alertSetting?.minuteLabel ?: DEFAULT_ALERT_MINUTE,
            alarmInputError = null,
        )
    }

    /**
     * 알림 시각 저장.
     *
     * 서버가 허용하는 KST 22:00~23:59 범위를 로컬에서 먼저 검증하고 PATCH 합니다.
     * 성공 응답 전체 객체를 화면 상태의 새 원본으로 사용합니다.
     */
    private fun confirmAlarm() {
        val minutes = currentState.draftAlertTimeMinutes
        if (minutes == null || !AlertSettingPolicy.isValid(minutes)) {
            setState { copy(alarmInputError = INVALID_ALERT_TIME_MESSAGE) }
            return
        }
        if (currentState.isAlertSettingSaving) return
        setState { copy(isAlertSettingSaving = true, alarmInputError = null) }
        viewModelScope.launch {
            updateAlertSettingUseCase(minutes)
                .onSuccess { setting ->
                    setState {
                        copy(
                            alertSetting = setting,
                            alarmHourDraft = setting.hourLabel,
                            alarmMinuteDraft = setting.minuteLabel,
                            isAlarmDialogVisible = false,
                            isAlertSettingSaving = false,
                        )
                    }
                    sendEffect(
                        ReportUiEffect.ShowMessage(
                            "${setting.hourLabel}:${setting.minuteLabel}에 리포트 알림을 보내드릴게요.",
                        ),
                    )
                }
                .onFailure { error ->
                    setState { copy(isAlertSettingSaving = false) }
                    handleAlertSettingFailure(error, "알림 시간을 저장하지 못했습니다.")
                }
        }
    }

    private fun handleAlertSettingFailure(throwable: Throwable, fallback: String) {
        handleError(throwable) { error ->
            sendEffect(
                ReportUiEffect.ShowMessage(error.message.takeIf { it.isNotBlank() } ?: fallback),
            )
        }
    }

    // ---------------------------------------------------------------- 날짜 선택 달력

    private fun openDatePicker() {
        setState { copy(isDatePickerVisible = true, pickerMonth = YearMonth.from(date)) }
        loadAchievedDates()
    }

    private fun movePickerMonth(event: ReportUiEvent.PickerMonthMoved) {
        setState { copy(pickerMonth = pickerMonth.plusMonths(event.offset), achievedDates = emptySet()) }
        loadAchievedDates()
    }

    /**
     * 하루 목표를 모두 지킨 날짜. 달력에 표시로 붙습니다.
     * 실패해도 달력 자체는 써야 하니 표시만 비우고 오류는 알리지 않습니다.
     *
     * 월을 빠르게 넘기면 이전 요청이 늦게 끝나 지금 보고 있는 달을 덮어쓸 수 있습니다.
     * 직전 조회를 취소하고, 응답이 도착했을 때 화면의 달이 그대로인지 한 번 더 확인합니다.
     */
    private fun loadAchievedDates() {
        val month = currentState.pickerMonthParam
        achievedDatesJob?.cancel()
        achievedDatesJob = viewModelScope.launch {
            getAchievedDatesUseCase(month)
                .onSuccess { dates ->
                    if (month != currentState.pickerMonthParam) return@onSuccess
                    val parsed = dates.mapNotNull { raw ->
                        runCatching { LocalDate.parse(raw) }.getOrNull()
                    }.toSet()
                    setState { copy(achievedDates = parsed) }
                }
                .onFailure {
                    if (month != currentState.pickerMonthParam) return@onFailure
                    setState { copy(achievedDates = emptySet()) }
                }
        }
    }

    private fun moveDate(offsetDays: Long) {
        if (offsetDays > 0 && !currentState.canGoNextDate) return
        setState { copy(date = date.plusDays(offsetDays), insufficientDataMessage = null) }
        load()
    }

    /** 달력에서 고른 날짜로 이동합니다. 오늘 이후는 선택할 수 없습니다. */
    private fun pickDate(event: ReportUiEvent.DatePicked) {
        val state = currentState
        if (event.date.isAfter(state.today) || event.date == state.date) {
            setState { copy(isDatePickerVisible = false) }
            return
        }
        setState {
            copy(date = event.date, isDatePickerVisible = false, insufficientDataMessage = null)
        }
        load()
    }

    private fun selectTab(event: ReportUiEvent.TabSelected) {
        if (event.tab == currentState.selectedTab) return
        setState { copy(selectedTab = event.tab) }
        sendEffect(ReportUiEffect.NavigateToTab(event.tab))
    }

    private fun selectPeriod(event: ReportUiEvent.PeriodSelected) {
        if (event.period == currentState.period) return
        setState { copy(period = event.period, insufficientDataMessage = null) }
        load()
    }

    /**
     * 타임테이블 막대를 누르면 그 구간의 사용 이유 입력으로 이동합니다.
     * 서버가 monitoredAppId 와 정확한 시간 구간을 요구해서 세션 정보를 함께 넘깁니다.
     */
    private fun openUsageReasonInput(event: ReportUiEvent.TimetableEntryClicked) {
        val session = currentState.sessions.firstOrNull { it.id == event.entryId } ?: return
        sendEffect(
            ReportUiEffect.NavigateToUsageReasonInput(
                UsageReasonTarget(
                    monitoredAppId = session.monitoredAppId,
                    date = session.date,
                    timeRangeStart = session.startTime.toString(),
                    timeRangeEnd = session.endTime.toString(),
                ),
            ),
        )
    }

    /**
     * 화면에 필요한 세 가지를 함께 불러옵니다.
     * 사용량(버블/사용 어플), 사용 구간(타임테이블), 사유 요약(어플 사용 요약 막대)입니다.
     */
    private fun load() {
        if (currentState.isLoading) return
        val state = currentState
        setState { copy(isLoading = true) }

        viewModelScope.launch {
            val reportDeferred = async {
                getDailyReportUseCase(date = state.requestDate, isToday = state.isToday)
            }
            val sessionsDeferred = async { getUsageSessionsUseCase(state.requestDate) }
            val summaryDeferred = async {
                getReportSummaryUseCase(
                    range = state.period.toReportRange(),
                    date = state.requestDate,
                )
            }
            // 쉼이의 제안은 두 화면 상단에 항상 붙어 있어 함께 불러옵니다.
            val suggestionDeferred = async { getRestSuggestionUseCase(state.requestDate) }

            val report = reportDeferred.await()
            val sessions = sessionsDeferred.await()
            val summary = summaryDeferred.await()
            val suggestion = suggestionDeferred.await()

            setState {
                copy(
                    report = report.getOrNull() ?: this.report,
                    sessions = sessions.getOrNull() ?: emptyList(),
                    summary = summary.getOrNull(),
                    restSuggestion = suggestion.getOrNull() ?: this.restSuggestion,
                    isLoading = false,
                )
            }

            listOf(report, sessions, summary, suggestion)
                .firstNotNullOfOrNull { it.exceptionOrNull() }
                ?.let { handleFailure(it, "리포트를 불러오지 못했습니다.") }
        }
    }

    /**
     * 쉼이의 제안. AI가 아니라 서버가 목표 대비 사용량을 보고 고른 문구를 그대로 받습니다.
     */
    private fun loadRestSuggestion() {
        if (currentState.isLoading) return
        val date = currentState.requestDate
        setState { copy(isLoading = true, insufficientDataMessage = null) }
        viewModelScope.launch {
            getRestSuggestionUseCase(date)
                .onSuccess { suggestion ->
                    setState { copy(restSuggestion = suggestion, isLoading = false) }
                }
                .onFailure { throwable ->
                    handleFailure(throwable, "쉼이의 제안을 불러오지 못했습니다.")
                }
        }
    }

    /**
     * INSUFFICIENT_* 는 오류가 아니라 "집계할 데이터가 아직 부족한 상태"입니다.
     * 오류 화면 대신 안내 문구로 표시하도록 상태에만 담고 스낵바는 띄우지 않습니다.
     */
    private fun handleFailure(throwable: Throwable, fallback: String) {
        handleError(throwable) { error ->
            if (error.code?.startsWith(ApiErrorCodes.INSUFFICIENT_PREFIX) == true) {
                setState {
                    copy(
                        isLoading = false,
                        insufficientDataMessage = error.message.takeIf { it.isNotBlank() }
                            ?: DEFAULT_INSUFFICIENT_MESSAGE,
                    )
                }
            } else {
                setState { copy(isLoading = false) }
                sendEffect(
                    ReportUiEffect.ShowMessage(
                        message = if (error.isRetryable) error.toSnackbarMessage() else fallback,
                    ),
                )
            }
        }
    }

    private companion object {
        const val DEFAULT_INSUFFICIENT_MESSAGE = "아직 분석할 기록이 충분하지 않아요."
        const val DEFAULT_ALERT_HOUR = "22"
        const val DEFAULT_ALERT_MINUTE = "00"
        const val INVALID_ALERT_TIME_MESSAGE = "알림 시간은 22:00~23:59 사이로 설정해 주세요."
    }
}
