package com.phoneshim.android.ui.features.report.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.phoneshim.android.ui.common.BottomBar
import com.phoneshim.android.ui.common.BottomBarTab
import com.phoneshim.android.ui.common.BottomBarDefaults
import com.phoneshim.android.ui.common.PhoneShimBottomBarSnackbarHost
import com.phoneshim.android.ui.common.TopAppBar
import com.phoneshim.android.ui.common.showPhoneShimSnackbar
import com.phoneshim.android.ui.common.base.CollectCommonEffect
import com.phoneshim.android.R
import com.phoneshim.android.ui.features.report.component.AlarmSettingDialog
import com.phoneshim.android.ui.features.report.component.ReportDateNavigator
import com.phoneshim.android.ui.features.report.component.ReportDatePickerDialog
import com.phoneshim.android.ui.features.report.component.ReportTab
import com.phoneshim.android.ui.features.report.component.ReportTabRow
import com.phoneshim.android.ui.features.report.component.TimetableChart
import com.phoneshim.android.ui.features.report.component.ReportSideCardWidth
import com.phoneshim.android.ui.features.report.component.RestSuggestionCard
import com.phoneshim.android.ui.features.report.component.UsedAppsCard
import com.phoneshim.android.ui.features.report.viewmodel.ReportUiEffect
import com.phoneshim.android.ui.features.report.viewmodel.ReportUiEvent
import com.phoneshim.android.ui.features.report.viewmodel.ReportUiState
import com.phoneshim.android.ui.features.report.viewmodel.ReportViewModel
import com.phoneshim.android.ui.features.report.viewmodel.UsageReasonTarget
import com.phoneshim.android.ui.theme.PhoneShimDimens
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimType

/**
 * 07. 데일리 리포트 (타임테이블) 진입점.
 * ViewModel 주입과 이펙트 처리를 담당하고, UI 는 상태만 받는 [TimetableScreen] 이 그립니다.
 */
@Composable
fun TimetableRoute(
    onEntryClick: (UsageReasonTarget) -> Unit,
    onNavigateToAiSuggestion: () -> Unit,
    onAuthExpired: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToSummary: () -> Unit = {},
    onNavigateToMain: () -> Unit = {},
    onNavigateToReminder: () -> Unit = {},
    onNavigateToMyPage: () -> Unit = {},
    viewModel: ReportViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    CollectCommonEffect(viewModel, onAuthExpired)

    LaunchedEffect(viewModel) {
        viewModel.onEvent(ReportUiEvent.ScreenEntered(ReportTab.TIMETABLE))
    }
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ReportUiEffect.NavigateToUsageReasonInput -> onEntryClick(effect.target)
                ReportUiEffect.NavigateToRestSuggestion -> onNavigateToAiSuggestion()
                is ReportUiEffect.NavigateToTab ->
                    if (effect.tab == ReportTab.SUMMARY) onNavigateToSummary()
                is ReportUiEffect.ShowMessage -> snackbarHostState.showPhoneShimSnackbar(
                    message = effect.message,
                    type = effect.type,
                )
            }
        }
    }

    TimetableScreen(
        state = state,
        modifier = modifier,
        snackbarHostState = snackbarHostState,
        onNavigateToSettings = onNavigateToSettings,
        onNavigateToMyPage = onNavigateToMyPage,
        onPrevDate = { viewModel.onEvent(ReportUiEvent.PreviousDateClicked) },
        onNextDate = { viewModel.onEvent(ReportUiEvent.NextDateClicked) },
        onCalendarClick = { viewModel.onEvent(ReportUiEvent.DatePickerOpened) },
        onDatePickerDismiss = { viewModel.onEvent(ReportUiEvent.DatePickerDismissed) },
        onDatePicked = { viewModel.onEvent(ReportUiEvent.DatePicked(it)) },
        onPickerPreviousMonth = { viewModel.onEvent(ReportUiEvent.PickerMonthMoved(-1)) },
        onPickerNextMonth = { viewModel.onEvent(ReportUiEvent.PickerMonthMoved(1)) },
        onTabSelected = { viewModel.onEvent(ReportUiEvent.TabSelected(it)) },
        onEntryClick = { viewModel.onEvent(ReportUiEvent.TimetableEntryClicked(it)) },
        onEditView = { viewModel.onEvent(ReportUiEvent.RestSuggestionClicked) },
        onAlarmSettings = { viewModel.onEvent(ReportUiEvent.AlarmSettingsClicked) },
        onAlarmDialogDismiss = { viewModel.onEvent(ReportUiEvent.AlarmDialogDismissed) },
        onAlarmHourChange = { viewModel.onEvent(ReportUiEvent.AlarmHourChanged(it)) },
        onAlarmMinuteChange = { viewModel.onEvent(ReportUiEvent.AlarmMinuteChanged(it)) },
        onAlarmConfirm = { viewModel.onEvent(ReportUiEvent.AlarmConfirmed) },
        onBottomNavSelected = { tab ->
            when (tab) {
                BottomBarTab.MAIN -> onNavigateToMain()
                BottomBarTab.REMINDER -> onNavigateToReminder()
                BottomBarTab.REPORT -> Unit
            }
        },
    )
}

