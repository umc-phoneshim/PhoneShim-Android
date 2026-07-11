package com.phoneshim.android.ui.features.report.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.phoneshim.android.ui.theme.PhoneShimDimens
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimType

/**
 * "07. 데일리 리포트" 화면군(ReportSummaryScreen / TimetableScreen)이 공유하는
 * 상단바, 날짜 네비게이터, 탭, 하단 네비게이션 바.
 *
 * 프로젝트에 material-icons-extended 의존성이 없어, 아이콘은 Canvas 로 직접 그립니다.
 */

enum class LineIconType { Target, Person, Home, Clock, Document, Info, Bell }

@Composable
fun LineIcon(
    type: LineIconType,
    modifier: Modifier = Modifier,
    tint: Color = PhoneShimTheme.colors.textPrimary,
) {
    Canvas(modifier = modifier.size(22.dp)) {
        val strokeWidth = size.minDimension * 0.09f
        val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        when (type) {
            LineIconType.Target -> {
                drawCircle(tint, radius = size.minDimension / 2 * 0.85f, style = stroke)
                drawCircle(tint, radius = size.minDimension / 2 * 0.32f, style = stroke)
            }

            LineIconType.Person -> {
                drawCircle(
                    color = tint,
                    radius = size.minDimension * 0.17f,
                    center = Offset(size.width / 2f, size.height * 0.32f),
                    style = stroke,
                )
                drawArc(
                    color = tint,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(size.width * 0.16f, size.height * 0.46f),
                    size = Size(size.width * 0.68f, size.height * 0.5f),
                    style = stroke,
                )
            }

            LineIconType.Home -> {
                val w = size.width
                val h = size.height
                val roof = Path().apply {
                    moveTo(w * 0.1f, h * 0.55f)
                    lineTo(w * 0.5f, h * 0.15f)
                    lineTo(w * 0.9f, h * 0.55f)
                }
                val body = Path().apply {
                    moveTo(w * 0.22f, h * 0.48f)
                    lineTo(w * 0.22f, h * 0.88f)
                    lineTo(w * 0.78f, h * 0.88f)
                    lineTo(w * 0.78f, h * 0.48f)
                }
                drawPath(roof, tint, style = stroke)
                drawPath(body, tint, style = stroke)
            }

            LineIconType.Clock -> {
                drawCircle(tint, radius = size.minDimension / 2 * 0.85f, style = stroke)
                drawLine(
                    tint,
                    start = Offset(size.width / 2f, size.height / 2f),
                    end = Offset(size.width / 2f, size.height * 0.28f),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
                drawLine(
                    tint,
                    start = Offset(size.width / 2f, size.height / 2f),
                    end = Offset(size.width * 0.68f, size.height / 2f),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
            }

            LineIconType.Document -> {
                val w = size.width
                val h = size.height
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(w * 0.22f, h * 0.1f),
                    size = Size(w * 0.56f, h * 0.8f),
                    cornerRadius = CornerRadius(w * 0.06f),
                    style = stroke,
                )
                listOf(0.36f, 0.52f, 0.68f).forEach { yFrac ->
                    drawLine(
                        tint,
                        start = Offset(w * 0.34f, h * yFrac),
                        end = Offset(w * 0.66f, h * yFrac),
                        strokeWidth = strokeWidth * 0.7f,
                        cap = StrokeCap.Round,
                    )
                }
            }

            LineIconType.Info -> {
                drawCircle(tint, radius = size.minDimension / 2 * 0.85f, style = stroke)
                drawCircle(tint, radius = strokeWidth * 0.6f, center = Offset(size.width / 2f, size.height * 0.32f))
                drawLine(
                    tint,
                    start = Offset(size.width / 2f, size.height * 0.46f),
                    end = Offset(size.width / 2f, size.height * 0.72f),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
            }

            LineIconType.Bell -> {
                val w = size.width
                val h = size.height
                val body = Path().apply {
                    moveTo(w * 0.28f, h * 0.62f)
                    cubicTo(w * 0.28f, h * 0.32f, w * 0.72f, h * 0.32f, w * 0.72f, h * 0.62f)
                    lineTo(w * 0.8f, h * 0.72f)
                    lineTo(w * 0.2f, h * 0.72f)
                    close()
                }
                drawPath(body, tint, style = stroke)
                drawLine(
                    tint,
                    start = Offset(w * 0.44f, h * 0.78f),
                    end = Offset(w * 0.56f, h * 0.78f),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
            }
        }
    }
}

@Composable
fun DailyReportHeader(
    dateLabel: String,
    onPrevDate: () -> Unit,
    onNextDate: () -> Unit,
    modifier: Modifier = Modifier,
    onProfileClick: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = PhoneShimDimens.screenHorizontalPadding),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = PhoneShimDimens.spacing12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LineIcon(LineIconType.Target)
            Text(
                text = "DAILY REPORT",
                style = PhoneShimType.KorH3,
                color = PhoneShimTheme.colors.textPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
            LineIcon(
                type = LineIconType.Person,
                modifier = Modifier.clickable(onClick = onProfileClick),
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = PhoneShimDimens.spacing16),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "‹",
                style = PhoneShimType.EngH2,
                color = PhoneShimTheme.colors.textTertiary,
                modifier = Modifier
                    .clickable(onClick = onPrevDate)
                    .padding(horizontal = PhoneShimDimens.spacing16),
            )
            Text(
                text = dateLabel,
                style = PhoneShimType.EngH2,
                color = PhoneShimTheme.colors.textPrimary,
            )
            Text(
                text = "›",
                style = PhoneShimType.EngH2,
                color = PhoneShimTheme.colors.textTertiary,
                modifier = Modifier
                    .clickable(onClick = onNextDate)
                    .padding(horizontal = PhoneShimDimens.spacing16),
            )
        }
    }
}

