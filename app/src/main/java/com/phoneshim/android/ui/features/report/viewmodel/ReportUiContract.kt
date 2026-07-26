package com.phoneshim.android.ui.features.report.viewmodel

import com.phoneshim.android.domain.model.DailyReport
import com.phoneshim.android.ui.common.base.UiEffect
import com.phoneshim.android.ui.common.base.UiEvent
import com.phoneshim.android.ui.common.base.UiState
import com.phoneshim.android.ui.features.report.component.AppBubble
import com.phoneshim.android.ui.features.report.component.CategoryUsageRow
import com.phoneshim.android.ui.features.report.component.HourUsage
import com.phoneshim.android.ui.features.report.component.ReportColorGreen
import com.phoneshim.android.ui.features.report.component.ReportColorRed
import com.phoneshim.android.ui.features.report.component.ReportColorYellow
import com.phoneshim.android.ui.features.report.component.ReportPeriod
import com.phoneshim.android.ui.features.report.component.ReportTab
import com.phoneshim.android.ui.features.report.component.UsageReasonLegend
import com.phoneshim.android.ui.features.report.component.UsageSegment
import java.time.LocalDate

/**
 * 07. 데일리 리포트 화면군(ReportSummaryScreen / TimetableScreen)의 MVI 계약.
 *
 * 차트 데이터는 아직 mock 입니다. 실제 연동 시 [DailyReport] 를 아래 표현 모델
 * (AppBubble / CategoryUsageRow / HourUsage)로 매핑해 상태에 담아 주세요.
 */
data class ReportUiState(
    val date: LocalDate = LocalDate.of(2026, 7, 11),
    val selectedTab: ReportTab = ReportTab.TIMETABLE,
    val period: ReportPeriod = ReportPeriod.DAY,
    val report: DailyReport? = null,
    val isLoading: Boolean = false,
    val appBubbles: List<AppBubble> = mockAppBubbles(),
    val categoryRows: List<CategoryUsageRow> = mockCategoryRows(),
    val hourUsages: List<HourUsage> = mockHourUsages(),
    val usageReasonLegend: List<UsageReasonLegend> = mockUsageReasonLegend(),
) : UiState {
    /** 상단 날짜 네비게이터 라벨. 예) "7.11" */
    val dateLabel: String get() = "${date.monthValue}.${date.dayOfMonth}"

    /** API 요청용 날짜 문자열. 예) "2026-07-11" */
    val requestDate: String get() = date.toString()
}

sealed interface ReportUiEvent : UiEvent {
    /** 화면 진입 시 1회. 해당 탭 기준으로 리포트 로딩을 트리거합니다. */
    data class ScreenEntered(val tab: ReportTab) : ReportUiEvent
    data object PreviousDateClicked : ReportUiEvent
    data object NextDateClicked : ReportUiEvent
    data class TabSelected(val tab: ReportTab) : ReportUiEvent
    data class PeriodSelected(val period: ReportPeriod) : ReportUiEvent
    data class TimetableEntryClicked(val entryId: String) : ReportUiEvent
    data object EditViewClicked : ReportUiEvent
    data object AlarmSettingsClicked : ReportUiEvent
}

sealed interface ReportUiEffect : UiEffect {
    data class NavigateToUsageReasonInput(val entryId: String) : ReportUiEffect
    data object NavigateToAiSuggestion : ReportUiEffect
    data object NavigateToAlarmSettings : ReportUiEffect
    data class NavigateToTab(val tab: ReportTab) : ReportUiEffect
    data class ShowMessage(val message: String) : ReportUiEffect
}

// ---------------------------------------------------------------------------
// mock 데이터. 화면의 remember 블록에 흩어져 있던 값을 상태 기본값으로 모았습니다.
// TODO: ReportRepository 연동 후 아래 함수들을 DailyReport → UI 모델 매퍼로 교체하세요.
// ---------------------------------------------------------------------------

private fun mockAppBubbles(): List<AppBubble> = listOf(
    AppBubble(label = "카카오톡", color = ReportColorYellow, value = 0.35f),
    AppBubble(label = "유튜브", color = ReportColorRed, value = 0.75f),
    AppBubble(label = "기타", color = ReportColorGreen, value = 0.2f),
)

private fun mockCategoryRows(): List<CategoryUsageRow> = listOf(
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

/** 타임테이블은 당일 22:00 부터 다음날 21:00 까지 24개 버킷으로 표시합니다. */
private fun mockHourUsages(): List<HourUsage> {
    val filled = mapOf(
        "22" to listOf(UsageSegment(ReportColorYellow, 0.32f, entryId = "e1")),
        "04" to listOf(UsageSegment(ReportColorRed, 0.4f, entryId = "e2")),
        "10" to listOf(UsageSegment(ReportColorGreen, 0.55f, entryId = "e3")),
    )
    return (0 until 24).map { offset ->
        val label = "%02d".format((22 + offset) % 24)
        HourUsage(label, filled[label].orEmpty())
    }
}

private fun mockUsageReasonLegend(): List<UsageReasonLegend> = listOf(
    UsageReasonLegend(ReportColorYellow, "카카오톡"),
    UsageReasonLegend(ReportColorRed, "유튜브"),
    UsageReasonLegend(ReportColorGreen, "혼자"),
)
