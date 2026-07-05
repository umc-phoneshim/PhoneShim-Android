package com.phoneshim.android.ui.features.reminder.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Dialog

@Composable
fun ReminderSetPopup(
    onDismiss: () -> Unit,
    onConfirm: (title: String, scheduledAt: Long) -> Unit,
) {
    // TODO: 06. 리마인더 설정 과정 팝업 UI 구성
    Dialog(onDismissRequest = onDismiss) {
    }
}
