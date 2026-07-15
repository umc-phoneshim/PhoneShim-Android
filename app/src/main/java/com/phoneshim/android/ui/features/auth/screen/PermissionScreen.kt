package com.phoneshim.android.ui.features.auth.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// 접근 권한 동의 화면 (Figma 03. 접근 권한 허용)
@Composable
fun PermissionScreen(
    onAllowAll: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // TODO: 03. 접근 권한 허용 화면 UI 구성 (동의 팝업 + 전체 허용/나중에 설정)
    Column(modifier = modifier.fillMaxSize()) {
    }
}
