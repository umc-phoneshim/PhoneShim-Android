package com.phoneshim.android.ui.features.auth.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimType
import kotlinx.coroutines.delay

// 스플래시 노출 시간 (ms)
private const val SPLASH_DURATION_MILLIS = 1_500L

// 앱 시작 시 잠깐 노출되는 스플래시 화면 (Figma 01. 앱 클릭 직후)
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(Unit) {
        delay(SPLASH_DURATION_MILLIS)
        onSplashFinished()
    }

    // TODO: 캐릭터/로고 에셋 확정 시 텍스트를 이미지로 교체
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PhoneShimTheme.colors.background),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "폰쉼",
            style = PhoneShimType.EngH1,
            color = PhoneShimTheme.colors.brand,
        )
    }
}
