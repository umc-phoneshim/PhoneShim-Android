package com.phoneshim.android.ui.features.report.viewmodel

import androidx.compose.ui.graphics.Color
import com.phoneshim.android.domain.model.DailyReport
import com.phoneshim.android.domain.model.AlertSetting
import com.phoneshim.android.domain.model.AlertSettingPolicy
import com.phoneshim.android.domain.model.ReportRange
import com.phoneshim.android.domain.model.ReportSummary
import com.phoneshim.android.domain.model.RestSuggestion
import com.phoneshim.android.domain.model.UsageReasonCode
import com.phoneshim.android.domain.model.UsageSession
import com.phoneshim.android.ui.common.PhoneShimSnackbarType
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
 * - 사용 구간: GET /api/usage-sessions  → 타임테이블 차트
 * - 사유 요약: GET /api/reports/summary → 어플 사용 요약 막대
 * - 쉼이의 제안: GET /api/reports/suggestion (AI 아님, 목표 대비 사용량 기반 문구)
 */
data class ReportUiState(
    val date: LocalDate = LocalDate.now(),
    val today: LocalDate = LocalDate.now(),
    /** 데일리 리포트 첫 진입은 "어플 사용 통계" 화면입니다. */
    val selectedTab: ReportTab = ReportTab.SUMMARY,
    val period: ReportPeriod = ReportPeriod.DAY,

    val report: DailyReport? = null,
    val sessions: List<UsageSession> = emptyList(),
    val summary: ReportSummary? = null,
    val restSuggestion: RestSuggestion? = null,

    val isLoading: Boolean = false,

    /** 날짜 선택 달력 팝업 노출 여부와 달력에 보이는 월. */
    val isDatePickerVisible: Boolean = false,
    val pickerMonth: YearMonth = YearMonth.from(LocalDate.now()),

    /**
     * 달력 버튼 안내 툴팁 노출 여부.
     * 첫 진입 화면(어플 사용 통계)에서만 보이고, 한 번 닫으면 DataStore 에 기록해
     * 앱을 다시 켜도 뜨지 않습니다. 로딩 전에는 깜빡임을 막으려고 false 로 시작합니다.
     */
    val isCalendarTooltipVisible: Boolean = false,

    /** 알림 설정 팝업 노출 여부와 팝업 안에서 편집 중인 시/분. */
    val isAlarmDialogVisible: Boolean = false,
    val alarmHourDraft: String = "22",
    val alarmMinuteDraft: String = "00",
    val alarmInputError: String? = null,
    val isAlertSettingLoading: Boolean = false,
    val isAlertSettingSaving: Boolean = false,

    /** 계정에 저장된 서버 AlertSetting. 신규 사용자는 GET에서 기본값이 자동 생성됩니다. */
    val alertSetting: AlertSetting? = null,

    /**
     * 집계할 기록이 부족하거나 아직 준비되지 않은 상태의 안내 문구.
     * 오류 화면이 아니라 본문 안내로 표시합니다.
     */
    val insufficientDataMessage: String? = null,
) : UiState {

    /** 상단 날짜 네비게이터 라벨. 예) "7.11" */
    val dateLabel: String get() = "${date.monthValue}.${date.dayOfMonth}"

    /** API 요청용 날짜 문자열. 예) "2026-07-11" */
    val requestDate: String get() = date.toString()

    val isToday: Boolean get() = date == today

    /** 오늘 이후로는 이동할 수 없습니다. */
    val canGoNextDate: Boolean get() = date.isBefore(today)

    val isDataInsufficient: Boolean get() = insufficientDataMessage != null

    val canSaveAlertSetting: Boolean
        get() = !isAlertSettingSaving && draftAlertTimeMinutes?.let(AlertSettingPolicy::isValid) == true

    val draftAlertTimeMinutes: Int?
        get() {
            val hour = alarmHourDraft.toIntOrNull() ?: return null
            val minute = alarmMinuteDraft.toIntOrNull() ?: return null
            if (minute !in 0..59) return null
            return hour * 60 + minute
        }

    // ---------------------------------------------------------------- 어플 사용 분포

    /**
     * monitoredAppId → 색상.
     * 버블/타임테이블/요약 막대가 같은 앱을 같은 색으로 그리도록 한곳에서 정합니다.
     */
    private val appColors: Map<String, Color>
        get() = report?.appUsages.orEmpty()
            .sortedByDescending { it.usedMinutes }
            .mapIndexed { index, usage -> usage.monitoredAppId to APP_PALETTE[index % APP_PALETTE.size] }
            .toMap()

    /** 버블 크기는 사용 시간 비율입니다. */
    val appBubbles: List<AppBubble>
        get() {
            val usages = report?.appUsages.orEmpty().filter { it.usedMinutes > 0 }
            if (usages.isEmpty()) return emptyList()
            val max = usages.maxOf { it.usedMinutes }.toFloat()
            val colors = appColors
            return usages.sortedByDescending { it.usedMinutes }
                .take(APP_PALETTE.size)
                .mapIndexed { index, usage ->
                    AppBubble(
                        label = usage.appName.ifBlank { "앱 ${index + 1}" },
                        color = colors[usage.monitoredAppId] ?: ReportColorGreen,
                        value = usage.usedMinutes / max,
                        packageName = usage.packageName,
                    )
                }
        }

    val hasReportData: Boolean get() = report?.isEmpty == false

    /**
     * 타임테이블 오른쪽 "사용 어플" 카드.
     *
     * packageName 이 있으면 화면에서 기기의 실제 앱 아이콘과 이름을 읽어 씁니다.
     * 과거 날짜 조회(/api/usage-logs)는 packageName 을 주지 않아 색 원으로 표시됩니다.
     */
    val usedApps: List<UsedApp>
        get() {
            val colors = appColors
            return report?.appUsages.orEmpty()
                .filter { it.usedMinutes > 0 }
                .sortedByDescending { it.usedMinutes }
                .mapIndexed { index, usage ->
                    UsedApp(
                        name = usage.appName.ifBlank { "앱 ${index + 1}" },
                        color = colors[usage.monitoredAppId] ?: ReportColorGreen,
                        packageName = usage.packageName,
                    )
                }
        }

    // ---------------------------------------------------------------- 타임테이블

    /**
     * 22시부터 다음날 21시까지 24개 버킷.
     * 각 사용 구간을 시작 시각이 속한 버킷에 넣고, 그 시간 안에서의 위치와 길이를 비율로 환산합니다.
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
     * 막대 안의 구간 색은 그 사유 안에서의 앱별 구성비입니다.
     * 기록이 없는 사유도 라벨은 보이도록 5개를 모두 그립니다.
     */
    val categoryRows: List<CategoryUsageRow>
        get() {
            val current = summary ?: return emptyList()
            val total = current.totalMinutes.takeIf { it > 0 } ?: return emptyList()
            val colors = appColors
            return UsageReasonCode.entries.map { code ->
                val reason = current.reasons.firstOrNull { it.reason == code }
                val reasonMinutes = reason?.totalMinutes ?: 0
                val reasonRatio = reasonMinutes.toFloat() / total
                CategoryUsageRow(
                    label = code.label,
                    segments = reason?.apps.orEmpty()
                        .filter { it.minutes > 0 && reasonMinutes > 0 }
                        .mapIndexed { index, app ->
                            UsageSegment(
                                color = colors[app.monitoredAppId]
                                    ?: APP_PALETTE[index % APP_PALETTE.size],
                                ratio = reasonRatio * (app.minutes.toFloat() / reasonMinutes),
                            )
                        },
                )
            }
        }

    /** 요약 기간 라벨. 예) "2026-07-01 ~ 2026-07-07" */
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
    /** 화면 진입 시 1회. 해당 탭 기준으로 로딩을 트리거합니다. */
    data class ScreenEntered(val tab: ReportTab) : ReportUiEvent
    data object PreviousDateClicked : ReportUiEvent
    data object NextDateClicked : ReportUiEvent

    /** 상단 달력 버튼으로 여는 날짜 선택 팝업. */
    data object DatePickerOpened : ReportUiEvent
    data object DatePickerDismissed : ReportUiEvent
    data object CalendarTooltipDismissed : ReportUiEvent
    data class DatePicked(val date: LocalDate) : ReportUiEvent
    data class PickerMonthMoved(val offset: Long) : ReportUiEvent

    data class TabSelected(val tab: ReportTab) : ReportUiEvent
    data class PeriodSelected(val period: ReportPeriod) : ReportUiEvent
    data class TimetableEntryClicked(val entryId: String) : ReportUiEvent
    data object RestSuggestionClicked : ReportUiEvent
    data object RestSuggestionRequested : ReportUiEvent

    /** 알림 설정 팝업. 별도 화면이 아니라 리포트 화면 위에 뜹니다. */
    data object AlarmSettingsClicked : ReportUiEvent
    data object AlarmDialogDismissed : ReportUiEvent
    data class AlarmHourChanged(val value: String) : ReportUiEvent
    data class AlarmMinuteChanged(val value: String) : ReportUiEvent
    data object AlarmConfirmed : ReportUiEvent

    data object Retry : ReportUiEvent
}

/** 사용 이유 입력 화면으로 넘길 대상 구간. */
data class UsageReasonTarget(
    val monitoredAppId: String,
    val date: String,
    val timeRangeStart: String,
    val timeRangeEnd: String,
)

sealed interface ReportUiEffect : UiEffect {
    data class NavigateToUsageReasonInput(val target: UsageReasonTarget) : ReportUiEffect
    data object NavigateToRestSuggestion : ReportUiEffect
    data class NavigateToTab(val tab: ReportTab) : ReportUiEffect
    data class ShowMessage(
        val message: String,
        val type: PhoneShimSnackbarType = PhoneShimSnackbarType.Error,
    ) : ReportUiEffect
}

/** 화면의 기간 토글 → 서버 range 파라미터. 서버는 day 도 지원합니다. */
fun ReportPeriod.toReportRange(): ReportRange = when (this) {
    ReportPeriod.DAY -> ReportRange.DAY
    ReportPeriod.WEEK -> ReportRange.WEEK
    ReportPeriod.MONTH -> ReportRange.MONTH
}
