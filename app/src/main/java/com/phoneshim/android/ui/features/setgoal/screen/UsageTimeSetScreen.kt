package com.phoneshim.android.ui.features.setgoal.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.phoneshim.android.ui.common.Toggle
import com.phoneshim.android.ui.common.GoalTimeCard
import com.phoneshim.android.ui.features.setgoal.component.AppLabel
import com.phoneshim.android.ui.features.setgoal.component.SetGoalBottomButtons
import com.phoneshim.android.ui.features.setgoal.component.SetGoalCard
import com.phoneshim.android.ui.features.setgoal.component.SetGoalCardDivider
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

// 앱별 하루 목표 사용 시간을 설정하는 화면 (Figma 04-3. 목표 사용 시간 설정)
@Composable
fun UsageTimeSetScreen(
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    viewModel: SetGoalViewModel = hiltViewModel(),
) {
    // 04-2에서 선택한 앱 목록과 시간 입력값을 viewModel이 공유
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
        apps = uiState.selectedApps,
        timeInputs = uiState.appSettings.mapValues { it.value.timeInput },
        onTimeChange = { app, input -> viewModel.onEvent(SetGoalEvent.SetAppTime(app, input)) },
        blockAfterGoal = uiState.blockAfterGoal,
        onBlockAfterGoalChange = { viewModel.onEvent(SetGoalEvent.SetBlockAfterGoal(it)) },
        onNext = { viewModel.onEvent(SetGoalEvent.SubmitTimeSet) },
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
private fun UsageTimeSetContent(
    apps: List<String>,
    timeInputs: Map<String, AppTimeInput>,
    onTimeChange: (String, AppTimeInput) -> Unit,
    blockAfterGoal: Boolean,
    onBlockAfterGoalChange: (Boolean) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val totalMinutes = apps.sumOf { timeInputs[it]?.totalMinutes ?: 0 }

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
            SetGoalStepIndicator(currentStep = 3)
            SetGoalTitle(
                title = "하루 목표 폰 사용 시간을 설정해주세요!",
                subtitle = "하루 동안 사용할 목표 시간을 설정해요",
            )

            SetGoalCard {
                apps.forEachIndexed { index, app ->
                    val input = timeInputs[app] ?: AppTimeInput()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AppLabel(name = app)
                        Spacer(modifier = Modifier.weight(1f))
                        TimeField(
                            value = input.hour,
                            onValueChange = { onTimeChange(app, input.copy(hour = it)) },
                        )
                        Text(
                            text = "시간",
                            style = PhoneShimType.KorLabel,
                            color = PhoneShimTheme.colors.textPrimary,
                            modifier = Modifier.padding(horizontal = PhoneShimDimens.spacing4),
                        )
                        TimeField(
                            value = input.minute,
                            onValueChange = { onTimeChange(app, input.copy(minute = it)) },
                            modifier = Modifier.padding(start = PhoneShimDimens.spacing8),
                        )
                        Text(
                            text = "분",
                            style = PhoneShimType.KorLabel,
                            color = PhoneShimTheme.colors.textPrimary,
                            modifier = Modifier.padding(start = PhoneShimDimens.spacing4),
                        )
                    }
                    if (index != apps.lastIndex) {
                        SetGoalCardDivider()
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "목표 시간 이후 폰 금지",
                        style = PhoneShimType.KorCaption,
                        color = PhoneShimTheme.colors.textPrimary,
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
                    modifier = Modifier.padding(start = PhoneShimDimens.spacing32),
                )
            }
        }

        Column(
            modifier = Modifier.padding(
                horizontal = PhoneShimDimens.screenHorizontalPadding,
                vertical = PhoneShimDimens.spacing16,
            ),
            verticalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing12),
        ) {
            GoalTimeCard(
                label = "총 목표 시간",
                totalMinutes = totalMinutes,
            )
            SetGoalBottomButtons(
                onBack = onBack,
                onNext = onNext,
            )
        }
    }
}

// 시/분 입력용 소형 텍스트 필드 (2자리 숫자)
@Composable
private fun TimeField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    BasicTextField(
        value = value,
        onValueChange = { new ->
            onValueChange(new.filter(Char::isDigit).take(2))
        },
        textStyle = PhoneShimType.KorLabel.copy(
            color = PhoneShimTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = modifier.size(width = 36.dp, height = 28.dp),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(MaterialTheme.shapes.small)
                    .background(PhoneShimTheme.colors.surface)
                    .border(1.dp, PhoneShimTheme.colors.border, MaterialTheme.shapes.small),
                contentAlignment = Alignment.Center,
            ) {
                innerTextField()
            }
        },
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun UsageTimeSetScreenPreview() {
    PhoneShimTheme {
        UsageTimeSetContent(
            apps = listOf("카카오톡", "페이스북", "틱톡"),
            timeInputs = mapOf(
                "카카오톡" to AppTimeInput("01", "00"),
                "페이스북" to AppTimeInput("01", "30"),
                "틱톡" to AppTimeInput("01", "00"),
            ),
            onTimeChange = { _, _ -> },
            blockAfterGoal = false,
            onBlockAfterGoalChange = {},
            onNext = {},
            onBack = {},
        )
    }
}
