package com.phoneshim.android.ui.features.setgoal.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Dialog

// 목표 설명 작성 방식 (직접 작성 / AI 제안 선택)
enum class GoalWriteTab {
    USER_WRITTEN,
    AI_SUGGESTION,
}

// 목표 설명을 직접 쓰거나 AI 제안을 선택하는 팝업
@Composable
fun GoalWritePopup(
    onDismiss: () -> Unit,
    onConfirm: (description: String) -> Unit,
) {
    // TODO: 04-5. 목표 설정 팝업 - 사용자 작성 / AI 제안 탭 UI 구성
    Dialog(onDismissRequest = onDismiss) {
    }
}
