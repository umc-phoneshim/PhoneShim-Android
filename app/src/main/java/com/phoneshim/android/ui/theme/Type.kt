package com.phoneshim.android.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * 폰쉼 디자인 토큰 - 타이포그래피
 *
 * Figma "폰쉼/KOR", "폰쉼/ENG · NUM" 텍스트 스타일을 그대로 옮겼습니다.
 * lineHeight 는 Figma 의 배수(1.6 등)를 sp 절대값으로 환산했습니다.
 */
object PhoneShimType {

    // ── 한글 (Pretendard) ──────────────────────────────
    val KorH3 = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 25.2.sp, // 18 * 1.4
        letterSpacing = (-0.5).sp,
    )
    val KorBodyL = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 25.6.sp, // 16 * 1.6
        letterSpacing = 0.sp,
    )
    val KorBodyM = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.4.sp, // 14 * 1.6
        letterSpacing = 0.sp,
    )
    val KorCaption = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp, // 12 * 1.5
        letterSpacing = 0.sp,
    )
    val KorLabel = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 13.sp, // 10 * 1.3
        letterSpacing = 0.sp,
    )
    val KorMicro = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Medium,
        fontSize = 8.sp,
        lineHeight = 9.6.sp, // 8 * 1.2
        letterSpacing = 0.sp,
    )

    // ── 영문 · 숫자 (Inter) ─────────────────────────────
    val EngH1 = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 31.2.sp, // 24 * 1.3
        letterSpacing = (-1.5).sp,
    )
    val EngH2 = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp, // 20 * 1.4
        letterSpacing = (-1).sp,
    )
    val EngBodyM = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.4.sp,
        letterSpacing = 0.sp,
    )
    val EngCaption = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp,
    )
    val EngLabel = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 13.sp,
        letterSpacing = 0.sp,
    )
}

/**
 * MaterialTheme.typography 매핑.
 * 폰쉼 한글 스케일을 M3 슬롯에 대응시켜, 기본 Material 컴포넌트도 폰쉼 서체를 따르게 합니다.
 */
val PhoneShimTypography = Typography(
    headlineLarge = PhoneShimType.EngH1,
    headlineMedium = PhoneShimType.EngH2,
    titleLarge = PhoneShimType.KorH3,
    bodyLarge = PhoneShimType.KorBodyL,
    bodyMedium = PhoneShimType.KorBodyM,
    bodySmall = PhoneShimType.KorCaption,
    labelMedium = PhoneShimType.KorLabel,
    labelSmall = PhoneShimType.KorMicro,
)
