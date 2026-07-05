package com.phoneshim.android.ui.features.setgoal.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.phoneshim.android.ui.features.setgoal.viewmodel.SetGoalViewModel

@Composable
fun AccessGoalSetScreen(
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SetGoalViewModel = hiltViewModel(),
) {
    // TODO: 04-3. 접근 횟수 & 목표 설정 UI 구성 (AccessCountPopup / GoalWritePopup 호출)
    Column(modifier = modifier.fillMaxSize()) {
    }
}
