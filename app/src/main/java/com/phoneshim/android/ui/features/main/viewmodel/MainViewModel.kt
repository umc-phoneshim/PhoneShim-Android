package com.phoneshim.android.ui.features.main.viewmodel

import androidx.lifecycle.viewModelScope
import com.phoneshim.android.domain.model.DashboardSummary
import com.phoneshim.android.domain.model.Reminder
import com.phoneshim.android.domain.model.UsageStatus
import com.phoneshim.android.domain.usecase.GetDashboardSummaryUseCase
import com.phoneshim.android.domain.usecase.GetGoalUseCase
import com.phoneshim.android.domain.usecase.GetMyInfoUseCase
import com.phoneshim.android.domain.usecase.GetRemindersUseCase
import com.phoneshim.android.domain.usecase.GetUsageStatusUseCase
import com.phoneshim.android.ui.common.base.BaseViewModel
import com.phoneshim.android.ui.common.base.UiEffect
import com.phoneshim.android.ui.common.base.UiEvent
import com.phoneshim.android.ui.common.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

data class MainUiState(
    val isGoalSet: Boolean = false,
    val usageStatus: List<UsageStatus> = emptyList(),
    val dashboardSummary: DashboardSummary? = null,
    val isLoading: Boolean = false,
    val userName: String = "",
    val todayReminders: List<Reminder> = emptyList(),
) : UiState

sealed interface MainUiEvent : UiEvent {
    data object LoadDashboard : MainUiEvent
}

sealed interface MainUiEffect : UiEffect {
    data class ShowMessage(val message: String) : MainUiEffect
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getUsageStatusUseCase: GetUsageStatusUseCase,
    private val getDashboardSummaryUseCase: GetDashboardSummaryUseCase,
    private val getGoalUseCase: GetGoalUseCase,
    private val getMyInfoUseCase: GetMyInfoUseCase,
    private val getRemindersUseCase: GetRemindersUseCase,
) : BaseViewModel<MainUiState, MainUiEvent, MainUiEffect>(MainUiState()) {

    init {
        onEvent(MainUiEvent.LoadDashboard)
    }

    override fun handleEvent(event: MainUiEvent) {
        when (event) {
            MainUiEvent.LoadDashboard -> fetchDashboard()
        }
    }

    /**
     * 사용 현황/오늘 요약은 서로 독립적인 요청이라 병렬로 불러옵니다.
     * 하나가 실패해도 다른 하나는 그대로 반영하고, 실패한 항목은 직전 값을 유지한 채
     * [MainUiEffect.ShowMessage] 로만 알립니다 — 부분 실패로 전체 화면을 비우지 않기 위함입니다.
     */
    private fun fetchDashboard() {
        if (currentState.isLoading) return
        setState { copy(isLoading = true) }
        viewModelScope.launch {
            coroutineScope {
                // 목표 설정 여부: 오프라인에서도 로컬 캐시로 읽힘(GoalRepository.getGoal 로컬 폴백).
                val isGoalSetDeferred = async { getGoalUseCase().getOrNull() != null }
                val usageStatusDeferred = async { getUsageStatusUseCase() }
                val dashboardSummaryDeferred = async { getDashboardSummaryUseCase() }
                val userNameDeferred = async { getMyInfoUseCase().map { it.nickname } }
                val todayRemindersDeferred = async { getRemindersUseCase(LocalDate.now(KOREA_ZONE_ID)) }

                val isGoalSet = isGoalSetDeferred.await()
                val usageStatusResult = usageStatusDeferred.await()
                val dashboardSummaryResult = dashboardSummaryDeferred.await()
                val userNameResult = userNameDeferred.await()
                val todayRemindersResult = todayRemindersDeferred.await()

                usageStatusResult.onFailure(::reportLoadFailure)
                dashboardSummaryResult.onFailure(::reportLoadFailure)
                userNameResult.onFailure(::reportLoadFailure)
                todayRemindersResult.onFailure(::reportLoadFailure)

                setState {
                    copy(
                        isGoalSet = isGoalSet,
                        usageStatus = usageStatusResult.getOrDefault(usageStatus),
                        dashboardSummary = dashboardSummaryResult.getOrNull() ?: dashboardSummary,
                        isLoading = false,
                        userName = userNameResult.getOrDefault(userName),
                        todayReminders = todayRemindersResult.map { it.reminders }.getOrDefault(todayReminders),
                    )
                }
            }
        }
    }

    private fun reportLoadFailure(throwable: Throwable) {
        handleError(throwable) { error -> sendEffect(MainUiEffect.ShowMessage(error.message)) }
    }
}

private val KOREA_ZONE_ID: ZoneId = ZoneId.of("Asia/Seoul")
