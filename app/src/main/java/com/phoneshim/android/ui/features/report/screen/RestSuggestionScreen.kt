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
import com.phoneshim.android.domain.model.SuggestionType
import com.phoneshim.android.ui.common.PhoneShimSnackbarHost
import com.phoneshim.android.ui.common.TopAppBar
import com.phoneshim.android.ui.common.showPhoneShimSnackbar
import com.phoneshim.android.ui.common.base.CollectCommonEffect
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
    onAuthExpired: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReportViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    CollectCommonEffect(viewModel, onAuthExpired)

    LaunchedEffect(viewModel) {
        viewModel.onEvent(ReportUiEvent.RestSuggestionRequested)
    }
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ReportUiEffect.ShowMessage -> snackbarHostState.showPhoneShimSnackbar(
                    message = effect.message,
                    type = effect.type,
                )
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
        snackbarHost = { PhoneShimSnackbarHost(snackbarHostState) },
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

                state.restSuggestion != null -> SuggestionCard(state.restSuggestion)

                else -> InsufficientDataCard(
                    message = state.insufficientDataMessage ?: "아직 보여드릴 제안이 없어요.",
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

/**
 * 서버가 골라준 문구를 그대로 보여줍니다.
 * 목표를 넘긴 경우에만 초과 시간을 강조해 한 줄 덧붙입니다.
 */
@Composable
private fun SuggestionCard(suggestion: RestSuggestion, modifier: Modifier = Modifier) {
    ReportCard(modifier = modifier) {
        if (suggestion.excessMinutes > 0) {
            Text(
                text = "목표보다 ${suggestion.excessMinutes}분",
                style = PhoneShimType.KorCaption,
                color = PhoneShimTheme.colors.error,
            )
            Spacer(modifier = Modifier.height(PhoneShimDimens.spacing8))
        } else if (suggestion.isAchieved) {
            Text(
                text = "목표 달성",
                style = PhoneShimType.KorCaption,
                color = PhoneShimTheme.colors.brandStrong,
            )
            Spacer(modifier = Modifier.height(PhoneShimDimens.spacing8))
        }
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

@Preview(name = "목표 초과", showBackground = true)
@Composable
private fun RestSuggestionPreview() {
    PhoneShimTheme {
        RestSuggestionScreen(
            state = ReportUiState(
                restSuggestion = RestSuggestion(
                    suggestionType = SuggestionType.TOTAL_EXCEEDED,
                    message = "오늘 폰 사용 시간이 목표보다 42분 많았어요. " +
                        "그 중 특히 유튜브 사용이 많이 나타났어요. " +
                        "내일은 유튜브 사용을 줄여 전체 폰 사용 시간을 줄여봐요.",
                    excessMinutes = 42,
                    appName = "유튜브",
                ),
            ),
        )
    }
}

@Preview(name = "목표 달성", showBackground = true)
@Composable
private fun RestSuggestionAchievedPreview() {
    PhoneShimTheme {
        RestSuggestionScreen(
            state = ReportUiState(
                restSuggestion = RestSuggestion(
                    suggestionType = SuggestionType.ACHIEVED,
                    message = "오늘 목표를 달성했어요! 지금처럼 꾸준히 이어가 보세요.",
                    excessMinutes = 0,
                ),
            ),
        )
    }
}

@Preview(name = "불러오는 중", showBackground = true)
@Composable
private fun RestSuggestionLoadingPreview() {
    PhoneShimTheme {
        RestSuggestionScreen(state = ReportUiState(isLoading = true))
    }
}
