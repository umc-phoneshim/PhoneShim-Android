package com.phoneshim.android.ui.features.report.viewmodel

import androidx.lifecycle.ViewModel
import com.phoneshim.android.domain.model.DailyReport
import com.phoneshim.android.domain.usecase.GetDailyReportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

data class ReportUiState(
    val report: DailyReport? = null,
    val isLoading: Boolean = false,
)

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val getDailyReportUseCase: GetDailyReportUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState

    fun loadReport(date: String) {
        // TODO: getDailyReportUseCase 호출 및 uiState 갱신
    }
}
