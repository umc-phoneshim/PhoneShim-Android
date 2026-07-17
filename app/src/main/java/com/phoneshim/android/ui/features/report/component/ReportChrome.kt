package com.phoneshim.android.ui.features.report.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.phoneshim.android.ui.common.PhoneShimIcon
import com.phoneshim.android.ui.common.PhoneShimIconType
import com.phoneshim.android.ui.theme.PhoneShimDimens
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimType

/**
 * "07. 데일리 리포트" 화면군(ReportSummaryScreen / TimetableScreen)이 공유하는
 * 상단바, 날짜 네비게이터, 탭. 하단 네비게이션 바는 여러 피처에서 공통으로 쓰여
 * [com.phoneshim.android.ui.common.PhoneShimBottomNavBar] 로 옮겼습니다.
 */

@Composable
fun DailyReportHeader(
    dateLabel: String,
    onPrevDate: () -> Unit,
    onNextDate: () -> Unit,
    modifier: Modifier = Modifier,
    onProfileClick: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = PhoneShimDimens.screenHorizontalPadding),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = PhoneShimDimens.spacing12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PhoneShimIcon(type = PhoneShimIconType.Target, contentDescription = null)
            Text(
                text = "DAILY REPORT",
                style = PhoneShimType.KorH3,
                color = PhoneShimTheme.colors.textPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
            PhoneShimIcon(
                type = PhoneShimIconType.Person,
                contentDescription = "마이페이지",
                modifier = Modifier.clickable(onClick = onProfileClick),
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = PhoneShimDimens.spacing16),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "‹",
                style = PhoneShimType.EngH2,
                color = PhoneShimTheme.colors.textTertiary,
                modifier = Modifier
                    .clickable(onClick = onPrevDate)
                    .padding(horizontal = PhoneShimDimens.spacing16),
            )
            Text(
                text = dateLabel,
                style = PhoneShimType.EngH2,
                color = PhoneShimTheme.colors.textPrimary,
            )
            Text(
                text = "›",
                style = PhoneShimType.EngH2,
                color = PhoneShimTheme.colors.textTertiary,
                modifier = Modifier
                    .clickable(onClick = onNextDate)
                    .padding(horizontal = PhoneShimDimens.spacing16),
            )
        }
    }
}

enum class ReportTab(val label: String) {
    SUMMARY("어플 사용 통계"),
    TIMETABLE("타임테이블"),
}

@Composable
fun ReportTabRow(
    selected: ReportTab,
    onTabSelected: (ReportTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = PhoneShimDimens.screenHorizontalPadding)
            .background(PhoneShimTheme.colors.surface, RoundedCornerShape(50))
            .border(1.dp, PhoneShimTheme.colors.border, RoundedCornerShape(50))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        ReportTab.values().forEach { tab ->
            val isSelected = tab == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTabSelected(tab) }
                    .background(
                        color = if (isSelected) PhoneShimTheme.colors.brand else Color.Transparent,
                        shape = RoundedCornerShape(50),
                    )
                    .padding(vertical = PhoneShimDimens.spacing12),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = tab.label,
                    style = PhoneShimType.KorBodyM,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) PhoneShimTheme.colors.onBrand else PhoneShimTheme.colors.textSecondary,
                )
            }
        }
    }
}
