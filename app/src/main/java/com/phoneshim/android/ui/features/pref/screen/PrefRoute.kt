package com.phoneshim.android.ui.features.pref.screen

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.phoneshim.android.BuildConfig
import androidx.hilt.navigation.compose.hiltViewModel
import com.phoneshim.android.ui.common.BottomBarTab
import com.phoneshim.android.ui.features.pref.viewmodel.PrefUiEffect
import com.phoneshim.android.ui.features.pref.viewmodel.PrefUiEvent
import com.phoneshim.android.ui.features.pref.viewmodel.PrefViewModel

@Composable
fun PrefRoute(
    onBack: () -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit,
    selectedBottomTab: BottomBarTab,
    onNavigateToMain: () -> Unit,
    onNavigateToReminder: () -> Unit,
    onNavigateToReport: () -> Unit,
    onDemoReset: () -> Unit,
    viewModel: PrefViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val discardAndGoBack = {
        viewModel.onEvent(PrefUiEvent.DiscardChanges)
        onBack()
    }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                PrefUiEffect.SettingsSaved -> onSave()
                PrefUiEffect.DemoDataReset -> {
                    Toast.makeText(context, "시연 데이터를 초기화했습니다.", Toast.LENGTH_SHORT).show()
                    onDemoReset()
                }
            }
        }
    }

    BackHandler(onBack = discardAndGoBack)

    PrefScreen(
        uiState = uiState,
        selectedBottomTab = selectedBottomTab,
        onBottomNavSelected = { tab ->
            viewModel.onEvent(PrefUiEvent.DiscardChanges)
            when (tab) {
                BottomBarTab.MAIN -> onNavigateToMain()
                BottomBarTab.REMINDER -> onNavigateToReminder()
                BottomBarTab.REPORT -> onNavigateToReport()
            }
        },
        onBack = discardAndGoBack,
        onCancel = {
            viewModel.onEvent(PrefUiEvent.DiscardChanges)
            onCancel()
        },
        onSave = { viewModel.onEvent(PrefUiEvent.SaveChanges) },
        onGenderClick = { viewModel.onEvent(PrefUiEvent.GenderSelectionOpened) },
        onAgeGroupClick = { viewModel.onEvent(PrefUiEvent.AgeGroupSelectionOpened) },
        onGenderSelected = { viewModel.onEvent(PrefUiEvent.GenderSelected(it)) },
        onAgeGroupSelected = { viewModel.onEvent(PrefUiEvent.AgeGroupSelected(it)) },
        onSelectionDismissed = { viewModel.onEvent(PrefUiEvent.SelectionPopupDismissed) },
        onTotalGoalClick = { viewModel.onEvent(PrefUiEvent.TotalTimeEditorOpened) },
        onHoursChanged = { viewModel.onEvent(PrefUiEvent.HoursInputChanged(it)) },
        onMinutesChanged = { viewModel.onEvent(PrefUiEvent.MinutesInputChanged(it)) },
        onTimeEditorLimitToggled = { viewModel.onEvent(PrefUiEvent.TimeEditorLimitToggled) },
        onTimeEditorDismissed = { viewModel.onEvent(PrefUiEvent.TimeEditorDismissed) },
        onTimeEditorConfirmed = { viewModel.onEvent(PrefUiEvent.GoalTimeConfirmed) },
        onEditAppTime = { viewModel.onEvent(PrefUiEvent.AppTimeEditorOpened(it)) },
        onToggleLimit = { viewModel.onEvent(PrefUiEvent.AppLimitToggled(it)) },
        onAppDescriptionChanged = {
            viewModel.onEvent(PrefUiEvent.AppDescriptionChanged(it))
        },
        onAppGoalEditorDismissed = {
            viewModel.onEvent(PrefUiEvent.AppGoalEditorDismissed)
        },
        onAppGoalSaved = { viewModel.onEvent(PrefUiEvent.AppDescriptionSaved) },
        onResetDemoData = if (BuildConfig.IS_DEMO) {
            { viewModel.onEvent(PrefUiEvent.ResetDemoData) }
        } else {
            null
        },
    )
}
