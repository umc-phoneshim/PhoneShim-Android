package com.phoneshim.android.ui.features.setgoal.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.phoneshim.android.domain.model.InstalledApp
import com.phoneshim.android.ui.common.InteractiveTimeSegmentInput
import com.phoneshim.android.ui.common.AppInfoRow
import com.phoneshim.android.ui.common.GoalTimeCard
import com.phoneshim.android.ui.features.setgoal.component.AppIcon
import com.phoneshim.android.ui.features.setgoal.component.MAX_HOUR_VALUE
import com.phoneshim.android.ui.features.setgoal.component.MAX_MINUTE_VALUE
import com.phoneshim.android.ui.features.setgoal.component.SetGoalBottomButtons
import com.phoneshim.android.ui.features.setgoal.component.SetGoalCard
import com.phoneshim.android.ui.features.setgoal.component.SetGoalCardDivider
import com.phoneshim.android.ui.common.PhoneShimSnackbarHost
import com.phoneshim.android.ui.features.setgoal.component.SetGoalStepIndicator
import com.phoneshim.android.ui.features.setgoal.component.SetGoalTitle
import com.phoneshim.android.ui.features.setgoal.component.SetGoalTopBar
import com.phoneshim.android.ui.features.setgoal.viewmodel.AppGoalSetting
import com.phoneshim.android.ui.features.setgoal.viewmodel.AppTimeInput
import com.phoneshim.android.ui.features.setgoal.viewmodel.SetGoalEffect
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
        AccessGoalSetContent(
            apps = uiState.selectedApps,
            settings = uiState.appSettings,
            totalMinutes = uiState.totalMinutes,
            onTimeChange = { app, input -> viewModel.onEvent(SetGoalEvent.SetAppTime(app, input)) },
            onToggleAccessLimit = { viewModel.onEvent(SetGoalEvent.ToggleAccessLimit(it)) },
            onNext = { viewModel.onEvent(SetGoalEvent.SubmitAppGoals) },
            onBack = onBack,
        )
        PhoneShimSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun AccessGoalSetContent(
    apps: List<InstalledApp>,
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
                title = "선택한 어플의 목표시간을 설정해주세요!",
                subtitle = "‘주의 어플’의 목표 시간을 설정해주세요!\n" +
                    "목표 시간 이후 어플의 제한을 원한다면,\n" +
                    "제한 버튼을 클릭하여 제한을 활성화해주세요",
                subtitleStyle = PhoneShimType.KorCaption,
            )

            SetGoalCard {
                apps.forEachIndexed { index, app ->
                    val setting = settings[app.packageName] ?: AppGoalSetting()
                    AppGoalRow(
                        app = app,
                        setting = setting,
                        onTimeChange = { onTimeChange(app.packageName, it) },
                        onToggleAccessLimit = { onToggleAccessLimit(app.packageName) },
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

// 앱 이름 + 목표 시간(입력/표시) + 접근 제한 토글 행
// editable=true → 04-4처럼 셀에서 직접 입력, false → 04-5처럼 텍스트로 표시
@Composable
fun AppGoalRow(
    app: InstalledApp,
    setting: AppGoalSetting,
    onTimeChange: (AppTimeInput) -> Unit,
    onToggleAccessLimit: () -> Unit,
    modifier: Modifier = Modifier,
    editable: Boolean = true,
) {
    AppInfoRow(
        appName = app.label,
        appNameStyle = PhoneShimType.KorCaption,
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp),
        iconContent = { AppIcon(packageName = app.packageName) },
        trailingContent = {
            // 접근 제한 아이콘과 시간 묶음 사이 간격 12
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing12),
            ) {
                // 시간/분 묶음 사이 간격 8
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing8),
                ) {
                    TimeUnitGroup(
                        value = setting.timeInput.hour,
                        unit = "시간",
                        editable = editable,
                        maxValue = MAX_HOUR_VALUE,
                        onValueChange = { hour ->
                            onTimeChange(setting.timeInput.copy(hour = hour))
                        },
                    )
                    TimeUnitGroup(
                        value = setting.timeInput.minute,
                        unit = "분",
                        editable = editable,
                        maxValue = MAX_MINUTE_VALUE,
                        onValueChange = { minute ->
                            onTimeChange(setting.timeInput.copy(minute = minute))
                        },
                    )
                }
                AccessLimitIcon(active = setting.accessLimited, onClick = onToggleAccessLimit)
            }
        },
    )
}

