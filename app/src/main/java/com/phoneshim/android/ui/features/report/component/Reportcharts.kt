package com.phoneshim.android.ui.features.report.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.phoneshim.android.ui.common.PhoneShimIcon
import com.phoneshim.android.ui.common.PhoneShimIconType
import com.phoneshim.android.ui.common.SectionCard
import com.phoneshim.android.ui.theme.PhoneShimDimens
import com.phoneshim.android.ui.theme.PhoneShimPalette
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimType
import kotlin.math.sqrt

/**
 * "07. 데일리 리포트" 화면군에서 쓰이는 차트/카드 컴포넌트와 UI 전용 모델.
 *
 * 여기 모델들은 화면 전용 표현 모델입니다. 실제 데이터 연결 시 ReportViewModel.uiState 의
 * DailyReport / AppUsage / TimetableEntry 를 이 모델로 매핑해서 사용하세요.
 */

// 앱 사용 카테고리 색상. Figma 팔레트(PhoneShimPalette)엔 아직 없는 토큰이라 리포트 화면 로컬로 정의합니다.
val ReportColorYellow = Color(0xFFD9B84A)
val ReportColorRed = Color(0xFFB5402E)
val ReportColorGreen = PhoneShimPalette.Primary500

data class AppBubble(val label: String, val color: Color, val value: Float)
data class UsageSegment(val color: Color, val ratio: Float, val entryId: String? = null)
data class CategoryUsageRow(val label: String, val segments: List<UsageSegment>)
data class HourUsage(val hourLabel: String, val segments: List<UsageSegment>)
data class UsageReasonLegend(val color: Color, val label: String)

enum class ReportPeriod(val label: String) { DAY("DAY"), WEEK("WEEK"), MONTH("MONTH") }

@Composable
fun ReportCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    SectionCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        borderColor = PhoneShimTheme.colors.divider,
        content = content,
    )
}

/** 어플 사용 분포 버블 차트. 원 크기(반지름)는 sqrt(value) 에 비례합니다 = 아이콘 크기 == 사용량. */
@Composable
fun AppUsageBubbleChart(
    bubbles: List<AppBubble>,
    modifier: Modifier = Modifier,
) {
    // 버블 좌표는 Figma 목업의 3버블 배치를 상대 좌표로 단순화한 값입니다.
    val positions = listOf(
        Offset(0.34f, 0.32f),
        Offset(0.62f, 0.5f),
        Offset(0.32f, 0.72f),
    )
    val maxValue = bubbles.maxOfOrNull { it.value } ?: 1f
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp)
            .background(PhoneShimTheme.colors.surfaceCream, RoundedCornerShape(12.dp)),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val maxRadius = size.minDimension * 0.28f
            bubbles.forEachIndexed { index, bubble ->
                val pos = positions.getOrElse(index) { Offset(0.5f, 0.5f) }
                val radius = maxRadius * sqrt(bubble.value / maxValue).coerceIn(0.35f, 1f)
                drawCircle(
                    color = bubble.color,
                    radius = radius,
                    center = Offset(size.width * pos.x, size.height * pos.y),
                )
            }
        }
    }
}

@Composable
fun ReportPeriodToggle(
    selected: ReportPeriod,
    onSelect: (ReportPeriod) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .background(PhoneShimTheme.colors.surfaceCream, RoundedCornerShape(8.dp))
            .padding(2.dp),
    ) {
        ReportPeriod.values().forEach { period ->
            val isSelected = period == selected
            Box(
                modifier = Modifier
                    .clickable { onSelect(period) }
                    .background(
                        color = if (isSelected) PhoneShimTheme.colors.surface else Color.Transparent,
                        shape = RoundedCornerShape(6.dp),
                    )
                    .padding(horizontal = PhoneShimDimens.spacing12, vertical = PhoneShimDimens.spacing8),
            ) {
                Text(
                    text = period.label,
                    style = PhoneShimType.EngCaption,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) PhoneShimTheme.colors.textPrimary else PhoneShimTheme.colors.textTertiary,
                )
            }
        }
    }
}

