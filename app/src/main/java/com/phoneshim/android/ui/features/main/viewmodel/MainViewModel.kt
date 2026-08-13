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
import com.phoneshim.android.domain.usecase.ObserveRemindersUseCase
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
import kotlinx.coroutines.flow.collect
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

    /** 메인 탭 재진입(ON_RESUME) 시 usageStatus/dashboardSummary만 다시 조회한다. */
    data object RefreshUsageAndDashboard : MainUiEvent
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
    private val observeRemindersUseCase: ObserveRemindersUseCase,
) : BaseViewModel<MainUiState, MainUiEvent, MainUiEffect>(MainUiState()) {

    init {
        onEvent(MainUiEvent.LoadDashboard)
        observeTodayReminders()
    }

    override fun handleEvent(event: MainUiEvent) {
        when (event) {
            MainUiEvent.LoadDashboard -> fetchDashboard()
            MainUiEvent.RefreshUsageAndDashboard -> refreshUsageAndDashboard()
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
                // 최초 진입 시 Room 캐시가 완전히 비어있으면(신규 설치 등) observeTodayReminders()의
                // Flow가 서버 데이터를 아직 모른다. 여기서 한 번 서버를 불러 캐시에 기록해두면
                // (ReminderRepositoryImpl.getReminders 내부의 reminderDao.replaceDate) 그 Flow가
                // 재emit되어 화면에 반영된다 — 그래서 응답값 자체는 여기서 쓰지 않는다(#74 타로 리뷰).
                val remindersDeferred = async { getRemindersUseCase(LocalDate.now(KOREA_ZONE_ID)) }

                val isGoalSet = isGoalSetDeferred.await()
                val usageStatusResult = usageStatusDeferred.await()
                val dashboardSummaryResult = dashboardSummaryDeferred.await()
                val userNameResult = userNameDeferred.await()
                val remindersResult = remindersDeferred.await()

                usageStatusResult.onFailure(::reportLoadFailure)
                dashboardSummaryResult.onFailure(::reportLoadFailure)
                userNameResult.onFailure(::reportLoadFailure)
                remindersResult.onFailure(::reportLoadFailure)

                setState {
                    copy(
                        isGoalSet = isGoalSet,
                        usageStatus = usageStatusResult.getOrDefault(usageStatus),
                        dashboardSummary = dashboardSummaryResult.getOrNull() ?: dashboardSummary,
                        isLoading = false,
                        userName = userNameResult.getOrDefault(userName),
                    )
                }
            }
        }
    }

    /**
     * usageStatus/dashboardSummary는 리마인더와 달리 로컬 캐시(Room)가 없어 Flow로 관찰할 수
     * 없습니다. 메인 탭을 벗어났다 돌아와도 MainViewModel은 살아있어(#74 너울 리뷰) 재진입 시
     * 이 둘만 다시 조회합니다. isGoalSet/userName/todayReminders/isLoading은 건드리지 않습니다.
     *
     * [fetchDashboard] 와 같은 isLoading 가드를 둡니다. MainScreen의 ON_RESUME 구독은 등록
     * 시점에 라이프사이클이 이미 RESUMED면 그 자리에서 즉시 한 번 더 발화하는데(안드로이드
     * Lifecycle 표준 동작), 최초 진입 시엔 이게 init의 [fetchDashboard] 가 아직 끝나기 전(
     * isLoading=true)에 도착하는 동기 catch-up 호출입니다. "몇 번째 ON_RESUME인지"를 화면
     * 쪽에서 세는 대신 이 가드로 자연스럽게 무시합니다 — 화면(Composable)은 탭 전환마다
     * dispose/재생성될 수 있어 지역 변수로는 판단이 안정적이지 않기 때문입니다.
     */
    private fun refreshUsageAndDashboard() {
        if (currentState.isLoading) return
        viewModelScope.launch {
            coroutineScope {
                val usageStatusDeferred = async { getUsageStatusUseCase() }
                val dashboardSummaryDeferred = async { getDashboardSummaryUseCase() }

                val usageStatusResult = usageStatusDeferred.await()
                val dashboardSummaryResult = dashboardSummaryDeferred.await()

                usageStatusResult.onFailure(::reportLoadFailure)
                dashboardSummaryResult.onFailure(::reportLoadFailure)

                setState {
                    copy(
                        usageStatus = usageStatusResult.getOrDefault(usageStatus),
                        dashboardSummary = dashboardSummaryResult.getOrNull() ?: dashboardSummary,
                    )
                }
            }
        }
    }

    /**
     * 오늘 리마인더는 Room 캐시([ObserveRemindersUseCase])를 계속 관찰합니다. 리마인더 화면의
     * CRUD가 성공하면 캐시가 즉시 갱신되므로, 메인 화면이 살아있는 동안 계속 최신 상태를
     * 반영합니다 — ON_RESUME 트리거나 별도 재조회 이벤트가 필요 없습니다(#74 리뷰 반영).
     * 캐시가 완전히 비어있는 최초 설치 시점은 이 Flow만으로는 채울 수 없어서, [fetchDashboard]가
     * 최초 1회 [getRemindersUseCase] 로 서버를 불러 캐시를 채우는 역할을 같이 합니다(#74 타로 리뷰).
     */
    private fun observeTodayReminders() {
        viewModelScope.launch {
            observeRemindersUseCase(LocalDate.now(KOREA_ZONE_ID)).collect { reminders ->
                setState { copy(todayReminders = reminders) }
            }
        }
    }

    private fun reportLoadFailure(throwable: Throwable) {
        handleError(throwable) { error -> sendEffect(MainUiEffect.ShowMessage(error.message)) }
    }
}

private val KOREA_ZONE_ID: ZoneId = ZoneId.of("Asia/Seoul")
