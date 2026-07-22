package com.phoneshim.android.ui.features.setgoal.screen

import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.phoneshim.android.ui.features.setgoal.component.AppLabel
import com.phoneshim.android.ui.features.setgoal.component.SetGoalBottomButtons
import com.phoneshim.android.ui.features.setgoal.component.SetGoalCard
import com.phoneshim.android.ui.features.setgoal.component.SetGoalCardDivider
import com.phoneshim.android.ui.features.setgoal.component.SetGoalStepIndicator
import com.phoneshim.android.ui.features.setgoal.component.SetGoalTitle
import com.phoneshim.android.ui.features.setgoal.component.SetGoalTopBar
import com.phoneshim.android.ui.features.setgoal.viewmodel.SetGoalEffect
import com.phoneshim.android.ui.features.setgoal.viewmodel.SetGoalEvent
import com.phoneshim.android.ui.features.setgoal.viewmodel.SetGoalViewModel
import com.phoneshim.android.ui.theme.PhoneShimDimens
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimType

// 설치 앱 목록 연동 전 placeholder 목록
private val SampleApps = listOf("카카오톡", "페이스북", "틱톡", "유튜브")

// 목표 대상으로 절제할 앱을 선택하는 화면 (Figma 04-2. 어플 선택)
@Composable
fun AppSelectScreen(
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    viewModel: SetGoalViewModel = hiltViewModel(),
) {
    // TODO: 설치된 앱 목록 조회 연동 (선택 상태는 viewModel이 플로우 전체에 공유)
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

    AppSelectContent(
        apps = SampleApps,
        selectedApps = uiState.selectedApps,
        onToggleApp = { viewModel.onEvent(SetGoalEvent.ToggleApp(it)) },
        onNext = { viewModel.onEvent(SetGoalEvent.SubmitAppSelection) },
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
private fun AppSelectContent(
    apps: List<String>,
    selectedApps: List<String>,
    onToggleApp: (String) -> Unit,
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
            SetGoalStepIndicator(currentStep = 2)
            SetGoalTitle(
                title = "특별히 관리가 필요한\n‘주의 앱’을 선택해주세요!",
                subtitle = "최대 5개까지 가능합니다",
            )

            // 라벨과 카드는 12dp 간격의 한 묶음 (Figma Frame 7)
            Column(verticalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing12)) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            PhoneShimType.KorBodyL
                                .copy(fontWeight = FontWeight.SemiBold)
                                .toSpanStyle(),
                        ) {
                            append("앱 선택 ")
                        }
                        withStyle(PhoneShimType.KorCaption.toSpanStyle()) {
                            append("(최대 5개)")
                        }
                    },
                    color = PhoneShimTheme.colors.textPrimary,
                )

                SetGoalCard {
                    apps.forEachIndexed { index, app ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(28.dp)
                                .clickable { onToggleApp(app) },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            AppLabel(name = app)
                            Spacer(modifier = Modifier.weight(1f))
                            AppCheckCircle(checked = selectedApps.contains(app))
                        }
                        if (index != apps.lastIndex) {
                            SetGoalCardDivider()
                        }
                    }

                    SetGoalCardDivider()

                    // TODO: 기타 어플 추가 플로우 연동
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                            .clickable { },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing8),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .border(1.dp, PhoneShimTheme.colors.textTertiary, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = null,
                                tint = PhoneShimTheme.colors.textTertiary,
                                modifier = Modifier.size(12.dp),
                            )
                        }
                        Text(
                            text = "기타 어플 추가",
                            style = PhoneShimType.KorLabel,
                            color = PhoneShimTheme.colors.textTertiary,
                        )
                    }
                }
            }

            // Figma 04-2: 버튼은 하단 고정이 아니라 카드 아래 24dp 간격(컬럼 gap)으로 배치
            SetGoalBottomButtons(
                onBack = onBack,
                onNext = onNext,
            )
        }
    }
}

// 원형 체크 표시. 선택 시 브랜드 컬러 원 + 흰색 체크로 채워집니다.
@Composable
private fun AppCheckCircle(
    checked: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(
                if (checked) PhoneShimTheme.colors.brand else PhoneShimTheme.colors.surface,
            )
            .border(
                width = 1.dp,
                color = if (checked) PhoneShimTheme.colors.brand else PhoneShimTheme.colors.border,
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = PhoneShimTheme.colors.onBrand,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun AppSelectScreenPreview() {
    PhoneShimTheme {
        AppSelectContent(
            apps = SampleApps,
            selectedApps = listOf("카카오톡", "페이스북", "틱톡"),
            onToggleApp = {},
            onNext = {},
            onBack = {},
        )
    }
}
