package com.phoneshim.android.ui.features.setgoal.viewmodel

import androidx.lifecycle.ViewModel
import com.phoneshim.android.domain.usecase.SetGoalUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

// 선택 가능한 앱 최대 개수
const val MAX_SELECTABLE_APPS = 5

// 앱별 목표 시간 입력값 (시/분 문자열)
data class AppTimeInput(
    val hour: String = "01",
    val minute: String = "00",
) {
    val totalMinutes: Int
        get() = (hour.toIntOrNull() ?: 0) * 60 + (minute.toIntOrNull() ?: 0)
}

// 앱별 목표 시간 + 접근 제한/목표 문구 설정
data class AppGoalSetting(
    val timeInput: AppTimeInput = AppTimeInput(),
    val accessLimited: Boolean = false,
)

// 목표 설정 플로우(04-1 ~ 04-6) 전체가 공유하는 UI 상태
data class SetGoalUiState(
    val gender: String? = null,
    val ageGroup: String? = null,
    val selectedApps: List<String> = emptyList(),
    val appSettings: Map<String, AppGoalSetting> = emptyMap(),
    val blockAfterGoal: Boolean = false,
    val isLoading: Boolean = false,
) {
    // 선택한 앱들의 목표 시간 합계 (분)
    val totalMinutes: Int
        get() = selectedApps.sumOf { appSettings[it]?.timeInput?.totalMinutes ?: 0 }
}

// 목표 설정 온보딩 화면들이 함께 사용하는 뷰모델.
// 네비게이션 setgoal 그래프 범위로 스코프되어 화면 간 선택 값을 이어줍니다.
@HiltViewModel
class SetGoalViewModel @Inject constructor(
    private val setGoalUseCase: SetGoalUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SetGoalUiState())
    val uiState: StateFlow<SetGoalUiState> = _uiState

    // 04-1. 성별 선택
    fun selectGender(gender: String) {
        _uiState.update { it.copy(gender = gender) }
    }

    // 04-1. 나이대 선택
    fun selectAgeGroup(ageGroup: String) {
        _uiState.update { it.copy(ageGroup = ageGroup) }
    }

    // 04-2. 주의 앱 선택/해제 (최대 MAX_SELECTABLE_APPS개)
    fun toggleApp(app: String) {
        _uiState.update { state ->
            when {
                state.selectedApps.contains(app) -> state.copy(
                    selectedApps = state.selectedApps - app,
                    appSettings = state.appSettings - app,
                )
                state.selectedApps.size < MAX_SELECTABLE_APPS -> state.copy(
                    selectedApps = state.selectedApps + app,
                    appSettings = state.appSettings + (app to AppGoalSetting()),
                )
                else -> state
            }
        }
    }

    // 04-3. 앱별 목표 시간 설정
    fun setAppTime(app: String, timeInput: AppTimeInput) {
        updateSetting(app) { it.copy(timeInput = timeInput) }
    }

    // 04-3. 목표 시간 이후 폰 금지 토글
    fun setBlockAfterGoal(enabled: Boolean) {
        _uiState.update { it.copy(blockAfterGoal = enabled) }
    }

    // 04-4/04-5. 앱별 접근 제한 토글
    fun toggleAccessLimit(app: String) {
        updateSetting(app) { it.copy(accessLimited = !it.accessLimited) }
    }

    private fun updateSetting(app: String, transform: (AppGoalSetting) -> AppGoalSetting) {
        _uiState.update { state ->
            val current = state.appSettings[app] ?: AppGoalSetting()
            state.copy(appSettings = state.appSettings + (app to transform(current)))
        }
    }

    fun submitGoal() {
        // TODO: uiState를 Goal 도메인 모델로 변환해 setGoalUseCase 호출
    }
}
