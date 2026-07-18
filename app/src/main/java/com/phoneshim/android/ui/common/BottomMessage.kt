package com.phoneshim.android.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.phoneshim.android.ui.theme.PhoneShimTheme

enum class BottomMessageType {
    Info,
    Warning,
    Error,
}

@Composable
fun BottomMessage(
    message: String,
    modifier: Modifier = Modifier,
    type: BottomMessageType = BottomMessageType.Info,
) {
    val containerColor = when (type) {
        BottomMessageType.Info -> PhoneShimTheme.colors.brandSubtle
        BottomMessageType.Warning -> Color(0xFFFFF2CC)
        BottomMessageType.Error -> PhoneShimTheme.colors.error.copy(alpha = 0.12f)
    }
    val contentColor = when (type) {
        BottomMessageType.Info -> PhoneShimTheme.colors.brandStrong
        BottomMessageType.Warning -> Color(0xFF7A5B00)
        BottomMessageType.Error -> PhoneShimTheme.colors.error
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(containerColor, MaterialTheme.shapes.small)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(text = message, style = MaterialTheme.typography.bodySmall, color = contentColor)
    }
}

@Preview(showBackground = true)
@Composable
private fun BottomMessagePreview() {
    PhoneShimTheme {
        androidx.compose.foundation.layout.Column {
            BottomMessage(message = "날짜가 선택되었습니다.")
            BottomMessage(message = "확인이 필요한 안내입니다.", type = BottomMessageType.Warning)
            BottomMessage(
                message = "목표 사용 시간은 10분 이상 입력해 주세요. 긴 문구에서도 영역이 자연스럽게 늘어납니다.",
                type = BottomMessageType.Error,
            )
        }
    }
}
