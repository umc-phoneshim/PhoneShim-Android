package com.phoneshim.android.ui.features.report.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.phoneshim.android.ui.features.report.viewmodel.ReportViewModel

@Composable
fun ReportSummaryScreen(
    modifier: Modifier = Modifier,
    viewModel: ReportViewModel = hiltViewModel(),
) {
    // TODO: 07. 데일리 리포트 (요약) UI 구성
    Column(modifier = modifier.fillMaxSize()) {
    }
}
