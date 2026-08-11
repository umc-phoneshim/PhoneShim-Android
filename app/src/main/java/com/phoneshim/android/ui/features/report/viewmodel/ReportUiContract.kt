package com.phoneshim.android.ui.features.report.viewmodel

import com.phoneshim.android.domain.model.DailyReport
import com.phoneshim.android.domain.model.ReportRange
import com.phoneshim.android.domain.model.ReportSummary
import com.phoneshim.android.domain.model.UsageReasonCode
import com.phoneshim.android.domain.model.UsageSession
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
import com.phoneshim.android.ui.features.report.component.UsageSegment
import com.phoneshim.android.ui.features.report.component.UsedApp
import java.time.LocalDate
import java.time.YearMonth

/**
 * 07. 데일리 리포트 화면군의 MVI 계약.
 *
 * 데이터 소스 (전부 백엔드 구현완료)
 * - 앱별 사용량: GET /api/usage-logs, /api/usage-logs/status
 * - 사용 구간: GET /api/usage-sessions  → 타임테이블
 * - 사유 요약: GET /api/reports/summary → 어플 사용 요약 막대
 */
data class ReportUiState(
    val date: LocalDate = LocalDate.now(),
    val today: LocalDate = LocalDate.now(),
    val selectedTab: ReportTab = ReportTab.TIMETABLE,
    val period: ReportPeriod = ReportPeriod.DAY,

    val report: DailyReport? = null,
    val sessions: List<UsageSession> = emptyList(),
    val summary: ReportSummary? = null,

    val isLoading: Boolean = false,

    /** 422 INSUFFICIENT_* 또는 집계할 기록이 없을 때의 안내 문구. 오류가 아닙니다. */
    val insufficientDataMessage: String? = null,

    val isDatePickerVisible: Boolean = false,
    val pickerMonth: YearMonth = YearMonth.from(LocalDate.now()),
) : UiState {

    /** 상단 날짜 네비게이터 라벨. 예) "7.11" */
    val dateLabel: String get() = "${date.monthValue}.${date.dayOfMonth}"

    /** API 요청용 날짜 문자열. 예) "2026-07-11" */
    val requestDate: String get() = date.toString()

    val isToday: Boolean get() = date == today

    val canGoNextDate: Boolean get() = date.isBefore(today)

    val isDataInsufficient: Boolean get() = insufficientDataMessage != null

    // ---------------------------------------------------------------- 어플 사용 분포

    /** 버블 크기는 사용 시간 비율입니다. */
    val appBubbles: List<AppBubble>
        get() {
            val usages = report?.appUsages.orEmpty().filter { it.usedMinutes > 0 }
            if (usages.isEmpty()) return emptyList()
            val max = usages.maxOf { it.usedMinutes }.toFloat()
            return usages.sortedByDescending { it.usedMinutes }
                .take(APP_PALETTE.size)
                .mapIndexed { index, usage ->
                    AppBubble(
                        label = usage.appName.ifBlank { "앱 ${index + 1}" },
                        color = APP_PALETTE[index % APP_PALETTE.size],
                        value = usage.usedMinutes / max,
                        packageName = usage.packageName,
                    )
                }
        }

    /** 타임테이블 오른쪽 "사용 어플" 카드. */
    val usedApps: List<UsedApp>
        get() = report?.appUsages.orEmpty()
            .filter { it.usedMinutes > 0 }
            .sortedByDescending { it.usedMinutes }
            .mapIndexed { index, usage ->
                UsedApp(
                    name = usage.appName.ifBlank { "앱 ${index + 1}" },
                    color = APP_PALETTE[index % APP_PALETTE.size],
                    packageName = usage.packageName,
                )
            }

    // ---------------------------------------------------------------- 타임테이블

    /** monitoredAppId → 색상. 버블/타임테이블/범례가 같은 앱을 같은 색으로 그리도록 맞춥니다. */
    private val appColors: Map<String, androidx.compose.ui.graphics.Color>
        get() = report?.appUsages.orEmpty()
            .sortedByDescending { it.usedMinutes }
            .mapIndexed { index, usage -> usage.monitoredAppId to APP_PALETTE[index % APP_PALETTE.size] }
            .toMap()

    /**
     * 22시부터 다음날 21시까지 24개 버킷.
     * 각 사용 구간을 시작 시각이 속한 버킷에 넣고, 시간 안에서의 위치와 길이를 비율로 환산합니다.
     */
    val hourUsages: List<HourUsage>
        get() {
            val colors = appColors
            val byHour = sessions.groupBy { it.startTime.hour }
            return (0 until HOURS_IN_DAY).map { offset ->
                val hour = (TIMETABLE_START_HOUR + offset) % HOURS_IN_DAY
                val segments = byHour[hour].orEmpty().map { session ->
                    val startRatio = session.startTime.minute / MINUTES_IN_HOUR
                    val lengthRatio = (session.durationMinutes / MINUTES_IN_HOUR)
                        .coerceIn(MIN_SEGMENT_RATIO, 1f - startRatio)
                    UsageSegment(
                        color = colors[session.monitoredAppId] ?: ReportColorGreen,
                        ratio = lengthRatio,
                        entryId = session.id,
                        startRatio = startRatio,
                    )
                }
                HourUsage(hourLabel = "%02d".format(hour), segments = segments)
            }
        }

    val hasSessions: Boolean get() = sessions.isNotEmpty()

    // ---------------------------------------------------------------- 어플 사용 요약

    /**
     * 사유별 가로 막대. 막대 전체 길이는 전체 사용 시간 대비 그 사유의 비중이고,
     * 막대 안의 구간 색은 그 사유 안에서 앱별 구성비입니다.
     */
    val categoryRows: List<CategoryUsageRow>
        get() {
            val current = summary ?: return emptyList()
            val total = current.totalMinutes.takeIf { it > 0 } ?: return emptyList()
            val colors = appColors
            // 기록이 없는 사유도 라벨은 보이도록 5개 전부 그립니다.
            return UsageReasonCode.entries.map { code ->
                val reason = current.reasons.firstOrNull { it.reason == code }
                val reasonRatio = (reason?.totalMinutes ?: 0).toFloat() / total
                CategoryUsageRow(
                    label = code.label,
                    segments = reason?.apps.orEmpty()
                        .filter { it.minutes > 0 }
                        .mapIndexed { index, app ->
                            UsageSegment(
                                color = colors[app.monitoredAppId]
                                    ?: APP_PALETTE[index % APP_PALETTE.size],
                                ratio = reasonRatio * (app.minutes.toFloat() / (reason?.totalMinutes ?: 1)),
                            )
                        },
                )
            }
        }

    val summaryPeriodLabel: String
        get() = summary?.let { "${it.from} ~ ${it.to}" }.orEmpty()

    val hasSummaryData: Boolean get() = summary?.isEmpty == false

    private companion object {
        val APP_PALETTE = listOf(ReportColorYellow, ReportColorRed, ReportColorGreen)
        const val TIMETABLE_START_HOUR = 22
        const val HOURS_IN_DAY = 24
        const val MINUTES_IN_HOUR = 60f
        const val MIN_SEGMENT_RATIO = 0.02f
    }
}

sealed interface ReportUiEvent : UiEvent {
    data class ScreenEntered(val tab: ReportTab) : ReportUiEvent
    data object PreviousDateClicked : ReportUiEvent
    data object NextDateClicked : ReportUiEvent

    data object DatePickerOpened : ReportUiEvent
    data object DatePickerDismissed : ReportUiEvent
    data class DatePicked(val date: LocalDate) : ReportUiEvent
    data class PickerMonthMoved(val offset: Long) : ReportUiEvent

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

/** 화면의 기간 토글 → 서버 range 파라미터. */
fun ReportPeriod.toReportRange(): ReportRange = when (this) {
    ReportPeriod.DAY -> ReportRange.DAY
    ReportPeriod.WEEK -> ReportRange.WEEK
    ReportPeriod.MONTH -> ReportRange.MONTH
}
