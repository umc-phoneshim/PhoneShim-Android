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
import androidx.compose.ui.unit.dp
import com.phoneshim.android.ui.common.PhoneShimButton
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
        // 뒤에 깔리는 목표 설정 시작 안내 (40% 투명도)
        GoalStartBackground(
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

        Text(
            text = "나중에 설정하기",
            style = PhoneShimType.KorCaption,
            color = PhoneShimTheme.colors.textTertiary,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp)
                .clickable(onClick = onSkip),
        )
    }
}

// 팝업 뒤로 흐리게 보이는 목표 설정 시작 안내 콘텐츠
@Composable
private fun GoalStartBackground(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(PhoneShimDimens.spacing16),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing24),
    ) {
        // 캐릭터 영역 (에셋 확정 전 placeholder)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
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

        // "나중에 설정하기" 는 팝업과 함께 실제 터치 대상으로 하단에 별도 배치
        PhoneShimButton(
            text = "목표 설정하기",
            onClick = { },
            modifier = Modifier.padding(bottom = PhoneShimDimens.spacing32),
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
