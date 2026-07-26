package com.phoneshim.android.ui.features.report.viewmodel

import com.phoneshim.android.domain.usecase.GetDailyReportUseCase
import com.phoneshim.android.ui.common.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val getDailyReportUseCase: GetDailyReportUseCase,
) : BaseViewModel<ReportUiState, ReportUiEvent, ReportUiEffect>(ReportUiState()) {

    override fun handleEvent(event: ReportUiEvent) {
        when (event) {
            is ReportUiEvent.ScreenEntered -> enterScreen(event)
            ReportUiEvent.PreviousDateClicked -> moveDate(-1)
            ReportUiEvent.NextDateClicked -> moveDate(1)
            is ReportUiEvent.TabSelected -> selectTab(event)
            is ReportUiEvent.PeriodSelected -> setState { copy(period = event.period) }
            is ReportUiEvent.TimetableEntryClicked ->
                sendEffect(ReportUiEffect.NavigateToUsageReasonInput(event.entryId))
            ReportUiEvent.EditViewClicked -> sendEffect(ReportUiEffect.NavigateToAiSuggestion)
            ReportUiEvent.AlarmSettingsClicked -> sendEffect(ReportUiEffect.NavigateToAlarmSettings)
        }
    }

    private fun enterScreen(event: ReportUiEvent.ScreenEntered) {
        setState { copy(selectedTab = event.tab) }
        loadReport(currentState.requestDate)
    }

    private fun moveDate(offsetDays: Long) {
        setState { copy(date = date.plusDays(offsetDays)) }
        loadReport(currentState.requestDate)
    }

    private fun selectTab(event: ReportUiEvent.TabSelected) {
        if (event.tab == currentState.selectedTab) return
        setState { copy(selectedTab = event.tab) }
        sendEffect(ReportUiEffect.NavigateToTab(event.tab))
    }

    private fun loadReport(date: String) {
        // TODO: viewModelScope 에서 getDailyReportUseCase(date) 를 호출하고
        //  성공 시 report 를 담아 appBubbles / categoryRows / hourUsages 로 매핑,
        //  실패 시 ShowMessage 이펙트를 발행하도록 교체하세요. (현재는 mock 상태 유지)
    }
}
