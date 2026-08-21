package com.phoneshim.android.ui.features.auth.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.phoneshim.android.R
import com.phoneshim.android.ui.theme.PhoneShimDimens
import com.phoneshim.android.ui.theme.PhoneShimTheme

@Composable
fun SplashScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PhoneShimTheme.colors.background),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.splash_system_logo),
            contentDescription = null,
            modifier = Modifier
                .size(PhoneShimDimens.splashLogoCanvasSize)
                .clipToBounds(),
            contentScale = ContentScale.Fit,
        )
    }
}

@Preview(
    showBackground = true,
    widthDp = 360,
    heightDp = 800,
)
@Composable
private fun SplashScreenPreview() {
    PhoneShimTheme {
        SplashScreen()
    }
}
