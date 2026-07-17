package com.phoneshim.android.ui.features.pref.screen

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import com.phoneshim.android.ui.features.pref.viewmodel.PrefViewModel

@Composable
fun PrefRoute(
    onBack: () -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit,
    viewModel: PrefViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    val discardAndGoBack = {
        viewModel.discardChanges()
        onBack()
    }

    BackHandler(onBack = discardAndGoBack)

    PrefScreen(
        uiState = uiState,
        onBack = discardAndGoBack,
        onCancel = {
            viewModel.discardChanges()
            onCancel()
        },
        onSave = {
            if (viewModel.saveChanges()) onSave()
        },
        onGenderClick = viewModel::showGenderSelection,
        onAgeGroupClick = viewModel::showAgeGroupSelection,
        onGenderSelected = viewModel::selectGender,
        onAgeGroupSelected = viewModel::selectAgeGroup,
        onSelectionDismissed = viewModel::dismissSelectionPopup,
        onTotalGoalClick = viewModel::showTotalTimeEditor,
        onHoursChanged = viewModel::updateHoursInput,
        onMinutesChanged = viewModel::updateMinutesInput,
        onTimeEditorDismissed = viewModel::dismissTimeEditor,
        onTimeEditorConfirmed = { viewModel.confirmGoalTime() },
        onEditAppTime = viewModel::showAppTimeEditor,
        onToggleLimit = viewModel::toggleAppLimit,
        onEditAppGoal = viewModel::showAppGoalEditor,
        onAppDescriptionChanged = viewModel::updateAppDescription,
        onAppGoalEditorDismissed = viewModel::dismissAppGoalEditor,
        onAppGoalSaved = viewModel::saveAppDescription,
    )
}
