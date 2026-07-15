package com.phoneshim.android.ui.features.reminder.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.phoneshim.android.R
import com.phoneshim.android.ui.features.reminder.component.ReminderSetPopup
import com.phoneshim.android.ui.features.reminder.viewmodel.ReminderTaskUiModel
import com.phoneshim.android.ui.features.reminder.viewmodel.ReminderUiState
import com.phoneshim.android.ui.features.reminder.viewmodel.ReminderViewModel
import com.phoneshim.android.ui.features.reminder.viewmodel.formatMinutes
import com.phoneshim.android.ui.theme.PhoneShimDimens
import com.phoneshim.android.ui.theme.PhoneShimPalette
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimType
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

private val ScreenHorizontalPadding = PhoneShimDimens.screenHorizontalPadding
private val CalendarCornerRadius = 12.dp
private val CalendarDayCellSize = 42.dp
private val TaskRowHeight = 56.dp
private val TopBarHeight = 49.dp
private val MainSectionSpacing = 24.dp

@Composable
fun ReminderRoute(
    onNavigateToSettings: () -> Unit,
    onNavigateToMyPage: () -> Unit,
    onNavigateToMain: () -> Unit = {},
    onNavigateToReminder: () -> Unit = {},
    onNavigateToReport: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ReminderViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }
    ReminderScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onNavigateToSettings = onNavigateToSettings,
        onNavigateToMyPage = onNavigateToMyPage,
        onNavigateToMain = onNavigateToMain,
        onNavigateToReminder = onNavigateToReminder,
        onNavigateToReport = onNavigateToReport,
        onSelectDate = viewModel::selectDate,
        onPreviousMonth = { viewModel.moveMonth(-1) },
        onNextMonth = { viewModel.moveMonth(1) },
        onAddTask = viewModel::openAddPopup,
        onEditTask = viewModel::openEditPopup,
        onDismissPopup = viewModel::dismissPopup,
        onTitleChange = viewModel::updateTitle,
        onStartTimeChange = viewModel::updateStartTime,
        onEndTimeChange = viewModel::updateEndTime,
        onRestrictionModeChange = viewModel::updateRestrictionMode,
        onToggleRestrictedApp = viewModel::toggleRestrictedApp,
        onSaveTask = viewModel::saveTask,
        onDeleteTask = viewModel::deleteTask,
        modifier = modifier,
    )
}

@Composable
fun ReminderScreen(
    state: ReminderUiState,
    onNavigateToSettings: () -> Unit,
    onNavigateToMyPage: () -> Unit,
    onSelectDate: (LocalDate) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onAddTask: () -> Unit,
    onEditTask: (ReminderTaskUiModel) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onNavigateToMain: () -> Unit = {},
    onNavigateToReminder: () -> Unit = {},
    onNavigateToReport: () -> Unit = {},
    onDismissPopup: () -> Unit = {},
    onTitleChange: (String) -> Unit = {},
    onStartTimeChange: (String) -> Unit = {},
    onEndTimeChange: (String) -> Unit = {},
    onRestrictionModeChange: (com.phoneshim.android.ui.features.reminder.viewmodel.RestrictionMode) -> Unit = {},
    onToggleRestrictedApp: (String) -> Unit = {},
    onSaveTask: () -> Unit = {},
    onDeleteTask: () -> Unit = {},
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = PhoneShimTheme.colors.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { ReminderTopBar(onNavigateToSettings, onNavigateToMyPage) },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = ScreenHorizontalPadding,
                top = 16.dp,
                end = ScreenHorizontalPadding,
                bottom = 80.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(MainSectionSpacing),
        ) {
            item { ReminderDateHeader(state.todayDate) }
            item {
                ReminderCalendar(
                    visibleMonth = state.visibleMonth,
                    todayDate = state.todayDate,
                    selectedDate = state.selectedDate,
                    onDateSelected = onSelectDate,
                    onPreviousMonth = onPreviousMonth,
                    onNextMonth = onNextMonth,
                )
            }
            item {
                ReminderTaskSection(
                    tasks = state.selectedTasks,
                    onAddTask = onAddTask,
                    onEditTask = onEditTask,
                )
            }
        }
    }
    if (state.isTaskPopupVisible) {
        ReminderSetPopup(
            selectedDate = state.selectedDate,
            draft = state.draft,
            onDismiss = onDismissPopup,
            onTitleChange = onTitleChange,
            onStartTimeChange = onStartTimeChange,
            onEndTimeChange = onEndTimeChange,
            onRestrictionModeChange = onRestrictionModeChange,
            onToggleApp = onToggleRestrictedApp,
            onSave = onSaveTask,
            onDelete = onDeleteTask,
        )
    }
}

