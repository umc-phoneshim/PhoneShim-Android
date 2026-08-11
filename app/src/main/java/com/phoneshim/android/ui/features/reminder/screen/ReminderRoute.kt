package com.phoneshim.android.ui.features.reminder.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material3.SnackbarHostState
import com.phoneshim.android.ui.features.reminder.viewmodel.ReminderUiEffect
import com.phoneshim.android.ui.features.reminder.viewmodel.ReminderUiEvent
import com.phoneshim.android.ui.features.reminder.viewmodel.ReminderViewModel
import com.phoneshim.android.ui.common.base.CollectCommonEffect

@Composable
fun ReminderRoute(
    onNavigateToSettings: () -> Unit,
    onNavigateToMyPage: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToMain: () -> Unit = {},
    onNavigateToReport: () -> Unit = {},
    onAuthExpired: () -> Unit = {},
    viewModel: ReminderViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    CollectCommonEffect(viewModel, onAuthExpired)

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ReminderUiEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    ReminderScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onNavigateToSettings = onNavigateToSettings,
        onNavigateToMyPage = onNavigateToMyPage,
        onNavigateToMain = onNavigateToMain,
        onNavigateToReport = onNavigateToReport,
        onSelectDate = { viewModel.onEvent(ReminderUiEvent.DateSelected(it)) },
        onPreviousMonth = { viewModel.onEvent(ReminderUiEvent.MonthMoved(-1)) },
        onNextMonth = { viewModel.onEvent(ReminderUiEvent.MonthMoved(1)) },
        onRetry = { viewModel.onEvent(ReminderUiEvent.RetryClicked) },
        onAddTask = { viewModel.onEvent(ReminderUiEvent.AddTaskClicked) },
        onEditTask = { viewModel.onEvent(ReminderUiEvent.EditTaskClicked(it)) },
        onMoveTask = { from, to -> viewModel.onEvent(ReminderUiEvent.TaskMoved(from, to)) },
        onDismissPopup = { viewModel.onEvent(ReminderUiEvent.PopupDismissed) },
        onTitleChange = { viewModel.onEvent(ReminderUiEvent.TitleChanged(it)) },
        onStartTimeChange = { viewModel.onEvent(ReminderUiEvent.StartTimeChanged(it)) },
        onEndTimeChange = { viewModel.onEvent(ReminderUiEvent.EndTimeChanged(it)) },
        onRestrictionModeChange = { viewModel.onEvent(ReminderUiEvent.RestrictionModeChanged(it)) },
        onToggleRestrictedApp = { viewModel.onEvent(ReminderUiEvent.RestrictedAppToggled(it)) },
        onSaveTask = { viewModel.onEvent(ReminderUiEvent.SaveTaskClicked) },
        onDeleteTask = { viewModel.onEvent(ReminderUiEvent.DeleteTaskClicked) },
        modifier = modifier,
    )
}
