package com.phoneshim.android.ui.theme

import androidx.compose.ui.text.font.FontFamily

/**
 * 폰쉼 서체
 *
 * Figma 기준: 한글은 Pretendard, 영문/숫자는 Inter 를 사용합니다.
 *
 * TODO: 실제 폰트 리소스를 추가한 뒤 아래 FontFamily 를 교체하세요.
 *   1. Pretendard: `app/src/main/res/font/` 에 pretendard_regular/medium/semibold/bold.ttf 배치
 *   2. Inter: `app/src/main/res/font/` 에 inter_regular/medium/semibold/bold.ttf 배치
 *   3. 예시:
 *      val Pretendard = FontFamily(
 *          Font(R.font.pretendard_regular, FontWeight.Normal),
 *          Font(R.font.pretendard_medium, FontWeight.Medium),
 *          Font(R.font.pretendard_semibold, FontWeight.SemiBold),
 *          Font(R.font.pretendard_bold, FontWeight.Bold),
 *      )
 *
 * 폰트 리소스가 추가되기 전까지는 시스템 기본 서체로 폴백합니다.
 */
val Pretendard: FontFamily = FontFamily.Default

val Inter: FontFamily = FontFamily.Default
