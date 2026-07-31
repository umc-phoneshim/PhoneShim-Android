package com.phoneshim.android.ui.features.report.viewmodel

import androidx.lifecycle.viewModelScope
import com.phoneshim.android.data.api.ApiException
import com.phoneshim.android.domain.model.ReasonCalendarDay
import com.phoneshim.android.domain.usecase.GetUsageReasonCalendarUseCase
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
 * 사용 이유 입력 달력. GET /api/usage-reasons/calendar?month=YYYY-MM
 *
 * 서버가 아직 "예정" 상태라 실패하면 빈 달력과 안내 문구를 보여줍니다.
 */
data class UsageReasonCalendarUiState(
    val visibleMonth: YearMonth = YearMonth.now(),
    val today: LocalDate = LocalDate.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val daysWithReason: Set<LocalDate> = emptySet(),
    val isLoading: Boolean = false,
    val emptyMessage: String? = null,
) : UiState {
    val monthLabel: String get() = visibleMonth.format(DateTimeFormatter.ofPattern("yyyy.MM"))

    /** API 요청용 월 문자열. 예) "2026-07" */
    val requestMonth: String get() = visibleMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"))

    val canGoNextMonth: Boolean get() = visibleMonth < YearMonth.from(today)

    val writtenCount: Int get() = daysWithReason.size
}

sealed interface UsageReasonCalendarUiEvent : UiEvent {
    data object ScreenEntered : UsageReasonCalendarUiEvent
    data class MonthMoved(val offset: Long) : UsageReasonCalendarUiEvent
    data class DateSelected(val date: LocalDate) : UsageReasonCalendarUiEvent
}

sealed interface UsageReasonCalendarUiEffect : UiEffect {
    /** 사유가 없는 날을 고르면 입력 화면으로 보냅니다. */
    data class NavigateToInput(val date: LocalDate) : UsageReasonCalendarUiEffect
    data class ShowMessage(val message: String) : UsageReasonCalendarUiEffect
}

@HiltViewModel
class UsageReasonCalendarViewModel @Inject constructor(
    private val getUsageReasonCalendarUseCase: GetUsageReasonCalendarUseCase,
) : BaseViewModel<UsageReasonCalendarUiState, UsageReasonCalendarUiEvent, UsageReasonCalendarUiEffect>(
    UsageReasonCalendarUiState(),
) {

    override fun handleEvent(event: UsageReasonCalendarUiEvent) {
        when (event) {
            UsageReasonCalendarUiEvent.ScreenEntered -> loadMonth()
            is UsageReasonCalendarUiEvent.MonthMoved -> moveMonth(event)
            is UsageReasonCalendarUiEvent.DateSelected -> selectDate(event)
        }
    }

    private fun moveMonth(event: UsageReasonCalendarUiEvent.MonthMoved) {
        val state = currentState
        if (event.offset > 0 && !state.canGoNextMonth) return
        setState { copy(visibleMonth = visibleMonth.plusMonths(event.offset), emptyMessage = null) }
        loadMonth()
    }

    private fun selectDate(event: UsageReasonCalendarUiEvent.DateSelected) {
        if (event.date.isAfter(currentState.today)) return
        setState { copy(selectedDate = event.date) }
        if (event.date !in currentState.daysWithReason) {
            sendEffect(UsageReasonCalendarUiEffect.NavigateToInput(event.date))
        }
    }

    private fun loadMonth() {
        if (currentState.isLoading) return
        val month = currentState.requestMonth
        setState { copy(isLoading = true) }
        viewModelScope.launch {
            getUsageReasonCalendarUseCase(month)
                .onSuccess { days -> setState { copy(daysWithReason = days.toDateSet(), isLoading = false, emptyMessage = null) } }
                .onFailure { throwable ->
                    val api = throwable as? ApiException
                    setState {
                        copy(
                            daysWithReason = emptySet(),
                            isLoading = false,
                            // 서버 미구현 구간에서도 화면이 오류처럼 보이지 않게 안내로 처리합니다.
                            emptyMessage = when {
                                api != null && api.isUnauthorized -> "다시 로그인해 주세요."
                                else -> "아직 기록을 불러올 수 없어요."
                            },
                        )
                    }
                }
        }
    }
}

private fun List<ReasonCalendarDay>.toDateSet(): Set<LocalDate> =
    filter { it.hasReason }
        .mapNotNull { runCatching { LocalDate.parse(it.date) }.getOrNull() }
        .toSet()
