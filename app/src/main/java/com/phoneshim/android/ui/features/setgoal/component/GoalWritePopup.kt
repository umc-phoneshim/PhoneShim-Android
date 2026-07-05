package com.phoneshim.android.ui.features.setgoal.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Dialog

enum class GoalWriteTab {
    USER_WRITTEN,
    AI_SUGGESTION,
}

@Composable
fun GoalWritePopup(
    onDismiss: () -> Unit,
    onConfirm: (description: String) -> Unit,
) {
    // TODO: 04-5. 목표 설정 팝업 - 사용자 작성 / AI 제안 탭 UI 구성
    Dialog(onDismissRequest = onDismiss) {
    }
}
