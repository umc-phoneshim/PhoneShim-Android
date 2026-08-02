package com.phoneshim.android.ui.features.main.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phoneshim.android.domain.model.DashboardSummary
import com.phoneshim.android.domain.model.UsageStatus
import com.phoneshim.android.domain.usecase.GetDashboardSummaryUseCase
import com.phoneshim.android.domain.usecase.GetGoalUseCase
import com.phoneshim.android.domain.usecase.GetUsageStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class MainUiState(
    val isGoalSet: Boolean = false,
    val usageStatus: List<UsageStatus> = emptyList(),
    val dashboardSummary: DashboardSummary? = null,
    val isLoading: Boolean = false,
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getUsageStatusUseCase: GetUsageStatusUseCase,
    private val getDashboardSummaryUseCase: GetDashboardSummaryUseCase,
    private val getGoalUseCase: GetGoalUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            coroutineScope {
                // 목표 설정 여부: 오프라인에서도 로컬 캐시로 읽힘(GoalRepository.getGoal 로컬 폴백).
                val isGoalSetDeferred = async { getGoalUseCase().getOrNull() != null }
                val usageStatusDeferred = async { getUsageStatusUseCase().getOrDefault(emptyList()) }
                val dashboardSummaryDeferred = async { getDashboardSummaryUseCase().getOrNull() }

                _uiState.value = _uiState.value.copy(
                    isGoalSet = isGoalSetDeferred.await(),
                    usageStatus = usageStatusDeferred.await(),
                    dashboardSummary = dashboardSummaryDeferred.await(),
                    isLoading = false,
                )
            }
        }
    }
}
