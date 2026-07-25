package com.phoneshim.android.ui.features.setgoal.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.phoneshim.android.ui.common.Toggle
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
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is SetGoalEffect.ShowMessage ->
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                SetGoalEffect.NavigateNext -> onNext()
            }
        }
    }

    UsageTimeSetContent(
        goalTime = uiState.goalTime,
        onTimeChange = { viewModel.onEvent(SetGoalEvent.SetGoalTime(it)) },
        blockAfterGoal = uiState.blockAfterGoal,
        onBlockAfterGoalChange = { viewModel.onEvent(SetGoalEvent.SetBlockAfterGoal(it)) },
        onNext = { viewModel.onEvent(SetGoalEvent.SubmitTimeSet) },
        onBack = onBack,
        modifier = modifier,
    )
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

            // 단일 총 목표 시간 클럭 카드 (Figma 04-2: 연한 초록 배경 + 브랜드 테두리, p16)
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
                            onValueChange = { onTimeChange(goalTime.copy(hour = it)) },
                        )
                        ClockUnit("시간")
                    }
                    // 분 그룹
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ClockField(
                            value = goalTime.minute,
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

// 큰 시/분 숫자 입력 (2자리)
@Composable
private fun ClockField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    BasicTextField(
        value = value,
        onValueChange = { new -> onValueChange(new.filter(Char::isDigit).take(2)) },
        textStyle = PhoneShimType.EngDisplay.copy(
            color = PhoneShimTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = modifier.width(56.dp),
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
            blockAfterGoal = false,
            onBlockAfterGoalChange = {},
            onNext = {},
            onBack = {},
        )
    }
}
