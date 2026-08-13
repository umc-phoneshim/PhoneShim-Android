package com.phoneshim.android.ui.features.report.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHost
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.phoneshim.android.ui.common.BottomBar
import com.phoneshim.android.ui.common.BottomBarTab
import com.phoneshim.android.ui.common.BottomBarDefaults
import com.phoneshim.android.ui.common.TopAppBar
import com.phoneshim.android.ui.common.base.CollectCommonEffect
import com.phoneshim.android.R
import com.phoneshim.android.ui.features.report.component.AppUsageBubbleChart
import com.phoneshim.android.ui.features.report.component.CategoryUsageBarChart
import com.phoneshim.android.ui.features.report.component.AlarmSettingDialog
import com.phoneshim.android.ui.features.report.component.ReportDateNavigator
import com.phoneshim.android.ui.features.report.component.ReportDatePickerDialog
import com.phoneshim.android.ui.features.report.component.ReportCard
import com.phoneshim.android.ui.features.report.component.ReportColorGreen
import com.phoneshim.android.ui.features.report.component.ReportColorRed
import com.phoneshim.android.ui.features.report.component.ReportColorYellow
import com.phoneshim.android.ui.features.report.component.ReportPeriod
import com.phoneshim.android.ui.features.report.component.ReportPeriodToggle
import com.phoneshim.android.ui.features.report.component.ReportSideCardWidth
import com.phoneshim.android.ui.features.report.component.RestSuggestionCard
import com.phoneshim.android.ui.features.report.component.UsedAppsCard
import com.phoneshim.android.ui.features.report.component.ReportTab
import com.phoneshim.android.ui.features.report.component.ReportTabRow
import com.phoneshim.android.ui.features.report.component.UsageLegendDots
import com.phoneshim.android.ui.features.report.viewmodel.ReportUiEffect
import com.phoneshim.android.ui.features.report.viewmodel.ReportUiEvent
import com.phoneshim.android.ui.features.report.viewmodel.ReportUiState
import com.phoneshim.android.ui.features.report.viewmodel.ReportViewModel
import com.phoneshim.android.ui.theme.PhoneShimDimens
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimType

/**
 * 07. 데일리 리포트 (어플 사용 통계) 진입점.
 * ViewModel 주입과 이펙트 처리를 담당하고, UI 는 상태만 받는 [ReportSummaryScreen] 이 그립니다.
 */
