package com.phoneshim.android.ui.features.auth.viewmodel

import androidx.lifecycle.viewModelScope
import com.phoneshim.android.data.api.common.ApiException
import com.phoneshim.android.domain.model.AuthException
import com.phoneshim.android.domain.model.AuthFeatureAvailability
import com.phoneshim.android.domain.model.SocialProvider
import com.phoneshim.android.domain.repository.CurrentUserRepository
import com.phoneshim.android.domain.usecase.GetMyInfoUseCase
import com.phoneshim.android.domain.usecase.SocialLoginUseCase
import com.phoneshim.android.ui.common.base.BaseViewModel
import com.phoneshim.android.ui.common.base.UiEffect
import com.phoneshim.android.ui.common.base.UiEvent
import com.phoneshim.android.ui.features.auth.client.AuthClientResult
import com.phoneshim.android.ui.features.auth.client.GoogleAuthClient
import com.phoneshim.android.ui.features.auth.client.KakaoAuthClient
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val googleAuthClient: GoogleAuthClient,
    private val kakaoAuthClient: KakaoAuthClient,
    private val socialLoginUseCase: SocialLoginUseCase,
    private val getMyInfoUseCase: GetMyInfoUseCase,
    private val currentUserRepository: CurrentUserRepository,
    private val authFeatureAvailability: AuthFeatureAvailability,
) :
    BaseViewModel<LoginUiState, LoginUiEvent, LoginUiEffect>(
        LoginUiState(
            canGoogleLogin = authFeatureAvailability.canGoogleLogin,
        ),
    ) {

    override fun handleEvent(event: LoginUiEvent) {
        when (event) {
            is LoginUiEvent.LoginClicked -> startLogin(event.provider)
            LoginUiEvent.ErrorDismissed -> setState { copy(errorMessage = null) }
            LoginUiEvent.WithdrawalPendingAcknowledged,
            LoginUiEvent.WithdrawalPendingDismissed,
            -> setState {
                copy(isWithdrawalPending = false)
            }
        }
    }

    private fun startLogin(provider: SocialProvider) {
        if (currentState.isLoading) return
        if (provider == SocialProvider.GOOGLE && !currentState.canGoogleLogin) return

        setState {
            copy(
                selectedProvider = provider,
                isLoading = true,
                errorMessage = null,
            )
        }

        viewModelScope.launch {
            val authResult = when (provider) {
                SocialProvider.GOOGLE -> googleAuthClient.authenticate()
                SocialProvider.KAKAO -> kakaoAuthClient.authenticate()
            }
            when (authResult) {
                AuthClientResult.Cancelled -> finishLoading()
                is AuthClientResult.Failure -> showError(authResult.cause.toUserMessage())
                is AuthClientResult.Success -> completeServerLogin(provider, authResult)
            }
        }
    }

    private suspend fun completeServerLogin(
        provider: SocialProvider,
        authResult: AuthClientResult.Success,
    ) {
        // provider token은 서버 JWT 교환에만 전달하며 ViewModel 상태나 로컬 저장소에 보관하지 않는다.
        socialLoginUseCase(provider, authResult.providerToken)
            .onSuccess { result ->
                if (result.isNewUser) {
                    finishLoading()
                    sendEffect(LoginUiEffect.NavigateToGoalSetup)
                } else if (!authFeatureAvailability.shouldLoadRemoteProfile) {
                    // dev mock은 서버 사용자 API에 의존하지 않고 화면 흐름만 검증한다.
                    finishLoading()
                    sendEffect(LoginUiEffect.NavigateToMain)
                } else {
                    loadExistingUserProfile()
                }
            }
            .onFailure { error ->
                if (error is AuthException.WithdrawalPending) {
                    showWithdrawalPending()
                } else {
                    showError(error.toUserMessage())
                }
            }
    }

    private suspend fun loadExistingUserProfile() {
        getMyInfoUseCase()
            .onSuccess { user ->
                currentUserRepository.update(user)
                finishLoading()
                sendEffect(LoginUiEffect.NavigateToMain)
            }
            .onFailure { throwable ->
                handleError(throwable) { error ->
                    finishLoading()
                    if (error.kind != com.phoneshim.android.ui.common.base.UiError.Kind.AUTH) {
                        sendEffect(LoginUiEffect.NavigateToMain)
                    }
                }
            }
    }

    private fun showWithdrawalPending() {
        setState {
            copy(
                isLoading = false,
                selectedProvider = null,
                errorMessage = null,
                isWithdrawalPending = true,
            )
        }
    }

    private fun finishLoading() {
        setState { copy(isLoading = false, selectedProvider = null) }
    }

    private fun showError(message: String) {
        setState {
            copy(
                isLoading = false,
                selectedProvider = null,
                errorMessage = message,
            )
        }
    }

    private fun Throwable.toUserMessage(): String = when (this) {
        is AuthException.FeatureUnavailable -> message ?: "현재 사용할 수 없는 기능입니다."
        is ApiException.Network -> "인터넷 연결을 확인한 뒤 다시 시도해주세요."
        is ApiException.Serialization,
        is ApiException.InvalidResponse,
        -> "서버 응답을 처리하지 못했습니다. 잠시 후 다시 시도해주세요."
        else -> "로그인 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
    }
}

sealed interface LoginUiEvent : UiEvent {
    data class LoginClicked(val provider: SocialProvider) : LoginUiEvent
    data object ErrorDismissed : LoginUiEvent
    data object WithdrawalPendingAcknowledged : LoginUiEvent
    data object WithdrawalPendingDismissed : LoginUiEvent
}

sealed interface LoginUiEffect : UiEffect {
    data object NavigateToGoalSetup : LoginUiEffect
    data object NavigateToMain : LoginUiEffect
}
