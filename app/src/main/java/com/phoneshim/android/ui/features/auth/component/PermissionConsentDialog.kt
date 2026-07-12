package com.phoneshim.android.ui.features.auth.component

import androidx.annotation.StringRes
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.phoneshim.android.R
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
                    title = R.string.permission_personal_data_title,
                    description = R.string.permission_personal_data_description,
                )
                ConsentSection(
                    title = R.string.permission_usage_data_title,
                    description = R.string.permission_usage_data_description,
                )
            }
            PrimaryAction(
                text = R.string.permission_allow_all,
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
    @StringRes title: Int,
    @StringRes description: Int,
) {
    Column(verticalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing12)) {
        Text(
            text = stringResource(title),
            color = PhoneShimPalette.Gray900,
            style = PhoneShimType.KorBodyM,
        )
        Text(
            text = stringResource(description),
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
    @StringRes text: Int,
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
            text = stringResource(text),
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
