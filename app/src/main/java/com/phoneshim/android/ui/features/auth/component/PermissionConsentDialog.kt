package com.phoneshim.android.ui.features.auth.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.phoneshim.android.ui.theme.PhoneShimDimens
import com.phoneshim.android.ui.theme.PhoneShimPalette
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimType

@Composable
fun PermissionConsentDialog(
    onAllowAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
        ),
    ) {
        Column(
            modifier = modifier
                .width(328.dp)
                .background(PhoneShimPalette.White, RoundedCornerShape(8.dp))
                .padding(PhoneShimDimens.spacing24),
            verticalArrangement = Arrangement.spacedBy(64.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
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
            PrimaryAction(
                text = "모두 허용하기",
                onClick = onAllowAll,
                modifier = Modifier.height(42.dp),
                cornerRadius = 8.dp,
                textStyle = PhoneShimType.KorBodyM,
            )
        }
    }
}

@Composable
private fun ConsentSection(
    title: String,
    description: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing12)) {
        Text(
            text = title,
            color = PhoneShimPalette.Gray900,
            style = PhoneShimType.KorBodyM,
        )
        Text(
            text = description,
            modifier = Modifier
                .fillMaxWidth()
                .background(PhoneShimPalette.Primary100, RoundedCornerShape(8.dp))
                .padding(horizontal = PhoneShimDimens.spacing12, vertical = 10.dp),
            color = PhoneShimPalette.Gray900,
            style = PhoneShimType.KorLabel,
        )
    }
}

@Composable
private fun PrimaryAction(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = PhoneShimDimens.spacing12,
    textStyle: TextStyle = PhoneShimType.KorH3,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(PhoneShimPalette.Primary500, RoundedCornerShape(cornerRadius))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = PhoneShimDimens.spacing16),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = PhoneShimPalette.White,
            style = textStyle,
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
