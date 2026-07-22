package com.phoneshim.android.ui.features.mypage.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.phoneshim.android.ui.theme.PhoneShimDimens
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimType
import com.phoneshim.android.ui.common.ConfirmationDialog

/** 08. 마이페이지 - 회원 탈퇴 확인 팝업. */
@Composable
fun WithdrawPopup(
    onDismiss: () -> Unit,
    onConfirmWithdraw: () -> Unit,
) = ConfirmationDialog(
    title = "탈퇴하시겠습니까?",
    message = "탈퇴 후 2주까지만 복구 가능합니다.",
    confirmText = "탈퇴",
    destructive = true,
    onDismiss = onDismiss,
    onConfirm = onConfirmWithdraw,
)
