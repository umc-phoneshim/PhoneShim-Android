package com.phoneshim.android.ui.features.setgoal.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.phoneshim.android.ui.features.setgoal.component.AccessCountPopup
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

// 앱별 설정 내용을 최종 확인하는 화면 (Figma 04-5. 최종 확인)
@Composable
fun SetGoalConfirmScreen(
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    viewModel: SetGoalViewModel = hiltViewModel(),
) {
    // 04-1~04-4에서 설정한 값을 viewModel이 공유
    val uiState by viewModel.uiState.collectAsState()
    var countEditingApp by remember { mutableStateOf<String?>(null) }

    SetGoalConfirmContent(
        apps = uiState.selectedApps,
        settings = uiState.appSettings,
        onEditAccessCount = { countEditingApp = it },
        onConfirm = {
            viewModel.submitGoal()
            onConfirm()
        },
        onBack = onBack,
        modifier = modifier,
    )

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
private fun SetGoalConfirmContent(
    apps: List<String>,
    settings: Map<String, AppGoalSetting>,
    onEditAccessCount: (String) -> Unit,
    onConfirm: () -> Unit,
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
                title = "어플 별 설정을 확인해주세요",
                subtitle = "수정이 필요하면 변경해보세요",
            )

            SetGoalCard {
                apps.forEachIndexed { index, app ->
                    val setting = settings[app] ?: AppGoalSetting()
                    AppGoalRow(
                        app = app,
                        setting = setting,
                        onEditAccessCount = { onEditAccessCount(app) },
                        onEditGoal = { },
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
            SetGoalBottomButtons(onBack = onBack, onNext = onConfirm)
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun SetGoalConfirmScreenPreview() {
    PhoneShimTheme {
        SetGoalConfirmContent(
            apps = listOf("카카오톡", "페이스북", "틱톡"),
            settings = mapOf(
                "카카오톡" to AppGoalSetting(AppTimeInput("01", "00"), accessLimited = true),
                "페이스북" to AppGoalSetting(AppTimeInput("01", "30")),
                "틱톡" to AppGoalSetting(AppTimeInput("01", "00")),
            ),
            onEditAccessCount = {},
            onConfirm = {},
            onBack = {},
        )
    }
}
