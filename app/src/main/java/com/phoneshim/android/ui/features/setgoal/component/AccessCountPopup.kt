package com.phoneshim.android.ui.features.setgoal.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Dialog

@Composable
fun AccessCountPopup(
    onDismiss: () -> Unit,
    onConfirm: (accessCount: Int) -> Unit,
) {
    // TODO: 04-4. 접근 횟수 설정 팝업 UI 구성
    Dialog(onDismissRequest = onDismiss) {
    }
}
