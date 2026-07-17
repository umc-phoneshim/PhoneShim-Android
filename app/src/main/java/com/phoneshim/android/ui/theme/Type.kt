package com.phoneshim.android.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/**
 * 폰쉼 디자인 토큰 - 타이포그래피
 *
 * Figma "폰쉼 Design Pages" 의 타이포그래피 명세(Display~Micro 9단계)를 그대로 옮겼습니다.
 * - 한글은 Pretendard, 영문·숫자는 Inter 를 사용합니다.
 * - lineHeight 는 명세의 배수(120~160%)를 fontSize 에 곱한 절대 sp 로 환산했습니다.
 * - letterSpacing 은 명세가 % 로 정의돼 있어 폰트 크기에 비례하는 em 으로 표기했습니다.
 *   (예: -2% → (-0.02).em)
 */
object PhoneShimType {

    // 9단계 공통 스펙을 한 곳에서 생성하는 헬퍼
    private fun style(
        family: FontFamily,
        weight: FontWeight,
        size: Int,
        lineHeightRatio: Float,
        letterSpacingEm: Float,
    ) = TextStyle(
        fontFamily = family,
        fontWeight = weight,
        fontSize = size.sp,
        lineHeight = (size * lineHeightRatio).sp,
        letterSpacing = letterSpacingEm.em,
    )

    // ── 한글 (Pretendard) ──────────────────────────────
    val KorDisplay = style(Pretendard, FontWeight.Bold, 32, 1.2f, -0.02f)
    val KorH1 = style(Pretendard, FontWeight.Bold, 24, 1.3f, -0.015f)
    val KorH2 = style(Pretendard, FontWeight.SemiBold, 20, 1.4f, -0.01f)
    val KorH3 = style(Pretendard, FontWeight.SemiBold, 18, 1.4f, -0.005f)
    val KorBodyL = style(Pretendard, FontWeight.Normal, 16, 1.6f, 0f)
    val KorBodyM = style(Pretendard, FontWeight.Normal, 14, 1.6f, 0f)
    val KorCaption = style(Pretendard, FontWeight.Normal, 12, 1.5f, 0f)
    val KorLabel = style(Pretendard, FontWeight.Medium, 10, 1.3f, 0f)
    val KorMicro = style(Pretendard, FontWeight.Medium, 8, 1.2f, 0f)

    // ── 영문 · 숫자 (Inter) ─────────────────────────────
    val EngDisplay = style(Inter, FontWeight.Bold, 32, 1.2f, -0.02f)
    val EngH1 = style(Inter, FontWeight.Bold, 24, 1.3f, -0.015f)
    val EngH2 = style(Inter, FontWeight.SemiBold, 20, 1.4f, -0.01f)
    val EngH3 = style(Inter, FontWeight.SemiBold, 18, 1.4f, -0.005f)
    val EngBodyL = style(Inter, FontWeight.Normal, 16, 1.6f, 0f)
    val EngBodyM = style(Inter, FontWeight.Normal, 14, 1.6f, 0f)
    val EngCaption = style(Inter, FontWeight.Normal, 12, 1.5f, 0f)
    val EngLabel = style(Inter, FontWeight.Medium, 10, 1.3f, 0f)
    val EngMicro = style(Inter, FontWeight.Medium, 8, 1.2f, 0f)
}

/**
 * MaterialTheme.typography 매핑.
 * 폰쉼 한글 스케일을 M3 슬롯에 대응시켜, 기본 Material 컴포넌트도 폰쉼 서체를 따르게 합니다.
 */
val PhoneShimTypography = Typography(
    displayLarge = PhoneShimType.KorDisplay,
    headlineLarge = PhoneShimType.KorH1,
    headlineMedium = PhoneShimType.KorH2,
    headlineSmall = PhoneShimType.KorH3,
    titleLarge = PhoneShimType.KorH3,
    bodyLarge = PhoneShimType.KorBodyL,
    bodyMedium = PhoneShimType.KorBodyM,
    bodySmall = PhoneShimType.KorCaption,
    labelMedium = PhoneShimType.KorLabel,
    labelSmall = PhoneShimType.KorMicro,
)
