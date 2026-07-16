package com.phoneshim.android.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * 폰쉼 디자인 토큰 - 색상
 *
 * Figma "폰쉼 Design Pages" 의 Color System(17색) 스와치를 그대로 옮긴 원시 팔레트입니다.
 * 화면에서는 가급적 [PhoneShimColors] 의 시맨틱 별칭을 사용하고,
 * 이 원시 토큰은 시맨틱 매핑에서만 참조하세요.
 */
object PhoneShimPalette {
    // Primary (그린 계열)
    val Primary100 = Color(0xFFF4F8F1)
    val Primary300 = Color(0xFFDCE7D4)
    val Primary400 = Color(0xFFB2C69D)
    val Primary500 = Color(0xFF8CAB7A) // 메인 브랜드 컬러
    val Primary600 = Color(0xFF6D8B5E)

    // Neutral / Gray
    val White = Color(0xFFFFFFFF)
    val Gray100 = Color(0xFFECECEC)
    val Gray300 = Color(0xFFCCCCCC)
    val Gray500 = Color(0xFF888888)
    val Gray700 = Color(0xFF555555)
    val Gray900 = Color(0xFF262626)

    // Background
    val SoftCream = Color(0xFFFFFDF7)
    val Cream = Color(0xFFFAF7F0) // Figma Color System 명세에 맞춰 수정 (기존 #FCFAF2)

    // Semantic
    val Success = Color(0xFF7DAA64)
    val Warning = Color(0xFFFFD5CE)
    val Info = Color(0xFFB7D4FF)
    val Error = Color(0xFFE56767)
}

/**
 * 시맨틱 컬러 - 역할 기반 별칭.
 * MaterialTheme.colorScheme 로 다 커버되지 않는 폰쉼 고유 컬러를 담습니다.
 * [LocalPhoneShimColors] 를 통해 컴포저블에서 접근합니다.
 */
data class PhoneShimColors(
    val brand: Color = PhoneShimPalette.Primary500,
    val brandStrong: Color = PhoneShimPalette.Primary600,
    val brandSubtle: Color = PhoneShimPalette.Primary100,
    val background: Color = PhoneShimPalette.SoftCream,
    val surface: Color = PhoneShimPalette.White,
    val surfaceCream: Color = PhoneShimPalette.Cream,
    val textPrimary: Color = PhoneShimPalette.Gray900,
    val textSecondary: Color = PhoneShimPalette.Gray700,
    val textTertiary: Color = PhoneShimPalette.Gray500,
    val border: Color = PhoneShimPalette.Gray300,
    val divider: Color = PhoneShimPalette.Gray100,
    val success: Color = PhoneShimPalette.Success,
    val warning: Color = PhoneShimPalette.Warning,
    val info: Color = PhoneShimPalette.Info,
    val error: Color = PhoneShimPalette.Error,
    val onBrand: Color = PhoneShimPalette.White,
)
