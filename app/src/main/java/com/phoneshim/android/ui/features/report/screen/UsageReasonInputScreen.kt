package com.phoneshim.android.ui.features.report.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.phoneshim.android.ui.features.report.viewmodel.ReportViewModel

@Composable
fun UsageReasonInputScreen(
    entryId: String,
    onSubmitted: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReportViewModel = hiltViewModel(),
) {
    // TODO: 07. 타임테이블 (사용 이유 입력) UI 구성, 제출 완료 시 PointRewardPopup 노출
    Column(modifier = modifier.fillMaxSize()) {
    }
}
