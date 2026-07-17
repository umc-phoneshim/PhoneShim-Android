package com.phoneshim.android.ui.features.setgoal.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.phoneshim.android.ui.common.PhoneShimButton
import com.phoneshim.android.ui.theme.PhoneShimDimens
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimType

// 접근 권한/개인정보 수집 동의 팝업 (Figma 03. 접근 권한 허용)
// 04. 목표 설정 시작 화면 위에 떠서 "모두 허용하기"로 동의를 받습니다.
@Composable
fun PermissionConsentPopup(
    onAllowAll: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(PhoneShimTheme.colors.surface)
                .padding(PhoneShimDimens.spacing24),
            verticalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing32),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing24)) {
                ConsentItem(
                    title = "개인정보 수집 및 이용 동의",
                    description = "서비스 제공 및 원활한 이용을 위해 필요한 개인 정보를 수집 및 이용합니다.",
                )
                ConsentItem(
                    title = "설치된 앱 목록 /스크린타임 수집 권한 동의",
                    description = "맞춤형 서비스 제공을 위해 앱 사용 시간 및 설치된 앱 정보를 수집합니다. " +
                        "수집된 정보는 서비스 제공 목적으로만 사용됩니다.",
                )
            }

            PhoneShimButton(
                text = "모두 허용하기",
                onClick = onAllowAll,
            )
        }
    }
}

// 동의 제목 + 연녹 배경 설명 박스 한 쌍
@Composable
private fun ConsentItem(
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

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun PermissionConsentPopupPreview() {
    PhoneShimTheme {
        PermissionConsentPopup(onAllowAll = {}, onDismiss = {})
    }
}
