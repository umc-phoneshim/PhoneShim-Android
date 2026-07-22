package com.phoneshim.android.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.phoneshim.android.ui.theme.PhoneShimDimens
import com.phoneshim.android.ui.theme.PhoneShimTheme

@Composable
fun AppInfoRow(
    appName: String,
    modifier: Modifier = Modifier,
    iconContent: (@Composable () -> Unit)? = null,
    supportingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    appNameStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    appNameColor: Color = PhoneShimTheme.colors.textPrimary,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing12),
    ) {
        iconContent?.invoke() ?: Box(
            modifier = Modifier
                .size(24.dp)
                .clip(MaterialTheme.shapes.extraSmall)
                .background(PhoneShimTheme.colors.divider),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = appName,
                style = appNameStyle,
                color = appNameColor,
                maxLines = 1,
            )
            supportingContent?.invoke()
        }
        trailingContent?.invoke()
    }
}

@Preview(showBackground = true)
@Composable
private fun AppInfoRowPreview() {
    PhoneShimTheme {
        AppInfoRow(
            appName = "카카오톡",
            supportingContent = {
                Text("목표 시간이 10분 미만입니다.", color = PhoneShimTheme.colors.error)
            },
            trailingContent = { Text("1시간 30분") },
        )
    }
}
