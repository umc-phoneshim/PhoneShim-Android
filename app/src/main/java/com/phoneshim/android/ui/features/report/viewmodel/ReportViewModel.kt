package com.phoneshim.android.ui.features.report.viewmodel

import androidx.lifecycle.viewModelScope
import com.phoneshim.android.data.api.common.ApiErrorCodes
import com.phoneshim.android.domain.model.DailyReportAlarm
import com.phoneshim.android.domain.repository.ReportPreferencesRepository
import com.phoneshim.android.domain.usecase.GetDailyReportUseCase
import com.phoneshim.android.domain.usecase.GetReportSummaryUseCase
import com.phoneshim.android.domain.usecase.GetRestSuggestionUseCase
import com.phoneshim.android.domain.usecase.GetUsageSessionsUseCase
import com.phoneshim.android.ui.common.base.BaseViewModel
import com.phoneshim.android.ui.common.base.toSnackbarMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val getDailyReportUseCase: GetDailyReportUseCase,
    private val getUsageSessionsUseCase: GetUsageSessionsUseCase,
    private val getReportSummaryUseCase: GetReportSummaryUseCase,
    private val getRestSuggestionUseCase: GetRestSuggestionUseCase,
    private val reportPreferencesRepository: ReportPreferencesRepository,
) : BaseViewModel<ReportUiState, ReportUiEvent, ReportUiEffect>(ReportUiState()) {

    override fun handleEvent(event: ReportUiEvent) {
        when (event) {
            is ReportUiEvent.ScreenEntered -> enterScreen(event)
            ReportUiEvent.PreviousDateClicked -> moveDate(-1)
            ReportUiEvent.NextDateClicked -> moveDate(1)
            ReportUiEvent.DatePickerOpened -> setState {
                copy(isDatePickerVisible = true, pickerMonth = YearMonth.from(date))
            }
            ReportUiEvent.DatePickerDismissed -> setState { copy(isDatePickerVisible = false) }
            ReportUiEvent.CalendarTooltipDismissed -> dismissCalendarTooltip()
            is ReportUiEvent.DatePicked -> pickDate(event)
            is ReportUiEvent.PickerMonthMoved -> setState {
                copy(pickerMonth = pickerMonth.plusMonths(event.offset))
            }
            is ReportUiEvent.TabSelected -> selectTab(event)
            is ReportUiEvent.PeriodSelected -> selectPeriod(event)
            is ReportUiEvent.TimetableEntryClicked -> openUsageReasonInput(event)
            ReportUiEvent.RestSuggestionClicked -> sendEffect(ReportUiEffect.NavigateToRestSuggestion)
            ReportUiEvent.RestSuggestionRequested -> loadRestSuggestion()

            ReportUiEvent.AlarmSettingsClicked -> openAlarmDialog()
            ReportUiEvent.AlarmDialogDismissed -> setState { copy(isAlarmDialogVisible = false) }
            is ReportUiEvent.AlarmHourChanged -> setState { copy(alarmHourDraft = event.value) }
            is ReportUiEvent.AlarmMinuteChanged -> setState { copy(alarmMinuteDraft = event.value) }
            ReportUiEvent.AlarmConfirmed -> confirmAlarm()

            ReportUiEvent.Retry -> load()
        }
    }

    private fun enterScreen(event: ReportUiEvent.ScreenEntered) {
        setState { copy(selectedTab = event.tab) }
        loadPreferences()
        load()
    }

    // ---------------------------------------------------------------- 기기 로컬 설정

    /**
     * 서버가 아니라 기기에 저장된 값들입니다.
     * 툴팁은 한 번 닫으면 다시 안 뜨고, 알림 시각은 마지막에 설정한 값을 그대로 되살립니다.
     */
    private fun loadPreferences() {
        viewModelScope.launch {
            val isTooltipDismissed = reportPreferencesRepository.isCalendarTooltipDismissed()
            val alarm = reportPreferencesRepository.getDailyReportAlarm()
            setState {
                copy(
                    isCalendarTooltipVisible = !isTooltipDismissed,
                    dailyReportAlarm = alarm,
                    alarmHourDraft = alarm?.hourLabel ?: alarmHourDraft,
                    alarmMinuteDraft = alarm?.minuteLabel ?: alarmMinuteDraft,
                )
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
            alarmHourDraft = dailyReportAlarm?.hourLabel ?: "00",
            alarmMinuteDraft = dailyReportAlarm?.minuteLabel ?: "00",
        )
    }

    /**
     * 알림 시각 저장.
     *
     * TODO: 실제 알림 예약은 아직 없습니다. 알림 도메인이 준비되면 저장 직후
     *  AlarmManager/WorkManager 예약을 붙이고, 서버 저장 API 가 생기면 여기서 함께 호출하세요.
     */
    private fun confirmAlarm() {
        val state = currentState
        val alarm = DailyReportAlarm.of(
            hour = state.alarmHourDraft.toIntOrNull() ?: 0,
            minute = state.alarmMinuteDraft.toIntOrNull() ?: 0,
        )
        setState { copy(isAlarmDialogVisible = false, dailyReportAlarm = alarm) }
        viewModelScope.launch {
            reportPreferencesRepository.saveDailyReportAlarm(alarm)
            sendEffect(
                ReportUiEffect.ShowMessage(
                    "${alarm.hourLabel}:${alarm.minuteLabel}에 리포트 알림을 보내드릴게요.",
                ),
            )
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
    }
}
