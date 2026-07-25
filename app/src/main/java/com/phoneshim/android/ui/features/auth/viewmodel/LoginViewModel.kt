package com.phoneshim.android.ui.features.auth.viewmodel

import androidx.lifecycle.viewModelScope
import com.phoneshim.android.ui.common.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor() :
    BaseViewModel<LoginUiState, LoginUiEvent, LoginUiEffect>(LoginUiState()) {

    override fun handleEvent(event: LoginUiEvent) {
        when (event) {
            LoginUiEvent.GoogleLoginClicked -> startMockLogin(LoginProvider.GOOGLE)
            LoginUiEvent.KakaoLoginClicked -> startMockLogin(LoginProvider.KAKAO)
            LoginUiEvent.ErrorDismissed -> setState { copy(errorMessage = null) }
        }
    }

    private fun startMockLogin(provider: LoginProvider) {
        if (currentState.isLoading) return

        setState {
            copy(
                selectedProvider = provider,
                isLoading = true,
                errorMessage = null,
            )
        }

        viewModelScope.launch {
            // TODO: 화면 전용 mock 로직을 실제 소셜 로그인 usecase로 교체한다.
            delay(MOCK_LOGIN_DELAY_MILLIS)
            setState { copy(isLoading = false) }
            sendEffect(LoginUiEffect.NavigateToGoalSetup)
        }
    }

    private companion object {
        const val MOCK_LOGIN_DELAY_MILLIS = 500L
    }
}
