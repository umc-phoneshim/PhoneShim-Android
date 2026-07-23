package com.phoneshim.android.ui.features.setgoal.screen

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.phoneshim.android.ui.common.AppInfoRow
import com.phoneshim.android.ui.common.GoalTimeCard
import com.phoneshim.android.ui.features.setgoal.component.SetGoalBottomButtons
import com.phoneshim.android.ui.features.setgoal.component.SetGoalCard
import com.phoneshim.android.ui.features.setgoal.component.SetGoalCardDivider
import com.phoneshim.android.ui.features.setgoal.component.SetGoalStepIndicator
import com.phoneshim.android.ui.features.setgoal.component.SetGoalTitle
import com.phoneshim.android.ui.features.setgoal.component.SetGoalTopBar
import com.phoneshim.android.ui.features.setgoal.viewmodel.AppGoalSetting
import com.phoneshim.android.ui.features.setgoal.viewmodel.AppTimeInput
import com.phoneshim.android.ui.features.setgoal.viewmodel.SetGoalEvent
import com.phoneshim.android.ui.features.setgoal.viewmodel.SetGoalViewModel
import com.phoneshim.android.ui.theme.PhoneShimDimens
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimType

// 선택한 앱의 접근 제한을 설정하는 화면 (Figma 04-4)
@Composable
fun AccessGoalSetScreen(
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    viewModel: SetGoalViewModel = hiltViewModel(),
) {
    // 04-2/04-3에서 설정한 앱 목록과 시간을 viewModel이 공유
    val uiState by viewModel.uiState.collectAsState()

    AccessGoalSetContent(
        apps = uiState.selectedApps,
        settings = uiState.appSettings,
        totalMinutes = uiState.totalMinutes,
        onTimeChange = { app, input -> viewModel.onEvent(SetGoalEvent.SetAppTime(app, input)) },
        onToggleAccessLimit = { viewModel.onEvent(SetGoalEvent.ToggleAccessLimit(it)) },
        onNext = onNext,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
private fun AccessGoalSetContent(
    apps: List<String>,
    settings: Map<String, AppGoalSetting>,
    totalMinutes: Int,
    onTimeChange: (String, AppTimeInput) -> Unit,
    onToggleAccessLimit: (String) -> Unit,
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

        // Figma Maincontainer: p16 + 세로 gap 24
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
            SetGoalStepIndicator(currentStep = 4)
            SetGoalTitle(
                title = "선택한 어플의\n목표 시간과 목표를 설정해주세요",
                subtitle = "‘주의 어플’의 목표 시간을 설정해주세요!\n" +
                    "목표 시간 이후 어플의 제한을 원한다면,\n" +
                    "제한 버튼을 클릭하여 제한을 활성화해주세요",
                subtitleStyle = PhoneShimType.KorCaption,
            )

            SetGoalCard {
                apps.forEachIndexed { index, app ->
                    val setting = settings[app] ?: AppGoalSetting()
                    AppGoalRow(
                        app = app,
                        setting = setting,
                        onTimeChange = { onTimeChange(app, it) },
                        onToggleAccessLimit = { onToggleAccessLimit(app) },
                    )
                    if (index != apps.lastIndex) {
                        SetGoalCardDivider()
                    }
                }
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
            SetGoalBottomButtons(onBack = onBack, onNext = onNext)
        }
    }
}

// 앱 이름 + 목표 시간 + 접근 제한 토글 행
@Composable
fun AppGoalRow(
    app: String,
    setting: AppGoalSetting,
    onTimeChange: (AppTimeInput) -> Unit,
    onToggleAccessLimit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppInfoRow(
        appName = app,
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp),
        trailingContent = {
            TimeField(
                value = setting.timeInput.hour,
                onValueChange = { onTimeChange(setting.timeInput.copy(hour = it)) },
            )
            Text(
                text = "시간",
                style = PhoneShimType.KorLabel,
                color = PhoneShimTheme.colors.textPrimary,
                modifier = Modifier.padding(horizontal = PhoneShimDimens.spacing4),
            )
            TimeField(
                value = setting.timeInput.minute,
                onValueChange = { onTimeChange(setting.timeInput.copy(minute = it)) },
            )
            Text(
                text = "분",
                style = PhoneShimType.KorLabel,
                color = PhoneShimTheme.colors.textPrimary,
                modifier = Modifier.padding(start = PhoneShimDimens.spacing4),
            )
            Spacer(modifier = Modifier.width(PhoneShimDimens.spacing8))
            AccessLimitIcon(active = setting.accessLimited, onClick = onToggleAccessLimit)
        },
    )
}

// 시/분 입력용 소형 텍스트 필드 (2자리)
@Composable
private fun TimeField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    BasicTextField(
        value = value,
        onValueChange = { new -> onValueChange(new.filter(Char::isDigit).take(2)) },
        textStyle = PhoneShimType.KorLabel.copy(
            color = PhoneShimTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = modifier.size(width = 32.dp, height = 24.dp),
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

// 접근 제한 토글 아이콘 (금지 표시). 활성화 시 붉은색으로 표시됩니다.
@Composable
private fun AccessLimitIcon(
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val color = if (active) PhoneShimTheme.colors.error else PhoneShimTheme.colors.border
    Box(
        modifier = modifier
            .size(20.dp)
            .clip(CircleShape)
            .border(1.5.dp, color, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .width(8.dp)
                .height(1.5.dp)
                .background(color),
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun AccessGoalSetScreenPreview() {
    PhoneShimTheme {
        AccessGoalSetContent(
            apps = listOf("카카오톡", "페이스북", "틱톡"),
            settings = mapOf(
                "카카오톡" to AppGoalSetting(accessLimited = true),
                "페이스북" to AppGoalSetting(),
                "틱톡" to AppGoalSetting(),
            ),
            totalMinutes = 210,
            onTimeChange = { _, _ -> },
            onToggleAccessLimit = {},
            onNext = {},
            onBack = {},
        )
    }
}
