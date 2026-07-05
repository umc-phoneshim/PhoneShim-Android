package com.phoneshim.android.ui.features.setgoal.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.phoneshim.android.ui.features.setgoal.viewmodel.SetGoalViewModel

@Composable
fun AppSelectScreen(
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SetGoalViewModel = hiltViewModel(),
) {
    // TODO: 04-1. 절제할 어플 선택 목록 구성
    Column(modifier = modifier.fillMaxSize()) {
    }
}
