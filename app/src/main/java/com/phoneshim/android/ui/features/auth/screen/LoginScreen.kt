package com.phoneshim.android.ui.features.auth.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.phoneshim.android.R
import com.phoneshim.android.domain.model.SocialProvider
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
    onNavigateToGoalSetup: () -> Unit,
    onNavigateToMain: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                LoginUiEffect.NavigateToGoalSetup -> onNavigateToGoalSetup()
                LoginUiEffect.NavigateToMain -> onNavigateToMain()
            }
        }
    }

    LoginContent(
        uiState = uiState,
        onGoogleLogin = {
            viewModel.onEvent(LoginUiEvent.LoginClicked(SocialProvider.GOOGLE))
        },
        onKakaoLogin = {
            viewModel.onEvent(LoginUiEvent.LoginClicked(SocialProvider.KAKAO))
        },
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
                .padding(vertical = PhoneShimDimens.spacing16),
            verticalArrangement = Arrangement.spacedBy(
                PhoneShimDimens.spacing32,
                Alignment.CenterVertically,
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp),
                verticalArrangement = Arrangement.spacedBy(
                    48.dp,
                    Alignment.CenterVertically,
                ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painter = painterResource(R.drawable.phoneshim_mascot),
                    contentDescription = null,
                    modifier = Modifier.size(width = 156.dp, height = 164.dp),
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing12),
                ) {
                    Text(
                        text = "폰쉼",
                        color = PhoneShimPalette.Primary600,
                        style = PhoneShimType.KorDisplay,
                    )
                    Text(
                        text = "잠시, 폰을 쉬게 하다",
                        color = PhoneShimPalette.Gray700,
                        style = PhoneShimType.KorBodyM,
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing12),
            ) {
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
                uiState.errorMessage?.let { message ->
                    Text(
                        text = message,
                        modifier = Modifier.fillMaxWidth(),
                        color = PhoneShimPalette.Error,
                        style = PhoneShimType.KorLabel,
                        textAlign = TextAlign.Center,
                    )
                }
            }

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
