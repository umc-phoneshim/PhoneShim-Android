package com.phoneshim.android.ui.features.mypage.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Dialog

@Composable
fun WithdrawPopup(
    onDismiss: () -> Unit,
    onConfirmWithdraw: () -> Unit,
) {
    // TODO: 08. 마이페이지 (탈퇴 클릭 팝업) UI 구성
    Dialog(onDismissRequest = onDismiss) {
    }
}
