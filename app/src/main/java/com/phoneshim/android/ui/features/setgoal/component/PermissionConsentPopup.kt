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
import com.phoneshim.android.ui.common.PhoneShimDialog
import com.phoneshim.android.ui.common.PermissionNoticeItem
import com.phoneshim.android.ui.common.PrimaryButton
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
    PhoneShimDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing32),
    ) {
            Column(verticalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing24)) {
                PermissionNoticeItem(
                    title = "개인정보 수집 및 이용 동의",
                    description = "서비스 제공 및 원활한 이용을 위해 필요한 개인 정보를 수집 및 이용합니다.",
                )
                PermissionNoticeItem(
                    title = "설치된 앱 목록 /스크린타임 수집 권한 동의",
                    description = "맞춤형 서비스 제공을 위해 앱 사용 시간 및 설치된 앱 정보를 수집합니다. " +
                        "수집된 정보는 서비스 제공 목적으로만 사용됩니다.",
                )
            }

            PrimaryButton(
                text = "모두 허용하기",
                onClick = onAllowAll,
            )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun PermissionConsentPopupPreview() {
    PhoneShimTheme {
        PermissionConsentPopup(onAllowAll = {}, onDismiss = {})
    }
}
