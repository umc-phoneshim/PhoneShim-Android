package com.phoneshim.android.ui.features.appblocking.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.phoneshim.android.ui.common.Checkbox
import com.phoneshim.android.ui.common.PrimaryButton
import com.phoneshim.android.ui.common.PhoneShimButtonSize
import com.phoneshim.android.ui.features.appblocking.component.BlockingOverlay
import com.phoneshim.android.ui.theme.PhoneShimPalette
import com.phoneshim.android.ui.theme.PhoneShimType
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimDimens

val DefaultUsageReasons = listOf("여가 시간", "이동 시간 중", "습관적으로", "정보를 얻기 위해", "기타")

@Composable
fun UsageReasonSelectionScreen(
    appName: String,
    selectedReason: String?,
    onReasonSelected: (String) -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
    reasons: List<String> = DefaultUsageReasons,
) {
    BlockingOverlay(contentAlignment = Alignment.BottomCenter) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(PhoneShimPalette.SoftCream, MaterialTheme.shapes.medium)
                .padding(PhoneShimDimens.spacing24),
            verticalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing24),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing8)) {
                Text("$appName 사용 이유 선택", style = PhoneShimType.KorH2, color = PhoneShimPalette.Gray900)
                Text("해당 어플의 사용 이유를 선택해주세요", style = MaterialTheme.typography.bodySmall, color = PhoneShimPalette.Gray900)
            }
            Column(verticalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing12)) {
                reasons.forEach { reason ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PhoneShimPalette.Primary100, MaterialTheme.shapes.small)
                            .clickable { onReasonSelected(reason) }
                            .padding(PhoneShimDimens.spacing12),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing8),
                    ) {
                        Text(reason, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                        Checkbox(checked = reason == selectedReason, onCheckedChange = { onReasonSelected(reason) })
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                PrimaryButton(
                    text = "완료",
                    onClick = onComplete,
                    modifier = Modifier
                        .fillMaxWidth(.43f)
                        .padding(top = PhoneShimDimens.spacing12),
                    enabled = selectedReason != null,
                    size = PhoneShimButtonSize.Small,
                )
            }
        }
    }
}

@Preview(name = "사용 이유 선택", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun UsageReasonSelectionScreenPreview() {
    PhoneShimTheme {
        var selectedReason by remember { mutableStateOf<String?>("여가 시간") }
        UsageReasonSelectionScreen(
            appName = "인스타그램",
            selectedReason = selectedReason,
            onReasonSelected = { selectedReason = it },
            onComplete = {},
        )
    }
}
