package com.phoneshim.android.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.dp
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimType

@Composable
fun BottomBar(
    selectedTab: BottomBarTab,
    onTabSelected: (BottomBarTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(PhoneShimTheme.colors.surface)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(36.dp),
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
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onTabSelected(tab) },
                    ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        painter = painterResource(tab.iconRes),
                        contentDescription = tab.label,
                        tint = tint,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(text = tab.label, style = PhoneShimType.KorLabel, color = tint)
                }
            }
        }
    }
}
