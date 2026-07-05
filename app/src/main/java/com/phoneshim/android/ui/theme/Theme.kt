package com.phoneshim.android.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * 폰쉼 고유 시맨틱 컬러 접근용 CompositionLocal.
 * `PhoneShimTheme.colors.brand` 형태로 사용합니다.
 */
val LocalPhoneShimColors = staticCompositionLocalOf { PhoneShimColors() }

private val PhoneShimColorScheme = lightColorScheme(
    primary = PhoneShimPalette.Primary500,
    onPrimary = PhoneShimPalette.White,
    primaryContainer = PhoneShimPalette.Primary100,
    onPrimaryContainer = PhoneShimPalette.Primary600,
    secondary = PhoneShimPalette.Primary400,
    onSecondary = PhoneShimPalette.White,
    background = PhoneShimPalette.SoftCream,
    onBackground = PhoneShimPalette.Gray900,
    surface = PhoneShimPalette.White,
    onSurface = PhoneShimPalette.Gray900,
    surfaceVariant = PhoneShimPalette.Cream,
    onSurfaceVariant = PhoneShimPalette.Gray700,
    outline = PhoneShimPalette.Gray300,
    outlineVariant = PhoneShimPalette.Gray100,
    error = PhoneShimPalette.Error,
    onError = PhoneShimPalette.White,
)

@Composable
fun PhoneShimTheme(
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalPhoneShimColors provides PhoneShimColors()) {
        MaterialTheme(
            colorScheme = PhoneShimColorScheme,
            typography = PhoneShimTypography,
            shapes = PhoneShimShapes,
            content = content,
        )
    }
}

/**
 * 폰쉼 테마 토큰 접근용 헬퍼 오브젝트.
 * MaterialTheme 로 커버되지 않는 시맨틱 컬러를 `PhoneShimTheme.colors` 로 노출합니다.
 */
object PhoneShimTheme {
    val colors: PhoneShimColors
        @Composable
        @ReadOnlyComposable
        get() = LocalPhoneShimColors.current
}
