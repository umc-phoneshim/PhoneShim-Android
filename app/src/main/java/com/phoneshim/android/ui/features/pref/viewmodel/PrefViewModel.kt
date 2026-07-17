package com.phoneshim.android.ui.features.pref.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

private const val MINIMUM_GOAL_MINUTES = 10

@HiltViewModel
class PrefViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(PrefUiState())
    val uiState: StateFlow<PrefUiState> = _uiState.asStateFlow()

    fun showGenderSelection() = updateState { copy(selectionPopup = SelectionPopup.GENDER) }

    fun showAgeGroupSelection() = updateState { copy(selectionPopup = SelectionPopup.AGE_GROUP) }

    fun dismissSelectionPopup() = updateState { copy(selectionPopup = null) }

    fun selectGender(gender: Gender) = updateState {
        copy(
            draftSettings = draftSettings.copy(gender = gender),
            selectionPopup = null,
        )
    }

    fun selectAgeGroup(ageGroup: AgeGroup) = updateState {
        copy(
            draftSettings = draftSettings.copy(ageGroup = ageGroup),
            selectionPopup = null,
        )
    }

    fun showTotalTimeEditor() = updateState {
        val totalMinutes = draftSettings.totalGoalMinutes
        copy(
            timeEditor = TimeEditorState(
                target = TimeEditTarget.TotalGoal,
                hoursInput = (totalMinutes / 60).toString(),
                minutesInput = (totalMinutes % 60).toString(),
            ),
        )
    }

    fun showAppTimeEditor(appId: String) = updateState {
        val appGoal = draftSettings.appGoals.firstOrNull { it.id == appId } ?: return@updateState this
        copy(
            timeEditor = TimeEditorState(
                target = TimeEditTarget.AppGoal(appId),
                hoursInput = (appGoal.goalMinutes / 60).toString(),
                minutesInput = (appGoal.goalMinutes % 60).toString(),
            ),
        )
    }

    fun updateHoursInput(value: String) = updateTimeEditor { editor ->
        editor.copy(hoursInput = value.filter(Char::isDigit), error = null)
    }

    fun updateMinutesInput(value: String) = updateTimeEditor { editor ->
        editor.copy(minutesInput = value.filter(Char::isDigit), error = null)
    }

    fun dismissTimeEditor() = updateState { copy(timeEditor = null) }

    fun confirmGoalTime(): Boolean {
        val editor = _uiState.value.timeEditor ?: return false
        val hours = editor.hoursInput.toIntOrNull() ?: 0
        val minutes = editor.minutesInput.toIntOrNull() ?: 0

        if (minutes !in 0..59) {
            updateTimeEditor { it.copy(error = TimeInputError.INVALID_MINUTE_RANGE) }
            return false
        }

        val totalMinutes = hours * 60 + minutes
        if (totalMinutes < MINIMUM_GOAL_MINUTES) {
            updateTimeEditor { it.copy(error = TimeInputError.BELOW_MINIMUM) }
            return false
        }

        updateState {
            val updatedDraft = when (val target = editor.target) {
                TimeEditTarget.TotalGoal -> draftSettings.copy(totalGoalMinutes = totalMinutes)
                is TimeEditTarget.AppGoal -> draftSettings.copy(
                    appGoals = draftSettings.appGoals.map { goal ->
                        if (goal.id == target.appId) goal.copy(goalMinutes = totalMinutes) else goal
                    },
                )
            }
            copy(
                draftSettings = updatedDraft,
                timeEditor = null,
                validation = validate(updatedDraft),
            )
        }
        return true
    }

    fun toggleAppLimit(appId: String) = updateState {
        val updatedDraft = draftSettings.copy(
            appGoals = draftSettings.appGoals.map { goal ->
                if (goal.id == appId) goal.copy(isLimitEnabled = !goal.isLimitEnabled) else goal
            },
        )
        copy(
            draftSettings = updatedDraft,
            validation = validate(updatedDraft),
        )
    }

    fun showAppGoalEditor(appId: String) = updateState {
        val appGoal = draftSettings.appGoals.firstOrNull { it.id == appId } ?: return@updateState this
        copy(
            editingAppId = appId,
            appDescriptionInput = appGoal.goalDescription,
        )
    }

    fun updateAppDescription(value: String) = updateState { copy(appDescriptionInput = value) }

    fun dismissAppGoalEditor() = updateState {
        copy(editingAppId = null, appDescriptionInput = "")
    }

    fun saveAppDescription() = updateState {
        val appId = editingAppId ?: return@updateState this
        val updatedDraft = draftSettings.copy(
            appGoals = draftSettings.appGoals.map { goal ->
                if (goal.id == appId) goal.copy(goalDescription = appDescriptionInput) else goal
            },
        )
        copy(
            draftSettings = updatedDraft,
            editingAppId = null,
            appDescriptionInput = "",
        )
    }

    fun validateDraft(): PrefValidationResult {
        val result = validate(_uiState.value.draftSettings)
        updateState { copy(validation = result) }
        return result
    }

    fun saveChanges(): Boolean {
        val result = validateDraft()
        if (!result.isValid) return false

        updateState { copy(savedSettings = draftSettings) }
        return true
    }

    fun discardChanges() = updateState {
        copy(
            draftSettings = savedSettings,
            selectionPopup = null,
            timeEditor = null,
            editingAppId = null,
            appDescriptionInput = "",
            validation = PrefValidationResult(),
        )
    }

    fun restoreDraftFromSaved() = discardChanges()

    private fun validate(settings: PrefSettings): PrefValidationResult {
        val isTotalGoalInvalid = settings.totalGoalMinutes < MINIMUM_GOAL_MINUTES
        val invalidAppGoalIds = settings.appGoals
            .filter { goal -> goal.isLimitEnabled && goal.goalMinutes < MINIMUM_GOAL_MINUTES }
            .mapTo(mutableSetOf()) { it.id }

        return PrefValidationResult(
            isValid = !isTotalGoalInvalid && invalidAppGoalIds.isEmpty(),
            isTotalGoalInvalid = isTotalGoalInvalid,
            invalidAppGoalIds = invalidAppGoalIds,
        )
    }

    private inline fun updateState(transform: PrefUiState.() -> PrefUiState) {
        _uiState.value = _uiState.value.transform()
    }

    private inline fun updateTimeEditor(transform: (TimeEditorState) -> TimeEditorState) {
        updateState { copy(timeEditor = timeEditor?.let(transform)) }
    }
}
