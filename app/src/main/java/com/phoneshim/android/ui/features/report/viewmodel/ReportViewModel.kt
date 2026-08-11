package com.phoneshim.android.ui.features.report.viewmodel

import androidx.lifecycle.viewModelScope
import com.phoneshim.android.data.api.ApiException
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
            is ReportUiEvent.TimetableEntryClicked ->
                sendEffect(ReportUiEffect.NavigateToUsageReasonInput(event.entryId))
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

    private fun pickDate(event: ReportUiEvent.DatePicked) {
        val state = currentState
        if (event.date.isAfter(state.today) || event.date == state.date) {
            setState { copy(isDatePickerVisible = false) }
            return
        }
        setState { copy(date = event.date, isDatePickerVisible = false, insufficientDataMessage = null) }
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

            val failure = listOf(report, sessions, summary).firstNotNullOfOrNull { it.exceptionOrNull() }

            setState {
                copy(
                    report = report.getOrNull() ?: this.report,
                    sessions = sessions.getOrNull() ?: emptyList(),
                    summary = summary.getOrNull(),
                    isLoading = false,
                    insufficientDataMessage = failure?.toInsufficientMessage(),
                )
            }

            // 데이터 부족은 화면 안내로 처리하고, 그 외 실패만 스낵바로 알립니다.
            failure?.takeIf { it.toInsufficientMessage() == null }?.let { throwable ->
                sendEffect(ReportUiEffect.ShowMessage(throwable.toUserMessage()))
            }
        }
    }

    /** 쉼이의 제안은 백엔드에 AI 도메인이 아직 없습니다. */
    private fun showRestSuggestionUnavailable() {
        // TODO: POST /api/ai/daily-feedback 같은 엔드포인트가 생기면 실제 조회로 교체하세요.
        setState { copy(insufficientDataMessage = "쉼이의 제안은 준비 중이에요.") }
    }
}

/** 422 INSUFFICIENT_*_DATA 는 오류가 아니라 "집계할 기록이 부족한 상태"입니다. */
private fun Throwable.toInsufficientMessage(): String? {
    val api = this as? ApiException ?: return null
    if (!api.isInsufficientData) return null
    return api.message.ifBlank { "아직 분석할 기록이 충분하지 않아요." }
}

private fun Throwable.toUserMessage(): String {
    val api = this as? ApiException ?: return "리포트를 불러오지 못했습니다."
    return when {
        api.isUnauthorized -> "다시 로그인해 주세요."
        else -> api.message.ifBlank { "리포트를 불러오지 못했습니다." }
    }
}
