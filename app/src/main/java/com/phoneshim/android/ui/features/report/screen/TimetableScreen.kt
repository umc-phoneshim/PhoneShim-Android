package com.phoneshim.android.ui.features.report.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.phoneshim.android.ui.features.report.viewmodel.ReportViewModel

@Composable
fun TimetableScreen(
    onEntryClick: (entryId: String) -> Unit,
    onNavigateToAiSuggestion: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReportViewModel = hiltViewModel(),
) {
    // TODO: 07. 데일리 리포트 (타임테이블) UI 구성
    Column(modifier = modifier.fillMaxSize()) {
    }
}
