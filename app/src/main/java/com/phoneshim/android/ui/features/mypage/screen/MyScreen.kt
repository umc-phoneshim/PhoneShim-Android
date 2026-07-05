package com.phoneshim.android.ui.features.mypage.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.phoneshim.android.ui.features.mypage.viewmodel.MyPageViewModel

@Composable
fun MyScreen(
    onNavigateToSideMenu: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MyPageViewModel = hiltViewModel(),
) {
    // TODO: 08. 마이페이지 본체 UI 구성
    Column(modifier = modifier.fillMaxSize()) {
    }
}
