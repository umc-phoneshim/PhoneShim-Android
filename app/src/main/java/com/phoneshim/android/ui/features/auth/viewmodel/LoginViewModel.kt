package com.phoneshim.android.ui.features.auth.viewmodel

import androidx.lifecycle.viewModelScope
import com.phoneshim.android.data.api.common.ApiException
import com.phoneshim.android.domain.model.SocialLoginResult
import com.phoneshim.android.domain.model.SocialProvider
import com.phoneshim.android.domain.model.SocialIdentity
import com.phoneshim.android.domain.model.AuthException
import com.phoneshim.android.domain.usecase.SocialLoginUseCase
import com.phoneshim.android.domain.usecase.RecoverWithdrawalUseCase
import com.phoneshim.android.ui.common.base.BaseViewModel
import com.phoneshim.android.ui.features.auth.social.SocialAuthClient
import com.phoneshim.android.ui.features.auth.social.SocialAuthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val socialAuthClient: SocialAuthClient,
    private val socialLoginUseCase: SocialLoginUseCase,
    private val recoverWithdrawalUseCase: RecoverWithdrawalUseCase,
) :
    BaseViewModel<LoginUiState, LoginUiEvent, LoginUiEffect>(LoginUiState()) {

    override fun handleEvent(event: LoginUiEvent) {
        when (event) {
            is LoginUiEvent.LoginClicked -> startLogin(event.provider)
            LoginUiEvent.ErrorDismissed -> setState { copy(errorMessage = null) }
            LoginUiEvent.WithdrawalRecoveryConfirmed -> recoverWithdrawal()
            LoginUiEvent.WithdrawalRecoveryDismissed -> setState {
                copy(withdrawalRecovery = null)
            }
        }
    }

    private fun startLogin(provider: SocialProvider) {
        if (currentState.isLoading) return

        setState {
            copy(
                selectedProvider = provider,
                isLoading = true,
                errorMessage = null,
            )
        }

        viewModelScope.launch {
            when (val authResult = socialAuthClient.authenticate(provider)) {
                SocialAuthResult.Cancelled -> finishLoading()
                is SocialAuthResult.Failure -> showError(authResult.cause.toUserMessage())
                is SocialAuthResult.Success -> completeServerLogin(provider, authResult)
            }
        }
    }

    private suspend fun completeServerLogin(
        provider: SocialProvider,
        authResult: SocialAuthResult.Success,
    ) {
        socialLoginUseCase(provider, authResult.providerAccessToken)
            .onSuccess { result ->
                finishLoading()
                sendEffect(
                    when (result) {
                        SocialLoginResult.NewUser -> LoginUiEffect.NavigateToGoalSetup
                        SocialLoginResult.ExistingUser -> LoginUiEffect.NavigateToMain
                    },
                )
            }
            .onFailure { error ->
                if (error is AuthException.WithdrawalPending) {
                    showWithdrawalRecovery(provider, authResult)
                } else {
                    showError(error.toUserMessage())
                }
            }
    }

    private fun showWithdrawalRecovery(
        provider: SocialProvider,
        authResult: SocialAuthResult.Success,
    ) {
        val providerUserId = authResult.providerUserId
        val email = authResult.email
        if (providerUserId == null || email == null) {
            showError("계정 복구 정보를 확인하지 못했습니다. 다시 로그인해주세요.")
            return
        }
        setState {
            copy(
                isLoading = false,
                selectedProvider = null,
                withdrawalRecovery = SocialIdentity(provider, providerUserId, email),
            )
        }
    }

    private fun recoverWithdrawal() {
        val identity = currentState.withdrawalRecovery ?: return
        if (currentState.isLoading) return
        setState { copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            recoverWithdrawalUseCase(identity)
                .onSuccess {
                    setState {
                        copy(
                            isLoading = false,
                            withdrawalRecovery = null,
                        )
                    }
                    sendEffect(LoginUiEffect.NavigateToMain)
                }
                .onFailure { error ->
                    setState { copy(isLoading = false) }
                    showError(error.toUserMessage())
                }
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
        is ApiException.Network -> "인터넷 연결을 확인한 뒤 다시 시도해주세요."
        is ApiException.Serialization,
        is ApiException.InvalidResponse,
        -> "서버 응답을 처리하지 못했습니다. 잠시 후 다시 시도해주세요."
        else -> "로그인 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
    }
}
