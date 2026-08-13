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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.phoneshim.android.R
import com.phoneshim.android.domain.model.SocialProvider
import com.phoneshim.android.ui.common.IconButton
import com.phoneshim.android.ui.features.auth.viewmodel.LoginUiState
import com.phoneshim.android.ui.theme.PhoneShimDimens
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimType

@Composable
fun LoginScreen(
    uiState: LoginUiState,
    noticeMessage: String? = null,
    onGoogleLogin: () -> Unit,
    onKakaoLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = PhoneShimTheme.colors.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = PhoneShimDimens.screenHorizontalPadding),
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
                    Image(
                        painter = painterResource(R.drawable.phoneshim_wordmark),
                        contentDescription = null,
                        modifier = Modifier.size(width = 88.dp, height = 39.dp),
                    )
                    Text(
                        text = "잠시, 폰을 쉬게 하다",
                        color = PhoneShimTheme.colors.brandStrong,
                        style = PhoneShimType.KorCaption,
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing12),
            ) {
                IconButton(
                    label = if (
                        uiState.selectedProvider == SocialProvider.GOOGLE && uiState.isLoading
                    ) "구글 로그인 중" else "구글로 계속하기",
                    icon = R.drawable.google_logo,
                    iconWidth = 16.dp,
                    backgroundColor = PhoneShimTheme.colors.loginGoogleBackground,
                    contentColor = PhoneShimTheme.colors.loginButtonContent,
                    onClick = onGoogleLogin,
                    enabled = uiState.canGoogleLogin && !uiState.isLoading,
                    isLoading = uiState.selectedProvider == SocialProvider.GOOGLE && uiState.isLoading,
                )
                IconButton(
                    label = if (
                        uiState.selectedProvider == SocialProvider.KAKAO && uiState.isLoading
                    ) "카카오 로그인 중" else "카카오톡으로 계속하기",
                    icon = R.drawable.kakao_logo,
                    iconWidth = 17.dp,
                    backgroundColor = PhoneShimTheme.colors.loginKakaoBackground,
                    contentColor = PhoneShimTheme.colors.loginButtonContent,
                    onClick = onKakaoLogin,
                    enabled = !uiState.isLoading,
                    isLoading = uiState.selectedProvider == SocialProvider.KAKAO && uiState.isLoading,
                )
                uiState.errorMessage?.let { message ->
                    Text(
                        text = message,
                        modifier = Modifier.fillMaxWidth(),
                        color = PhoneShimTheme.colors.error,
                        style = PhoneShimType.KorLabel,
                        textAlign = TextAlign.Center,
                    )
                }
                if (uiState.errorMessage == null) {
                    noticeMessage?.let { message ->
                        Text(
                            text = message,
                            modifier = Modifier.fillMaxWidth(),
                            color = PhoneShimTheme.colors.textSecondary,
                            style = PhoneShimType.KorLabel,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

            Text(
                text = "로그인하면 이용약관 및 개인정보 처리방침에 동의하게 됩니다.",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PhoneShimDimens.spacing24),
                color = PhoneShimTheme.colors.textTertiary,
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
        LoginScreen(
            uiState = LoginUiState(),
            onGoogleLogin = {},
            onKakaoLogin = {},
        )
    }
}
