package com.phoneshim.android.ui.features.report.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.phoneshim.android.R
import com.phoneshim.android.ui.features.report.component.AppUsageBubbleChart
import com.phoneshim.android.ui.features.report.component.CategoryUsageBarChart
import com.phoneshim.android.ui.features.report.component.ReportDateNavigator
import com.phoneshim.android.ui.features.report.component.ReportCard
import com.phoneshim.android.ui.features.report.component.ReportColorGreen
import com.phoneshim.android.ui.features.report.component.ReportColorRed
import com.phoneshim.android.ui.features.report.component.ReportColorYellow
import com.phoneshim.android.ui.features.report.component.ReportPeriod
import com.phoneshim.android.ui.features.report.component.ReportPeriodToggle
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
                ReportUiEffect.NavigateToAiSuggestion -> Unit
                ReportUiEffect.NavigateToAlarmSettings -> Unit
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
        onTabSelected = { viewModel.onEvent(ReportUiEvent.TabSelected(it)) },
        onPeriodSelected = { viewModel.onEvent(ReportUiEvent.PeriodSelected(it)) },
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
                )
                ReportTabRow(selected = ReportTab.SUMMARY, onTabSelected = onTabSelected)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(PhoneShimDimens.screenHorizontalPadding),
                    verticalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing16),
                ) {
                    ReportCard {
                        Text(
                            text = "어플 사용 분포",
                            style = PhoneShimType.KorBodyM,
                            color = PhoneShimTheme.colors.textSecondary,
                        )
                        Spacer(modifier = Modifier.height(PhoneShimDimens.spacing12))
                        AppUsageBubbleChart(bubbles = state.appBubbles)
                        Spacer(modifier = Modifier.height(PhoneShimDimens.spacing8))
                        Text(
                            text = "앱 아이콘 크기 = 사용량",
                            style = PhoneShimType.KorMicro,
                            color = PhoneShimTheme.colors.textTertiary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
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
                        UsageLegendDots(
                            colors = listOf(ReportColorYellow, ReportColorRed, ReportColorGreen),
                        )
                        Spacer(modifier = Modifier.height(PhoneShimDimens.spacing16))
                        CategoryUsageBarChart(rows = state.categoryRows)
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
