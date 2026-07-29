package com.phoneshim.android.ui.features.report.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.phoneshim.android.ui.common.PhoneShimIcon
import com.phoneshim.android.ui.common.PhoneShimIconType
import com.phoneshim.android.ui.common.SectionCard
import com.phoneshim.android.ui.theme.PhoneShimDimens
import com.phoneshim.android.ui.theme.PhoneShimPalette
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimType
import kotlin.math.sqrt

/*
  "07. 데일리 리포트" 화면군에서 쓰이는 차트/카드 컴포넌트와 UI 전용 모델.
 */

// 앱 사용 카테고리 색상. Figma 팔레트(PhoneShimPalette)엔 아직 없는 토큰이라 리포트 화면 로컬로 정의합니다.
val ReportColorYellow = Color(0xFFD9B84A)
val ReportColorRed = Color(0xFFB5402E)
val ReportColorGreen = PhoneShimPalette.Primary500

data class AppBubble(val label: String, val color: Color, val value: Float)

/**
 * 막대 한 구간.
 *
 * @param startRatio 막대가 시작하는 위치(0~1). 타임테이블처럼 구간의 시작 시각이
 *  의미를 갖는 차트에서 사용합니다. 카테고리 막대는 기본값 0 을 그대로 씁니다.
 */
data class UsageSegment(
    val color: Color,
    val ratio: Float,
    val entryId: String? = null,
    val startRatio: Float = 0f,
)

data class CategoryUsageRow(val label: String, val segments: List<UsageSegment>)
data class HourUsage(val hourLabel: String, val segments: List<UsageSegment>)
data class UsageReasonLegend(val color: Color, val label: String)

/**
 * 타임테이블 오른쪽 "사용 어플" 카드 항목.
 *
 * [packageName] 이 있으면 기기의 PackageManager 에서 실제 앱 아이콘과 이름을 읽어 씁니다.
 * 서버가 아이콘을 내려주지 않아도 되고, 앱이 삭제됐거나 packageName 이 없으면
 * [color] 로 칠한 원과 [name] 으로 대체합니다.
 */
data class UsedApp(
    val name: String,
    val color: Color,
    val packageName: String = "",
)

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

/** DAY / WEEK / MONTH 토글. 선택된 항목은 브랜드 색으로 채워 확실히 구분합니다. */
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
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        ReportPeriod.entries.forEach { period ->
            val isSelected = period == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        color = if (isSelected) PhoneShimTheme.colors.brand else Color.Transparent,
                    )
                    .clickable { onSelect(period) }
                    .padding(horizontal = PhoneShimDimens.spacing12, vertical = PhoneShimDimens.spacing8),
            ) {
                Text(
                    text = period.label,
                    style = PhoneShimType.EngCaption,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) {
                        PhoneShimTheme.colors.onBrand
                    } else {
                        PhoneShimTheme.colors.textTertiary
                    },
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

// 타임테이블 치수. 막대는 얇게, 시간 행 사이는 살짝만 띄웁니다.
private val TimetableHourLabelWidth = 30.dp
private val TimetableBarHeight = 8.dp
private val TimetableRowHeight = 16.dp
private val TimetableRowGap = 3.dp

/**
 * 시간대별 타임테이블. 상단 눈금은 분 단위 스케일(10~60)입니다.
 *
 * 각 행은 연한 트랙 위에 얇은 막대를 얹는 형태이고,
 * 막대의 가로 위치는 [UsageSegment.startRatio], 길이는 [UsageSegment.ratio] 로 정합니다.
 */
@Composable
fun TimetableChart(
    hours: List<HourUsage>,
    modifier: Modifier = Modifier,
    onSegmentClick: (String) -> Unit = {},
    scaleLabels: List<String> = listOf("10", "20", "30", "40", "50", "60"),
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(TimetableRowGap),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = TimetableHourLabelWidth),
        ) {
            scaleLabels.forEach { label ->
                Text(
                    text = label,
                    style = PhoneShimType.KorMicro,
                    color = PhoneShimTheme.colors.textTertiary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Spacer(modifier = Modifier.height(PhoneShimDimens.spacing4))

        hours.forEach { hour ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(TimetableRowHeight),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = hour.hourLabel,
                    style = PhoneShimType.KorMicro,
                    color = PhoneShimTheme.colors.textTertiary,
                    modifier = Modifier.width(TimetableHourLabelWidth),
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(TimetableBarHeight)
                        .clip(RoundedCornerShape(50))
                        .background(PhoneShimTheme.colors.surfaceCream),
                ) {
                    hour.segments.forEach { segment ->
                        TimetableSegment(segment = segment, onSegmentClick = onSegmentClick)
                    }
                }
            }
        }
    }
}

