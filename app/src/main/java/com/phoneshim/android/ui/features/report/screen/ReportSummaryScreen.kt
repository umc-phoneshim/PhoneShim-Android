package com.phoneshim.android.ui.features.report.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.phoneshim.android.ui.common.BottomNavTab
import com.phoneshim.android.ui.common.PhoneShimBottomNavBar
import com.phoneshim.android.ui.features.report.component.AppBubble
import com.phoneshim.android.ui.features.report.component.AppUsageBubbleChart
import com.phoneshim.android.ui.features.report.component.CategoryUsageBarChart
import com.phoneshim.android.ui.features.report.component.CategoryUsageRow
import com.phoneshim.android.ui.features.report.component.DailyReportHeader
import com.phoneshim.android.ui.features.report.component.ReportCard
import com.phoneshim.android.ui.features.report.component.ReportColorGreen
import com.phoneshim.android.ui.features.report.component.ReportColorRed
import com.phoneshim.android.ui.features.report.component.ReportColorYellow
import com.phoneshim.android.ui.features.report.component.ReportPeriod
import com.phoneshim.android.ui.features.report.component.ReportPeriodToggle
import com.phoneshim.android.ui.features.report.component.ReportTab
import com.phoneshim.android.ui.features.report.component.ReportTabRow
import com.phoneshim.android.ui.features.report.component.UsageLegendDots
import com.phoneshim.android.ui.features.report.component.UsageSegment
import com.phoneshim.android.ui.features.report.viewmodel.ReportViewModel
import com.phoneshim.android.ui.theme.PhoneShimDimens
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimType

/** 07. 데일리 리포트 (어플 사용 통계) 화면. */
@Composable
fun ReportSummaryScreen(
    modifier: Modifier = Modifier,
    onNavigateToTimetable: () -> Unit = {},
    onNavigateToMain: () -> Unit = {},
    onNavigateToReminder: () -> Unit = {},
    viewModel: ReportViewModel = hiltViewModel(),
) {
    // TODO: viewModel.uiState 의 DailyReport/AppUsage 를 AppBubble·CategoryUsageRow 로 매핑해
    //  아래 mock 데이터를 대체하세요. (기간 토글 변경 시 loadReport 재호출 등)
    ReportSummaryContent(
        modifier = modifier,
        dateLabel = "7.11",
        onTabSelected = { tab -> if (tab == ReportTab.TIMETABLE) onNavigateToTimetable() },
        onBottomNavSelected = { tab ->
            when (tab) {
                BottomNavTab.MAIN -> onNavigateToMain()
                BottomNavTab.REMINDER -> onNavigateToReminder()
                BottomNavTab.REPORT -> Unit
            }
        },
    )
}

@Composable
private fun ReportSummaryContent(
    dateLabel: String,
    onTabSelected: (ReportTab) -> Unit,
    onBottomNavSelected: (BottomNavTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    var period by remember { mutableStateOf(ReportPeriod.DAY) }

    val bubbles = remember {
        listOf(
            AppBubble(label = "카카오톡", color = ReportColorYellow, value = 0.35f),
            AppBubble(label = "유튜브", color = ReportColorRed, value = 0.75f),
            AppBubble(label = "기타", color = ReportColorGreen, value = 0.2f),
        )
    }
    val categoryRows = remember {
        listOf(
            CategoryUsageRow(
                label = "여가 시간",
                segments = listOf(
                    UsageSegment(ReportColorRed, 0.35f),
                    UsageSegment(ReportColorGreen, 0.15f),
                    UsageSegment(ReportColorYellow, 0.1f),
                ),
            ),
            CategoryUsageRow(label = "이동 중", segments = listOf(UsageSegment(ReportColorYellow, 0.4f))),
            CategoryUsageRow(
                label = "습관적으로",
                segments = listOf(UsageSegment(ReportColorGreen, 0.2f), UsageSegment(ReportColorRed, 0.3f)),
            ),
            CategoryUsageRow(label = "정보성", segments = listOf(UsageSegment(ReportColorYellow, 0.25f))),
            CategoryUsageRow(
                label = "기타",
                segments = listOf(UsageSegment(ReportColorGreen, 0.2f), UsageSegment(ReportColorYellow, 0.15f)),
            ),
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = PhoneShimTheme.colors.background,
        bottomBar = {
            PhoneShimBottomNavBar(selected = BottomNavTab.REPORT, onTabSelected = onBottomNavSelected)
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            DailyReportHeader(dateLabel = dateLabel, onPrevDate = {}, onNextDate = {})
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

                Text(
                    text = "▶ 어플 사용 요약",
                    style = PhoneShimType.KorBodyM,
                    color = PhoneShimTheme.colors.textPrimary,
                )

                ReportCard {
                    ReportPeriodToggle(selected = period, onSelect = { period = it })
                    Spacer(modifier = Modifier.height(PhoneShimDimens.spacing12))
                    UsageLegendDots(colors = listOf(ReportColorYellow, ReportColorRed, ReportColorGreen))
                    Spacer(modifier = Modifier.height(PhoneShimDimens.spacing16))
                    CategoryUsageBarChart(rows = categoryRows)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ReportSummaryContentPreview() {
    PhoneShimTheme {
        ReportSummaryContent(
            dateLabel = "7.11",
            onTabSelected = {},
            onBottomNavSelected = {},
        )
    }
}