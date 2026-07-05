package com.phoneshim.android.ui.features.setgoal.viewmodel

import androidx.lifecycle.ViewModel
import com.phoneshim.android.domain.model.AppUsage
import com.phoneshim.android.domain.usecase.SetGoalUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

data class SetGoalUiState(
    val selectedApps: List<AppUsage> = emptyList(),
    val dailyUsageLimitMinutes: Int = 0,
    val accessCountLimit: Int = 0,
    val description: String = "",
    val isLoading: Boolean = false,
)

@HiltViewModel
class SetGoalViewModel @Inject constructor(
    private val setGoalUseCase: SetGoalUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SetGoalUiState())
    val uiState: StateFlow<SetGoalUiState> = _uiState

    fun submitGoal() {
        // TODO: setGoalUseCase 호출 및 uiState 갱신
    }
}
