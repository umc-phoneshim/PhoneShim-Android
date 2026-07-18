package com.phoneshim.android.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.TextStyle
import com.phoneshim.android.R
import com.phoneshim.android.ui.theme.PhoneShimDimens
import com.phoneshim.android.ui.theme.PhoneShimPalette
import com.phoneshim.android.ui.theme.PhoneShimTheme

@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    containerColor: Color = PhoneShimTheme.colors.surface,
    borderColor: Color = PhoneShimPalette.Primary300,
    contentPadding: PaddingValues = PaddingValues(PhoneShimDimens.spacing16),
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .clip(shape)
            .background(containerColor)
            .border(1.dp, borderColor, shape)
            .padding(contentPadding),
        content = content,
    )
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    titleStyle: TextStyle = MaterialTheme.typography.titleSmall,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing4),
    ) {
        leadingContent?.invoke() ?: Icon(
            painter = painterResource(R.drawable.ic_section_indicator),
            contentDescription = null,
            tint = PhoneShimTheme.colors.textPrimary,
            modifier = Modifier.size(width = 8.dp, height = 10.dp),
        )
        Text(
            text = title,
            style = titleStyle,
            color = PhoneShimTheme.colors.textPrimary,
            modifier = Modifier.weight(1f),
        )
        trailingContent?.invoke()
    }
}

@Preview(showBackground = true)
@Composable
private fun SectionComponentsPreview() {
    PhoneShimTheme {
        SectionCard {
            SectionHeader(title = "오늘의 목표")
            Text("긴 콘텐츠도 카드 슬롯에서 화면별로 구성합니다.")
        }
    }
}