@Composable
private fun ReminderTopBar(onSettings: () -> Unit, onMyPage: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().height(TopBarHeight).padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        IconButton(onClick = onSettings, modifier = Modifier.align(Alignment.CenterStart).size(40.dp)) {
            Icon(painterResource(R.drawable.ic_reminder_settings), null, tint = PhoneShimTheme.colors.textPrimary, modifier = Modifier.size(24.dp))
        }
        Text("REMINDER", style = PhoneShimType.KorH3, color = PhoneShimTheme.colors.textPrimary)
        IconButton(onClick = onMyPage, modifier = Modifier.align(Alignment.CenterEnd).size(40.dp)) {
            Icon(painterResource(R.drawable.ic_reminder_profile), null, tint = PhoneShimTheme.colors.textPrimary, modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
private fun ReminderDateHeader(todayDate: LocalDate) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        Box(Modifier.width(50.dp).height(21.dp).clip(CircleShape).background(PhoneShimTheme.colors.brandStrong), contentAlignment = Alignment.Center) {
            Text("Today", style = PhoneShimType.EngLabel, color = PhoneShimTheme.colors.onBrand)
        }
        Text("${todayDate.monthValue}.${todayDate.dayOfMonth}", style = PhoneShimType.EngBodyM, color = PhoneShimTheme.colors.textPrimary)
    }
}

@Composable
private fun ReminderCalendar(
    visibleMonth: YearMonth,
    todayDate: LocalDate,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(CalendarCornerRadius)).background(PhoneShimTheme.colors.surface).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.height(42.dp)) {
            CalendarArrow(R.drawable.ic_reminder_chevron_left, onPreviousMonth)
            Text(visibleMonth.format(DateTimeFormatter.ofPattern("yyyy.MM")), style = PhoneShimType.KorH3, color = PhoneShimTheme.colors.brandStrong)
            CalendarArrow(R.drawable.ic_reminder_chevron_right, onNextMonth)
        }
        CalendarWeekHeader()
        calendarDates(visibleMonth).chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                week.forEach { date -> CalendarDay(date, visibleMonth, todayDate, selectedDate, onDateSelected) }
            }
        }
    }
}

@Composable
private fun CalendarArrow(icon: Int, onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.size(40.dp)) {
        Icon(painterResource(icon), null, tint = Color.Unspecified, modifier = Modifier.size(24.dp))
    }
}

@Composable
private fun CalendarWeekHeader() {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        listOf("M", "T", "W", "T", "F", "S", "S").forEachIndexed { index, label ->
            Text(
                label,
                modifier = Modifier.size(CalendarDayCellSize).padding(top = 10.dp),
                textAlign = TextAlign.Center,
                style = PhoneShimType.EngBodyM,
                color = when (index) { 5 -> Color(0xFF2B00FF); 6 -> Color.Red; else -> PhoneShimTheme.colors.textPrimary },
            )
        }
    }
}

@Composable
private fun CalendarDay(date: LocalDate, month: YearMonth, today: LocalDate, selected: LocalDate, onClick: (LocalDate) -> Unit) {
    val isSelected = date == selected
    val isToday = date == today
    val shape = CircleShape
    val foreground = when {
        isSelected -> PhoneShimTheme.colors.onBrand
        YearMonth.from(date) != month -> PhoneShimPalette.Gray300
        date.dayOfWeek == DayOfWeek.SATURDAY -> Color(0xFF2B00FF)
        date.dayOfWeek == DayOfWeek.SUNDAY -> Color.Red
        else -> PhoneShimTheme.colors.textPrimary
    }
    Box(Modifier.size(CalendarDayCellSize).clickable { onClick(date) }, contentAlignment = Alignment.Center) {
        Box(
            Modifier.size(40.dp).clip(shape)
                .then(if (isToday && !isSelected) Modifier.border(1.dp, PhoneShimTheme.colors.brandStrong, shape) else Modifier)
                .background(if (isSelected) PhoneShimTheme.colors.brand else Color.Transparent),
            contentAlignment = Alignment.Center,
        ) { Text(date.dayOfMonth.toString(), style = PhoneShimType.EngBodyM, color = foreground) }
    }
}

@Composable
private fun ReminderTaskSection(tasks: List<ReminderTaskUiModel>, onAddTask: () -> Unit, onEditTask: (ReminderTaskUiModel) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(painterResource(R.drawable.ic_reminder_goal), null, tint = Color.Unspecified, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(4.dp))
            // TODO: 선택 날짜에 따라 섹션 제목을 변경할지 기획 확인 필요
            Text("오늘 할 일 설정", style = PhoneShimType.KorBodyM.copy(fontWeight = FontWeight.SemiBold), color = PhoneShimTheme.colors.textPrimary)
        }
        if (tasks.isNotEmpty()) {
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(PhoneShimTheme.colors.surface).padding(12.dp)) {
                tasks.forEachIndexed { index, task ->
                    ReminderTaskItem(task, onEditTask)
                    if (index != tasks.lastIndex) {
                        Divider(color = PhoneShimTheme.colors.divider)
                        Spacer(Modifier.height(11.dp))
                    }
                }
            }
        }
        ReminderEmptyTaskCard(compact = tasks.isNotEmpty(), onClick = onAddTask)
    }
}