@Composable
private fun TimetableSegment(segment: UsageSegment, onSegmentClick: (String) -> Unit) {
    val start = segment.startRatio.coerceIn(0f, 1f)
    val length = segment.ratio.coerceIn(0f, 1f - start)
    if (length <= 0f) return
    val tail = (1f - start - length).coerceAtLeast(0f)

    // Row 의 weight 는 0보다 커야 해서 양 끝 여백에 아주 작은 값을 더해 둡니다.
    val epsilon = 0.0001f
    Row(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.weight(start + epsilon))
        Box(
            modifier = Modifier
                .weight(length)
                .fillMaxHeight()
                .clip(RoundedCornerShape(50))
                .background(segment.color)
                .let { base ->
                    if (segment.entryId != null) {
                        base.clickable { onSegmentClick(segment.entryId) }
                    } else {
                        base
                    }
                },
        )
        Spacer(modifier = Modifier.weight(tail + epsilon))
    }
}

/** 타임테이블 오른쪽 액션 묶음. "제안 보기"와 "알림 설정"을 한 카드에 담습니다. */
@Composable
fun ReportSideActionCard(
    onSuggestionClick: () -> Unit,
    onAlarmSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(PhoneShimTheme.colors.brandSubtle, RoundedCornerShape(16.dp))
            .padding(vertical = PhoneShimDimens.spacing8),
        verticalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing4),
    ) {
        SideActionRow(text = "제안 보기", icon = PhoneShimIconType.Info, onClick = onSuggestionClick)
        SideActionRow(text = "알림 설정", icon = PhoneShimIconType.Bell, onClick = onAlarmSettingsClick)
    }
}

@Composable
private fun SideActionRow(
    text: String,
    icon: PhoneShimIconType,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = PhoneShimDimens.spacing8, vertical = PhoneShimDimens.spacing8),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing8),
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(PhoneShimTheme.colors.surface, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            PhoneShimIcon(
                type = icon,
                contentDescription = text,
                tint = PhoneShimTheme.colors.brandStrong,
                modifier = Modifier.size(14.dp),
            )
        }
        Text(
            text = text,
            style = PhoneShimType.KorCaption,
            color = PhoneShimTheme.colors.brandStrong,
        )
    }
}

/** 타임테이블 오른쪽 "사용 어플" 카드. */
@Composable
fun UsedAppsCard(apps: List<UsedApp>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(PhoneShimTheme.colors.surface, RoundedCornerShape(16.dp))
            .border(1.dp, PhoneShimTheme.colors.divider, RoundedCornerShape(16.dp))
            .padding(PhoneShimDimens.spacing12),
        verticalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing12),
    ) {
        Text(
            text = "사용 어플",
            style = PhoneShimType.KorCaption,
            color = PhoneShimTheme.colors.textTertiary,
        )
        if (apps.isEmpty()) {
            Text(
                text = "기록 없음",
                style = PhoneShimType.KorMicro,
                color = PhoneShimTheme.colors.textTertiary,
            )
        }
        apps.forEach { app ->
            val info = rememberInstalledAppInfo(app.packageName)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing8),
            ) {
                if (info?.icon != null) {
                    Image(
                        bitmap = info.icon,
                        contentDescription = app.name,
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape),
                    )
                } else {
                    // 앱이 삭제됐거나 packageName 을 모르는 경우.
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(app.color, CircleShape),
                    )
                }
                Text(
                    text = info?.label?.takeIf { it.isNotBlank() } ?: app.name,
                    style = PhoneShimType.KorCaption,
                    color = PhoneShimTheme.colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/** PackageManager 에서 읽어온 앱 표시 정보. */
private data class InstalledAppInfo(val label: String, val icon: ImageBitmap?)

/**
 * 설치된 앱의 아이콘과 이름을 기기에서 직접 읽습니다.
 *
 * 서버가 아이콘을 내려줄 필요가 없습니다. manifest 의 <queries> 에 LAUNCHER 인텐트가
 * 선언돼 있어 API 30+ 패키지 가시성 제한에도 런처 앱은 조회할 수 있습니다.
 * 조회 실패(미설치·가시성 없음)는 null 로 떨어뜨려 호출부에서 대체 표시합니다.
 */
@Composable
private fun rememberInstalledAppInfo(packageName: String): InstalledAppInfo? {
    if (packageName.isBlank()) return null
    val context = LocalContext.current
    return remember(packageName) {
        runCatching {
            val pm = context.packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            InstalledAppInfo(
                label = pm.getApplicationLabel(appInfo).toString(),
                icon = pm.getApplicationIcon(appInfo).toBitmap().asImageBitmap(),
            )
        }.getOrNull()
    }
}

/** 사용 이유 범례. */
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
