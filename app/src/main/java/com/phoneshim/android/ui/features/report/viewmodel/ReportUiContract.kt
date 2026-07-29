package com.phoneshim.android.ui.features.report.viewmodel

import com.phoneshim.android.domain.model.DailyReport
import com.phoneshim.android.domain.model.ReportRange
import com.phoneshim.android.domain.model.ReportSummary
import com.phoneshim.android.domain.model.RestSuggestion
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
 * 07. 데일리 리포트 화면군의 MVI 계약.
 *
 * 데이터 소스
 * - 앱별 사용량: GET /api/usage-logs (폴 담당, 구현완료)
 * - 주간/월간 요약: GET /api/reports/summary (예정)
 * - 쉼이의 제안: POST /api/ai/daily-feedback (예정)
 */
data class ReportUiState(
    val date: LocalDate = LocalDate.now(),
    val today: LocalDate = LocalDate.now(),
    val selectedTab: ReportTab = ReportTab.TIMETABLE,
    val period: ReportPeriod = ReportPeriod.DAY,

    val report: DailyReport? = null,
    val summary: ReportSummary? = null,
    val restSuggestion: RestSuggestion? = null,

    val isLoading: Boolean = false,

    /**
     * 422 INSUFFICIENT_*_DATA 응답을 받은 상태.
     * 오류 화면이 아니라 "아직 데이터가 부족합니다" 안내로 표시합니다.
     */
    val insufficientDataMessage: String? = null,

    /** 아직 서버/기획이 확정되지 않은 차트용 mock. 아래 TODO 참고. */
    val categoryRows: List<CategoryUsageRow> = mockCategoryRows(),
    val hourUsages: List<HourUsage> = mockHourUsages(),
    val usageReasonLegend: List<UsageReasonLegend> = mockUsageReasonLegend(),
) : UiState {

    /** 상단 날짜 네비게이터 라벨. 예) "7.11" */
    val dateLabel: String get() = "${date.monthValue}.${date.dayOfMonth}"

    /** API 요청용 날짜 문자열. 예) "2026-07-11" */
    val requestDate: String get() = date.toString()

    val isToday: Boolean get() = date == today

    /** 오늘 이후로는 이동할 수 없습니다. */
    val canGoNextDate: Boolean get() = date.isBefore(today)

    /** 어플 사용 분포 버블. 실제 사용량(usedMinutes)을 비율로 환산합니다. */
    val appBubbles: List<AppBubble>
        get() {
            val usages = report?.appUsages.orEmpty().filter { it.usedMinutes > 0 }
            if (usages.isEmpty()) return emptyList()
            val max = usages.maxOf { it.usedMinutes }.toFloat()
            // TODO: 앱별 표시 색상 규칙이 정해지면 팔레트 순환 대신 규칙을 적용하세요.
            val palette = listOf(ReportColorYellow, ReportColorRed, ReportColorGreen)
            return usages.sortedByDescending { it.usedMinutes }
                .take(palette.size)
                .mapIndexed { index, usage ->
                    AppBubble(
                        label = usage.appName.ifBlank { "앱 ${index + 1}" },
                        color = palette[index % palette.size],
                        value = usage.usedMinutes / max,
                    )
                }
        }

    val hasReportData: Boolean get() = report?.isEmpty == false

    val isDataInsufficient: Boolean get() = insufficientDataMessage != null
}

sealed interface ReportUiEvent : UiEvent {
    /** 화면 진입 시 1회. 해당 탭 기준으로 로딩을 트리거합니다. */
    data class ScreenEntered(val tab: ReportTab) : ReportUiEvent
    data object PreviousDateClicked : ReportUiEvent
    data object NextDateClicked : ReportUiEvent
    data class TabSelected(val tab: ReportTab) : ReportUiEvent
    data class PeriodSelected(val period: ReportPeriod) : ReportUiEvent
    data class TimetableEntryClicked(val entryId: String) : ReportUiEvent
    data object RestSuggestionClicked : ReportUiEvent
    data object RestSuggestionRequested : ReportUiEvent
    data object AlarmSettingsClicked : ReportUiEvent
    data object Retry : ReportUiEvent
}

sealed interface ReportUiEffect : UiEffect {
    data class NavigateToUsageReasonInput(val entryId: String) : ReportUiEffect
    data object NavigateToRestSuggestion : ReportUiEffect
    data object NavigateToAlarmSettings : ReportUiEffect
    data class NavigateToTab(val tab: ReportTab) : ReportUiEffect
    data class ShowMessage(val message: String) : ReportUiEffect
}

/** ReportPeriod → 서버 range 파라미터. DAY 는 요약 API 대상이 아닙니다. */
fun ReportPeriod.toReportRange(): ReportRange? = when (this) {
    ReportPeriod.DAY -> null
    ReportPeriod.WEEK -> ReportRange.WEEK
    ReportPeriod.MONTH -> ReportRange.MONTH
}

// ---------------------------------------------------------------------------
// 아직 실제 데이터로 대체하지 못한 mock.
//
// TODO(카테고리 막대): 화면은 "여가 시간 / 이동 중 / 습관적으로 / 정보성 / 기타" 5개 고정
//  카테고리인데 API 의 usageReason 은 자유 입력 문자열(최대 100자)입니다.
//  둘을 잇는 분류 규칙이 기획에서 확정돼야 합니다.
//
// TODO(타임테이블): 시간대별 사용 로그를 조회하는 API 가 명세에 없습니다.
//  usage-logs 는 일별 합계만 주고, 시간 구간 정보는 usage-reasons 에 POST 로 넣을 수만 있습니다.
//  조회용 엔드포인트 신설이 필요합니다.
// ---------------------------------------------------------------------------

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
