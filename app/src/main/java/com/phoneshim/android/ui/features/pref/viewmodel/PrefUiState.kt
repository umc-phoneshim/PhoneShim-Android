package com.phoneshim.android.ui.features.pref.viewmodel

import com.phoneshim.android.ui.common.base.UiEffect
import com.phoneshim.android.ui.common.base.UiEvent
import com.phoneshim.android.ui.common.base.UiState

enum class Gender {
    MALE,
    FEMALE,
}

enum class AgeGroup {
    TEENS,
    TWENTIES,
    THIRTIES,
    FORTIES,
    FIFTIES_OR_MORE,
}

data class AppGoal(
    val id: String,
    val appName: String,
    val goalMinutes: Int,
    val isLimitEnabled: Boolean,
    val goalDescription: String,
    val packageName: String = id,
    val targetCount: Int = 1,
)

data class PrefSettings(
    val gender: Gender,
    val ageGroup: AgeGroup,
    val totalGoalMinutes: Int,
    val isTotalLimitEnabled: Boolean,
    val appGoals: List<AppGoal>,
    val goalId: String? = null,
)

enum class SelectionPopup {
    GENDER,
    AGE_GROUP,
}

enum class TimeInputError {
    INVALID_MINUTE_RANGE,
    BELOW_MINIMUM,
}

data class TimeEditorState(
    val target: TimeEditTarget,
    val hoursInput: String,
    val minutesInput: String,
    val isLimitEnabled: Boolean = false,
    val error: TimeInputError? = null,
)

sealed interface TimeEditTarget {
    data object TotalGoal : TimeEditTarget
    data class AppGoal(val appId: String) : TimeEditTarget
}

data class PrefValidationResult(
    val isValid: Boolean = true,
    val isTotalGoalInvalid: Boolean = false,
    val invalidAppGoalIds: Set<String> = emptySet(),
)

data class PrefUiState(
    val savedSettings: PrefSettings = PrefMockData.initialSettings,
    val draftSettings: PrefSettings = savedSettings,
    val selectionPopup: SelectionPopup? = null,
    val timeEditor: TimeEditorState? = null,
    val editingAppId: String? = null,
    val appDescriptionInput: String = "",
    val validation: PrefValidationResult = PrefValidationResult(),
    val isLoading: Boolean = true,
    val hasGoalData: Boolean = false,
    /** GET /users/me에 성별·연령대가 모두 존재하는지. null이면 다음 저장 때 서버에 보충합니다. */
    val hasServerUserProfile: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
) : UiState {
    val hasUnsavedChanges: Boolean
        get() = draftSettings != savedSettings
}

sealed interface PrefUiEvent : UiEvent {
    data object GenderSelectionOpened : PrefUiEvent
    data object AgeGroupSelectionOpened : PrefUiEvent
    data object SelectionPopupDismissed : PrefUiEvent
    data class GenderSelected(val gender: Gender) : PrefUiEvent
    data class AgeGroupSelected(val ageGroup: AgeGroup) : PrefUiEvent
    data object TotalTimeEditorOpened : PrefUiEvent
    data object TotalLimitToggled : PrefUiEvent
    data class AppTimeEditorOpened(val appId: String) : PrefUiEvent
    data class HoursInputChanged(val value: String) : PrefUiEvent
    data class MinutesInputChanged(val value: String) : PrefUiEvent
    data object TimeEditorLimitToggled : PrefUiEvent
    data object TimeEditorDismissed : PrefUiEvent
    data object GoalTimeConfirmed : PrefUiEvent
    data class AppLimitToggled(val appId: String) : PrefUiEvent
    data class AppGoalEditorOpened(val appId: String) : PrefUiEvent
    data class AppDescriptionChanged(val value: String) : PrefUiEvent
    data object AppGoalEditorDismissed : PrefUiEvent
    data object AppDescriptionSaved : PrefUiEvent
    data object SaveChanges : PrefUiEvent
    data object DiscardChanges : PrefUiEvent
}

sealed interface PrefUiEffect : UiEffect {
    data object SettingsSaved : PrefUiEffect
}

object PrefMockData {
    const val DEFAULT_GOAL_DESCRIPTION =
        "숏폼 이용이 잦아 관련 어플에 대해 강한 제한이 필요합니다."

    val initialSettings = PrefSettings(
        gender = Gender.MALE,
        ageGroup = AgeGroup.TWENTIES,
        totalGoalMinutes = 210,
        isTotalLimitEnabled = true,
        appGoals = listOf(
            AppGoal(
                id = "kakao",
                appName = "카카오톡",
                goalMinutes = 60,
                isLimitEnabled = true,
                goalDescription = DEFAULT_GOAL_DESCRIPTION,
            ),
            AppGoal(
                id = "facebook",
                appName = "페이스북",
                goalMinutes = 90,
                isLimitEnabled = true,
                goalDescription = DEFAULT_GOAL_DESCRIPTION,
            ),
            AppGoal(
                id = "tiktok",
                appName = "틱톡",
                goalMinutes = 60,
                isLimitEnabled = true,
                goalDescription = DEFAULT_GOAL_DESCRIPTION,
            ),
        ),
    )
}
