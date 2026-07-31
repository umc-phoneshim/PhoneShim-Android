package com.phoneshim.android.ui.features.report.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.phoneshim.android.R
import com.phoneshim.android.ui.common.CalendarGrid
import com.phoneshim.android.ui.common.DateNavigator
import com.phoneshim.android.ui.theme.PhoneShimDimens
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimType
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

/**
 * "07. 데일리 리포트" 화면군(ReportSummaryScreen / TimetableScreen)이 공유하는
 * 상단바, 날짜 네비게이터, 탭. 하단 네비게이션 바는 여러 피처에서 공통으로 쓰여
 * 공통 [com.phoneshim.android.ui.common.BottomBar]로 옮겼습니다.
 */

/**
 * 리포트 상단 날짜 네비게이터.
 * 오른쪽 달력 버튼을 누르면 [ReportDatePickerDialog] 로 원하는 날짜로 바로 이동합니다.
 */
@Composable
fun ReportDateNavigator(
    dateLabel: String,
    onPrevDate: () -> Unit,
    onNextDate: () -> Unit,
    modifier: Modifier = Modifier,
    nextEnabled: Boolean = true,
    onCalendarClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = PhoneShimDimens.spacing16),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DateNavigator(
            label = dateLabel,
            onPrevious = onPrevDate,
            onNext = onNextDate,
            nextEnabled = nextEnabled,
        )
        if (onCalendarClick != null) {
            Spacer(modifier = Modifier.width(PhoneShimDimens.spacing12))
            CalendarOpenButton(onClick = onCalendarClick)
        }
    }
}

@Composable
private fun CalendarOpenButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(32.dp)
            .clip(CircleShape)
            .border(1.dp, PhoneShimTheme.colors.brand, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        // TODO: 전용 달력 아이콘이 추가되면 교체하세요. 지금은 표 형태 아이콘으로 대체합니다.
        Icon(
            painter = painterResource(R.drawable.ic_timetable),
            contentDescription = "날짜 선택",
            tint = PhoneShimTheme.colors.brandStrong,
            modifier = Modifier.size(18.dp),
        )
    }
}

/**
 * 날짜 선택 팝업. 리마인더 화면과 동일한 공통 [CalendarGrid] 를 씁니다.
 *
 * @param maxDate 이 날짜 이후는 선택할 수 없습니다. 보통 오늘.
 */
@Composable
fun ReportDatePickerDialog(
    visibleMonth: YearMonth,
    selectedDate: LocalDate,
    todayDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDismiss: () -> Unit,
    maxDate: LocalDate = todayDate,
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(PhoneShimTheme.colors.surface)
                .padding(PhoneShimDimens.spacing16),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing8),
        ) {
            DateNavigator(
                label = visibleMonth.format(DateTimeFormatter.ofPattern("yyyy.MM")),
                onPrevious = onPreviousMonth,
                onNext = onNextMonth,
                nextEnabled = visibleMonth < YearMonth.from(maxDate),
                labelStyle = PhoneShimType.KorH3,
                labelColor = PhoneShimTheme.colors.brandStrong,
            )
            CalendarGrid(
                visibleMonth = visibleMonth,
                selectedDate = selectedDate,
                todayDate = todayDate,
                onDateSelected = { date ->
                    if (!date.isAfter(maxDate)) onDateSelected(date)
                },
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
