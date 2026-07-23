package com.phoneshim.android.ui.features.setgoal.viewmodel

import com.phoneshim.android.domain.usecase.SetGoalUseCase
import com.phoneshim.android.ui.common.base.BaseViewModel
import com.phoneshim.android.ui.common.base.UiEffect
import com.phoneshim.android.ui.common.base.UiEvent
import com.phoneshim.android.ui.common.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

// 선택 가능한 앱 최대 개수
const val MAX_SELECTABLE_APPS = 5

// 허용되는 최소 목표 사용 시간 (분)
const val MIN_GOAL_MINUTES = 10

// 목표 사용 시간 입력값 (시/분 문자열)
data class AppTimeInput(
    val hour: String = "00",
    val minute: String = "00",
) {
    val totalMinutes: Int
        get() = (hour.toIntOrNull() ?: 0) * 60 + (minute.toIntOrNull() ?: 0)
}

// 앱별 설정 (목표 시간 + 접근 제한) — 04-4에서 입력
data class AppGoalSetting(
    val timeInput: AppTimeInput = AppTimeInput("01", "00"),
    val accessLimited: Boolean = false,
)

// 목표 설정 플로우(04-1 ~ 04-4) 전체가 공유하는 UI 상태
data class SetGoalUiState(
    val gender: String? = null,
    val ageGroup: String? = null,
    val goalTime: AppTimeInput = AppTimeInput(),
    val blockAfterGoal: Boolean = false,
    val selectedApps: List<String> = emptyList(),
    val appSettings: Map<String, AppGoalSetting> = emptyMap(),
    val isLoading: Boolean = false,
) : UiState {
    // 하루 목표 사용 시간 합계 (분)
    val totalMinutes: Int
        get() = goalTime.totalMinutes
}

// 목표 설정 화면에서 발생하는 사용자 이벤트
sealed interface SetGoalEvent : UiEvent {
    data class SelectGender(val gender: String) : SetGoalEvent
    data class SelectAgeGroup(val ageGroup: String) : SetGoalEvent
    data class SetGoalTime(val timeInput: AppTimeInput) : SetGoalEvent
    data class SetBlockAfterGoal(val enabled: Boolean) : SetGoalEvent
    data class ToggleApp(val app: String) : SetGoalEvent
    data class ToggleAccessLimit(val app: String) : SetGoalEvent
    data class SetAppTime(val app: String, val timeInput: AppTimeInput) : SetGoalEvent
    // 각 단계 '다음' 시 검증 후 통과하면 NavigateNext, 실패하면 ShowMessage
    data object SubmitGenderAge : SetGoalEvent
    data object SubmitTimeSet : SetGoalEvent
    data object SubmitAppSelection : SetGoalEvent
    data object SubmitGoal : SetGoalEvent
}

// 목표 설정 화면의 1회성 효과
sealed interface SetGoalEffect : UiEffect {
    data class ShowMessage(val message: String) : SetGoalEffect
    data object NavigateNext : SetGoalEffect
}

// 목표 설정 온보딩 화면들이 함께 사용하는 뷰모델.
// 네비게이션 setgoal 그래프 범위로 스코프되어 화면 간 선택 값을 이어줍니다.
@HiltViewModel
class SetGoalViewModel @Inject constructor(
    private val setGoalUseCase: SetGoalUseCase,
) : BaseViewModel<SetGoalUiState, SetGoalEvent, SetGoalEffect>(SetGoalUiState()) {

    override fun handleEvent(event: SetGoalEvent) {
        when (event) {
            is SetGoalEvent.SelectGender -> setState { copy(gender = event.gender) }
            is SetGoalEvent.SelectAgeGroup -> setState { copy(ageGroup = event.ageGroup) }
            is SetGoalEvent.SetGoalTime -> setState { copy(goalTime = event.timeInput) }
            is SetGoalEvent.SetBlockAfterGoal -> setState { copy(blockAfterGoal = event.enabled) }
            is SetGoalEvent.ToggleApp -> toggleApp(event.app)
            is SetGoalEvent.ToggleAccessLimit ->
                updateSetting(event.app) { it.copy(accessLimited = !it.accessLimited) }
            is SetGoalEvent.SetAppTime ->
                updateSetting(event.app) { it.copy(timeInput = event.timeInput) }
            SetGoalEvent.SubmitGenderAge -> submitGenderAge()
            SetGoalEvent.SubmitTimeSet -> submitTimeSet()
            SetGoalEvent.SubmitAppSelection -> submitAppSelection()
            SetGoalEvent.SubmitGoal -> submitGoal()
        }
    }

    // 04-3. 주의 앱 선택/해제 (최대 MAX_SELECTABLE_APPS개, 초과 시 안내)
    private fun toggleApp(app: String) {
        val state = currentState
        when {
            state.selectedApps.contains(app) -> setState {
                copy(selectedApps = selectedApps - app, appSettings = appSettings - app)
            }
            state.selectedApps.size < MAX_SELECTABLE_APPS -> setState {
                copy(
                    selectedApps = selectedApps + app,
                    appSettings = appSettings + (app to AppGoalSetting()),
                )
            }
            else -> sendEffect(
                SetGoalEffect.ShowMessage("주의 앱은 최대 ${MAX_SELECTABLE_APPS}개까지 선택할 수 있어요"),
            )
        }
    }

    // 04-1. 성별/나이 필수 검증
    private fun submitGenderAge() {
        val state = currentState
        when {
            state.gender == null -> sendEffect(SetGoalEffect.ShowMessage("성별을 선택해주세요"))
            state.ageGroup == null -> sendEffect(SetGoalEffect.ShowMessage("나이를 선택해주세요"))
            else -> sendEffect(SetGoalEffect.NavigateNext)
        }
    }

    // 04-2. 목표 시간 최소 10분 검증
    private fun submitTimeSet() {
        if (currentState.totalMinutes < MIN_GOAL_MINUTES) {
            sendEffect(
                SetGoalEffect.ShowMessage("목표 시간은 최소 ${MIN_GOAL_MINUTES}분 이상으로 설정해주세요"),
            )
        } else {
            sendEffect(SetGoalEffect.NavigateNext)
        }
    }

    // 04-3. 앱 최소 1개 선택 검증
    private fun submitAppSelection() {
        if (currentState.selectedApps.isEmpty()) {
            sendEffect(SetGoalEffect.ShowMessage("관리할 주의 앱을 최소 1개 선택해주세요"))
        } else {
            sendEffect(SetGoalEffect.NavigateNext)
        }
    }

    private fun updateSetting(app: String, transform: (AppGoalSetting) -> AppGoalSetting) = setState {
        val current = appSettings[app] ?: AppGoalSetting()
        copy(appSettings = appSettings + (app to transform(current)))
    }

    private fun submitGoal() {
        // TODO: uiState를 Goal 도메인 모델로 변환해 setGoalUseCase 호출
    }
}
