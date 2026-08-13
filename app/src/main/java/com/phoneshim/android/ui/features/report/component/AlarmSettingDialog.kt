package com.phoneshim.android.ui.features.report.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.phoneshim.android.ui.common.InteractiveTimeSegmentInput
import com.phoneshim.android.ui.features.setgoal.component.MAX_HOUR_VALUE
import com.phoneshim.android.ui.features.setgoal.component.MAX_MINUTE_VALUE
import com.phoneshim.android.ui.theme.PhoneShimDimens
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimType

private val TimeCellRestingWidth = 56.dp
private val TimeCellActiveWidth = 64.dp
private val TimeCellHeight = 48.dp
private val ActionButtonWidth = 88.dp
private val ActionButtonHeight = 36.dp

/**
 * 데일리 리포트 알림 설정 팝업.
 *
 * 리포트 하루의 기준이 22:00 - 21:59 라서, 제목에 그 구간을 그대로 노출해
 * "어느 하루에 대한 알림인지" 를 사용자가 알 수 있게 합니다.
 *
 * 시/분 입력은 온보딩 목표 설정과 같은 공통 [InteractiveTimeSegmentInput] 을 씁니다.
 */
@Composable
fun AlarmSettingDialog(
    hour: String,
    minute: String,
    onHourChange: (String) -> Unit,
    onMinuteChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = DEFAULT_TITLE,
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(PhoneShimTheme.colors.surface, RoundedCornerShape(20.dp))
                .padding(PhoneShimDimens.spacing20),
        ) {
            Text(
                text = title,
                style = PhoneShimType.KorBodyM,
                color = PhoneShimTheme.colors.textPrimary,
            )

            Spacer(modifier = Modifier.height(PhoneShimDimens.spacing24))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TimeSegment(value = hour, maxValue = MAX_HOUR_VALUE, onValueChange = onHourChange)
                Text(
                    text = ":",
                    style = PhoneShimType.EngDisplay,
                    color = PhoneShimTheme.colors.brand,
                    modifier = Modifier.padding(horizontal = PhoneShimDimens.spacing8),
                )
                TimeSegment(value = minute, maxValue = MAX_MINUTE_VALUE, onValueChange = onMinuteChange)
            }

            Spacer(modifier = Modifier.height(PhoneShimDimens.spacing24))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                DialogButton(text = "취소", isPrimary = false, onClick = onDismiss)
                Spacer(modifier = Modifier.width(PhoneShimDimens.spacing8))
                DialogButton(text = "설정", isPrimary = true, onClick = onConfirm)
            }
        }
    }
}

@Composable
private fun TimeSegment(value: String, maxValue: Int, onValueChange: (String) -> Unit) {
    InteractiveTimeSegmentInput(
        value = value,
        maxValue = maxValue,
        onValueChange = onValueChange,
        textStyle = PhoneShimType.EngDisplay,
        restingWidth = TimeCellRestingWidth,
        activeWidth = TimeCellActiveWidth,
        restingHeight = TimeCellHeight,
        activeHeight = TimeCellHeight,
    )
}

/** 팝업 하단 버튼. 취소는 테두리만, 설정은 브랜드 색으로 채웁니다. */
@Composable
private fun DialogButton(text: String, isPrimary: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(ActionButtonWidth)
            .height(ActionButtonHeight)
            .background(
                color = if (isPrimary) {
                    PhoneShimTheme.colors.brand
                } else {
                    PhoneShimTheme.colors.surfaceCream
                },
                shape = RoundedCornerShape(8.dp),
            )
            .border(
                width = 1.dp,
                color = PhoneShimTheme.colors.brand,
                shape = RoundedCornerShape(8.dp),
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = PhoneShimType.KorCaption,
            color = if (isPrimary) {
                PhoneShimTheme.colors.onBrand
            } else {
                PhoneShimTheme.colors.brandStrong
            },
        )
    }
}

private const val DEFAULT_TITLE = "22:00 - 21:59 데일리 리포트 알림 설정"

@Preview(showBackground = true)
@Composable
private fun AlarmSettingDialogPreview() {
    PhoneShimTheme {
        AlarmSettingDialog(
            hour = "00",
            minute = "00",
            onHourChange = {},
            onMinuteChange = {},
            onConfirm = {},
            onDismiss = {},
        )
    }
}
