package com.phoneshim.android.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimType

object TopAppBarDefaults {
    val Height = 48.dp
    val HorizontalPadding = 16.dp
    val IconTouchTargetSize = 48.dp
    internal val IconEdgeOffset = 12.dp
}

@Composable
fun TopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    titleStyle: TextStyle = PhoneShimType.KorH3,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable () -> Unit = {},
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(TopAppBarDefaults.Height)
            .padding(horizontal = TopAppBarDefaults.HorizontalPadding),
    ) {
        navigationIcon?.let {
            Box(
                modifier = Modifier
                    .size(TopAppBarDefaults.IconTouchTargetSize)
                    .offset(x = -TopAppBarDefaults.IconEdgeOffset)
                    .align(Alignment.CenterStart),
                contentAlignment = Alignment.Center,
            ) {
                it()
            }
        }
        Text(
            text = title,
            style = titleStyle,
            color = PhoneShimTheme.colors.textPrimary,
            modifier = Modifier.align(Alignment.Center),
        )
        Box(
            modifier = Modifier
                .size(TopAppBarDefaults.IconTouchTargetSize)
                .offset(x = TopAppBarDefaults.IconEdgeOffset)
                .align(Alignment.CenterEnd),
            contentAlignment = Alignment.Center,
        ) {
            actions()
        }
    }
}
