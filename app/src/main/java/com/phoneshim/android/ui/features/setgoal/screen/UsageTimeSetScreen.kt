package com.phoneshim.android.ui.features.setgoal.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.phoneshim.android.ui.common.InteractiveTimeSegmentInput
import com.phoneshim.android.ui.common.PhoneShimBottomSnackbarHost
import com.phoneshim.android.ui.common.Toggle
import com.phoneshim.android.ui.features.setgoal.component.MAX_HOUR_VALUE
import com.phoneshim.android.ui.features.setgoal.component.MAX_MINUTE_VALUE
import com.phoneshim.android.ui.features.setgoal.component.SetGoalBottomButtons
import com.phoneshim.android.ui.features.setgoal.component.SetGoalStepIndicator
import com.phoneshim.android.ui.features.setgoal.component.SetGoalTitle
import com.phoneshim.android.ui.features.setgoal.component.SetGoalTopBar
import com.phoneshim.android.ui.features.setgoal.viewmodel.AppTimeInput
import com.phoneshim.android.ui.features.setgoal.viewmodel.SetGoalEffect
import com.phoneshim.android.ui.features.setgoal.viewmodel.SetGoalEvent
import com.phoneshim.android.ui.features.setgoal.viewmodel.SetGoalViewModel
import com.phoneshim.android.ui.theme.PhoneShimDimens
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimType

// 하루 목표 사용 시간을 설정하는 화면 (Figma 04-2. 목표 사용 시간 설정)
@Composable
fun UsageTimeSetScreen(
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    viewModel: SetGoalViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is SetGoalEffect.ShowMessage -> snackbarHostState.showSnackbar(
                    message = effect.message,
                    duration = SnackbarDuration.Short,
                )
                SetGoalEffect.NavigateNext -> onNext()
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        UsageTimeSetContent(
            goalTime = uiState.goalTime,
            onTimeChange = { viewModel.onEvent(SetGoalEvent.SetGoalTime(it)) },
            blockAfterGoal = uiState.blockAfterGoal,
            onBlockAfterGoalChange = { viewModel.onEvent(SetGoalEvent.SetBlockAfterGoal(it)) },
            onNext = { viewModel.onEvent(SetGoalEvent.SubmitTimeSet) },
            onBack = onBack,
        )
        PhoneShimBottomSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun UsageTimeSetContent(
    goalTime: AppTimeInput,
    onTimeChange: (AppTimeInput) -> Unit,
    blockAfterGoal: Boolean,
    onBlockAfterGoalChange: (Boolean) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PhoneShimTheme.colors.background),
    ) {
        SetGoalTopBar(onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(
                    start = PhoneShimDimens.screenHorizontalPadding,
                    end = PhoneShimDimens.screenHorizontalPadding,
                    top = PhoneShimDimens.spacing16,
                )
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing24),
        ) {
            SetGoalStepIndicator(currentStep = 2)
            SetGoalTitle(
                title = "하루 목표 폰 사용 시간을 설정해주세요!",
                subtitle = "하루 동안 사용할 목표 시간을 설정해요",
            )

            // 단일 총 목표 시간 클럭 카드 (Figma 04-2: 연한 초록 배경 + 브랜드 테두리, p16).
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .background(PhoneShimTheme.colors.brandSubtle)
                    .border(1.dp, PhoneShimTheme.colors.brand, MaterialTheme.shapes.medium)
                    .padding(PhoneShimDimens.spacing16),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing4),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // 시간 그룹 (숫자 + 단위, 사이 간격 없음)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ClockField(
                            value = goalTime.hour,
                            maxValue = MAX_HOUR_VALUE,
                            onValueChange = { onTimeChange(goalTime.copy(hour = it)) },
                        )
                        ClockUnit("시간")
                    }
                    // 분 그룹
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ClockField(
                            value = goalTime.minute,
                            maxValue = MAX_MINUTE_VALUE,
                            onValueChange = { onTimeChange(goalTime.copy(minute = it)) },
                        )
                        ClockUnit("분")
                    }
                }
            }

            // 목표 시간 이후 폰 금지 토글 (Figma 04-2: 텍스트 묶음 + 토글, gap 16)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(
                    space = PhoneShimDimens.spacing16,
                    alignment = Alignment.CenterHorizontally,
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing4),
                ) {
                    Text(
                        text = "목표 시간 이후 폰 금지",
                        style = PhoneShimType.KorCaption,
                        color = PhoneShimTheme.colors.textSecondary,
                    )
                    Text(
                        text = "(기본 어플만 사용 가능합니다.)",
                        style = PhoneShimType.KorLabel,
                        color = PhoneShimTheme.colors.textTertiary,
                    )
                }
                Toggle(
                    checked = blockAfterGoal,
                    onCheckedChange = onBlockAfterGoalChange,
                )
            }
        }

        Column(
            modifier = Modifier.padding(
                horizontal = PhoneShimDimens.screenHorizontalPadding,
                vertical = PhoneShimDimens.spacing16,
            ),
        ) {
            SetGoalBottomButtons(onBack = onBack, onNext = onNext)
        }
    }
}

// Figma "Time Cell (On Boarding)" 규격. 설정(PREF) 팝업의 같은 셀과 값을 맞춘다.
private val CLOCK_RESTING_WIDTH = 56.dp
private val CLOCK_ACTIVE_WIDTH = 64.dp
private val CLOCK_RESTING_HEIGHT = 39.dp
private val CLOCK_ACTIVE_HEIGHT = 47.dp

// Figma "Time Cell (On Boarding)". 설정(PREF) 팝업과 같은 셀이라 공용 컴포넌트를 그대로 쓴다.
// 크기·색 전환과 입력 정제는 InteractiveTimeSegmentInput 이 담당한다.
@Composable
private fun ClockField(
    value: String,
    maxValue: Int,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    InteractiveTimeSegmentInput(
        value = value,
        maxValue = maxValue,
        onValueChange = onValueChange,
        modifier = modifier,
        textStyle = PhoneShimType.EngDisplay,
        restingWidth = CLOCK_RESTING_WIDTH,
        activeWidth = CLOCK_ACTIVE_WIDTH,
        restingHeight = CLOCK_RESTING_HEIGHT,
        activeHeight = CLOCK_ACTIVE_HEIGHT,
        restingTextColor = PhoneShimTheme.colors.textPrimary,
        activeTextColor = PhoneShimTheme.colors.brandStrong,
    )
}

// 시/분 단위 라벨
@Composable
private fun ClockUnit(text: String) {
    Text(
        text = text,
        style = PhoneShimType.KorH2,
        color = PhoneShimTheme.colors.textPrimary,
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun UsageTimeSetScreenPreview() {
    PhoneShimTheme {
        UsageTimeSetContent(
            goalTime = AppTimeInput("03", "30"),
            onTimeChange = {},
            blockAfterGoal = true,
            onBlockAfterGoalChange = {},
            onNext = {},
            onBack = {},
        )
    }
}
