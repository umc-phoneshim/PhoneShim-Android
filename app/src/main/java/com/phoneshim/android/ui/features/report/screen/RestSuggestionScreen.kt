package com.phoneshim.android.ui.features.report.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.phoneshim.android.domain.model.RestSuggestion
import com.phoneshim.android.ui.common.TopAppBar
import com.phoneshim.android.ui.features.report.component.ReportCard
import com.phoneshim.android.ui.features.report.viewmodel.ReportUiEffect
import com.phoneshim.android.ui.features.report.viewmodel.ReportUiEvent
import com.phoneshim.android.ui.features.report.viewmodel.ReportUiState
import com.phoneshim.android.ui.features.report.viewmodel.ReportViewModel
import com.phoneshim.android.ui.theme.PhoneShimDimens
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimType

/**
 * 07. 쉼이의 제안.
 *
 * 클라이언트가 분석하지 않습니다. 백엔드가 사용 로그와 사용 사유를 분석해
 * 완성된 문구를 내려주면(POST /api/ai/daily-feedback) 화면은 그대로 출력만 합니다.
 */
@Composable
fun RestSuggestionRoute(
    onNavigateToSummary: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReportViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.onEvent(ReportUiEvent.RestSuggestionRequested)
    }
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ReportUiEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
                // 이 화면에서는 발생하지 않는 이펙트입니다.
                is ReportUiEffect.NavigateToUsageReasonInput -> Unit
                is ReportUiEffect.NavigateToTab -> Unit
                ReportUiEffect.NavigateToRestSuggestion -> Unit
                ReportUiEffect.NavigateToAlarmSettings -> Unit
            }
        }
    }

    RestSuggestionScreen(
        state = state,
        modifier = modifier,
        snackbarHostState = snackbarHostState,
        onRetry = { viewModel.onEvent(ReportUiEvent.Retry) },
        onSeeSummary = onNavigateToSummary,
    )
}

@Composable
fun RestSuggestionScreen(
    state: ReportUiState,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onRetry: () -> Unit = {},
    onSeeSummary: () -> Unit = {},
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = PhoneShimTheme.colors.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { TopAppBar(title = "쉼이의 제안", titleStyle = PhoneShimType.KorH3) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(PhoneShimDimens.screenHorizontalPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing16),
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(PhoneShimTheme.colors.brandSubtle, CircleShape),
            )

            when {
                state.isLoading -> {
                    Spacer(modifier = Modifier.height(PhoneShimDimens.spacing24))
                    CircularProgressIndicator(color = PhoneShimTheme.colors.brand)
                }

                // 422 INSUFFICIENT_AI_FEEDBACK_DATA. 오류가 아니라 데이터 부족 안내입니다.
                state.isDataInsufficient -> InsufficientDataCard(
                    message = state.insufficientDataMessage.orEmpty(),
                    onRetry = onRetry,
                )

                state.restSuggestion != null -> SuggestionCard(state.restSuggestion)

                else -> InsufficientDataCard(
                    message = "아직 보여드릴 제안이 없어요.",
                    onRetry = onRetry,
                )
            }

            Text(
                text = "어플 사용 통계 보기",
                style = PhoneShimType.KorBodyM,
                color = PhoneShimTheme.colors.brandStrong,
                modifier = Modifier
                    .clickable(onClick = onSeeSummary)
                    .padding(PhoneShimDimens.spacing12),
            )
        }
    }
}

@Composable
private fun SuggestionCard(suggestion: RestSuggestion, modifier: Modifier = Modifier) {
    ReportCard(modifier = modifier) {
        Text(
            text = suggestion.date,
            style = PhoneShimType.KorCaption,
            color = PhoneShimTheme.colors.textTertiary,
        )
        Spacer(modifier = Modifier.height(PhoneShimDimens.spacing8))
        Text(
            text = suggestion.message,
            style = PhoneShimType.KorBodyM,
            color = PhoneShimTheme.colors.textPrimary,
        )
    }
}

@Composable
private fun InsufficientDataCard(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ReportCard(modifier = modifier) {
        Text(
            text = message,
            style = PhoneShimType.KorBodyM,
            color = PhoneShimTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(PhoneShimDimens.spacing8))
        Text(
            text = "사용 이유를 조금 더 기록하면 제안을 받아볼 수 있어요.",
            style = PhoneShimType.KorCaption,
            color = PhoneShimTheme.colors.textTertiary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(PhoneShimDimens.spacing16))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(PhoneShimDimens.textFieldHeight)
                .background(PhoneShimTheme.colors.brandSubtle, RoundedCornerShape(12.dp))
                .clickable(onClick = onRetry),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "다시 시도",
                style = PhoneShimType.KorBodyM,
                color = PhoneShimTheme.colors.brandStrong,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RestSuggestionPreview() {
    PhoneShimTheme {
        RestSuggestionScreen(
            state = ReportUiState(
                restSuggestion = RestSuggestion(
                    date = "2026-07-11",
                    message = "오늘은 점심 시간대 사용이 많았습니다. 앱을 열기 전 5분 휴식을 먼저 시도해보세요.",
                ),
            ),
        )
    }
}

@Preview(name = "데이터 부족", showBackground = true)
@Composable
private fun RestSuggestionInsufficientPreview() {
    PhoneShimTheme {
        RestSuggestionScreen(
            state = ReportUiState(insufficientDataMessage = "아직 분석할 기록이 충분하지 않아요."),
        )
    }
}
