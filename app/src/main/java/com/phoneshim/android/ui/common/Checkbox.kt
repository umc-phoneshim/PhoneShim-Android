package com.phoneshim.android.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.phoneshim.android.ui.theme.PhoneShimPalette

/** Circular checkbox matching the PhoneShim design system. */
@Composable
fun Checkbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val backgroundColor = if (checked) PhoneShimPalette.Primary600 else PhoneShimPalette.White
    val borderColor = if (checked) Color.Transparent else PhoneShimPalette.Gray500

    Box(
        modifier = modifier
            .size(24.dp)
            .semantics { selected = checked }
            .background(backgroundColor, CircleShape)
            .border(1.dp, borderColor, CircleShape)
            .clickable(
                enabled = enabled,
                role = Role.Checkbox,
                interactionSource = interactionSource,
                indication = ripple(bounded = false, radius = 20.dp),
            ) { onCheckedChange(!checked) },
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Canvas(modifier = Modifier.size(width = 14.dp, height = 10.dp)) {
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(size.width * 0.08f, size.height * 0.48f)
                    lineTo(size.width * 0.38f, size.height * 0.82f)
                    lineTo(size.width * 0.92f, size.height * 0.16f)
                }
                drawPath(
                    path = path,
                    color = PhoneShimPalette.White,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
                )
            }
        }
    }
}
