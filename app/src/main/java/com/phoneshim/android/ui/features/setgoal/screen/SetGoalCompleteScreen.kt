package com.phoneshim.android.ui.features.setgoal.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.phoneshim.android.ui.common.PrimaryButton
import com.phoneshim.android.ui.features.setgoal.component.AppLabel
import com.phoneshim.android.ui.features.setgoal.component.SetGoalCard
import com.phoneshim.android.ui.features.setgoal.component.SetGoalCardDivider
import com.phoneshim.android.ui.features.setgoal.viewmodel.AppTimeInput
import com.phoneshim.android.ui.features.setgoal.viewmodel.SetGoalViewModel
import com.phoneshim.android.ui.theme.PhoneShimDimens
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimType

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
            val time = uiState.appSettings[app]?.timeInput ?: AppTimeInput()
            Triple(app, time.hour, time.minute)
        },
        totalMinutes = uiState.totalMinutes,
        onFinish = onFinish,
        modifier = modifier,
    )
}

@Composable
private fun SetGoalCompleteContent(
    apps: List<Triple<String, String, String>>,
    totalMinutes: Int,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Figma Maincontainer: padding 16, 섹션 간 간격 24, 가로 중앙 정렬
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PhoneShimTheme.colors.brandSubtle)
            .verticalScroll(rememberScrollState())
            .padding(PhoneShimDimens.spacing16),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing24),
    ) {
        // 캐릭터 영역 (에셋 확정 전 placeholder)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "캐릭터의 목표 설정 페이지로 넘어가는 문구",
                style = PhoneShimType.KorCaption,
                color = PhoneShimTheme.colors.textPrimary,
            )
        }

        Column(
            modifier = Modifier.padding(vertical = PhoneShimDimens.spacing12),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing12),
        ) {
            Text(
                text = "목표 설정이\n완료되었어요!",
                style = PhoneShimType.KorH3.copy(fontSize = 24.sp, lineHeight = 31.sp),
                fontWeight = FontWeight.Bold,
                color = PhoneShimTheme.colors.brandStrong,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "이제 더 건강한 디지털 습관을 만들어봐요!",
                style = PhoneShimType.KorBodyM,
                color = PhoneShimTheme.colors.textPrimary,
            )
        }

        SetGoalCard {
            Text(
                text = "총 목표 시간",
                style = PhoneShimType.KorCaption,
                color = PhoneShimTheme.colors.textPrimary,
            )
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing4),
                modifier = Modifier.padding(top = PhoneShimDimens.spacing12),
            ) {
                Text(
                    text = "${totalMinutes / 60}",
                    style = PhoneShimType.KorH3.copy(fontSize = 24.sp),
                    fontWeight = FontWeight.Bold,
                    color = PhoneShimTheme.colors.textPrimary,
                )
                Text(
                    text = "시간",
                    style = PhoneShimType.KorH3,
                    color = PhoneShimTheme.colors.textPrimary,
                )
                Text(
                    text = "${totalMinutes % 60}",
                    style = PhoneShimType.KorH3.copy(fontSize = 24.sp),
                    fontWeight = FontWeight.Bold,
                    color = PhoneShimTheme.colors.textPrimary,
                    modifier = Modifier.padding(start = PhoneShimDimens.spacing8),
                )
                Text(
                    text = "분",
                    style = PhoneShimType.KorH3,
                    color = PhoneShimTheme.colors.textPrimary,
                )
            }

            SetGoalCardDivider()

            Text(
                text = "어플 별 목표 시간",
                style = PhoneShimType.KorCaption,
                color = PhoneShimTheme.colors.textPrimary,
                modifier = Modifier.padding(bottom = PhoneShimDimens.spacing12),
            )
            apps.forEach { (app, hour, minute) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AppLabel(name = app)
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "$hour 시간 $minute 분",
                        style = PhoneShimType.KorLabel,
                        color = PhoneShimTheme.colors.textPrimary,
                    )
                }
                if (app != apps.last().first) {
                    Spacer(modifier = Modifier.height(PhoneShimDimens.spacing12))
                }
            }
        }

        PrimaryButton(
            text = "메인으로 이동",
            onClick = onFinish,
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun SetGoalCompleteScreenPreview() {
    PhoneShimTheme {
        SetGoalCompleteContent(
            apps = listOf(
                Triple("카카오톡", "01", "00"),
                Triple("페이스북", "01", "30"),
                Triple("틱톡", "01", "00"),
            ),
            totalMinutes = 210,
            onFinish = {},
        )
    }
}
