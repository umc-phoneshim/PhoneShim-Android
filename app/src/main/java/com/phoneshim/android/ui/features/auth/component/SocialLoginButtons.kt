package com.phoneshim.android.ui.features.auth.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phoneshim.android.R
import com.phoneshim.android.ui.theme.PhoneShimTheme

/**
 * 각 사업자의 브랜딩 규격을 지키면서 동일한 컨테이너 규격으로 표시합니다.
 *
 * Google: https://developers.google.com/identity/branding-guidelines
 * Kakao: https://developers.kakao.com/tool/resource/login
 *
 * 브랜드 로고·문구·색상·여백은 각 사업자의 공식 규격을 유지합니다.
 */
@Composable
fun GoogleSignInButton(
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    SocialLoginButton(
        backgroundColor = PhoneShimTheme.colors.loginGoogleBackground,
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
    ) {
        SocialLoginButtonContent(
            label = stringResource(R.string.google_sign_in_button_description),
            labelColor = PhoneShimTheme.colors.loginGoogleContent,
            labelFontWeight = FontWeight.Medium,
            icon = { GoogleBrandLogo() },
        )
    }
}

@Composable
fun KakaoLoginButton(
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    SocialLoginButton(
        backgroundColor = PhoneShimTheme.colors.loginKakaoBackground,
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
    ) {
        SocialLoginButtonContent(
            label = stringResource(R.string.kakao_login_button_description),
            labelColor = PhoneShimTheme.colors.loginKakaoContent,
            labelFontWeight = FontWeight.Normal,
            icon = { KakaoBrandLogo() },
        )
    }
}

/** 버튼 폭과 관계없이 브랜드 심볼은 왼쪽 16dp, 레이블은 컨테이너 중앙에 유지합니다. */
@Composable
private fun SocialLoginButtonContent(
    label: String,
    labelColor: Color,
    labelFontWeight: FontWeight,
    icon: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .align(Alignment.CenterStart),
            contentAlignment = Alignment.Center,
        ) {
            icon()
        }
        Text(
            text = label,
            color = labelColor,
            fontFamily = FontFamily.SansSerif,
            fontWeight = labelFontWeight,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

@Composable
private fun SocialLoginButton(
    backgroundColor: Color,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(6.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(45.dp)
            .clip(shape)
            .background(backgroundColor)
            .clickable(
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

/** Google 공식 40dp 아이콘 버튼에서 원본 20dp G 영역만 노출합니다. */
@Composable
private fun GoogleBrandLogo() {
    Box(
        modifier = Modifier
            .size(20.dp)
            .clip(RoundedCornerShape(0.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.google_g_button_source),
            contentDescription = null,
            modifier = Modifier.requiredSize(40.dp),
        )
    }
}

/** 카카오 공식 로그인 버튼 원본에서 분리한 20dp 말풍선 심볼입니다. */
@Composable
private fun KakaoBrandLogo() {
    Image(
        painter = painterResource(R.drawable.kakao_login_symbol),
        contentDescription = null,
        modifier = Modifier.size(20.dp),
    )
}
