package com.phoneshim.android.ui.features.setgoal.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.phoneshim.android.R
import com.phoneshim.android.ui.common.PhoneShimButtonSize
import com.phoneshim.android.ui.common.PrimaryButton
import com.phoneshim.android.ui.common.SecondaryButton
import com.phoneshim.android.ui.theme.PhoneShimDimens
import com.phoneshim.android.ui.theme.PhoneShimPalette
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimType

// ── 목표 설정 플로우 공용 컴포넌트 (Figma 04-x 공통 요소) ──────────────

// 상단 바: 뒤로가기 화살표 + 중앙 "목표 설정" 타이틀
@Composable
fun SetGoalTopBar(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = PhoneShimDimens.spacing16),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_back),
                contentDescription = "뒤로가기",
                tint = Color.Unspecified,
                modifier = Modifier.size(24.dp),
            )
        }
        Text(
            text = "목표 설정",
            style = PhoneShimType.KorBodyL.copy(
                fontWeight = FontWeight.Bold,
                lineHeight = 24.sp,
                letterSpacing = (-0.005).em,
            ),
            color = PhoneShimTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.size(24.dp))
    }
}

// 1~4 단계 스텝 인디케이터. 현재 단계만 브랜드 컬러로 채워집니다.
@Composable
fun SetGoalStepIndicator(
    currentStep: Int,
    modifier: Modifier = Modifier,
    totalSteps: Int = 4,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for (step in 1..totalSteps) {
            val isActive = step == currentStep
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        if (isActive) {
                            PhoneShimTheme.colors.brandStrong
                        } else {
                            Color.Transparent
                        },
                    )
                    .border(1.dp, PhoneShimTheme.colors.brandStrong, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = step.toString(),
                    style = PhoneShimType.EngCaption,
                    color = if (isActive) {
                        PhoneShimTheme.colors.onBrand
                    } else {
                        PhoneShimTheme.colors.brandStrong
                    },
                )
            }
            if (step < totalSteps) {
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(1.dp)
                        .background(PhoneShimTheme.colors.brandStrong),
                )
            }
        }
    }
}

// 화면 상단 타이틀 + 서브타이틀 (중앙 정렬)
@Composable
fun SetGoalTitle(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    subtitleStyle: androidx.compose.ui.text.TextStyle = PhoneShimType.KorBodyM,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = PhoneShimDimens.spacing24),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing12),
    ) {
        Text(
            text = title,
            style = PhoneShimType.KorH3,
            color = PhoneShimTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = subtitle,
            style = subtitleStyle,
            color = PhoneShimTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
        )
    }
}

// 흰색 라운드 카드 컨테이너 (목록형 콘텐츠 감싸기)
// Figma 04-x 카드: 기본 내부 여백 12dp (완료 화면 카드만 16dp)
@Composable
fun SetGoalCard(
    modifier: Modifier = Modifier,
    contentPadding: Dp = PhoneShimDimens.spacing12,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(PhoneShimTheme.colors.surface)
            .border(1.dp, PhoneShimPalette.Primary300, MaterialTheme.shapes.medium)
            .padding(contentPadding),
        verticalArrangement = verticalArrangement,
        content = content,
    )
}

// 카드 내부 행 사이 구분선
@Composable
fun SetGoalCardDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier.padding(vertical = PhoneShimDimens.spacing12),
        thickness = 1.dp,
        color = PhoneShimTheme.colors.divider,
    )
}

// 뒤로가기(아웃라인) + 다음(채움) 하단 버튼 한 쌍
@Composable
fun SetGoalBottomButtons(
    onBack: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
    nextText: String = "다음",
    nextEnabled: Boolean = true,
    showBack: Boolean = true,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing8),
    ) {
        if (showBack) {
            SecondaryButton(
                text = "뒤로가기",
                onClick = onBack,
                modifier = Modifier.weight(1f),
                size = PhoneShimButtonSize.Medium,
                fullWidth = false,
                labelStyle = PhoneShimType.KorCaption,
            )
        } else {
            Box(modifier = Modifier.weight(1f))
        }
        PrimaryButton(
            text = nextText,
            onClick = onNext,
            modifier = Modifier.weight(1f),
            enabled = nextEnabled,
            size = PhoneShimButtonSize.Medium,
            fullWidth = false,
            labelStyle = PhoneShimType.KorCaption,
        )
    }
}

@Preview(name = "목표 설정 하단 버튼", showBackground = true, widthDp = 360)
@Composable
private fun SetGoalBottomButtonsPreview() {
    PhoneShimTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SetGoalBottomButtons(onBack = {}, onNext = {})
            SetGoalBottomButtons(onBack = {}, onNext = {}, nextEnabled = false)
            SetGoalBottomButtons(onBack = {}, onNext = {}, showBack = false)
        }
    }
}
