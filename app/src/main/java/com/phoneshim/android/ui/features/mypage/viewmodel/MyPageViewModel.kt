package com.phoneshim.android.ui.features.mypage.viewmodel

import androidx.lifecycle.ViewModel
import com.phoneshim.android.domain.model.User
import com.phoneshim.android.domain.usecase.WithdrawUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

data class MyPageUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
)

@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val withdrawUseCase: WithdrawUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyPageUiState())
    val uiState: StateFlow<MyPageUiState> = _uiState

    fun withdraw() {
        // TODO: withdrawUseCase 호출 및 uiState 갱신
    }
}
