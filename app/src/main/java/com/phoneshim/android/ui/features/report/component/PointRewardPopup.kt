package com.phoneshim.android.ui.features.report.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Dialog

@Composable
fun PointRewardPopup(
    earnedPoint: Int,
    onDismiss: () -> Unit,
) {
    // TODO: 07. 타임테이블 (포인트 적립) 팝업 UI 구성
    Dialog(onDismissRequest = onDismiss) {
    }
}
