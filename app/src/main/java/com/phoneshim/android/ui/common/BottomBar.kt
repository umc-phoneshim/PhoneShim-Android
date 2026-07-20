package com.phoneshim.android.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimType

object BottomBarDefaults {
    val BarHeight = 56.dp
    val HorizontalPadding = 16.dp
    val VerticalPadding = 24.dp
    val ContentBottomPadding = BarHeight + (VerticalPadding * 2)

    internal val BarHorizontalPadding = 24.dp
    internal val TabSize = 48.dp
    internal val TabSpacing = 36.dp
    internal val IconSize = 20.dp
    internal val IconLabelSpacing = 4.dp
    internal val Shape = RoundedCornerShape(12.dp)
}

@Composable
fun BottomBar(
    selectedTab: BottomBarTab,
    onTabSelected: (BottomBarTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(
                horizontal = BottomBarDefaults.HorizontalPadding,
                vertical = BottomBarDefaults.VerticalPadding,
            ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(BottomBarDefaults.BarHeight)
                .shadow(4.dp, BottomBarDefaults.Shape)
                .clip(BottomBarDefaults.Shape)
                .background(PhoneShimTheme.colors.surface)
                .padding(horizontal = BottomBarDefaults.BarHorizontalPadding),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                horizontalArrangement = Arrangement.spacedBy(
                    space = BottomBarDefaults.TabSpacing,
                    alignment = Alignment.CenterHorizontally,
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BottomBarTab.entries.forEach { tab ->
                    val selected = selectedTab == tab
                    val tint = if (selected) {
                        PhoneShimTheme.colors.brandStrong
                    } else {
                        PhoneShimTheme.colors.brand
                    }
                    Column(
                        modifier = Modifier
                            .size(BottomBarDefaults.TabSize)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onTabSelected(tab) },
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            painter = painterResource(tab.iconRes),
                            contentDescription = tab.label,
                            tint = tint,
                            modifier = Modifier.size(BottomBarDefaults.IconSize),
                        )
                        Spacer(Modifier.height(BottomBarDefaults.IconLabelSpacing))
                        Text(text = tab.label, style = PhoneShimType.KorLabel, color = tint)
                    }
                }
            }
        }
    }
}

@Preview(widthDp = 360, heightDp = 140, showBackground = true)
@Composable
private fun BottomBarPreview() {
    PhoneShimTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PhoneShimTheme.colors.background),
        ) {
            BottomBar(
                selectedTab = BottomBarTab.REMINDER,
                onTabSelected = {},
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}
