package com.phoneshim.android.ui.features.auth.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.phoneshim.android.ui.common.PhoneShimDialog
import com.phoneshim.android.ui.common.PermissionNoticeItem
import com.phoneshim.android.ui.common.PhoneShimButtonSize
import com.phoneshim.android.ui.common.PrimaryButton
import com.phoneshim.android.ui.theme.PhoneShimDimens
import com.phoneshim.android.ui.theme.PhoneShimPalette
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimType

@Composable
fun PermissionConsentDialog(
    onAllowAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PhoneShimDialog(
        onDismissRequest = {},
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
        ),
        verticalArrangement = Arrangement.spacedBy(64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
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
                size = PhoneShimButtonSize.Medium,
                shape = RoundedCornerShape(8.dp),
            )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun PermissionConsentDialogPreview() {
    PhoneShimTheme {
        PermissionConsentDialog(onAllowAll = {})
    }
}