@Composable
fun UsageLegendDots(colors: List<Color>, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing8)) {
        colors.forEach { color ->
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .background(color, CircleShape),
            )
        }
    }
}

/** 카테고리별 어플 사용 요약 가로 막대. 막대 전체 길이 = 카테고리 사용 비중, 구간 색 = 앱별 구성비. */
@Composable
fun CategoryUsageBarChart(rows: List<CategoryUsageRow>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing12),
    ) {
        rows.forEach { row ->
            val totalRatio = row.segments.sumOf { it.ratio.toDouble() }.toFloat().coerceIn(0.05f, 1f)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = row.label,
                    style = PhoneShimType.KorBodyM,
                    color = PhoneShimTheme.colors.textSecondary,
                    modifier = Modifier.width(72.dp),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth(totalRatio)
                        .height(10.dp)
                        .clip(RoundedCornerShape(50)),
                ) {
                    row.segments.forEach { segment ->
                        if (segment.ratio > 0f) {
                            Box(
                                modifier = Modifier
                                    .weight(segment.ratio)
                                    .fillMaxHeight()
                                    .background(segment.color),
                            )
                        }
                    }
                }
            }
        }
    }
}

/** 시간대별(0~60분) 타임테이블 막대 차트. 상단 눈금은 분 단위 스케일(10~60)입니다. */
@Composable
fun TimetableChart(
    hours: List<HourUsage>,
    modifier: Modifier = Modifier,
    onSegmentClick: (String) -> Unit = {},
    scaleLabels: List<String> = listOf("10", "20", "30", "40", "50", "60"),
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 30.dp),
        ) {
            scaleLabels.forEach { label ->
                Text(
                    text = label,
                    style = PhoneShimType.KorMicro,
                    color = PhoneShimTheme.colors.textTertiary,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Spacer(modifier = Modifier.height(PhoneShimDimens.spacing4))
        hours.forEach { hour ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = hour.hourLabel,
                    style = PhoneShimType.KorMicro,
                    color = PhoneShimTheme.colors.textTertiary,
                    modifier = Modifier.width(30.dp),
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .border(width = 0.5.dp, color = PhoneShimTheme.colors.divider),
                ) {
                    hour.segments.forEach { segment ->
                        var segmentModifier: Modifier = Modifier
                            .fillMaxWidth(segment.ratio.coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .background(segment.color)
                        if (segment.entryId != null) {
                            segmentModifier = segmentModifier.clickable { onSegmentClick(segment.entryId) }
                        }
                        Box(modifier = segmentModifier)
                    }
                }
            }
        }
    }
}

@Composable
fun ReportSideActionButton(
    text: String,
    icon: PhoneShimIconType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(PhoneShimTheme.colors.brandSubtle, RoundedCornerShape(12.dp))
            .padding(PhoneShimDimens.spacing12),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PhoneShimIcon(
            type = icon,
            contentDescription = text,
            tint = PhoneShimTheme.colors.brandStrong,
        )
        Spacer(modifier = Modifier.height(PhoneShimDimens.spacing4))
        Text(text = text, style = PhoneShimType.KorLabel, color = PhoneShimTheme.colors.brandStrong)
    }
}

@Composable
fun UsageReasonLegendCard(items: List<UsageReasonLegend>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(PhoneShimTheme.colors.surface, RoundedCornerShape(12.dp))
            .border(1.dp, PhoneShimTheme.colors.divider, RoundedCornerShape(12.dp))
            .padding(PhoneShimDimens.spacing12),
        verticalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing8),
    ) {
        Text(text = "사용 이유", style = PhoneShimType.KorCaption, color = PhoneShimTheme.colors.textTertiary)
        items.forEach { item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing8),
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(item.color, CircleShape),
                )
                Text(text = item.label, style = PhoneShimType.KorCaption, color = PhoneShimTheme.colors.textSecondary)
            }
        }
    }
}
