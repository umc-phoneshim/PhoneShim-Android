package com.phoneshim.android.ui.features.reminder.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.phoneshim.android.ui.common.CalendarGrid
import com.phoneshim.android.ui.common.DateNavigator
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimType
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
internal fun ReminderDateHeader(selectedDate: LocalDate) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .width(50.dp)
                .height(24.dp)
                .clip(CircleShape)
                .background(PhoneShimTheme.colors.brandStrong),
            contentAlignment = Alignment.Center,
        ) {
            Text("Today", style = PhoneShimType.EngLabel, color = PhoneShimTheme.colors.onBrand)
        }
        Text(
            "${selectedDate.monthValue}.${selectedDate.dayOfMonth}",
            style = PhoneShimType.EngBodyM,
            color = PhoneShimTheme.colors.textPrimary,
        )
    }
}

@Composable
internal fun ReminderCalendar(
    visibleMonth: YearMonth,
    todayDate: LocalDate,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 337.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(PhoneShimTheme.colors.surface)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        DateNavigator(
            label = visibleMonth.format(DateTimeFormatter.ofPattern("yyyy.MM")),
            onPrevious = onPreviousMonth,
            onNext = onNextMonth,
            modifier = Modifier.height(42.dp),
            labelStyle = PhoneShimType.KorH3,
            labelColor = PhoneShimTheme.colors.brandStrong,
        )
        CalendarGrid(visibleMonth, selectedDate, todayDate, onDateSelected)
    }
}
