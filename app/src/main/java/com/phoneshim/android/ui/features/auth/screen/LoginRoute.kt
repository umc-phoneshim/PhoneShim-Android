package com.phoneshim.android.ui.features.auth.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.phoneshim.android.domain.model.SocialProvider
import com.phoneshim.android.ui.common.ConfirmationDialog
import com.phoneshim.android.ui.common.base.CollectCommonEffect
import com.phoneshim.android.ui.features.auth.viewmodel.LoginUiEffect
import com.phoneshim.android.ui.features.auth.viewmodel.LoginUiEvent
import com.phoneshim.android.ui.features.auth.viewmodel.LoginViewModel

@Composable
fun LoginRoute(
    onNavigateToGoalSetup: () -> Unit,
    onNavigateToMain: () -> Unit,
    onAuthExpired: () -> Unit,
    noticeMessage: String? = null,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    CollectCommonEffect(viewModel, onAuthExpired)

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                LoginUiEffect.NavigateToGoalSetup -> onNavigateToGoalSetup()
                LoginUiEffect.NavigateToMain -> onNavigateToMain()
            }
        }
    }

    LoginScreen(
        uiState = uiState,
        noticeMessage = noticeMessage,
        onGoogleLogin = {
            viewModel.onEvent(LoginUiEvent.LoginClicked(SocialProvider.GOOGLE))
        },
        onKakaoLogin = {
            viewModel.onEvent(LoginUiEvent.LoginClicked(SocialProvider.KAKAO))
        },
        modifier = modifier,
    )

    if (uiState.isWithdrawalPending) {
        ConfirmationDialog(
            title = "탈퇴 유예 중인 계정입니다",
            message = "계정 복구 기능이 준비 중입니다. 현재 앱에서는 복구를 진행할 수 없습니다.",
            confirmText = "확인",
            onConfirm = {
                viewModel.onEvent(LoginUiEvent.WithdrawalPendingAcknowledged)
            },
            onDismiss = {
                viewModel.onEvent(LoginUiEvent.WithdrawalPendingDismissed)
            },
        )
    }
}
