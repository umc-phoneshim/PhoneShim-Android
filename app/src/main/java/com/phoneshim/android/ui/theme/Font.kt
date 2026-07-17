@file:OptIn(ExperimentalTextApi::class)

package com.phoneshim.android.ui.theme

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.phoneshim.android.R

/**
 * 폰쉼 서체
 *
 * Figma 기준: 한글은 Pretendard, 영문/숫자는 Inter 를 사용합니다.
 * 각 서체는 가변폰트(variable font) 1개로 배치하고, wght 축으로 웨이트를 지정합니다.
 * (variable font 는 minSdk 26+ 에서 지원 — 라이선스는 docs/fonts/ 의 OFL 참고)
 */

private fun pretendard(weight: FontWeight, axis: Int) = Font(
    resId = R.font.pretendard_variable,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(axis)),
)

private fun inter(weight: FontWeight, axis: Int) = Font(
    resId = R.font.inter_variable,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(axis)),
)

val Pretendard: FontFamily = FontFamily(
    pretendard(FontWeight.Normal, 400),
    pretendard(FontWeight.Medium, 500),
    pretendard(FontWeight.SemiBold, 600),
    pretendard(FontWeight.Bold, 700),
)

val Inter: FontFamily = FontFamily(
    inter(FontWeight.Normal, 400),
    inter(FontWeight.Medium, 500),
    inter(FontWeight.SemiBold, 600),
    inter(FontWeight.Bold, 700),
)
