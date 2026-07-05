package com.phoneshim.android.ui.features.auth.viewmodel

import androidx.lifecycle.ViewModel
import com.phoneshim.android.domain.usecase.SignUpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

data class SignUpUiState(
    val email: String = "",
    val password: String = "",
    val nickname: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val signUpUseCase: SignUpUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState

    fun signUp() {
        // TODO: signUpUseCase 호출 및 uiState 갱신
    }
}
