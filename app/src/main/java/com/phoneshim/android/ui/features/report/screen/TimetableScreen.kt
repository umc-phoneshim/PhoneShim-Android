package com.phoneshim.android.ui.features.report.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.phoneshim.android.ui.common.BottomBar
import com.phoneshim.android.ui.common.BottomBarTab
import com.phoneshim.android.ui.common.PhoneShimIconType
import com.phoneshim.android.ui.features.report.component.DailyReportHeader
import com.phoneshim.android.ui.features.report.component.HourUsage
import com.phoneshim.android.ui.features.report.component.ReportColorGreen
import com.phoneshim.android.ui.features.report.component.ReportColorRed
import com.phoneshim.android.ui.features.report.component.ReportColorYellow
import com.phoneshim.android.ui.features.report.component.ReportSideActionButton
import com.phoneshim.android.ui.features.report.component.ReportTab
import com.phoneshim.android.ui.features.report.component.ReportTabRow
import com.phoneshim.android.ui.features.report.component.TimetableChart
import com.phoneshim.android.ui.features.report.component.UsageReasonLegend
import com.phoneshim.android.ui.features.report.component.UsageReasonLegendCard
import com.phoneshim.android.ui.features.report.component.UsageSegment
import com.phoneshim.android.ui.features.report.viewmodel.ReportViewModel
import com.phoneshim.android.ui.theme.PhoneShimDimens
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimType

/** 07. 데일리 리포트 (타임테이블) 화면. */
@Composable
fun TimetableScreen(
    onEntryClick: (entryId: String) -> Unit,
    onNavigateToAiSuggestion: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToSummary: () -> Unit = {},
    onNavigateToMain: () -> Unit = {},
    onNavigateToReminder: () -> Unit = {},
    viewModel: ReportViewModel = hiltViewModel(),
) {
    // TODO: viewModel.uiState.report.timetable(List<TimetableEntry>) 을 시간대별 HourUsage 로
    //  변환해 아래 mock 데이터를 대체하세요. (00~23시 버킷팅 + 색상은 usageReason 기준 매핑)
    TimetableContent(
        modifier = modifier,
        dateLabel = "7.11",
        onTabSelected = { tab -> if (tab == ReportTab.SUMMARY) onNavigateToSummary() },
        onBottomNavSelected = { tab ->
            when (tab) {
                BottomBarTab.MAIN -> onNavigateToMain()
                BottomBarTab.REMINDER -> onNavigateToReminder()
                BottomBarTab.REPORT -> Unit
            }
        },
        onEntryClick = onEntryClick,
        onEditView = onNavigateToAiSuggestion,
        onAlarmSettings = {},
    )
}

@Composable
private fun TimetableContent(
    dateLabel: String,
    onTabSelected: (ReportTab) -> Unit,
    onBottomNavSelected: (BottomBarTab) -> Unit,
    onEntryClick: (String) -> Unit,
    onEditView: () -> Unit,
    onAlarmSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hours = remember {
        listOf(
            HourUsage("22", listOf(UsageSegment(ReportColorYellow, 0.32f, entryId = "e1"))),
            HourUsage("23", emptyList()),
            HourUsage("00", emptyList()),
            HourUsage("01", emptyList()),
            HourUsage("02", emptyList()),
            HourUsage("03", emptyList()),
            HourUsage("04", listOf(UsageSegment(ReportColorRed, 0.4f, entryId = "e2"))),
            HourUsage("05", emptyList()),
            HourUsage("06", emptyList()),
            HourUsage("07", emptyList()),
            HourUsage("08", emptyList()),
            HourUsage("09", emptyList()),
            HourUsage("10", listOf(UsageSegment(ReportColorGreen, 0.55f, entryId = "e3"))),
            HourUsage("11", emptyList()),
            HourUsage("12", emptyList()),
            HourUsage("13", emptyList()),
            HourUsage("14", emptyList()),
            HourUsage("15", emptyList()),
            HourUsage("16", emptyList()),
            HourUsage("17", emptyList()),
            HourUsage("18", emptyList()),
            HourUsage("19", emptyList()),
            HourUsage("20", emptyList()),
            HourUsage("21", emptyList()),
        )
    }
    val legend = remember {
        listOf(
            UsageReasonLegend(ReportColorYellow, "카카오톡"),
            UsageReasonLegend(ReportColorRed, "유튜브"),
            UsageReasonLegend(ReportColorGreen, "혼자"),
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = PhoneShimTheme.colors.background,
        bottomBar = {
            BottomBar(selectedTab = BottomBarTab.REPORT, onTabSelected = onBottomNavSelected)
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            DailyReportHeader(dateLabel = dateLabel, onPrevDate = {}, onNextDate = {})
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

                Row {
                    TimetableChart(
                        hours = hours,
                        onSegmentClick = onEntryClick,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(modifier = Modifier.width(PhoneShimDimens.spacing12))
                    Column(
                        modifier = Modifier.width(84.dp),
                        verticalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing8),
                    ) {
                        ReportSideActionButton(text = "재편 보기", icon = PhoneShimIconType.Info, onClick = onEditView)
                        ReportSideActionButton(text = "알림 설정", icon = PhoneShimIconType.Bell, onClick = onAlarmSettings)
                        UsageReasonLegendCard(items = legend)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TimetableContentPreview() {
    PhoneShimTheme {
        TimetableContent(
            dateLabel = "7.11",
            onTabSelected = {},
            onBottomNavSelected = {},
            onEntryClick = {},
            onEditView = {},
            onAlarmSettings = {},
        )
    }
}
