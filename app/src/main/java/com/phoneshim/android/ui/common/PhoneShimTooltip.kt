package com.phoneshim.android.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.phoneshim.android.ui.theme.PhoneShimPalette
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimType

enum class TooltipTailAlignment {
    Start,
    Center,
    End,
}

/**
 * 기능을 처음 접한 사용자에게 짧은 설명을 보여주는 폰쉼 공통 툴팁.
 *
 * 툴팁을 가리킬 대상 가까이에 배치하고 [tailAlignment]로 꼬리 방향을 맞춘다.
 * 화면별 좌표나 노출 정책은 호출부가 소유하므로 PREF와 Reminder에서 함께 쓸 수 있다.
 */
@Composable
fun PhoneShimTooltip(
    text: String,
    modifier: Modifier = Modifier,
    tailAlignment: TooltipTailAlignment = TooltipTailAlignment.Center,
    width: Dp = TooltipDefaults.Width,
) {
    val tailHorizontalAlignment = when (tailAlignment) {
        TooltipTailAlignment.Start -> Alignment.Start
        TooltipTailAlignment.Center -> Alignment.CenterHorizontally
        TooltipTailAlignment.End -> Alignment.End
    }
    val tailPadding = when (tailAlignment) {
        TooltipTailAlignment.Start -> Modifier.padding(start = TooltipDefaults.TailEdgePadding)
        TooltipTailAlignment.Center -> Modifier
        TooltipTailAlignment.End -> Modifier.padding(end = TooltipDefaults.TailEdgePadding)
    }

    Column(
        modifier = modifier.width(width),
        horizontalAlignment = tailHorizontalAlignment,
    ) {
        Box(
            modifier = tailPadding
                .width(TooltipDefaults.BorderWidth)
                .height(TooltipDefaults.TailHeight)
                .background(PhoneShimPalette.Primary400),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(TooltipDefaults.Height)
                .background(
                    color = PhoneShimPalette.Primary100,
                    shape = RoundedCornerShape(TooltipDefaults.CornerRadius),
                )
                .border(
                    width = TooltipDefaults.BorderWidth,
                    color = PhoneShimPalette.Primary400,
                    shape = RoundedCornerShape(TooltipDefaults.CornerRadius),
                )
                .padding(horizontal = TooltipDefaults.HorizontalPadding),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                style = PhoneShimType.KorLabel,
                color = PhoneShimPalette.Gray700,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
    }
}

private object TooltipDefaults {
    val Width = 212.dp
    val Height = 31.dp
    val TailHeight = 16.dp
    val TailEdgePadding = 22.dp
    // Figma의 24dp 패딩은 웹 렌더링 기준이다. Compose Pretendard에서는 같은 212dp 폭에서
    // 한국어 안내 문구 끝이 잘리므로 외곽 크기를 유지하고 텍스트 가용 폭만 확보한다.
    val HorizontalPadding = 16.dp
    val CornerRadius = 8.dp
    val BorderWidth = 1.dp
}

@Preview(showBackground = true)
@Composable
private fun PhoneShimTooltipPreview() {
    PhoneShimTheme {
        PhoneShimTooltip(
            text = "목표 시간 이후 어플 제한을 표시해줍니다.",
            tailAlignment = TooltipTailAlignment.End,
        )
    }
}
