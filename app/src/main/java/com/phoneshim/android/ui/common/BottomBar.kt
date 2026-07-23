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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import com.phoneshim.android.ui.theme.PhoneShimType
import com.phoneshim.android.ui.theme.PhoneShimTheme

/**
 * 폰쉼 플로팅 하단 탭 바.
 *
 * Scaffold의 bottomBar 슬롯용이 아니라 화면 콘텐츠 위에 떠 있는 플로팅 바이다.
 * 사용 예시:
 * ```
 * Box {
 *     // 화면 콘텐츠
 *     BottomBar(
 *         selectedTab = selectedTab,
 *         onTabSelected = { tab -> ... },
 *         modifier = Modifier
 *             .align(Alignment.BottomCenter)
 *             .padding(start = 16.dp, end = 16.dp, bottom = 24.dp)
 *     )
 * }
 * ```
 */
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
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp)
                // NOTE: 피그마 box-shadow 0 4 4 rgba(0,0,0,0.25). Compose shadow와 1:1 매핑 불가, 실기기 확인 후 조정
            )
            .clip(RoundedCornerShape(12.dp))
            .background(PhoneShimTheme.colors.surface)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(36.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomBarTab.entries.forEach { tab ->
                val isSelected = selectedTab == tab
                val tint = if (isSelected) PhoneShimTheme.colors.brandStrong else PhoneShimTheme.colors.brand

                Column(
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onTabSelected(tab) }
                    ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = tab.iconRes),
                        contentDescription = tab.label,
                        tint = tint,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = tab.label,
                        style = PhoneShimType.KorLabel,
                        color = tint
                    )
                }
            }
        }
    }
}
