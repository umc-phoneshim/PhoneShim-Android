package com.phoneshim.android.ui.theme

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
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

/* ============================================================
 * 추가: 스크립트 불문 통합 타이포 토큰 + 자동 분리 PhoneShimText
 *
 * 위 PhoneShimType(Kor*, Eng* 계열)과 별개로 공존하는 세트입니다. 기존 Kor*, Eng* 계열을
 * 이미 쓰고 있는 화면은 그대로 두고, 한글/영문·숫자가 한 문자열에 섞여 있어
 * 스타일을 수동으로 나누기 번거로운 곳(예: "3회", "01 시간 30 분")에서
 * 아래 PhoneShimTextTokens + PhoneShimText를 사용하세요.
 *
 * NOTE: 이름을 PhoneShimTypography로 하지 않은 이유 - 이 파일에 이미 그 이름으로
 * M3 Typography 인스턴스(위쪽 val PhoneShimTypography)가 정의되어 있어 충돌합니다.
 * ============================================================ */

private fun phoneShimTextStyle(
    fontSize: androidx.compose.ui.unit.TextUnit,
    lineHeight: androidx.compose.ui.unit.TextUnit,
    letterSpacing: androidx.compose.ui.unit.TextUnit,
    fontWeight: FontWeight,
): TextStyle = TextStyle(
    fontFamily = Pretendard,
    fontWeight = fontWeight,
    fontSize = fontSize,
    lineHeight = lineHeight,
    letterSpacing = letterSpacing,
    platformStyle = PlatformTextStyle(includeFontPadding = false),
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.None,
    ),
)

/**
 * 스크립트 불문 통합 타이포 토큰. [PhoneShimText]의 한글/영문·숫자 자동 분리와 짝을 이룹니다.
 * lineHeight/letterSpacing 수치는 위 PhoneShimType과 동일한 Figma 스펙 환산값입니다.
 */
object PhoneShimTextTokens {
    val display = phoneShimTextStyle(32.sp, 38.4.sp, (-0.64).sp, FontWeight.Bold)
    val h1 = phoneShimTextStyle(24.sp, 31.2.sp, (-0.36).sp, FontWeight.Bold)
    val h2 = phoneShimTextStyle(20.sp, 28.sp, (-0.2).sp, FontWeight.SemiBold)
    val h3 = phoneShimTextStyle(18.sp, 25.2.sp, (-0.09).sp, FontWeight.SemiBold)
    val bodyL = phoneShimTextStyle(16.sp, 25.6.sp, 0.sp, FontWeight.Normal)
    val bodyM = phoneShimTextStyle(14.sp, 22.4.sp, 0.sp, FontWeight.Normal)
    val caption = phoneShimTextStyle(12.sp, 18.sp, 0.sp, FontWeight.Normal)
    val label = phoneShimTextStyle(10.sp, 13.sp, 0.sp, FontWeight.Medium)
    val micro = phoneShimTextStyle(8.sp, 9.6.sp, 0.sp, FontWeight.Medium)
}

private fun Char.isHangul(): Boolean =
    this in '가'..'힣' || this in 'ᄀ'..'ᇿ' || this in '㄰'..'㆏'

private fun Char.isLatinOrDigit(): Boolean =
    this in 'A'..'Z' || this in 'a'..'z' || this.isDigit()

/**
 * 문자열을 한글 구간(Pretendard) / 영문·숫자 구간(Inter)으로 분리한다.
 * 공백, 쉼표, %, ~ 같은 중립 문자는 별도 구간을 만들지 않고 앞 구간에 흡수된다.
 * (문자열이 중립 문자로 시작하면 앞 구간이 없으므로 뒤에 오는 구간에 흡수된다)
 */
private fun splitByScript(text: String): List<Pair<String, FontFamily>> {
    if (text.isEmpty()) return emptyList()

    val segments = mutableListOf<Pair<String, FontFamily>>()
    var currentFamily: FontFamily? = null
    val currentBuilder = StringBuilder()

    for (c in text) {
        val family = when {
            c.isHangul() -> Pretendard
            c.isLatinOrDigit() -> Inter
            else -> null
        }
        when {
            family == null -> currentBuilder.append(c)
            currentFamily == null -> {
                currentFamily = family
                currentBuilder.append(c)
            }
            family == currentFamily -> currentBuilder.append(c)
            else -> {
                segments.add(currentBuilder.toString() to currentFamily)
                currentBuilder.clear()
                currentBuilder.append(c)
                currentFamily = family
            }
        }
    }
    if (currentBuilder.isNotEmpty()) {
        segments.add(currentBuilder.toString() to (currentFamily ?: Pretendard))
    }
    return segments
}

/**
 * 한글=Pretendard, 영문/숫자=Inter로 자동 분리해서 그리는 Text.
 * 예) "3회" -> [3:Inter][회:Pretendard]
 *
 * NOTE: Pretendard/Inter는 Font.kt(FONT_SETUP.md 참고)에 실제 폰트 리소스가
 * 추가되기 전까지 FontFamily.Default로 폴백되어 있어, 리소스 추가 전엔
 * 시스템 기본 서체로 렌더링됩니다.
 */
@Composable
fun PhoneShimText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
) {
    val annotatedText = remember(text) {
        buildAnnotatedString {
            splitByScript(text).forEach { (segment, family) ->
                withStyle(SpanStyle(fontFamily = family)) {
                    append(segment)
                }
            }
        }
    }
    Text(
        text = annotatedText,
        modifier = modifier,
        style = style,
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
    )
}
