package com.phoneshim.android.ui.features.main.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phoneshim.android.domain.model.AppUsage
import com.phoneshim.android.domain.model.Goal
import com.phoneshim.android.domain.usecase.GetGoalUseCase
import com.phoneshim.android.domain.usecase.GetMainDashboardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainUiState(
    val isGoalSet: Boolean = false,
    val goal: Goal? = null,
    val todayUsage: List<AppUsage> = emptyList(),
    val isLoading: Boolean = false,
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getMainDashboardUseCase: GetMainDashboardUseCase,
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
            // 목표 설정 여부: 오프라인에서도 로컬 캐시로 읽힘(GoalRepository.getGoal 로컬 폴백).
            val goal = getGoalUseCase().getOrNull()
            val usage = getMainDashboardUseCase().getOrDefault(emptyList())
            _uiState.value = _uiState.value.copy(
                isGoalSet = goal != null,
                goal = goal,
                todayUsage = usage,
                isLoading = false,
            )
        }
    }
}
