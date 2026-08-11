package com.phoneshim.android.ui.features.report.viewmodel

import androidx.lifecycle.viewModelScope
import com.phoneshim.android.data.api.ApiException
import com.phoneshim.android.domain.usecase.GetAchievedDatesUseCase
import com.phoneshim.android.ui.common.base.BaseViewModel
import com.phoneshim.android.ui.common.base.UiEffect
import com.phoneshim.android.ui.common.base.UiEvent
import com.phoneshim.android.ui.common.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * 목표 달성 달력. GET /api/usage-logs/calendar?month=YYYY-MM
 *
 * 그 달에 전체 목표를 지킨 날짜 목록을 받아 표시합니다.
 * (사용 이유 입력 여부를 조회하는 엔드포인트는 서버에 없습니다)
 */
data class AchievementCalendarUiState(
    val visibleMonth: YearMonth = YearMonth.now(),
    val today: LocalDate = LocalDate.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val achievedDates: Set<LocalDate> = emptySet(),
    val isLoading: Boolean = false,
    val emptyMessage: String? = null,
) : UiState {
    val monthLabel: String get() = visibleMonth.format(DateTimeFormatter.ofPattern("yyyy.MM"))

    /** API 요청용 월 문자열. 예) "2026-07" */
    val requestMonth: String get() = visibleMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"))

    val canGoNextMonth: Boolean get() = visibleMonth < YearMonth.from(today)

    val achievedCount: Int get() = achievedDates.size
}

sealed interface AchievementCalendarUiEvent : UiEvent {
    data object ScreenEntered : AchievementCalendarUiEvent
    data class MonthMoved(val offset: Long) : AchievementCalendarUiEvent
    data class DateSelected(val date: LocalDate) : AchievementCalendarUiEvent
}

sealed interface AchievementCalendarUiEffect : UiEffect {
    /** 날짜를 고르면 그 날짜의 리포트로 이동합니다. */
    data class NavigateToReport(val date: LocalDate) : AchievementCalendarUiEffect
    data class ShowMessage(val message: String) : AchievementCalendarUiEffect
}

@HiltViewModel
class AchievementCalendarViewModel @Inject constructor(
    private val getAchievedDatesUseCase: GetAchievedDatesUseCase,
) : BaseViewModel<AchievementCalendarUiState, AchievementCalendarUiEvent, AchievementCalendarUiEffect>(
    AchievementCalendarUiState(),
) {

    override fun handleEvent(event: AchievementCalendarUiEvent) {
        when (event) {
            AchievementCalendarUiEvent.ScreenEntered -> loadMonth()
            is AchievementCalendarUiEvent.MonthMoved -> moveMonth(event)
            is AchievementCalendarUiEvent.DateSelected -> selectDate(event)
        }
    }

    private fun moveMonth(event: AchievementCalendarUiEvent.MonthMoved) {
        if (event.offset > 0 && !currentState.canGoNextMonth) return
        setState { copy(visibleMonth = visibleMonth.plusMonths(event.offset), emptyMessage = null) }
        loadMonth()
    }

    private fun selectDate(event: AchievementCalendarUiEvent.DateSelected) {
        if (event.date.isAfter(currentState.today)) return
        setState { copy(selectedDate = event.date) }
        sendEffect(AchievementCalendarUiEffect.NavigateToReport(event.date))
    }

    private fun loadMonth() {
        if (currentState.isLoading) return
        val month = currentState.requestMonth
        setState { copy(isLoading = true) }
        viewModelScope.launch {
            getAchievedDatesUseCase(month)
                .onSuccess { dates ->
                    setState {
                        copy(achievedDates = dates.toDateSet(), isLoading = false, emptyMessage = null)
                    }
                }
                .onFailure { throwable ->
                    val api = throwable as? ApiException
                    setState {
                        copy(
                            achievedDates = emptySet(),
                            isLoading = false,
                            emptyMessage = when {
                                api != null && api.isUnauthorized -> "다시 로그인해 주세요."
                                else -> "기록을 불러오지 못했어요."
                            },
                        )
                    }
                }
        }
    }
}

private fun List<String>.toDateSet(): Set<LocalDate> =
    mapNotNull { runCatching { LocalDate.parse(it) }.getOrNull() }.toSet()
