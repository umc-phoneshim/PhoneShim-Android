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

/** 08. 마이페이지 - 회원 탈퇴 확인 팝업. */
@Composable
fun WithdrawPopup(
    onDismiss: () -> Unit,
    onConfirmWithdraw: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .width(280.dp)
                .background(PhoneShimTheme.colors.surface, RoundedCornerShape(16.dp))
                .padding(top = PhoneShimDimens.spacing24, start = PhoneShimDimens.spacing20, end = PhoneShimDimens.spacing20),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "탈퇴하시겠습니까?",
                style = PhoneShimType.KorH3,
                color = PhoneShimTheme.colors.textPrimary,
            )
            Spacer(modifier = Modifier.height(PhoneShimDimens.spacing8))
            Text(
                text = "탈퇴 후 2주까지만 복구 가능합니다.",
                style = PhoneShimType.KorCaption,
                color = PhoneShimTheme.colors.textTertiary,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(PhoneShimDimens.spacing20))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(PhoneShimTheme.colors.divider),
            )
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "취소",
                    style = PhoneShimType.KorBodyM,
                    color = PhoneShimTheme.colors.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onDismiss)
                        .padding(vertical = PhoneShimDimens.spacing12),
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(20.dp)
                        .background(PhoneShimTheme.colors.divider),
                )
                Text(
                    text = "탈퇴",
                    style = PhoneShimType.KorBodyM,
                    fontWeight = FontWeight.SemiBold,
                    color = PhoneShimTheme.colors.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onConfirmWithdraw)
                        .padding(vertical = PhoneShimDimens.spacing12),
                )
            }
        }
    }
}