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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.phoneshim.android.R
import com.phoneshim.android.ui.common.PhoneShimButtonSize
import com.phoneshim.android.ui.common.PrimaryButton
import com.phoneshim.android.ui.features.setgoal.component.AccessCountPopup
import com.phoneshim.android.ui.features.setgoal.component.AppLabel
import com.phoneshim.android.ui.features.setgoal.component.SetGoalBottomButtons
import com.phoneshim.android.ui.features.setgoal.component.SetGoalCard
import com.phoneshim.android.ui.features.setgoal.component.SetGoalCardDivider
import com.phoneshim.android.ui.features.setgoal.component.SetGoalStepIndicator
import com.phoneshim.android.ui.features.setgoal.component.SetGoalTitle
import com.phoneshim.android.ui.features.setgoal.component.SetGoalTopBar
import com.phoneshim.android.ui.features.setgoal.component.TotalTimeCard
import com.phoneshim.android.ui.features.setgoal.viewmodel.AppGoalSetting
import com.phoneshim.android.ui.features.setgoal.viewmodel.AppTimeInput
import com.phoneshim.android.ui.features.setgoal.viewmodel.SetGoalViewModel
import com.phoneshim.android.ui.theme.PhoneShimDimens
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimType

// 선택한 앱의 접근 제한과 목표를 설정하는 화면 (Figma 04-4. 어플 접근 횟수&목표 설정)
@Composable
fun AccessGoalSetScreen(
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    viewModel: SetGoalViewModel = hiltViewModel(),
) {
    // 04-2/04-3에서 설정한 앱 목록과 시간을 viewModel이 공유
    val uiState by viewModel.uiState.collectAsState()
    var editingApp by remember { mutableStateOf<String?>(null) }
    var countEditingApp by remember { mutableStateOf<String?>(null) }

    AccessGoalSetContent(
        apps = uiState.selectedApps,
        settings = uiState.appSettings,
        onEditAccessCount = { countEditingApp = it },
        onEditGoal = { editingApp = it },
        onNext = onNext,
        onBack = onBack,
        modifier = modifier,
    )

    editingApp?.let { app ->
        GoalWriteDialog(
            goalText = uiState.appSettings[app]?.goalText.orEmpty(),
            onSave = { text ->
                viewModel.setGoalText(app, text)
                editingApp = null
            },
            onDismiss = { editingApp = null },
        )
    }

    countEditingApp?.let { app ->
        AccessCountPopup(
            initialCount = uiState.appSettings[app]?.accessCount ?: 0,
            onConfirm = { count ->
                viewModel.setAccessCount(app, count)
                countEditingApp = null
            },
            onDismiss = { countEditingApp = null },
        )
    }
}

@Composable
private fun AccessGoalSetContent(
    apps: List<String>,
    settings: Map<String, AppGoalSetting>,
    onEditAccessCount: (String) -> Unit,
    onEditGoal: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val totalMinutes = apps.sumOf { settings[it]?.timeInput?.totalMinutes ?: 0 }

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
                        onEditAccessCount = { onEditAccessCount(app) },
                        onEditGoal = { onEditGoal(app) },
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
            TotalTimeCard(totalMinutes = totalMinutes)
            SetGoalBottomButtons(onBack = onBack, onNext = onNext)
        }
    }
}

// 앱 이름 + 목표 시간 + 접근 제한/목표 입력 아이콘 버튼 행
@Composable
fun AppGoalRow(
    app: String,
    setting: AppGoalSetting,
    onEditAccessCount: () -> Unit,
    onEditGoal: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppLabel(name = app)
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "${setting.timeInput.hour} 시간 ${setting.timeInput.minute} 분",
            style = PhoneShimType.KorLabel,
            color = PhoneShimTheme.colors.textPrimary,
        )
        Spacer(modifier = Modifier.width(PhoneShimDimens.spacing12))
        AccessLimitIcon(
            active = setting.accessLimited,
            onClick = onEditAccessCount,
        )
        Spacer(modifier = Modifier.width(PhoneShimDimens.spacing4))
        GoalEditIcon(onClick = onEditGoal)
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

// 목표 입력(연필) 아이콘 버튼
@Composable
private fun GoalEditIcon(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(20.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_modify),
            contentDescription = "목표 입력",
            tint = Color.Unspecified,
            modifier = Modifier.size(11.dp),
        )
    }
}

// 어플 목표 작성 팝업 (Figma 04-4. 어플 목표 설정 팝업)
@Composable
fun GoalWriteDialog(
    goalText: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember { mutableStateOf(goalText) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(PhoneShimTheme.colors.surface)
                .padding(PhoneShimDimens.spacing24),
            verticalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing12),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "어플 목표 설정",
                    style = PhoneShimType.KorCaption,
                    color = PhoneShimTheme.colors.textPrimary,
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "닫기",
                    tint = PhoneShimTheme.colors.textPrimary,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable(onClick = onDismiss),
                )
            }

            BasicTextField(
                value = text,
                onValueChange = { text = it },
                textStyle = PhoneShimType.KorCaption.copy(
                    color = PhoneShimTheme.colors.textPrimary,
                ),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.small)
                            .background(PhoneShimTheme.colors.brandSubtle)
                            .padding(horizontal = PhoneShimDimens.spacing12, vertical = 10.dp),
                    ) {
                        if (text.isEmpty()) {
                            Text(
                                text = "이 어플에 대한 목표를 작성해보세요.",
                                style = PhoneShimType.KorCaption,
                                color = PhoneShimTheme.colors.textTertiary,
                            )
                        }
                        innerTextField()
                    }
                },
            )

            PrimaryButton(
                text = "저장",
                onClick = { onSave(text) },
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = PhoneShimDimens.spacing12)
                    .width(120.dp),
                size = PhoneShimButtonSize.Small,
                fullWidth = false,
                shape = MaterialTheme.shapes.small,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun AccessGoalSetScreenPreview() {
    PhoneShimTheme {
        AccessGoalSetContent(
            apps = listOf("카카오톡", "페이스북", "틱톡"),
            settings = mapOf(
                "카카오톡" to AppGoalSetting(AppTimeInput("01", "00")),
                "페이스북" to AppGoalSetting(AppTimeInput("01", "30")),
                "틱톡" to AppGoalSetting(AppTimeInput("01", "00")),
            ),
            onEditAccessCount = {},
            onEditGoal = {},
            onNext = {},
            onBack = {},
        )
    }
}
