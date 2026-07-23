package com.phoneshim.android.ui.features.setgoal.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.phoneshim.android.blocking.permission.rememberBlockingPermissionRequest
import com.phoneshim.android.ui.common.PrimaryButton
import com.phoneshim.android.ui.features.setgoal.component.PermissionConsentPopup
import com.phoneshim.android.ui.theme.PhoneShimDimens
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimType

// 목표 설정 온보딩 플로우의 시작 안내 화면 (Figma 04. 목표 설정 시작)
// 로그인 직후 첫 진입 시 접근 권한 동의 팝업(Figma 03)이 이 화면 위에 뜹니다.
@Composable
fun SetGoalStartScreen(
    onStart: () -> Unit,
    modifier: Modifier = Modifier,
    onSkip: () -> Unit = {},
    showPermissionConsent: Boolean = true,
) {
    var permissionConsentVisible by remember { mutableStateOf(showPermissionConsent) }

    // 차단 엔진용 특수 권한(사용정보 접근·오버레이) 요청 왕복 헬퍼.
    // 설정 화면을 순서대로 띄우고 돌아와 다시 확인하며, 둘 다 허용되면 엔진을 시작한다.
    // 프리뷰에선 ActivityResult 런처를 만들 수 없어 생성하지 않는다.
    val permissionRequest = if (LocalInspectionMode.current) {
        null
    } else {
        rememberBlockingPermissionRequest { _ ->
            // 권한 왕복 종료(허용/미허용 무관) → 동의 팝업 닫기. 허용됐다면 엔진은 이미 시작됨.
            permissionConsentVisible = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PhoneShimTheme.colors.brandSubtle)
            .padding(PhoneShimDimens.spacing16),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing24),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "캐릭터의 목표 설정 페이지로 넘어가는 문구",
                style = PhoneShimType.KorCaption,
                color = PhoneShimTheme.colors.textPrimary,
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = PhoneShimDimens.spacing12),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing12),
        ) {
            Text(
                text = "쉼이와 함께 목표 설정을 해볼까요?",
                style = PhoneShimType.KorH3,
                color = PhoneShimTheme.colors.textPrimary,
            )
            Text(
                text = "처음부터 차근차근 설정해나가봐요!",
                style = PhoneShimType.KorBodyM,
                color = PhoneShimTheme.colors.textPrimary,
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing12),
        ) {
            PrimaryButton(
                text = "목표 설정하기",
                onClick = onStart,
            )
            Text(
                text = "나중에 설정하기",
                style = PhoneShimType.KorCaption,
                color = PhoneShimTheme.colors.textTertiary,
                modifier = Modifier.clickable(onClick = onSkip),
            )
        }
    }

    if (permissionConsentVisible) {
        PermissionConsentPopup(
            // "모두 허용하기" → 특수 권한 설정 화면으로 안내(왕복). 완료 시 콜백에서 팝업을 닫는다.
            // (프리뷰 등 헬퍼가 없을 때는 팝업만 닫는다.)
            onAllowAll = { permissionRequest?.launch() ?: run { permissionConsentVisible = false } },
            onDismiss = { permissionConsentVisible = false },
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun SetGoalStartScreenPreview() {
    PhoneShimTheme {
        SetGoalStartScreen(onStart = {})
    }
}
