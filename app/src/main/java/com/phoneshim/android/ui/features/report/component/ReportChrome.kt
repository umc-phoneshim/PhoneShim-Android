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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.tooling.preview.Preview
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
 *
 * 날짜는 화면 가운데에 두고, 그 오른쪽(화면 우측 끝)에 달력 버튼과 알림 설정 버튼을 나란히 둡니다.
 * 왼쪽 여백과 오른쪽 버튼 묶음에 같은 weight 를 줘서 날짜가 가운데에 오면서도
 * 버튼과 겹치지 않도록 했습니다.
 * 알림 설정은 원래 타임테이블 사이드에 있었는데 두 화면 공통이라 상단으로 올렸습니다.
 *
 * @param showCalendarTooltip 달력 버튼 아래에 안내 툴팁을 띄울지. 첫 진입 화면에서만 사용합니다.
 */
@Composable
fun ReportDateNavigator(
    dateLabel: String,
    onPrevDate: () -> Unit,
    onNextDate: () -> Unit,
    modifier: Modifier = Modifier,
    nextEnabled: Boolean = true,
    onCalendarClick: (() -> Unit)? = null,
    onAlarmSettingsClick: (() -> Unit)? = null,
    showCalendarTooltip: Boolean = false,
    onTooltipDismiss: () -> Unit = {},
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = PhoneShimDimens.screenHorizontalPadding,
                    end = PhoneShimDimens.screenHorizontalPadding,
                    top = PhoneShimDimens.spacing16,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 왼쪽 여백과 오른쪽 버튼 묶음에 같은 weight 를 줘서 날짜가 화면 가운데에 오도록 합니다.
            Spacer(modifier = Modifier.weight(1f))

            DateNavigator(
                label = dateLabel,
                onPrevious = onPrevDate,
                onNext = onNextDate,
                nextEnabled = nextEnabled,
            )

            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(
                    space = PhoneShimDimens.spacing8,
                    alignment = Alignment.End,
                ),
            ) {
                if (onCalendarClick != null) {
                    CalendarOpenButton(onClick = onCalendarClick)
                }
                if (onAlarmSettingsClick != null) {
                    AlarmSettingsButton(onClick = onAlarmSettingsClick)
                }
            }
        }

        if (showCalendarTooltip && onCalendarClick != null) {
            CalendarTooltip(
                onDismiss = onTooltipDismiss,
                modifier = Modifier.padding(
                    start = PhoneShimDimens.screenHorizontalPadding,
                    end = PhoneShimDimens.screenHorizontalPadding,
                    top = PhoneShimDimens.spacing8,
                ),
            )
        }

        Spacer(modifier = Modifier.height(PhoneShimDimens.spacing16))
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

/** 상단 알림 설정 버튼. 아이콘 + 텍스트를 알약 형태로 묶습니다. */
@Composable
private fun AlarmSettingsButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(PhoneShimTheme.colors.brandSubtle)
            .clickable(onClick = onClick)
            .padding(horizontal = PhoneShimDimens.spacing12, vertical = PhoneShimDimens.spacing8),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing4),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_target_alarm),
            contentDescription = null,
            tint = PhoneShimTheme.colors.brandStrong,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = "알림 설정",
            style = PhoneShimType.KorCaption,
            color = PhoneShimTheme.colors.brandStrong,
        )
    }
}

/** 달력 버튼 사용법 안내 툴팁. 누르면 사라집니다. */
@Composable
private fun CalendarTooltip(onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PhoneShimTheme.colors.brandSubtle)
            .clickable(onClick = onDismiss)
            .padding(horizontal = PhoneShimDimens.spacing12, vertical = PhoneShimDimens.spacing8),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing8),
    ) {
        Text(
            text = "달력을 눌러 다른 날 리포트를 볼 수 있어요.",
            style = PhoneShimType.KorCaption,
            color = PhoneShimTheme.colors.brandStrong,
            modifier = Modifier.weight(1f),
        )
        Icon(
            painter = painterResource(R.drawable.ic_close),
            contentDescription = "안내 닫기",
            tint = PhoneShimTheme.colors.brandStrong,
            modifier = Modifier.size(14.dp),
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

            Spacer(modifier = Modifier.height(PhoneShimDimens.spacing4))

            // 달력에서 목표 달성 여부를 표시하는 방식 안내.
            // TODO: 공통 CalendarGrid 에 날짜별 표시 슬롯이 생기면 문구 대신 실제 표시를 넣으세요.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(PhoneShimTheme.colors.brandSubtle)
                    .padding(PhoneShimDimens.spacing12),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing8),
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(PhoneShimTheme.colors.brand, CircleShape),
                )
                Text(
                    text = "목표를 달성한 날은 초록색으로 표시돼요.",
                    style = PhoneShimType.KorCaption,
                    color = PhoneShimTheme.colors.brandStrong,
                )
            }
        }
    }
}

@Preview(name = "날짜 + 달력 + 알림 설정", showBackground = true)
@Composable
private fun ReportDateNavigatorPreview() {
    PhoneShimTheme {
        ReportDateNavigator(
            dateLabel = "7.11",
            onPrevDate = {},
            onNextDate = {},
            onCalendarClick = {},
            onAlarmSettingsClick = {},
        )
    }
}

@Preview(name = "툴팁 노출 상태", showBackground = true)
@Composable
private fun ReportDateNavigatorTooltipPreview() {
    PhoneShimTheme {
        ReportDateNavigator(
            dateLabel = "7.11",
            onPrevDate = {},
            onNextDate = {},
            onCalendarClick = {},
            onAlarmSettingsClick = {},
            showCalendarTooltip = true,
        )
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
        ReportTab.entries.forEach { tab ->
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
