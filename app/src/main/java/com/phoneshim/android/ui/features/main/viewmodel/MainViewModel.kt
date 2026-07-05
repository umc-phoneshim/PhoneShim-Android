package com.phoneshim.android.ui.features.main.viewmodel

import androidx.lifecycle.ViewModel
import com.phoneshim.android.domain.model.AppUsage
import com.phoneshim.android.domain.usecase.GetMainDashboardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

data class MainUiState(
    val isGoalSet: Boolean = false,
    val todayUsage: List<AppUsage> = emptyList(),
    val isLoading: Boolean = false,
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getMainDashboardUseCase: GetMainDashboardUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState

    fun loadDashboard() {
        // TODO: getMainDashboardUseCase 호출 및 uiState 갱신
    }
}
