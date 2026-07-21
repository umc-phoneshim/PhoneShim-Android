package com.phoneshim.android.ui.features.auth.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.phoneshim.android.R
import com.phoneshim.android.ui.common.IconButton
import com.phoneshim.android.ui.features.auth.viewmodel.LoginViewModel
import com.phoneshim.android.ui.features.auth.viewmodel.LoginUiEffect
import com.phoneshim.android.ui.features.auth.viewmodel.LoginUiEvent
import com.phoneshim.android.ui.features.auth.viewmodel.LoginUiState
import com.phoneshim.android.ui.theme.PhoneShimDimens
import com.phoneshim.android.ui.theme.PhoneShimPalette
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimType

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                LoginUiEffect.NavigateToGoalSetup -> onLoginSuccess()
                is LoginUiEffect.ShowSnackbar -> Unit
            }
        }
    }

    LoginContent(
        uiState = uiState,
        onGoogleLogin = { viewModel.onEvent(LoginUiEvent.GoogleLoginClicked) },
        onKakaoLogin = { viewModel.onEvent(LoginUiEvent.KakaoLoginClicked) },
        modifier = modifier,
    )
}

@Composable
private fun LoginContent(
    uiState: LoginUiState,
    onGoogleLogin: () -> Unit,
    onKakaoLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = PhoneShimPalette.SoftCream,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = PhoneShimDimens.screenHorizontalPadding)
                .padding(top = 28.dp, bottom = 27.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(480.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "캐릭터 및 환영 문구가 들어갈 예정",
                    style = PhoneShimType.EngCaption,
                    color = Color.Black,
                )
            }

            Spacer(Modifier.height(PhoneShimDimens.spacing32))

            Column(verticalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing12)) {
                IconButton(
                    label = "구글로 계속하기",
                    icon = R.drawable.google_logo,
                    iconWidth = 16.dp,
                    backgroundColor = PhoneShimPalette.Gray100,
                    contentColor = PhoneShimPalette.LoginButtonText,
                    onClick = onGoogleLogin,
                    enabled = !uiState.isLoading,
                )
                IconButton(
                    label = "카카오톡으로 계속하기",
                    icon = R.drawable.kakao_logo,
                    iconWidth = 17.dp,
                    backgroundColor = PhoneShimPalette.KakaoYellow,
                    contentColor = PhoneShimPalette.LoginButtonText,
                    onClick = onKakaoLogin,
                    enabled = !uiState.isLoading,
                )
            }

            Spacer(Modifier.height(PhoneShimDimens.spacing32))
            Text(
                text = "로그인하면 이용약관 및 개인정보 처리방침에 동의하게 됩니다.",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PhoneShimDimens.spacing24),
                color = PhoneShimPalette.Gray500,
                style = PhoneShimType.KorLabel,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun LoginScreenPreview() {
    PhoneShimTheme {
        LoginContent(
            uiState = LoginUiState(),
            onGoogleLogin = {},
            onKakaoLogin = {},
        )
    }
}
