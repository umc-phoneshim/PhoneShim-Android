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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import com.phoneshim.android.domain.model.UsageReasonEntry
import com.phoneshim.android.ui.common.TopAppBar
import com.phoneshim.android.ui.features.report.viewmodel.UsageReasonInputUiEffect
import com.phoneshim.android.ui.features.report.viewmodel.UsageReasonInputUiEvent
import com.phoneshim.android.ui.features.report.viewmodel.UsageReasonInputUiState
import com.phoneshim.android.ui.features.report.viewmodel.UsageReasonInputViewModel
import com.phoneshim.android.ui.theme.PhoneShimDimens
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimType

/**
 * 07. 타임테이블 - 사용 이유 입력.
 *
 * POST /api/usage-reasons (구현완료). 입력 가능 시간은 당일 22:00 ~ 익일 10:00 이고
 * 사유는 최대 100자입니다.
 */
@Composable
fun UsageReasonInputRoute(
    entryId: String,
    date: String,
    timeRangeStart: String,
    timeRangeEnd: String,
    onSubmitted: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: UsageReasonInputViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(entryId, date) {
        viewModel.onEvent(
            UsageReasonInputUiEvent.Started(
                entryId = entryId,
                date = date,
                timeRangeStart = timeRangeStart,
                timeRangeEnd = timeRangeEnd,
            ),
        )
    }
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                // TODO: 포인트 적립 정책이 확정되면 여기서 PointRewardPopup 을 노출하세요.
                //  현재 명세서에 포인트 관련 API가 없습니다.
                UsageReasonInputUiEffect.Submitted -> onSubmitted()
                is UsageReasonInputUiEffect.ShowMessage ->
                    snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    UsageReasonInputScreen(
        state = state,
        modifier = modifier,
        snackbarHostState = snackbarHostState,
        onReasonChange = { viewModel.onEvent(UsageReasonInputUiEvent.ReasonChanged(it)) },
        onSubmit = { viewModel.onEvent(UsageReasonInputUiEvent.SubmitClicked) },
    )
}

@Composable
fun UsageReasonInputScreen(
    state: UsageReasonInputUiState,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onReasonChange: (String) -> Unit = {},
    onSubmit: () -> Unit = {},
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = PhoneShimTheme.colors.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { TopAppBar(title = "사용 이유", titleStyle = PhoneShimType.KorH3) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(PhoneShimDimens.screenHorizontalPadding),
            verticalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing12),
        ) {
            Text(
                text = "이 시간에 앱을 사용한 이유를 적어 주세요.",
                style = PhoneShimType.KorBodyM,
                color = PhoneShimTheme.colors.textPrimary,
            )
            Text(
                text = "입력 가능 시간: 당일 ${UsageReasonEntry.INPUT_WINDOW_START_HOUR}:00 ~ " +
                    "다음날 ${UsageReasonEntry.INPUT_WINDOW_END_HOUR}:00",
                style = PhoneShimType.KorCaption,
                color = PhoneShimTheme.colors.textTertiary,
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(PhoneShimTheme.colors.surfaceCream, RoundedCornerShape(12.dp))
                    .padding(PhoneShimDimens.spacing16),
            ) {
                if (state.reason.isEmpty()) {
                    Text(
                        text = "예) 친구와 약속을 잡느라 확인했어요",
                        style = PhoneShimType.KorBodyM,
                        color = PhoneShimTheme.colors.textTertiary,
                    )
                }
                BasicTextField(
                    value = state.reason,
                    onValueChange = onReasonChange,
                    enabled = !state.isOutsideInputWindow,
                    textStyle = PhoneShimType.KorBodyM.copy(color = PhoneShimTheme.colors.textPrimary),
                    modifier = Modifier.fillMaxSize(),
                )
            }

            Text(
                text = "${state.reason.length} / ${UsageReasonEntry.MAX_REASON_LENGTH}",
                style = PhoneShimType.KorCaption,
                color = PhoneShimTheme.colors.textTertiary,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth(),
            )

            state.errorMessage?.let { error ->
                Text(text = error, style = PhoneShimType.KorCaption, color = PhoneShimTheme.colors.error)
            }

            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(PhoneShimDimens.textFieldHeight)
                    .background(
                        color = if (state.canSubmit) {
                            PhoneShimTheme.colors.brand
                        } else {
                            PhoneShimTheme.colors.divider
                        },
                        shape = RoundedCornerShape(12.dp),
                    )
                    .clickable(enabled = state.canSubmit, onClick = onSubmit),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (state.isSubmitting) "저장 중..." else "저장",
                    style = PhoneShimType.KorBodyM,
                    color = PhoneShimTheme.colors.onBrand,
                )
            }
            Spacer(modifier = Modifier.height(PhoneShimDimens.spacing16))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UsageReasonInputPreview() {
    PhoneShimTheme {
        UsageReasonInputScreen(
            state = UsageReasonInputUiState(
                entryId = "app-1",
                date = "2026-07-11",
                reason = "친구와 약속을 잡느라 확인했어요",
            ),
        )
    }
}

@Preview(name = "입력 가능 시간대 아님", showBackground = true)
@Composable
private fun UsageReasonInputOutsideWindowPreview() {
    PhoneShimTheme {
        UsageReasonInputScreen(
            state = UsageReasonInputUiState(
                entryId = "app-1",
                date = "2026-07-11",
                isOutsideInputWindow = true,
                errorMessage = "사용 이유는 당일 22시부터 다음날 10시까지만 입력할 수 있어요.",
            ),
        )
    }
}
