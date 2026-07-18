package com.phoneshim.android.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.phoneshim.android.ui.theme.PhoneShimPalette
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimType

enum class TodoRowVariant {
    Plain,
    Card,
}

@Composable
fun TodoRow(
    title: String,
    timeRange: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    variant: TodoRowVariant = TodoRowVariant.Plain,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    val shape = MaterialTheme.shapes.medium
    val containerModifier = when (variant) {
        TodoRowVariant.Plain -> modifier
        TodoRowVariant.Card -> modifier
            .clip(shape)
            .background(PhoneShimTheme.colors.surface)
            .border(1.dp, PhoneShimPalette.Primary300, shape)
    }
    val contentSpacing = when (variant) {
        TodoRowVariant.Plain -> 12.dp
        TodoRowVariant.Card -> 24.dp
    }
    Row(
        modifier = containerModifier
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(contentSpacing),
    ) {
        leadingContent?.invoke()
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = if (variant == TodoRowVariant.Card) PhoneShimType.KorBodyM else PhoneShimType.KorCaption,
                color = PhoneShimTheme.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = timeRange,
                style = PhoneShimType.KorCaption,
                color = PhoneShimTheme.colors.textTertiary,
            )
        }
        trailingContent?.invoke()
    }
}

@Preview(showBackground = true)
@Composable
private fun TodoRowPreview() {
    PhoneShimTheme {
        TodoRow(
            title = "독서하고 기록한 내용을 아주 길게 정리하기",
            timeRange = "19:00 ~ 20:00",
            variant = TodoRowVariant.Card,
        )
    }
}
