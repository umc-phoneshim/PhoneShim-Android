package com.phoneshim.android.ui.features.report.viewmodel

import androidx.lifecycle.viewModelScope
import com.phoneshim.android.data.api.common.ApiErrorCodes
import com.phoneshim.android.domain.usecase.GetDailyReportUseCase
import com.phoneshim.android.domain.usecase.GetReportSummaryUseCase
import com.phoneshim.android.domain.usecase.GetUsageSessionsUseCase
import com.phoneshim.android.ui.common.base.BaseViewModel
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
            is ReportUiEvent.DatePicked -> pickDate(event)
            is ReportUiEvent.PickerMonthMoved -> setState {
                copy(pickerMonth = pickerMonth.plusMonths(event.offset))
            }
            is ReportUiEvent.TabSelected -> selectTab(event)
            is ReportUiEvent.PeriodSelected -> selectPeriod(event)
            is ReportUiEvent.TimetableEntryClicked -> openUsageReasonInput(event)
            ReportUiEvent.RestSuggestionClicked -> sendEffect(ReportUiEffect.NavigateToRestSuggestion)
            ReportUiEvent.RestSuggestionRequested -> showRestSuggestionUnavailable()
            ReportUiEvent.AlarmSettingsClicked -> sendEffect(ReportUiEffect.NavigateToAlarmSettings)
            ReportUiEvent.Retry -> load()
        }
    }

    private fun enterScreen(event: ReportUiEvent.ScreenEntered) {
        setState { copy(selectedTab = event.tab) }
        load()
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

            val report = reportDeferred.await()
            val sessions = sessionsDeferred.await()
            val summary = summaryDeferred.await()

            setState {
                copy(
                    report = report.getOrNull() ?: this.report,
                    sessions = sessions.getOrNull() ?: emptyList(),
                    summary = summary.getOrNull(),
                    isLoading = false,
                )
            }

            listOf(report, sessions, summary)
                .firstNotNullOfOrNull { it.exceptionOrNull() }
                ?.let { handleFailure(it, "리포트를 불러오지 못했습니다.") }
        }
    }

    /** 쉼이의 제안은 백엔드에 AI 도메인이 아직 없습니다. */
    private fun showRestSuggestionUnavailable() {
        // TODO: 서버에 daily-feedback 류 엔드포인트가 생기면 실제 조회로 교체하세요.
        setState { copy(isLoading = false, insufficientDataMessage = "쉼이의 제안은 준비 중이에요.") }
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
                    ReportUiEffect.ShowMessage(error.message.takeIf { it.isNotBlank() } ?: fallback),
                )
            }
        }
    }

    private companion object {
        const val DEFAULT_INSUFFICIENT_MESSAGE = "아직 분석할 기록이 충분하지 않아요."
    }
}
