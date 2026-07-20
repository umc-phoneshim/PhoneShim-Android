package com.phoneshim.android.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.phoneshim.android.R
import com.phoneshim.android.ui.theme.PhoneShimPalette
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimType
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

@Composable
fun DateNavigator(
    label: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
    previousEnabled: Boolean = true,
    nextEnabled: Boolean = true,
    labelStyle: TextStyle = PhoneShimType.EngH2,
    labelColor: Color = PhoneShimTheme.colors.textPrimary,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DateNavigationButton(
            iconRes = R.drawable.ic_chevron_left,
            contentDescription = "이전 날짜",
            enabled = previousEnabled,
            onClick = onPrevious,
        )
        Text(text = label, style = labelStyle, color = labelColor)
        DateNavigationButton(
            iconRes = R.drawable.ic_chevron_right,
            contentDescription = "다음 날짜",
            enabled = nextEnabled,
            onClick = onNext,
        )
    }
}

@Composable
private fun DateNavigationButton(
    iconRes: Int,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick, enabled = enabled, modifier = Modifier.size(40.dp)) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            tint = if (enabled) Color.Unspecified else PhoneShimPalette.Gray300,
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
fun CalendarGrid(
    visibleMonth: YearMonth,
    selectedDate: LocalDate,
    todayDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        CalendarWeekHeader()
        calendarDates(visibleMonth).chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                week.forEach { date ->
                    CalendarDayCell(
                        date = date,
                        visibleMonth = visibleMonth,
                        selected = date == selectedDate,
                        today = date == todayDate,
                        enabled = enabled,
                        onClick = { onDateSelected(date) },
                    )
                }
            }
        }
    }
}

@Composable
fun CalendarDayCell(
    date: LocalDate,
    visibleMonth: YearMonth,
    selected: Boolean,
    today: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val foreground = when {
        !enabled -> PhoneShimPalette.Gray300
        selected -> PhoneShimTheme.colors.onBrand
        YearMonth.from(date) != visibleMonth -> PhoneShimPalette.Gray300
        else -> PhoneShimTheme.colors.textPrimary
    }
    Box(
        modifier = modifier.size(44.dp).clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .then(
                    if (today && !selected) {
                        Modifier.border(1.dp, PhoneShimTheme.colors.brandStrong, CircleShape)
                    } else {
                        Modifier
                    },
                )
                .background(if (selected) PhoneShimTheme.colors.brand else Color.Transparent),
            contentAlignment = Alignment.Center,
        ) {
            Text(date.dayOfMonth.toString(), style = PhoneShimType.EngBodyM, color = foreground)
        }
    }
}

@Composable
private fun CalendarWeekHeader() {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        listOf("M", "T", "W", "T", "F", "S", "S").forEachIndexed { index, label ->
            Text(
                text = label,
                modifier = Modifier.size(44.dp).padding(top = 10.dp),
                textAlign = TextAlign.Center,
                style = PhoneShimType.EngBodyM,
                color = when (index) {
                    5 -> Color(0xFF2B00FF)
                    6 -> Color.Red
                    else -> PhoneShimTheme.colors.textPrimary
                },
            )
        }
    }
}

private fun calendarDates(month: YearMonth): List<LocalDate> {
    val first = month.atDay(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val last = month.atEndOfMonth().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
    val requiredCellCount = ChronoUnit.DAYS.between(first, last).toInt() + 1
    val cellCount = if (requiredCellCount > 35) 42 else 35
    return List(cellCount) { first.plusDays(it.toLong()) }
}

@Preview(name = "6주 달력", showBackground = true)
@Composable
private fun CalendarGridPreview() {
    val today = LocalDate.of(2026, 3, 1)
    PhoneShimTheme {
        Column {
            DateNavigator(
                label = YearMonth.from(today).format(DateTimeFormatter.ofPattern("yyyy.MM")),
                onPrevious = {},
                onNext = {},
                labelStyle = PhoneShimType.KorH3,
            )
            CalendarGrid(
                visibleMonth = YearMonth.from(today),
                selectedDate = today,
                todayDate = today,
                onDateSelected = {},
            )
            CalendarDayCell(
                date = today.plusDays(1),
                visibleMonth = YearMonth.from(today),
                selected = false,
                today = false,
                enabled = false,
                onClick = {},
            )
        }
    }
}
