package com.phoneshim.android.ui.features.setgoal.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.phoneshim.android.R
import com.phoneshim.android.ui.common.AppInfoRow
import com.phoneshim.android.ui.common.DurationDisplay
import com.phoneshim.android.ui.common.PrimaryButton
import com.phoneshim.android.ui.features.setgoal.component.AppIcon
import com.phoneshim.android.ui.features.setgoal.component.SetGoalCard
import com.phoneshim.android.ui.features.setgoal.viewmodel.AppTimeInput
import com.phoneshim.android.ui.features.setgoal.viewmodel.SetGoalViewModel
import com.phoneshim.android.ui.theme.PhoneShimDimens
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimType

// 완료 화면 어플별 목표 시간 한 줄. 아이콘을 PackageManager에서 읽으려면 packageName이 필요합니다.
private data class CompleteAppRow(
    val packageName: String,
    val label: String,
    val hour: String,
    val minute: String,
)

// 목표 설정 완료를 안내하는 화면 (Figma 04-6. 목표 설정 완료)
@Composable
fun SetGoalCompleteScreen(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SetGoalViewModel = hiltViewModel(),
) {
    // 플로우에서 설정한 목표 요약을 viewModel이 공유
    val uiState by viewModel.uiState.collectAsState()

    SetGoalCompleteContent(
        apps = uiState.selectedApps.map { app ->
            val time = uiState.appSettings[app.packageName]?.timeInput ?: AppTimeInput()
            CompleteAppRow(app.packageName, app.label, time.hour, time.minute)
        },
        // '총 목표 시간' 카드는 전체 폰 목표가 아니라 앱별 목표의 합계다(Figma 04-6).
        totalMinutes = uiState.appGoalTotalMinutes,
        onFinish = onFinish,
        modifier = modifier,
    )
}

@Composable
private fun SetGoalCompleteContent(
    apps: List<CompleteAppRow>,
    totalMinutes: Int,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Figma 04-6: Maincontainer p16, 세로 gap 24.
    // 버튼은 하단 고정(Figma 기준 버튼이 y=644~700, 컨테이너 724 → 하단 여백 24)이고
    // 마스코트·타이틀·요약 카드만 위쪽에 쌓입니다. 화면이 짧으면 위 묶음이 스크롤됩니다.
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PhoneShimTheme.colors.brandSubtle),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(
                    start = PhoneShimDimens.spacing16,
                    end = PhoneShimDimens.spacing16,
                    top = PhoneShimDimens.spacing16,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing24),
        ) {
            // 캐릭터 '쉼이' (Figma 04-6 — Frame 3 328×180 안에 176dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.setgoal_mascot_complete),
                    contentDescription = null,
                    modifier = Modifier.size(176.dp),
                )
            }

            // 타이틀 묶음 (py 12, gap 12)
            Column(
                modifier = Modifier.padding(vertical = PhoneShimDimens.spacing12),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing12),
            ) {
                Text(
                    text = "목표 설정이\n완료되었어요!",
                    style = PhoneShimType.KorH1,
                    color = PhoneShimTheme.colors.brandStrong,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "이제 더 건강한 디지털 습관을 만들어봐요!",
                    style = PhoneShimType.KorBodyM,
                    color = PhoneShimTheme.colors.textPrimary,
                )
            }

            // 요약 카드 (Figma 04-6: 흰 카드 p16, 내부 gap 12)
            SetGoalCard(
                contentPadding = PhoneShimDimens.spacing16,
                verticalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing12),
            ) {
                // 총 목표 시간
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing12),
                ) {
                    Text(
                        text = "총 목표 시간",
                        style = PhoneShimType.KorCaption,
                        color = PhoneShimTheme.colors.textPrimary,
                    )
                    DurationDisplay(totalMinutes = totalMinutes)
                }

                HorizontalDivider(thickness = 1.dp, color = PhoneShimTheme.colors.divider)

                // 어플 별 목표 시간
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing12),
                ) {
                    Text(
                        text = "어플 별 목표 시간",
                        style = PhoneShimType.KorCaption,
                        color = PhoneShimTheme.colors.textPrimary,
                    )
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing12),
                    ) {
                        apps.forEach { row ->
                            AppInfoRow(
                                appName = row.label,
                                appNameStyle = PhoneShimType.KorCaption,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(28.dp),
                                iconContent = { AppIcon(packageName = row.packageName) },
                                trailingContent = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing8),
                                    ) {
                                        CompleteTimeUnit(value = row.hour, unit = "시간")
                                        CompleteTimeUnit(value = row.minute, unit = "분")
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }

        // 메인으로 이동 — 하단 고정 (Figma 04-6: 카드 아래 gap 24, 하단 여백 24)
        PrimaryButton(
            text = "메인으로 이동",
            onClick = onFinish,
            modifier = Modifier.padding(
                start = PhoneShimDimens.spacing16,
                end = PhoneShimDimens.spacing16,
                top = PhoneShimDimens.spacing24,
                bottom = PhoneShimDimens.spacing24,
            ),
        )
    }
}

// 완료 화면 어플별 시간 표시 (숫자 EngLabel + 단위 KorLabel, gap 4)
@Composable
private fun CompleteTimeUnit(value: String, unit: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing4),
    ) {
        Text(
            text = value.ifEmpty { "00" },
            style = PhoneShimType.EngLabel,
            color = PhoneShimTheme.colors.textPrimary,
        )
        Text(
            text = unit,
            style = PhoneShimType.KorLabel,
            color = PhoneShimTheme.colors.textSecondary,
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun SetGoalCompleteScreenPreview() {
    PhoneShimTheme {
        SetGoalCompleteContent(
            apps = listOf(
                CompleteAppRow("com.kakao.talk", "카카오톡", "01", "00"),
                CompleteAppRow("com.facebook.katana", "페이스북", "01", "30"),
                CompleteAppRow("com.zhiliaoapp.musically", "틱톡", "01", "00"),
            ),
            totalMinutes = 210,
            onFinish = {},
        )
    }
}
