package com.phoneshim.android.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.phoneshim.android.ui.theme.PhoneShimTheme

/**
 * 폰쉼 공용 라인 아이콘.
 */
enum class LineIconType { Target, Person, Home, Clock, Document, Info, Bell, ChevronRight, ChevronLeft }

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

            LineIconType.ChevronRight -> {
                val w = size.width
                val h = size.height
                val path = Path().apply {
                    moveTo(w * 0.38f, h * 0.22f)
                    lineTo(w * 0.68f, h * 0.5f)
                    lineTo(w * 0.38f, h * 0.78f)
                }
                drawPath(path, tint, style = stroke)
            }

            LineIconType.ChevronLeft -> {
                val w = size.width
                val h = size.height
                val path = Path().apply {
                    moveTo(w * 0.62f, h * 0.22f)
                    lineTo(w * 0.32f, h * 0.5f)
                    lineTo(w * 0.62f, h * 0.78f)
                }
                drawPath(path, tint, style = stroke)
            }
        }
    }
}