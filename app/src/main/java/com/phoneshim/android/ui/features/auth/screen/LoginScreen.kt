package com.phoneshim.android.ui.features.auth.screen

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.Role
import androidx.hilt.navigation.compose.hiltViewModel
import com.phoneshim.android.R
import com.phoneshim.android.ui.features.auth.viewmodel.LoginViewModel
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
    LoginContent(
        onGoogleLogin = onLoginSuccess,
        onKakaoLogin = onLoginSuccess,
        modifier = modifier,
    )
}

@Composable
private fun LoginContent(
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
                    text = stringResource(R.string.login_welcome_placeholder),
                    style = PhoneShimType.EngCaption,
                    color = Color.Black,
                )
            }

            Spacer(Modifier.height(PhoneShimDimens.spacing32))

            Column(verticalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing12)) {
                SocialLoginButton(
                    label = R.string.login_with_google,
                    logo = R.drawable.google_logo,
                    logoWidth = 16.dp,
                    backgroundColor = PhoneShimPalette.Gray100,
                    onClick = onGoogleLogin,
                )
                SocialLoginButton(
                    label = R.string.login_with_kakao,
                    logo = R.drawable.kakao_logo,
                    logoWidth = 17.dp,
                    backgroundColor = KakaoYellow,
                    onClick = onKakaoLogin,
                )
            }

            Spacer(Modifier.height(PhoneShimDimens.spacing32))

            Text(
                text = stringResource(R.string.login_terms_notice),
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

@Composable
private fun SocialLoginButton(
    @StringRes label: Int,
    @DrawableRes logo: Int,
    logoWidth: Dp,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor, RoundedCornerShape(PhoneShimDimens.spacing12))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(PhoneShimDimens.spacing16),
        horizontalArrangement = Arrangement.spacedBy(
            PhoneShimDimens.spacing12,
            Alignment.CenterHorizontally,
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(logo),
            contentDescription = null,
            modifier = Modifier
                .width(logoWidth)
                .height(16.dp),
        )
        Text(
            text = stringResource(label),
            color = LoginButtonText,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

private val KakaoYellow = Color(0xFFFEE500)
private val LoginButtonText = Color(0xFF3A1D1D)

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun LoginScreenPreview() {
    PhoneShimTheme {
        LoginContent(onGoogleLogin = {}, onKakaoLogin = {})
    }
}