@Composable
private fun ReminderTaskItem(task: ReminderTaskUiModel, onEditTask: (ReminderTaskUiModel) -> Unit) {
    Row(Modifier.fillMaxWidth().height(TaskRowHeight), verticalAlignment = Alignment.CenterVertically) {
        // TODO: 드래그 핸들의 실제 정렬 기능 여부를 기획 확인 후 결정
        Icon(painterResource(R.drawable.ic_reminder_drag_handle), null, tint = Color.Unspecified, modifier = Modifier.size(24.dp).padding(horizontal = 4.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(task.title, style = PhoneShimType.KorCaption, color = PhoneShimTheme.colors.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("${formatMinutes(task.startMinutes)} - ${formatMinutes(task.endMinutes)}", style = PhoneShimType.EngLabel, color = PhoneShimTheme.colors.textTertiary)
        }
        IconButton(onClick = { onEditTask(task) }, modifier = Modifier.size(40.dp)) {
            Icon(painterResource(R.drawable.ic_reminder_edit), null, tint = Color.Unspecified, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun ReminderEmptyTaskCard(compact: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().height(if (compact) 48.dp else 72.dp).clip(RoundedCornerShape(12.dp))
            .background(PhoneShimTheme.colors.brandSubtle).clickable(onClick = onClick).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(painterResource(R.drawable.ic_reminder_add), null, tint = Color.Unspecified, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Text("할 일을 추가하세요", style = PhoneShimType.KorCaption, color = PhoneShimTheme.colors.brand)
    }
}

private fun calendarDates(month: YearMonth): List<LocalDate> {
    val first = month.atDay(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val last = month.atEndOfMonth().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
    return List((last.toEpochDay() - first.toEpochDay() + 1).toInt()) { first.plusDays(it.toLong()) }
}

private fun previewState(tasks: Boolean = true) = ReminderUiState(tasksByDate = if (tasks) ReminderUiState().tasksByDate else emptyMap())

@Preview(name = "리마인더 초기 설정 전", widthDp = 360, heightDp = 800, showBackground = true)
@Composable private fun EmptyPreview() = PhoneShimTheme { ReminderScreen(previewState(false), {}, {}, {}, {}, {}, {}, {}) }

@Preview(name = "리마인더 설정 후", widthDp = 360, heightDp = 800, showBackground = true)
@Composable private fun FilledPreview() = PhoneShimTheme { ReminderScreen(previewState(), {}, {}, {}, {}, {}, {}, {}) }

@Preview(name = "오늘과 선택 날짜 다름", widthDp = 360, heightDp = 800, showBackground = true)
@Composable private fun DifferentDatePreview() = PhoneShimTheme { ReminderScreen(previewState(), {}, {}, {}, {}, {}, {}, {}) }

@Preview(name = "다른 달 날짜 포함 달력", widthDp = 360, heightDp = 800, showBackground = true)
@Composable private fun AdjacentMonthPreview() = PhoneShimTheme { ReminderScreen(previewState(false).copy(visibleMonth = YearMonth.of(2026, 7)), {}, {}, {}, {}, {}, {}, {}) }

@Preview(name = "5주 달력", widthDp = 360, heightDp = 800, showBackground = true)
@Composable private fun FiveWeekCalendarPreview() = PhoneShimTheme { ReminderScreen(previewState(false).copy(visibleMonth = YearMonth.of(2026, 7)), {}, {}, {}, {}, {}, {}, {}) }

@Preview(name = "6주 달력", widthDp = 360, heightDp = 800, showBackground = true)
@Composable private fun SixWeekCalendarPreview() = PhoneShimTheme { ReminderScreen(previewState(false).copy(visibleMonth = YearMonth.of(2026, 8)), {}, {}, {}, {}, {}, {}, {}) }

@Preview(name = "긴 할 일 이름 ellipsis", widthDp = 360, heightDp = 800, showBackground = true)
@Composable private fun LongTitlePreview() {
    val date = LocalDate.of(2026, 7, 17)
    val task = ReminderTaskUiModel("long", date, "아주 길어서 한 줄을 넘어가는 할 일 이름입니다", 600, 660)
    PhoneShimTheme { ReminderScreen(previewState().copy(tasksByDate = mapOf(date to listOf(task))), {}, {}, {}, {}, {}, {}, {}) }
}
