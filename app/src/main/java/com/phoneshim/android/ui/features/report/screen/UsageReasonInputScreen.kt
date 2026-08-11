package com.phoneshim.android.ui.features.report.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.phoneshim.android.R
import com.phoneshim.android.domain.model.UsageReasonCode
import com.phoneshim.android.domain.model.UsageReasonEntry
import com.phoneshim.android.ui.common.TopAppBar
import com.phoneshim.android.ui.common.base.CollectCommonEffect
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
 * POST /api/usage-reasons. 자유 입력이 아니라 5개 고정 선택지에서 복수 선택합니다.
 * 입력 가능 시간은 KST 기준 당일 22:00 ~ 익일 10:00 입니다.
 */
@Composable
fun UsageReasonInputRoute(
    monitoredAppId: String,
    date: String,
    timeRangeStart: String,
    timeRangeEnd: String,
    onSubmitted: () -> Unit,
    onAuthExpired: () -> Unit,
    modifier: Modifier = Modifier,
    appName: String = "",
    timeRangeLabel: String = "",
    viewModel: UsageReasonInputViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    CollectCommonEffect(viewModel, onAuthExpired)

    LaunchedEffect(monitoredAppId, date, timeRangeStart) {
        viewModel.onEvent(
            UsageReasonInputUiEvent.Started(
                monitoredAppId = monitoredAppId,
                appName = appName,
                date = date,
                timeRangeStart = timeRangeStart,
                timeRangeEnd = timeRangeEnd,
                timeRangeLabel = timeRangeLabel,
            ),
        )
    }
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                // TODO: 포인트 적립 정책이 확정되면 여기서 PointRewardPopup 을 노출하세요.
                //  백엔드에 포인트 관련 API가 아직 없습니다.
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
        onReasonToggle = { viewModel.onEvent(UsageReasonInputUiEvent.ReasonToggled(it)) },
        onSubmit = { viewModel.onEvent(UsageReasonInputUiEvent.SubmitClicked) },
    )
}

@Composable
fun UsageReasonInputScreen(
    state: UsageReasonInputUiState,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onReasonToggle: (UsageReasonCode) -> Unit = {},
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
                text = if (state.appName.isBlank()) {
                    "이 시간에 앱을 사용한 이유를 골라 주세요."
                } else {
                    "${state.appName}을(를) 사용한 이유를 골라 주세요."
                },
                style = PhoneShimType.KorBodyM,
                color = PhoneShimTheme.colors.textPrimary,
            )
            if (state.timeRangeLabel.isNotBlank()) {
                Text(
                    text = state.timeRangeLabel,
                    style = PhoneShimType.KorCaption,
                    color = PhoneShimTheme.colors.brandStrong,
                )
            }
            Text(
                text = "여러 개 고를 수 있어요. 입력 가능 시간은 당일 " +
                    "${UsageReasonEntry.INPUT_WINDOW_START_HOUR}:00 ~ 다음날 " +
                    "${UsageReasonEntry.INPUT_WINDOW_END_HOUR}:00 입니다.",
                style = PhoneShimType.KorCaption,
                color = PhoneShimTheme.colors.textTertiary,
            )

            Spacer(modifier = Modifier.height(PhoneShimDimens.spacing4))

            Column(verticalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing8)) {
                state.options.forEach { reason ->
                    ReasonOptionRow(
                        reason = reason,
                        selected = reason in state.selectedReasons,
                        enabled = !state.isOutsideInputWindow,
                        onClick = { onReasonToggle(reason) },
                    )
                }
            }

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

@Composable
private fun ReasonOptionRow(
    reason: UsageReasonCode,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = if (selected) PhoneShimTheme.colors.brandSubtle else PhoneShimTheme.colors.surface,
                shape = RoundedCornerShape(12.dp),
            )
            .border(
                width = 1.dp,
                color = if (selected) PhoneShimTheme.colors.brand else PhoneShimTheme.colors.divider,
                shape = RoundedCornerShape(12.dp),
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = PhoneShimDimens.spacing16, vertical = PhoneShimDimens.spacing16),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = reason.label,
            style = PhoneShimType.KorBodyM,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) {
                PhoneShimTheme.colors.brandStrong
            } else {
                PhoneShimTheme.colors.textPrimary
            },
            modifier = Modifier.weight(1f),
        )
        Box(
            modifier = Modifier
                .size(22.dp)
                .background(
                    color = if (selected) PhoneShimTheme.colors.brand else PhoneShimTheme.colors.surfaceCream,
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                Icon(
                    painter = painterResource(R.drawable.ic_check),
                    contentDescription = null,
                    tint = PhoneShimTheme.colors.onBrand,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UsageReasonInputPreview() {
    PhoneShimTheme {
        UsageReasonInputScreen(
            state = UsageReasonInputUiState(
                monitoredAppId = "app-1",
                appName = "유튜브",
                date = "2026-07-11",
                timeRangeStart = "2026-07-11T22:00:00.000Z",
                timeRangeEnd = "2026-07-11T22:35:00.000Z",
                timeRangeLabel = "22:00 ~ 22:35",
                selectedReasons = setOf(UsageReasonCode.LEISURE, UsageReasonCode.HABIT),
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
                monitoredAppId = "app-1",
                date = "2026-07-11",
                isOutsideInputWindow = true,
                errorMessage = "사용 이유는 당일 22시부터 다음날 10시까지만 입력할 수 있어요.",
            ),
        )
    }
}
