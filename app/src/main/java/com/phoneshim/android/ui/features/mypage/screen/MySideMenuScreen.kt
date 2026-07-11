package com.phoneshim.android.ui.features.mypage.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.phoneshim.android.ui.features.mypage.component.WithdrawPopup
import com.phoneshim.android.ui.theme.PhoneShimDimens
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimType

/**
 * 08. 마이페이지 - 우측 슬라이드 메뉴.
 * [MyScreen] 상단의 토글 아이콘으로 진입하는 별도 화면(destination)입니다.
 * 왼쪽 스크림을 탭하면 [onDismiss], "회원 탈퇴" 를 누르면 확인 팝업을 띄우고
 * 팝업에서 최종 확인 시 [onNavigateToWithdraw] 를 호출합니다.
 */
@Composable
fun MySideMenuScreen(
    onNavigateToWithdraw: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onLogout: () -> Unit = {},
    onContactSupport: () -> Unit = {},
) {
    var showWithdrawPopup by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.32f))
                .clickable(onClick = onDismiss),
        )

        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .width(240.dp)
                .background(PhoneShimTheme.colors.surface)
                .padding(horizontal = PhoneShimDimens.spacing20, vertical = PhoneShimDimens.spacing24),
        ) {
            Spacer(modifier = Modifier.weight(1f))

            MenuDivider()
            MenuRow(text = "로그아웃", onClick = onLogout)
            MenuDivider()
            MenuRow(
                text = "회원 탈퇴",
                onClick = { showWithdrawPopup = true },
                textColor = PhoneShimTheme.colors.error,
            )
            MenuDivider()
            MenuRow(text = "문의", onClick = onContactSupport)
        }
    }

    if (showWithdrawPopup) {
        WithdrawPopup(
            onDismiss = { showWithdrawPopup = false },
            onConfirmWithdraw = {
                showWithdrawPopup = false
                onNavigateToWithdraw()
            },
        )
    }
}

@Composable
private fun MenuRow(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    textColor: Color = PhoneShimTheme.colors.textPrimary,
) {
    Text(
        text = text,
        style = PhoneShimType.KorBodyM,
        color = textColor,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = PhoneShimDimens.spacing12),
    )
}

@Composable
private fun MenuDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(PhoneShimTheme.colors.divider),
    )
}
