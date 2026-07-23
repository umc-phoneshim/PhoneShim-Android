package com.phoneshim.android.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.phoneshim.android.ui.theme.PhoneShimDimens
import com.phoneshim.android.ui.theme.PhoneShimTheme

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    titleStyle: TextStyle = MaterialTheme.typography.titleSmall,
) {
    val textPrimary = PhoneShimTheme.colors.textPrimary

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing4),
    ) {
        leadingContent?.invoke() ?: Canvas(modifier = Modifier.size(width = 8.dp, height = 10.dp)) {
            val triangle = Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width, size.height / 2f)
                lineTo(0f, size.height)
                close()
            }
            drawPath(path = triangle, color = textPrimary)
        }
        Text(
            text = title,
            style = titleStyle,
            color = textPrimary,
            modifier = Modifier.weight(1f),
        )
        trailingContent?.invoke()
    }
}
