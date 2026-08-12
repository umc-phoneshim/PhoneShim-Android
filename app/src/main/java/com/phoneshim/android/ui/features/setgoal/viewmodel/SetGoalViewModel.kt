package com.phoneshim.android.ui.features.setgoal.viewmodel

import androidx.lifecycle.viewModelScope
import com.phoneshim.android.domain.model.GoalLimits
import com.phoneshim.android.domain.model.InstalledApp
import com.phoneshim.android.domain.repository.InstalledAppsRepository
import com.phoneshim.android.domain.usecase.SetGoalUseCase
import com.phoneshim.android.ui.common.base.BaseViewModel
import com.phoneshim.android.ui.common.base.UiEffect
import com.phoneshim.android.ui.common.base.UiEvent
import com.phoneshim.android.ui.common.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

// 화면 검증 기준은 서버 계약(GoalLimits)과 같은 값을 쓴다.
// 숫자를 여기 따로 적어두면 서버 범위가 바뀔 때 한쪽만 고쳐진다.

// 선택 가능한 앱 최대 개수
const val MAX_SELECTABLE_APPS = GoalLimits.MAX_MONITORED_APPS

// 허용되는 최소 목표 사용 시간 (분)
const val MIN_GOAL_MINUTES = GoalLimits.MIN_TARGET_MINUTES

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
// selectedApps/appSettings의 키는 표시명이 아니라 packageName 입니다(차단 엔진 감지 기준).
data class SetGoalUiState(
    val gender: String? = null,
    val ageGroup: String? = null,
    val goalTime: AppTimeInput = AppTimeInput(),
    val blockAfterGoal: Boolean = true,
    val installedApps: List<InstalledApp> = emptyList(),
    val selectedApps: List<InstalledApp> = emptyList(),
    val appSettings: Map<String, AppGoalSetting> = emptyMap(), // key = packageName
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
    data class ToggleApp(val app: InstalledApp) : SetGoalEvent
    data class ToggleAccessLimit(
        val packageName: String,
        val showNotice: Boolean = true,
    ) : SetGoalEvent
    data class SetAppTime(val packageName: String, val timeInput: AppTimeInput) : SetGoalEvent
    // 각 단계 '다음' 시 검증 후 통과하면 NavigateNext, 실패하면 ShowMessage
    data object SubmitGenderAge : SetGoalEvent
    data object SubmitTimeSet : SetGoalEvent
    data object SubmitAppSelection : SetGoalEvent
    data object SubmitAppGoals : SetGoalEvent
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
    private val installedAppsRepository: InstalledAppsRepository,
) : BaseViewModel<SetGoalUiState, SetGoalEvent, SetGoalEffect>(SetGoalUiState()) {

    init {
        loadInstalledApps()
    }

    override fun handleEvent(event: SetGoalEvent) {
        when (event) {
            is SetGoalEvent.SelectGender -> setState { copy(gender = event.gender) }
            is SetGoalEvent.SelectAgeGroup -> setState { copy(ageGroup = event.ageGroup) }
            is SetGoalEvent.SetGoalTime -> setState { copy(goalTime = event.timeInput) }
            is SetGoalEvent.SetBlockAfterGoal -> setBlockAfterGoal(event.enabled)
            is SetGoalEvent.ToggleApp -> toggleApp(event.app)
            is SetGoalEvent.ToggleAccessLimit ->
                toggleAccessLimit(event.packageName, event.showNotice)
            is SetGoalEvent.SetAppTime ->
                updateSetting(event.packageName) { it.copy(timeInput = event.timeInput) }
            SetGoalEvent.SubmitGenderAge -> submitGenderAge()
            SetGoalEvent.SubmitTimeSet -> submitTimeSet()
            SetGoalEvent.SubmitAppSelection -> submitAppSelection()
            SetGoalEvent.SubmitAppGoals -> submitAppGoals()
            SetGoalEvent.SubmitGoal -> submitGoal()
        }
    }

    private fun setBlockAfterGoal(enabled: Boolean) {
        setState { copy(blockAfterGoal = enabled) }
        if (enabled) {
            sendEffect(
                SetGoalEffect.ShowMessage("폰 제한 시 전화 어플, 문자 어플만 사용 가능합니다."),
            )
        }
    }

    // 04-3. 주의 앱 선택 화면에 뿌릴 설치 앱 목록 로드 (그래프 진입 시 1회)
    private fun loadInstalledApps() {
        viewModelScope.launch {
            runCatching { installedAppsRepository.getInstalledApps() }
                .onSuccess { apps -> setState { copy(installedApps = apps) } }
        }
    }

    // 04-3. 주의 앱 선택/해제 (최대 MAX_SELECTABLE_APPS개, 초과 시 안내). 키는 packageName.
    private fun toggleApp(app: InstalledApp) {
        val state = currentState
        when {
            state.selectedApps.any { it.packageName == app.packageName } -> setState {
                copy(
                    selectedApps = selectedApps.filterNot { it.packageName == app.packageName },
                    appSettings = appSettings - app.packageName,
                )
            }
            state.selectedApps.size < MAX_SELECTABLE_APPS -> setState {
                copy(
                    selectedApps = selectedApps + app,
                    appSettings = appSettings + (app.packageName to AppGoalSetting()),
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
        if (state.gender == null || state.ageGroup == null) {
            sendEffect(SetGoalEffect.ShowMessage("성별과 나이 모두 입력해주세요"))
        } else {
            sendEffect(SetGoalEffect.NavigateNext)
        }
    }

    // 04-2. 전체 폰 목표 시간 입력 여부 검증
    private fun submitTimeSet() {
        if (currentState.totalMinutes <= 0) {
            sendEffect(SetGoalEffect.ShowMessage("목표 시간을 입력해주세요."))
        } else {
            sendEffect(SetGoalEffect.NavigateNext)
        }
    }

    // 04-3. 앱 최소 1개 선택 검증
    private fun submitAppSelection() {
        if (currentState.selectedApps.isEmpty()) {
            sendEffect(SetGoalEffect.ShowMessage("어플을 한 개 이상 선택해주세요."))
        } else {
            sendEffect(SetGoalEffect.NavigateNext)
        }
    }

    private fun toggleAccessLimit(packageName: String, showNotice: Boolean) {
        val nextEnabled = !(currentState.appSettings[packageName]?.accessLimited ?: false)
        updateSetting(packageName) { it.copy(accessLimited = nextEnabled) }
        if (nextEnabled && showNotice) {
            sendEffect(SetGoalEffect.ShowMessage("목표 시간 이후 어플 사용이 제한됩니다."))
        }
    }

    // 04-4. 선택한 모든 앱의 목표 시간은 최소 10분이어야 한다.
    private fun submitAppGoals() {
        val hasInvalidGoal = currentState.selectedApps.any { app ->
            val setting = currentState.appSettings[app.packageName] ?: AppGoalSetting()
            setting.timeInput.totalMinutes < MIN_GOAL_MINUTES
        }
        if (hasInvalidGoal) {
            sendEffect(SetGoalEffect.ShowMessage("목표 사용 시간을 10분 이상 입력하세요."))
        } else {
            sendEffect(SetGoalEffect.NavigateNext)
        }
    }

    private fun updateSetting(app: String, transform: (AppGoalSetting) -> AppGoalSetting) = setState {
        val current = appSettings[app] ?: AppGoalSetting()
        copy(appSettings = appSettings + (app to transform(current)))
    }

    // 04-5. 확인 → 목표 저장 (UiState를 도메인 Goal로 매핑해 UseCase 호출)
    private fun submitGoal() {
        viewModelScope.launch {
            setGoalUseCase(currentState.toGoal())
                .onSuccess { sendEffect(SetGoalEffect.NavigateNext) }
                .onFailure { sendEffect(SetGoalEffect.ShowMessage("목표 저장에 실패했어요")) }
        }
    }
}
