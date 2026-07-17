package com.phoneshim.android.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimType

@Composable
fun TopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    titleStyle: TextStyle = PhoneShimType.KorH3,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 4.dp),
    ) {
        navigationIcon?.let {
            Box(modifier = Modifier.align(Alignment.CenterStart)) { it() }
        }
        Text(
            text = title,
            style = titleStyle,
            color = PhoneShimTheme.colors.textPrimary,
            modifier = Modifier.align(Alignment.Center),
        )
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            verticalAlignment = Alignment.CenterVertically,
            content = actions,
        )
    }
}