// 숫자 입력/표시 + 단위("시간"/"분") 묶음, 사이 간격 4
@Composable
private fun TimeUnitGroup(
    value: String,
    unit: String,
    editable: Boolean,
    maxValue: Int,
    onValueChange: (String) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing4),
    ) {
        if (editable) {
            TimeCell(
                value = value,
                maxValue = maxValue,
                onValueChange = onValueChange,
            )
        } else {
            Text(
                text = value.ifEmpty { "00" },
                style = PhoneShimType.EngLabel,
                color = PhoneShimTheme.colors.textPrimary,
            )
        }
        Text(
            text = unit,
            style = PhoneShimType.KorLabel,
            color = PhoneShimTheme.colors.textSecondary,
        )
    }
}

// 시/분 입력 셀 치수 (Figma 36×28 라운드 박스).
private val TIME_CELL_WIDTH = 36.dp
private val TIME_CELL_HEIGHT = 28.dp

// 시/분 값을 직접 입력하는 소형 셀.
//
// 04-2·설정 팝업과 같은 입력 동작(2자리 제한, 상한 검사, 포커스 시 전체 선택,
// 포커스 해제 시 "5" -> "05" 정규화)이 필요해 공용 InteractiveTimeSegmentInput 을 쓴다.
// 다만 공용 컴포넌트는 평상시 배경·테두리가 투명이고 04-4 는 평상시에도 박스가 보여야 해서,
// 박스는 이 래퍼가 그리고 공용 컴포넌트는 안에서 입력만 담당한다.
// resting 과 active 크기를 같게 줘서 고정 박스 안에서 크기가 변하지 않도록 한다.
@Composable
private fun TimeCell(
    value: String,
    maxValue: Int,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(width = TIME_CELL_WIDTH, height = TIME_CELL_HEIGHT)
            .clip(MaterialTheme.shapes.small)
            .background(PhoneShimTheme.colors.surface)
            .border(1.dp, PhoneShimTheme.colors.border, MaterialTheme.shapes.small),
        contentAlignment = Alignment.Center,
    ) {
        InteractiveTimeSegmentInput(
            value = value,
            maxValue = maxValue,
            onValueChange = onValueChange,
            textStyle = PhoneShimType.EngLabel,
            restingWidth = TIME_CELL_WIDTH,
            activeWidth = TIME_CELL_WIDTH,
            restingHeight = TIME_CELL_HEIGHT,
            activeHeight = TIME_CELL_HEIGHT,
            restingTextColor = PhoneShimTheme.colors.textPrimary,
            activeTextColor = PhoneShimTheme.colors.brandStrong,
        )
    }
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
            // 토글이라는 걸 알 수 있도록 역할과 상태를 노출한다.
            // 아이콘만으로는 기능을 알기 어렵다는 피드백이 있어 최소한 접근성 경로는 열어둔다.
            .toggleable(
                value = active,
                role = Role.Switch,
                onValueChange = { onClick() },
            )
            .semantics {
                contentDescription = if (active) {
                    "목표 시간 이후 앱 사용 제한 켬"
                } else {
                    "목표 시간 이후 앱 사용 제한 끔"
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .width(12.dp)
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
            apps = listOf(
                InstalledApp("com.kakao.talk", "카카오톡"),
                InstalledApp("com.facebook.katana", "페이스북"),
                InstalledApp("com.zhiliaoapp.musically", "틱톡"),
            ),
            settings = mapOf(
                "com.kakao.talk" to AppGoalSetting(accessLimited = true),
                "com.facebook.katana" to AppGoalSetting(),
                "com.zhiliaoapp.musically" to AppGoalSetting(),
            ),
            totalMinutes = 210,
            onTimeChange = { _, _ -> },
            onToggleAccessLimit = {},
            onNext = {},
            onBack = {},
        )
    }
}
