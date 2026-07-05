package com.phoneshim.android.ui.features.reminder.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.phoneshim.android.ui.features.reminder.viewmodel.ReminderViewModel

@Composable
fun ReminderScreen(
    onAddReminder: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReminderViewModel = hiltViewModel(),
) {
    // TODO: 06. 리마인더 화면 - 초기 설정 전/후 상태에 따른 분기 UI 구성
    Column(modifier = modifier.fillMaxSize()) {
    }
}
