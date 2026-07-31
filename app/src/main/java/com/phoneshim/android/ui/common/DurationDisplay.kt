package com.phoneshim.android.ui.common

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimType

@Composable
fun DurationDisplay(
    totalMinutes: Int,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = PhoneShimType.EngH1,
) {
    val safeMinutes = totalMinutes.coerceAtLeast(0)
    val hours = safeMinutes / 60
    val minutes = safeMinutes % 60

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = String.format("%02d", hours),
            style = textStyle,
            color = PhoneShimTheme.colors.textPrimary,
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "시간",
            style = PhoneShimType.KorBodyM,
            color = PhoneShimTheme.colors.textPrimary,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = String.format("%02d", minutes),
            style = textStyle,
            color = PhoneShimTheme.colors.textPrimary,
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "분",
            style = PhoneShimType.KorBodyM,
            color = PhoneShimTheme.colors.textPrimary,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DurationDisplayPreview() {
    PhoneShimTheme {
        DurationDisplay(totalMinutes = 210)
    }
}
