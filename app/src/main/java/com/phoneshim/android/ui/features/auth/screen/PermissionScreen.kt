package com.phoneshim.android.ui.features.auth.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.phoneshim.android.ui.features.setgoal.screen.SetGoalStartScreen
import com.phoneshim.android.ui.theme.PhoneShimDimens
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimType

// 접근 권한 동의 화면 (Figma 03. 접근 권한 허용)
// 목표 설정 시작 안내가 흐리게 깔린 위에 권한 동의 팝업이 노출됩니다.
@Composable
fun PermissionScreen(
    onAllowAll: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PhoneShimTheme.colors.brandSubtle),
    ) {
        // 뒤에 깔리는 목표 설정 시작 화면(Figma 04)을 40% 투명도로 재사용
        // "나중에 설정하기"도 이 배경 레이어의 위치 그대로 노출되고, 탭하면 onSkip 처리
        SetGoalStartScreen(
            onStart = {},
            onSkip = onSkip,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.4f),
        )

        PermissionConsentDialog(
            onAllowAll = onAllowAll,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = PhoneShimDimens.screenHorizontalPadding),
        )
    }
}

// 개인정보/앱 사용 정보 수집 동의 팝업 카드
@Composable
private fun PermissionConsentDialog(
    onAllowAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(PhoneShimTheme.colors.surface)
            .padding(PhoneShimDimens.spacing24),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(64.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing24)) {
            ConsentSection(
                title = "개인정보 수집 및 이용 동의",
                description = "서비스 제공 및 원활한 이용을 위해 필요한 개인 정보를 수집 및 이용합니다.",
            )
            ConsentSection(
                title = "설치된 앱 목록 /스크린타임 수집 권한 동의",
                description = "맞춤형 서비스 제공을 위해 앱 사용 시간 및 설치된 앱 정보를 수집합니다. " +
                    "수집된 정보는 서비스 제공 목적으로만 사용됩니다.",
            )
        }

        // TODO: 실제 사용 정보 접근 권한(Usage Access) 요청 연동
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .clip(MaterialTheme.shapes.small)
                .background(PhoneShimTheme.colors.brand)
                .clickable(onClick = onAllowAll),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "모두 허용하기",
                style = PhoneShimType.KorBodyM,
                color = PhoneShimTheme.colors.onBrand,
            )
        }
    }
}

// 동의 항목 제목 + 설명 박스
@Composable
private fun ConsentSection(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing12),
    ) {
        Text(
            text = title,
            style = PhoneShimType.KorBodyM,
            color = PhoneShimTheme.colors.textPrimary,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.small)
                .background(PhoneShimTheme.colors.brandSubtle)
                .padding(horizontal = PhoneShimDimens.spacing12, vertical = 10.dp),
        ) {
            Text(
                text = description,
                style = PhoneShimType.KorLabel,
                color = PhoneShimTheme.colors.textPrimary,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun PermissionScreenPreview() {
    PhoneShimTheme {
        PermissionScreen(onAllowAll = {}, onSkip = {})
    }
}
