package com.phoneshim.android.ui.features.reminder.viewmodel

import androidx.lifecycle.ViewModel
import com.phoneshim.android.domain.model.Reminder
import com.phoneshim.android.domain.usecase.AddReminderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

data class ReminderUiState(
    val reminders: List<Reminder> = emptyList(),
    val isLoading: Boolean = false,
)

@HiltViewModel
class ReminderViewModel @Inject constructor(
    private val addReminderUseCase: AddReminderUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReminderUiState())
    val uiState: StateFlow<ReminderUiState> = _uiState

    fun addReminder(reminder: Reminder) {
        // TODO: addReminderUseCase 호출 및 uiState 갱신
    }
}