@Composable
fun TimetableScreen(
    state: ReportUiState,
    onTabSelected: (ReportTab) -> Unit,
    onBottomNavSelected: (BottomBarTab) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onEntryClick: (String) -> Unit = {},
    onEditView: () -> Unit = {},
    onAlarmSettings: () -> Unit = {},
    onAlarmDialogDismiss: () -> Unit = {},
    onAlarmHourChange: (String) -> Unit = {},
    onAlarmMinuteChange: (String) -> Unit = {},
    onAlarmConfirm: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToMyPage: () -> Unit = {},
    onPrevDate: () -> Unit = {},
    onNextDate: () -> Unit = {},
    onCalendarClick: () -> Unit = {},
    onDatePickerDismiss: () -> Unit = {},
    onDatePicked: (java.time.LocalDate) -> Unit = {},
    onPickerPreviousMonth: () -> Unit = {},
    onPickerNextMonth: () -> Unit = {},
) {
    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = PhoneShimTheme.colors.background,
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = BottomBarDefaults.ContentBottomPadding),
            ) {
                TopAppBar(
                    title = "DAILY REPORT",
                    titleStyle = PhoneShimType.KorH3,
                    leadingAction = {
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(
                                painter = painterResource(R.drawable.ic_topbar_goal),
                                contentDescription = "설정",
                            )
                        }
                    },
                    trailingAction = {
                        IconButton(onClick = onNavigateToMyPage) {
                            Icon(
                                painter = painterResource(R.drawable.ic_my),
                                contentDescription = "마이페이지",
                            )
                        }
                    },
                )
                ReportDateNavigator(
                    dateLabel = state.dateLabel,
                    onPrevDate = onPrevDate,
                    onNextDate = onNextDate,
                    nextEnabled = state.canGoNextDate,
                    onCalendarClick = onCalendarClick,
                    onAlarmSettingsClick = onAlarmSettings,
                    // 툴팁은 첫 진입 화면(어플 사용 통계)에만 띄웁니다.
                )

                state.restSuggestion?.let { suggestion ->
                    RestSuggestionCard(
                        suggestion = suggestion,
                        modifier = Modifier.padding(
                            horizontal = PhoneShimDimens.screenHorizontalPadding,
                        ),
                    )
                    Spacer(modifier = Modifier.height(PhoneShimDimens.spacing16))
                }

                ReportTabRow(selected = ReportTab.TIMETABLE, onTabSelected = onTabSelected)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(PhoneShimDimens.screenHorizontalPadding),
                ) {
                    Text(
                        text = "사용 이유를 입력해주세요.\n(당일 22:00 - 다음날 23:00)",
                        style = PhoneShimType.KorBodyM,
                        color = PhoneShimTheme.colors.textSecondary,
                    )
                    Spacer(modifier = Modifier.height(PhoneShimDimens.spacing16))

                    Row(verticalAlignment = Alignment.Top) {
                        // 타임테이블도 "사용 어플" 카드와 같은 흰 카드 안에 넣습니다.
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    PhoneShimTheme.colors.surface,
                                    RoundedCornerShape(16.dp),
                                )
                                .border(
                                    1.dp,
                                    PhoneShimTheme.colors.divider,
                                    RoundedCornerShape(16.dp),
                                )
                                .padding(PhoneShimDimens.spacing12),
                        ) {
                            TimetableChart(
                                hours = state.hourUsages,
                                onSegmentClick = onEntryClick,
                            )
                        }
                        Spacer(modifier = Modifier.width(PhoneShimDimens.spacing12))
                        // 알림 설정은 상단으로 올라가서 사이드에는 사용 어플 카드만 남깁니다.
                        UsedAppsCard(
                            apps = state.usedApps,
                            modifier = Modifier.width(ReportSideCardWidth),
                        )
                    }
                }
            }
        }
        BottomBar(
            selectedTab = BottomBarTab.REPORT,
            onTabSelected = onBottomNavSelected,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
        PhoneShimBottomBarSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }

    if (state.isDatePickerVisible) {
        ReportDatePickerDialog(
            visibleMonth = state.pickerMonth,
            selectedDate = state.date,
            todayDate = state.today,
            onDateSelected = onDatePicked,
            onPreviousMonth = onPickerPreviousMonth,
            onNextMonth = onPickerNextMonth,
            onDismiss = onDatePickerDismiss,
        )
    }

    if (state.isAlarmDialogVisible) {
        AlarmSettingDialog(
            hour = state.alarmHourDraft,
            minute = state.alarmMinuteDraft,
            onHourChange = onAlarmHourChange,
            onMinuteChange = onAlarmMinuteChange,
            onConfirm = onAlarmConfirm,
            onDismiss = onAlarmDialogDismiss,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TimetableScreenPreview() {
    PhoneShimTheme {
        TimetableScreen(
            state = ReportUiState(),
            onTabSelected = {},
            onBottomNavSelected = {},
        )
    }
}

@Preview(name = "사용 기록 없는 날", showBackground = true)
@Composable
private fun TimetableEmptyPreview() {
    PhoneShimTheme {
        TimetableScreen(
            state = ReportUiState(),
            onTabSelected = {},
            onBottomNavSelected = {},
        )
    }
}
