package com.phoneshim.android.ui.features.main.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.phoneshim.android.ui.features.main.viewmodel.MainViewModel

@Composable
fun MainScreen(
    onNavigateToSetGoal: () -> Unit,
    onNavigateToMyPage: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = hiltViewModel(),
) {
    // TODO: 05. 메인 화면 - 초기 설정 전/후 상태에 따른 분기 UI 구성
    Column(modifier = modifier.fillMaxSize()) {
    }
}