enum class ReportTab(val label: String) {
    SUMMARY("어플 사용 통계"),
    TIMETABLE("타임테이블"),
}

@Composable
fun ReportTabRow(
    selected: ReportTab,
    onTabSelected: (ReportTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = PhoneShimDimens.screenHorizontalPadding)
            .background(PhoneShimTheme.colors.surface, RoundedCornerShape(50))
            .border(1.dp, PhoneShimTheme.colors.border, RoundedCornerShape(50))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        ReportTab.values().forEach { tab ->
            val isSelected = tab == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTabSelected(tab) }
                    .background(
                        color = if (isSelected) PhoneShimTheme.colors.brand else Color.Transparent,
                        shape = RoundedCornerShape(50),
                    )
                    .padding(vertical = PhoneShimDimens.spacing12),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = tab.label,
                    style = PhoneShimType.KorBodyM,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) PhoneShimTheme.colors.onBrand else PhoneShimTheme.colors.textSecondary,
                )
            }
        }
    }
}

enum class BottomNavTab(val label: String, val icon: LineIconType) {
    MAIN("메인", LineIconType.Home),
    REMINDER("리마인더", LineIconType.Clock),
    REPORT("리포트", LineIconType.Document),
}

@Composable
fun ReportBottomNavBar(
    selected: BottomNavTab,
    onTabSelected: (BottomNavTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(PhoneShimTheme.colors.surface)
            .border(width = 1.dp, color = PhoneShimTheme.colors.divider)
            .padding(vertical = PhoneShimDimens.spacing8),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        BottomNavTab.values().forEach { tab ->
            val isSelected = tab == selected
            val tint = if (isSelected) PhoneShimTheme.colors.brand else PhoneShimTheme.colors.textTertiary
            Column(
                modifier = Modifier
                    .clickable { onTabSelected(tab) }
                    .padding(horizontal = PhoneShimDimens.spacing12),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                LineIcon(type = tab.icon, tint = tint)
                Text(text = tab.label, style = PhoneShimType.KorLabel, color = tint)
            }
        }
    }
}