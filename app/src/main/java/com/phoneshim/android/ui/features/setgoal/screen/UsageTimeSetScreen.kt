package com.phoneshim.android.ui.features.setgoal.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.phoneshim.android.ui.features.setgoal.viewmodel.SetGoalViewModel

@Composable
fun UsageTimeSetScreen(
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SetGoalViewModel = hiltViewModel(),
) {
    // TODO: 04-2. 목표 사용 시간 설정 UI 구성
    Column(modifier = modifier.fillMaxSize()) {
    }
}
