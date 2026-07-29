package com.phoneshim.android.ui.features.pref.viewmodel

import com.phoneshim.android.ui.common.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

private const val MINIMUM_GOAL_MINUTES = 10

@HiltViewModel
class PrefViewModel @Inject constructor() :
    BaseViewModel<PrefUiState, PrefUiEvent, PrefUiEffect>(PrefUiState()) {

    override fun handleEvent(event: PrefUiEvent) {
        when (event) {
            PrefUiEvent.GenderSelectionOpened -> showGenderSelection()
            PrefUiEvent.AgeGroupSelectionOpened -> showAgeGroupSelection()
            PrefUiEvent.SelectionPopupDismissed -> dismissSelectionPopup()
            is PrefUiEvent.GenderSelected -> selectGender(event)
            is PrefUiEvent.AgeGroupSelected -> selectAgeGroup(event)
            PrefUiEvent.TotalTimeEditorOpened -> showTotalTimeEditor()
            is PrefUiEvent.AppTimeEditorOpened -> showAppTimeEditor(event)
            is PrefUiEvent.HoursInputChanged -> updateHoursInput(event)
            is PrefUiEvent.MinutesInputChanged -> updateMinutesInput(event)
            PrefUiEvent.TimeEditorLimitToggled -> toggleTimeEditorLimit()
            PrefUiEvent.TimeEditorDismissed -> dismissTimeEditor()
            PrefUiEvent.GoalTimeConfirmed -> confirmGoalTime()
            is PrefUiEvent.AppLimitToggled -> toggleAppLimit(event)
            is PrefUiEvent.AppGoalEditorOpened -> showAppGoalEditor(event)
            is PrefUiEvent.AppDescriptionChanged -> updateAppDescription(event)
            PrefUiEvent.AppGoalEditorDismissed -> dismissAppGoalEditor()
            PrefUiEvent.AppDescriptionSaved -> saveAppDescription()
            PrefUiEvent.SaveChanges -> saveChanges()
            PrefUiEvent.DiscardChanges -> discardChanges()
        }
    }

    private fun showGenderSelection() = setState { copy(selectionPopup = SelectionPopup.GENDER) }

    private fun showAgeGroupSelection() = setState { copy(selectionPopup = SelectionPopup.AGE_GROUP) }

    private fun dismissSelectionPopup() = setState { copy(selectionPopup = null) }

    private fun selectGender(event: PrefUiEvent.GenderSelected) = setState {
        copy(
            draftSettings = draftSettings.copy(gender = event.gender),
            selectionPopup = null,
        )
    }

    private fun selectAgeGroup(event: PrefUiEvent.AgeGroupSelected) = setState {
        copy(
            draftSettings = draftSettings.copy(ageGroup = event.ageGroup),
            selectionPopup = null,
        )
    }

    private fun showTotalTimeEditor() = setState {
        val totalMinutes = draftSettings.totalGoalMinutes
        copy(
            timeEditor = TimeEditorState(
                target = TimeEditTarget.TotalGoal,
                hoursInput = (totalMinutes / 60).toString().padStart(2, '0'),
                minutesInput = (totalMinutes % 60).toString().padStart(2, '0'),
                isLimitEnabled = draftSettings.isTotalLimitEnabled,
            ),
        )
    }

    private fun showAppTimeEditor(event: PrefUiEvent.AppTimeEditorOpened) = setState {
        val appGoal = draftSettings.appGoals.firstOrNull { it.id == event.appId }
            ?: return@setState this
        copy(
            timeEditor = TimeEditorState(
                target = TimeEditTarget.AppGoal(event.appId),
                hoursInput = (appGoal.goalMinutes / 60).toString().padStart(2, '0'),
                minutesInput = (appGoal.goalMinutes % 60).toString().padStart(2, '0'),
                isLimitEnabled = appGoal.isLimitEnabled,
            ),
        )
    }

    private fun updateHoursInput(event: PrefUiEvent.HoursInputChanged) = updateTimeEditor { editor ->
        editor.copy(hoursInput = event.value.filter(Char::isDigit), error = null)
    }

    private fun updateMinutesInput(event: PrefUiEvent.MinutesInputChanged) = updateTimeEditor { editor ->
        editor.copy(minutesInput = event.value.filter(Char::isDigit), error = null)
    }

    private fun toggleTimeEditorLimit() = updateTimeEditor { editor ->
        editor.copy(isLimitEnabled = !editor.isLimitEnabled)
    }

    private fun dismissTimeEditor() = setState { copy(timeEditor = null) }

    private fun confirmGoalTime() {
        val editor = currentState.timeEditor ?: return
        val hours = editor.hoursInput.toIntOrNull() ?: 0
        val minutes = editor.minutesInput.toIntOrNull() ?: 0

        if (minutes !in 0..59) {
            updateTimeEditor { it.copy(error = TimeInputError.INVALID_MINUTE_RANGE) }
            return
        }

        val totalMinutes = hours * 60 + minutes
        if (totalMinutes < MINIMUM_GOAL_MINUTES) {
            updateTimeEditor { it.copy(error = TimeInputError.BELOW_MINIMUM) }
            return
        }

        setState {
            val updatedDraft = when (val target = editor.target) {
                TimeEditTarget.TotalGoal -> draftSettings.copy(
                    totalGoalMinutes = totalMinutes,
                    isTotalLimitEnabled = editor.isLimitEnabled,
                )
                is TimeEditTarget.AppGoal -> draftSettings.copy(
                    appGoals = draftSettings.appGoals.map { goal ->
                        if (goal.id == target.appId) {
                            goal.copy(
                                goalMinutes = totalMinutes,
                                isLimitEnabled = editor.isLimitEnabled,
                            )
                        } else {
                            goal
                        }
                    },
                )
            }
            copy(
                draftSettings = updatedDraft,
                timeEditor = null,
                validation = validate(updatedDraft),
            )
        }
    }

    private fun toggleAppLimit(event: PrefUiEvent.AppLimitToggled) = setState {
        val updatedDraft = draftSettings.copy(
            appGoals = draftSettings.appGoals.map { goal ->
                if (goal.id == event.appId) {
                    goal.copy(isLimitEnabled = !goal.isLimitEnabled)
                } else {
                    goal
                }
            },
        )
        copy(
            draftSettings = updatedDraft,
            validation = validate(updatedDraft),
        )
    }

    private fun showAppGoalEditor(event: PrefUiEvent.AppGoalEditorOpened) = setState {
        val appGoal = draftSettings.appGoals.firstOrNull { it.id == event.appId }
            ?: return@setState this
        copy(
            editingAppId = event.appId,
            appDescriptionInput = appGoal.goalDescription,
        )
    }

    private fun updateAppDescription(event: PrefUiEvent.AppDescriptionChanged) = setState {
        copy(appDescriptionInput = event.value)
    }

    private fun dismissAppGoalEditor() = setState {
        copy(editingAppId = null, appDescriptionInput = "")
    }

    private fun saveAppDescription() = setState {
        val appId = editingAppId ?: return@setState this
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

    private fun validateDraft(): PrefValidationResult {
        val result = validate(currentState.draftSettings)
        setState { copy(validation = result) }
        return result
    }

    private fun saveChanges() {
        val result = validateDraft()
        if (!result.isValid) return

        setState { copy(savedSettings = draftSettings) }
        sendEffect(PrefUiEffect.SettingsSaved)
    }

    private fun discardChanges() = setState {
        copy(
            draftSettings = savedSettings,
            selectionPopup = null,
            timeEditor = null,
            editingAppId = null,
            appDescriptionInput = "",
            validation = PrefValidationResult(),
        )
    }

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

    private fun updateTimeEditor(transform: (TimeEditorState) -> TimeEditorState) {
        setState { copy(timeEditor = timeEditor?.let(transform)) }
    }
}
