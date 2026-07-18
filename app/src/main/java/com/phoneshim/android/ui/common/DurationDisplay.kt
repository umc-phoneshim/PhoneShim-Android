package com.phoneshim.android.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimType

@Composable
fun DurationDisplay(
    totalMinutes: Int,
    modifier: Modifier = Modifier,
) {
    val safeMinutes = totalMinutes.coerceAtLeast(0)
    val hours = safeMinutes / 60
    val minutes = safeMinutes % 60

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = hours.toString(),
                style = PhoneShimType.EngH1,
                color = PhoneShimTheme.colors.textPrimary,
            )
            Text(
                text = "시간",
                style = PhoneShimType.KorH3,
                color = PhoneShimTheme.colors.textPrimary,
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = minutes.toString(),
                style = PhoneShimType.EngH1,
                color = PhoneShimTheme.colors.textPrimary,
            )
            Text(
                text = "분",
                style = PhoneShimType.KorH3,
                color = PhoneShimTheme.colors.textPrimary,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DurationDisplayPreview() {
    PhoneShimTheme {
        DurationDisplay(totalMinutes = 210)
    }
}