@Composable
fun ReportSummaryRoute(
    onAuthExpired: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToTimetable: () -> Unit = {},
    onNavigateToMain: () -> Unit = {},
    onNavigateToReminder: () -> Unit = {},
    onNavigateToMyPage: () -> Unit = {},
    viewModel: ReportViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    CollectCommonEffect(viewModel, onAuthExpired)

    LaunchedEffect(viewModel) {
        viewModel.onEvent(ReportUiEvent.ScreenEntered(ReportTab.SUMMARY))
    }
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ReportUiEffect.NavigateToTab ->
                    if (effect.tab == ReportTab.TIMETABLE) onNavigateToTimetable()
                is ReportUiEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
                // 요약 화면에서는 발생하지 않는 이펙트입니다. (타임테이블 전용)
                is ReportUiEffect.NavigateToUsageReasonInput -> Unit
                ReportUiEffect.NavigateToRestSuggestion -> Unit
            }
        }
    }

    ReportSummaryScreen(
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
        onPeriodSelected = { viewModel.onEvent(ReportUiEvent.PeriodSelected(it)) },
        onAlarmSettings = { viewModel.onEvent(ReportUiEvent.AlarmSettingsClicked) },
        onAlarmDialogDismiss = { viewModel.onEvent(ReportUiEvent.AlarmDialogDismissed) },
        onAlarmHourChange = { viewModel.onEvent(ReportUiEvent.AlarmHourChanged(it)) },
        onAlarmMinuteChange = { viewModel.onEvent(ReportUiEvent.AlarmMinuteChanged(it)) },
        onAlarmConfirm = { viewModel.onEvent(ReportUiEvent.AlarmConfirmed) },
        onTooltipDismiss = { viewModel.onEvent(ReportUiEvent.CalendarTooltipDismissed) },
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
fun ReportSummaryScreen(
    state: ReportUiState,
    onTabSelected: (ReportTab) -> Unit,
    onBottomNavSelected: (BottomBarTab) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onNavigateToSettings: () -> Unit = {},
    onNavigateToMyPage: () -> Unit = {},
    onPrevDate: () -> Unit = {},
    onNextDate: () -> Unit = {},
    onPeriodSelected: (ReportPeriod) -> Unit = {},
    onCalendarClick: () -> Unit = {},
    onDatePickerDismiss: () -> Unit = {},
    onDatePicked: (java.time.LocalDate) -> Unit = {},
    onPickerPreviousMonth: () -> Unit = {},
    onPickerNextMonth: () -> Unit = {},
    onAlarmSettings: () -> Unit = {},
    onAlarmDialogDismiss: () -> Unit = {},
    onAlarmHourChange: (String) -> Unit = {},
    onAlarmMinuteChange: (String) -> Unit = {},
    onAlarmConfirm: () -> Unit = {},
    onTooltipDismiss: () -> Unit = {},
) {
    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = PhoneShimTheme.colors.background,
            snackbarHost = { SnackbarHost(snackbarHostState) },
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
                    // 데일리 리포트 첫 진입 화면이라 여기에만 안내를 띄웁니다.
                    showCalendarTooltip = state.isCalendarTooltipVisible,
                    onTooltipDismiss = onTooltipDismiss,
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

                ReportTabRow(selected = ReportTab.SUMMARY, onTabSelected = onTabSelected)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(PhoneShimDimens.screenHorizontalPadding),
                    verticalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing16),
                ) {
                    // 사용 분포 카드와 사용 어플 카드를 가로로 나란히 둡니다.
                    Row(horizontalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing12)) {
                        ReportCard(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "어플 사용 분포",
                                style = PhoneShimType.KorBodyM,
                                color = PhoneShimTheme.colors.textSecondary,
                            )
                            Spacer(modifier = Modifier.height(PhoneShimDimens.spacing12))
                            val bubbles = state.appBubbles
                            if (bubbles.isEmpty()) {
                                // 데이터 부족 응답도 오류가 아니라 이 안내로 표시합니다.
                                Text(
                                    text = state.insufficientDataMessage
                                        ?: "이 날짜에는 기록된 사용량이 없어요.",
                                    style = PhoneShimType.KorCaption,
                                    color = PhoneShimTheme.colors.textTertiary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            } else {
                                AppUsageBubbleChart(bubbles = bubbles)
                                Spacer(modifier = Modifier.height(PhoneShimDimens.spacing8))
                                Text(
                                    text = "앱 아이콘 크기 = 사용량",
                                    style = PhoneShimType.KorMicro,
                                    color = PhoneShimTheme.colors.textTertiary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                        UsedAppsCard(
                            apps = state.usedApps,
                            modifier = Modifier.width(ReportSideCardWidth),
                        )
                    }

                    Text(
                        text = "▶ 어플 사용 요약",
                        style = PhoneShimType.KorBodyM,
                        color = PhoneShimTheme.colors.textPrimary,
                    )

                    ReportCard {
                        ReportPeriodToggle(selected = state.period, onSelect = onPeriodSelected)
                        Spacer(modifier = Modifier.height(PhoneShimDimens.spacing12))

                        if (state.summaryPeriodLabel.isNotBlank()) {
                            Text(
                                text = state.summaryPeriodLabel,
                                style = PhoneShimType.KorMicro,
                                color = PhoneShimTheme.colors.textTertiary,
                            )
                            Spacer(modifier = Modifier.height(PhoneShimDimens.spacing8))
                        }

                        // GET /api/reports/summary 의 사유별 집계를 그대로 막대로 그립니다.
                        if (state.hasSummaryData) {
                            UsageLegendDots(
                                colors = listOf(ReportColorYellow, ReportColorRed, ReportColorGreen),
                            )
                            Spacer(modifier = Modifier.height(PhoneShimDimens.spacing16))
                            CategoryUsageBarChart(rows = state.categoryRows)
                        } else {
                            Text(
                                text = state.insufficientDataMessage
                                    ?: "아직 이 기간에 기록한 사용 이유가 없어요.",
                                style = PhoneShimType.KorCaption,
                                color = PhoneShimTheme.colors.textTertiary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
        }
        BottomBar(
            selectedTab = BottomBarTab.REPORT,
            onTabSelected = onBottomNavSelected,
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
            errorMessage = state.alarmInputError,
            isSaving = state.isAlertSettingSaving,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ReportSummaryScreenPreview() {
    PhoneShimTheme {
        ReportSummaryScreen(
            state = ReportUiState(selectedTab = ReportTab.SUMMARY),
            onTabSelected = {},
            onBottomNavSelected = {},
        )
    }
}

@Preview(name = "WEEK 기간 선택", showBackground = true)
@Composable
private fun ReportSummaryWeekPreview() {
    PhoneShimTheme {
        ReportSummaryScreen(
            state = ReportUiState(selectedTab = ReportTab.SUMMARY, period = ReportPeriod.WEEK),
            onTabSelected = {},
            onBottomNavSelected = {},
        )
    }
}
