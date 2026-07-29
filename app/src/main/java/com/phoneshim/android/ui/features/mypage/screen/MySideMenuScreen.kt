package com.phoneshim.android.ui.features.mypage.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.phoneshim.android.ui.features.mypage.component.WithdrawPopup
import com.phoneshim.android.ui.features.mypage.viewmodel.MyPageUiEffect
import com.phoneshim.android.ui.features.mypage.viewmodel.MyPageUiEvent
import com.phoneshim.android.ui.features.mypage.viewmodel.MyPageUiState
import com.phoneshim.android.ui.features.mypage.viewmodel.MyPageViewModel
import com.phoneshim.android.ui.theme.PhoneShimDimens
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimType

/**
 * 08. 마이페이지 - 우측 슬라이드 메뉴 진입점.
 * 탈퇴 확인 팝업 노출 여부는 화면이 아니라 [MyPageViewModel] 의 상태로 관리합니다.
 */
@Composable
fun MySideMenuRoute(
    onNavigateToWithdraw: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToLogin: () -> Unit = {},
    onContactSupport: () -> Unit = {},
    viewModel: MyPageViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                MyPageUiEffect.NavigateToWithdraw -> onNavigateToWithdraw()
                MyPageUiEffect.NavigateToLogin -> onNavigateToLogin()
                MyPageUiEffect.OpenContactSupport -> onContactSupport()
                is MyPageUiEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
                // 사이드 메뉴에서는 발생하지 않는 이펙트입니다. (마이페이지 본체 전용)
                MyPageUiEffect.NavigateToSideMenu -> Unit
            }
        }
    }

    MySideMenuScreen(
        state = state,
        modifier = modifier,
        snackbarHostState = snackbarHostState,
        onDismiss = onDismiss,
        onLogoutClick = { viewModel.onEvent(MyPageUiEvent.LogoutClicked) },
        onWithdrawClick = { viewModel.onEvent(MyPageUiEvent.WithdrawMenuClicked) },
        onContactSupportClick = { viewModel.onEvent(MyPageUiEvent.ContactSupportClicked) },
        onWithdrawPopupDismiss = { viewModel.onEvent(MyPageUiEvent.WithdrawPopupDismissed) },
        onWithdrawConfirm = { viewModel.onEvent(MyPageUiEvent.WithdrawConfirmed) },
    )
}

@Composable
fun MySideMenuScreen(
    state: MyPageUiState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onLogoutClick: () -> Unit = {},
    onWithdrawClick: () -> Unit = {},
    onContactSupportClick: () -> Unit = {},
    onWithdrawPopupDismiss: () -> Unit = {},
    onWithdrawConfirm: () -> Unit = {},
) {
    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.32f))
                .clickable(onClick = onDismiss),
        )

        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .width(240.dp)
                .background(PhoneShimTheme.colors.surface)
                .padding(horizontal = PhoneShimDimens.spacing20, vertical = PhoneShimDimens.spacing24),
        ) {
            Spacer(modifier = Modifier.weight(1f))

            MenuDivider()
            MenuRow(text = "로그아웃", onClick = onLogoutClick)
            MenuDivider()
            MenuRow(
                text = "회원 탈퇴",
                onClick = onWithdrawClick,
                textColor = PhoneShimTheme.colors.error,
            )
            MenuDivider()
            MenuRow(text = "문의", onClick = onContactSupportClick)
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }

    if (state.isWithdrawPopupVisible) {
        WithdrawPopup(
            onDismiss = onWithdrawPopupDismiss,
            onConfirmWithdraw = onWithdrawConfirm,
        )
    }
}

@Composable
private fun MenuRow(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    textColor: Color = PhoneShimTheme.colors.textPrimary,
) {
    Text(
        text = text,
        style = PhoneShimType.KorBodyM,
        color = textColor,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = PhoneShimDimens.spacing12),
    )
}

@Composable
private fun MenuDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(PhoneShimTheme.colors.divider),
    )
}

@Preview(showBackground = true)
@Composable
private fun MySideMenuScreenPreview() {
    PhoneShimTheme {
        MySideMenuScreen(state = MyPageUiState(), onDismiss = {})
    }
}

@Preview(name = "탈퇴 확인 팝업", showBackground = true)
@Composable
private fun MySideMenuWithdrawPopupPreview() {
    PhoneShimTheme {
        MySideMenuScreen(state = MyPageUiState(isWithdrawPopupVisible = true), onDismiss = {})
    }
}
