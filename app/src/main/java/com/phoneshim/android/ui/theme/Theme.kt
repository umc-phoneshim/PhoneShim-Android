package com.phoneshim.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val PhoneShimColorScheme = lightColorScheme(
    primary = PhoneShimPrimary,
    secondary = PhoneShimSecondary,
    background = PhoneShimBackground,
    surface = PhoneShimSurface,
    onPrimary = PhoneShimOnPrimary,
    onBackground = PhoneShimOnBackground,
)

@Composable
fun PhoneShimTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = PhoneShimColorScheme,
        typography = PhoneShimTypography,
        content = content,
    )
}
