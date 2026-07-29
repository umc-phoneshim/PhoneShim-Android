package com.phoneshim.android.ui.features.report.viewmodel

import androidx.lifecycle.viewModelScope
import com.phoneshim.android.data.api.ApiException
import com.phoneshim.android.domain.usecase.GetDailyReportUseCase
import com.phoneshim.android.domain.usecase.GetReportSummaryUseCase
import com.phoneshim.android.domain.usecase.GetRestSuggestionUseCase
import com.phoneshim.android.ui.common.base.BaseViewModel
import com.phoneshim.android.ui.features.report.component.ReportPeriod
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val getDailyReportUseCase: GetDailyReportUseCase,
    private val getReportSummaryUseCase: GetReportSummaryUseCase,
    private val getRestSuggestionUseCase: GetRestSuggestionUseCase,
) : BaseViewModel<ReportUiState, ReportUiEvent, ReportUiEffect>(ReportUiState()) {

    override fun handleEvent(event: ReportUiEvent) {
        when (event) {
            is ReportUiEvent.ScreenEntered -> enterScreen(event)
            ReportUiEvent.PreviousDateClicked -> moveDate(-1)
            ReportUiEvent.NextDateClicked -> moveDate(1)
            is ReportUiEvent.TabSelected -> selectTab(event)
            is ReportUiEvent.PeriodSelected -> selectPeriod(event)
            is ReportUiEvent.TimetableEntryClicked ->
                sendEffect(ReportUiEffect.NavigateToUsageReasonInput(event.entryId))
            ReportUiEvent.RestSuggestionClicked -> sendEffect(ReportUiEffect.NavigateToRestSuggestion)
            ReportUiEvent.RestSuggestionRequested -> loadRestSuggestion()
            ReportUiEvent.AlarmSettingsClicked -> sendEffect(ReportUiEffect.NavigateToAlarmSettings)
            ReportUiEvent.Retry -> reload()
        }
    }

    private fun enterScreen(event: ReportUiEvent.ScreenEntered) {
        setState { copy(selectedTab = event.tab) }
        loadReport()
    }

    private fun moveDate(offsetDays: Long) {
        val state = currentState
        if (offsetDays > 0 && !state.canGoNextDate) return
        setState { copy(date = date.plusDays(offsetDays), insufficientDataMessage = null) }
        loadReport()
    }

    private fun selectTab(event: ReportUiEvent.TabSelected) {
        if (event.tab == currentState.selectedTab) return
        setState { copy(selectedTab = event.tab) }
        sendEffect(ReportUiEffect.NavigateToTab(event.tab))
    }

    private fun selectPeriod(event: ReportUiEvent.PeriodSelected) {
        setState { copy(period = event.period, insufficientDataMessage = null) }
        val range = event.period.toReportRange()
        if (range == null) {
            loadReport()
            return
        }
        setState { copy(isLoading = true) }
        viewModelScope.launch {
            getReportSummaryUseCase(range = range, date = currentState.requestDate)
                .onSuccess { summary -> setState { copy(summary = summary, isLoading = false) } }
                .onFailure { throwable -> handleFailure(throwable, "요약을 불러오지 못했습니다.") }
        }
    }

    private fun reload() {
        setState { copy(insufficientDataMessage = null) }
        if (currentState.period == ReportPeriod.DAY) {
            loadReport()
        } else {
            handleEvent(ReportUiEvent.PeriodSelected(currentState.period))
        }
    }

    /** 선택 날짜의 앱별 사용량. 폴 담당 UsageLog API 결과를 그대로 리포트 입력으로 씁니다. */
    private fun loadReport() {
        if (currentState.isLoading) return
        val state = currentState
        setState { copy(isLoading = true) }
        viewModelScope.launch {
            getDailyReportUseCase(date = state.requestDate, isToday = state.isToday)
                .onSuccess { report ->
                    setState { copy(report = report, isLoading = false, insufficientDataMessage = null) }
                }
                .onFailure { throwable -> handleFailure(throwable, "리포트를 불러오지 못했습니다.") }
        }
    }

    /** 쉼이의 제안. 백엔드 분석 결과 문구를 받아 그대로 표시합니다. */
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
     * 422 INSUFFICIENT_*_DATA 는 오류가 아니라 "집계할 데이터가 아직 부족한 상태"입니다.
     * 오류 화면 대신 안내 문구로 표시하도록 상태에만 담고 스낵바는 띄우지 않습니다.
     */
    private fun handleFailure(throwable: Throwable, fallback: String) {
        val api = throwable as? ApiException
        if (api != null && api.isInsufficientData) {
            setState {
                copy(
                    isLoading = false,
                    insufficientDataMessage = api.message.ifBlank { DEFAULT_INSUFFICIENT_MESSAGE },
                )
            }
            return
        }
        setState { copy(isLoading = false) }
        val message = when {
            api == null -> fallback
            api.isUnauthorized -> "다시 로그인해 주세요."
            else -> api.message.ifBlank { fallback }
        }
        sendEffect(ReportUiEffect.ShowMessage(message))
    }

    private companion object {
        const val DEFAULT_INSUFFICIENT_MESSAGE = "아직 분석할 기록이 충분하지 않아요."
    }
}
