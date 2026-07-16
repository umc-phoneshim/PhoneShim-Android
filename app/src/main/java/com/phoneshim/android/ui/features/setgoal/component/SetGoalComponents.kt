package com.phoneshim.android.ui.features.setgoal.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.CenterStart),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "뒤로가기",
                tint = PhoneShimTheme.colors.textPrimary,
            )
        }
        Text(
            text = "목표 설정",
            style = PhoneShimType.KorBodyL,
            fontWeight = FontWeight.SemiBold,
            color = PhoneShimTheme.colors.textPrimary,
            modifier = Modifier.align(Alignment.Center),
        )
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
                            PhoneShimTheme.colors.surface
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
            fontWeight = FontWeight.Bold,
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
@Composable
fun SetGoalCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(PhoneShimTheme.colors.surface)
            .border(1.dp, PhoneShimPalette.Primary300, MaterialTheme.shapes.medium)
            .padding(PhoneShimDimens.spacing12),
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

// 앱 아이콘 자리 placeholder (에셋 확정 전 회색 박스) + 앱 이름
@Composable
fun AppLabel(
    name: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing12),
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(MaterialTheme.shapes.extraSmall)
                .background(PhoneShimTheme.colors.divider),
        )
        Text(
            text = name,
            style = PhoneShimType.KorBodyM,
            color = PhoneShimTheme.colors.textPrimary,
        )
    }
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
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(PhoneShimTheme.colors.surface)
                    .border(1.dp, PhoneShimPalette.Primary400, MaterialTheme.shapes.medium)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "뒤로가기",
                    style = PhoneShimType.KorCaption,
                    color = PhoneShimPalette.Primary400,
                )
            }
        } else {
            Box(modifier = Modifier.weight(1f))
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .height(42.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(
                    if (nextEnabled) PhoneShimTheme.colors.brand else PhoneShimPalette.Primary300,
                )
                .clickable(enabled = nextEnabled, onClick = onNext),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = nextText,
                style = PhoneShimType.KorCaption,
                color = PhoneShimTheme.colors.onBrand,
            )
        }
    }
}

// 총 목표 시간 표시 카드 (연녹 배경 + 큰 시간 표기)
@Composable
fun TotalTimeCard(
    totalMinutes: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(PhoneShimTheme.colors.brandSubtle)
            .border(1.dp, PhoneShimPalette.Primary300, MaterialTheme.shapes.medium)
            .padding(PhoneShimDimens.spacing16),
        verticalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing8),
    ) {
        Text(
            text = "총 목표 시간",
            style = PhoneShimType.KorCaption,
            color = PhoneShimTheme.colors.brandStrong,
        )
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing4),
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
    }
}
