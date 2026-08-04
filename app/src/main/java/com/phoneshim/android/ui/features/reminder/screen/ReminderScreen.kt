package com.phoneshim.android.ui.features.reminder.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.phoneshim.android.R
import com.phoneshim.android.ui.common.BottomBar
import com.phoneshim.android.ui.common.BottomBarDefaults
import com.phoneshim.android.ui.common.BottomBarTab
import com.phoneshim.android.ui.common.TopAppBar
import com.phoneshim.android.ui.features.reminder.component.ReminderCalendar
import com.phoneshim.android.ui.features.reminder.component.ReminderDateHeader
import com.phoneshim.android.ui.features.reminder.component.ReminderSetPopup
import com.phoneshim.android.ui.features.reminder.component.ReminderTaskSection
import com.phoneshim.android.ui.features.reminder.viewmodel.ReminderTaskUiModel
import com.phoneshim.android.ui.features.reminder.viewmodel.ReminderUiState
import com.phoneshim.android.ui.features.reminder.viewmodel.RestrictionMode
import com.phoneshim.android.ui.theme.PhoneShimDimens
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimType
import java.time.LocalDate
import java.time.YearMonth

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
    onRetry: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onNavigateToMain: () -> Unit = {},
    onNavigateToReport: () -> Unit = {},
    onMoveTask: (Int, Int) -> Unit = { _, _ -> },
    onDismissPopup: () -> Unit = {},
    onTitleChange: (String) -> Unit = {},
    onStartTimeChange: (String) -> Unit = {},
    onEndTimeChange: (String) -> Unit = {},
    onRestrictionModeChange: (RestrictionMode) -> Unit = {},
    onToggleRestrictedApp: (String) -> Unit = {},
    onSaveTask: () -> Unit = {},
    onDeleteTask: () -> Unit = {},
) {
    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = PhoneShimTheme.colors.background,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = "REMINDER",
                    titleStyle = PhoneShimType.KorH3,
                    leadingAction = {
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(painterResource(R.drawable.ic_topbar_goal), "목표 설정")
                        }
                    },
                    trailingAction = {
                        IconButton(onClick = onNavigateToMyPage) {
                            Icon(painterResource(R.drawable.ic_my), "마이페이지")
                        }
                    },
                )
            },
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding).navigationBarsPadding(),
                contentPadding = PaddingValues(
                    start = PhoneShimDimens.screenHorizontalPadding,
                    top = 16.dp,
                    end = PhoneShimDimens.screenHorizontalPadding,
                    bottom = BottomBarDefaults.ContentBottomPadding,
                ),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                item { ReminderDateHeader(state.selectedDate) }
                item {
                    ReminderCalendar(
                        state.visibleMonth,
                        state.todayDate,
                        state.selectedDate,
                        onSelectDate,
                        onPreviousMonth,
                        onNextMonth,
                    )
                }
                item {
                    ReminderTaskSection(
                        tasks = state.selectedTasks,
                        isLoading = state.isLoading,
                        errorMessage = state.loadErrorMessage,
                        onAddTask = onAddTask,
                        onEditTask = onEditTask,
                        onMoveTask = onMoveTask,
                        onRetry = onRetry,
                    )
                }
            }
        }
        BottomBar(
            selectedTab = BottomBarTab.REMINDER,
            onTabSelected = { tab ->
                when (tab) {
                    BottomBarTab.MAIN -> onNavigateToMain()
                    BottomBarTab.REMINDER -> Unit
                    BottomBarTab.REPORT -> onNavigateToReport()
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }

    if (state.isTaskPopupVisible) {
        ReminderSetPopup(
            selectedDate = state.selectedDate,
            todayDate = state.todayDate,
            draft = state.draft,
            onDismiss = onDismissPopup,
            onTitleChange = onTitleChange,
            onStartTimeChange = onStartTimeChange,
            onEndTimeChange = onEndTimeChange,
            onRestrictionModeChange = onRestrictionModeChange,
            onToggleApp = onToggleRestrictedApp,
            onSave = onSaveTask,
            onDelete = onDeleteTask,
            isSubmitting = state.isSubmitting,
        )
    }
}

private fun previewState(tasks: Boolean = true) =
    ReminderUiState(
        todayDate = LocalDate.of(2026, 7, 11),
        selectedDate = LocalDate.of(2026, 7, 17),
        tasksByDate = if (tasks) previewTasks() else emptyMap(),
    )

private fun previewTasks(): Map<LocalDate, List<ReminderTaskUiModel>> {
    val date = LocalDate.of(2026, 7, 17)
    return mapOf(
        date to listOf(
            ReminderTaskUiModel("mock-1", date, "과제하기", 600, 660),
            ReminderTaskUiModel("mock-2", date, "운동하기", 780, 840),
        ),
    )
}

@Preview(name = "리마인더 초기 설정 전", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun EmptyPreview() = PhoneShimTheme { ReminderScreen(previewState(false), {}, {}, {}, {}, {}, {}, {}) }

@Preview(name = "리마인더 설정 후", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun FilledPreview() = PhoneShimTheme { ReminderScreen(previewState(), {}, {}, {}, {}, {}, {}, {}) }

@Preview(name = "다른 달 날짜 포함 달력", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun AdjacentMonthPreview() = PhoneShimTheme {
    ReminderScreen(previewState(false).copy(visibleMonth = YearMonth.of(2026, 7)), {}, {}, {}, {}, {}, {}, {})
}

@Preview(name = "긴 할 일 이름 ellipsis", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun LongTitlePreview() {
    val date = LocalDate.of(2026, 7, 17)
    val task = ReminderTaskUiModel("long", date, "아주 길어서 한 줄을 넘어가는 할 일 이름입니다", 600, 660)
    PhoneShimTheme {
        ReminderScreen(previewState().copy(tasksByDate = mapOf(date to listOf(task))), {}, {}, {}, {}, {}, {}, {})
    }
}
