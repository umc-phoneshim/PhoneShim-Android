package com.phoneshim.android.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.phoneshim.android.R

/**
 * 폰쉼 서체
 *
 * Figma 기준: 한글은 Pretendard, 영문/숫자는 Inter 를 사용합니다.
 * weight별 정적(static) 폰트 파일을 사용합니다 — variable font 1개 +
 * variationSettings 조합은 일부 기기/OS에서 같은 resId를 공유하는 Font()
 * 엔트리들의 weight 구분이 캐시 문제로 무시되는 알려진 이슈가 있어
 * 정적 파일 방식으로 전환했습니다. (라이선스는 docs/fonts/ 의 OFL 참고)
 */
val Pretendard: FontFamily = FontFamily(
    Font(R.font.pretendard_regular, FontWeight.Normal),
    Font(R.font.pretendard_medium, FontWeight.Medium),
    Font(R.font.pretendard_semibold, FontWeight.SemiBold),
    Font(R.font.pretendard_bold, FontWeight.Bold),
)

val Inter: FontFamily = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold),
)