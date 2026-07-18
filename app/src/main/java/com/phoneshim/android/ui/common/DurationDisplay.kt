package com.phoneshim.android.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimType

enum class DurationDisplayVariant {
    Default,
    Compact,
}

@Composable
fun DurationDisplay(
    totalMinutes: Int,
    modifier: Modifier = Modifier,
    variant: DurationDisplayVariant = DurationDisplayVariant.Default,
    label: (@Composable () -> Unit)? = null,
) {
    val safeMinutes = totalMinutes.coerceAtLeast(0)
    val hours = safeMinutes / 60
    val minutes = safeMinutes % 60
    if (variant == DurationDisplayVariant.Compact) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            label?.invoke()
            Text(
                text = "${hours}시간  ${minutes}분",
                style = PhoneShimType.EngH1,
                color = PhoneShimTheme.colors.textPrimary,
            )
        }
    } else {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            label?.invoke()
            Text(
                text = hours.toString(),
                style = PhoneShimType.KorH3.copy(fontSize = 24.sp),
                fontWeight = FontWeight.Bold,
                color = PhoneShimTheme.colors.textPrimary,
            )
            Text("시간", style = PhoneShimType.KorH3, color = PhoneShimTheme.colors.textPrimary)
            Spacer(Modifier.width(8.dp))
            Text(
                text = minutes.toString(),
                style = PhoneShimType.KorH3.copy(fontSize = 24.sp),
                fontWeight = FontWeight.Bold,
                color = PhoneShimTheme.colors.textPrimary,
            )
            Text("분", style = PhoneShimType.KorH3, color = PhoneShimTheme.colors.textPrimary)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DurationDisplayPreview() {
    PhoneShimTheme {
        androidx.compose.foundation.layout.Column {
            DurationDisplay(totalMinutes = 90)
            DurationDisplay(totalMinutes = 90, variant = DurationDisplayVariant.Compact)
        }
    }
}
