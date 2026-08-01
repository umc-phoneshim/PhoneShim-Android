package com.phoneshim.android.ui.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.phoneshim.android.ui.theme.PhoneShimPalette
import com.phoneshim.android.ui.theme.PhoneShimTheme

private const val InteractionDurationMillis = 180

@Composable
fun InteractiveTimeSegmentInput(
    value: String,
    maxValue: Int,
    onValueChange: (String) -> Unit,
    textStyle: TextStyle,
    restingWidth: Dp,
    activeWidth: Dp,
    restingHeight: Dp,
    activeHeight: Dp,
    modifier: Modifier = Modifier,
    restingTextColor: Color = PhoneShimPalette.Primary400,
    activeTextColor: Color = PhoneShimTheme.colors.brandStrong,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()
    var isFocused by remember { mutableStateOf(false) }
    var replaceOnNextInput by remember { mutableStateOf(false) }
    var fieldValue by remember {
        mutableStateOf(TextFieldValue(value.ifBlank { "00" }))
    }
    val isInteractive = isFocused || isHovered || isPressed
    val animationSpec = tween<Dp>(InteractionDurationMillis, easing = FastOutSlowInEasing)
    val width by animateDpAsState(
        targetValue = if (isInteractive) activeWidth else restingWidth,
        animationSpec = animationSpec,
        label = "timeSegmentWidth",
    )
    val height by animateDpAsState(
        targetValue = if (isInteractive) activeHeight else restingHeight,
        animationSpec = animationSpec,
        label = "timeSegmentHeight",
    )
    val backgroundColor by animateColorAsState(
        targetValue = if (isInteractive) PhoneShimPalette.Primary100 else Color.Transparent,
        animationSpec = tween(InteractionDurationMillis, easing = FastOutSlowInEasing),
        label = "timeSegmentBackground",
    )
    val borderColor by animateColorAsState(
        targetValue = if (isInteractive) PhoneShimTheme.colors.brand else Color.Transparent,
        animationSpec = tween(InteractionDurationMillis, easing = FastOutSlowInEasing),
        label = "timeSegmentBorder",
    )
    val textColor by animateColorAsState(
        targetValue = if (isInteractive) activeTextColor else restingTextColor,
        animationSpec = tween(InteractionDurationMillis, easing = FastOutSlowInEasing),
        label = "timeSegmentText",
    )

    LaunchedEffect(value, isFocused) {
        if (!isFocused) {
            fieldValue = TextFieldValue(value.ifBlank { "00" })
        }
    }

    BasicTextField(
        value = fieldValue,
        onValueChange = { changed ->
            if (changed.text == fieldValue.text) {
                fieldValue = changed
                return@BasicTextField
            }
            val input = if (replaceOnNextInput) {
                replaceOnFirstInput(fieldValue.text, changed.text)
            } else {
                changed.text
            }
            replaceOnNextInput = false
            val sanitized = sanitizeTimeSegment(input, fieldValue.text, maxValue)
            if (sanitized != fieldValue.text || changed.text.filter(Char::isDigit).isEmpty()) {
                fieldValue = TextFieldValue(
                    text = sanitized,
                    selection = TextRange(sanitized.length),
                )
                onValueChange(sanitized)
            }
        },
        modifier = modifier
            .width(width)
            .height(height)
            .hoverable(interactionSource)
            .background(backgroundColor, RoundedCornerShape(6.dp))
            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
            .onFocusChanged { focusState ->
                val wasFocused = isFocused
                isFocused = focusState.isFocused
                if (!wasFocused && isFocused) {
                    replaceOnNextInput = true
                    fieldValue = fieldValue.copy(selection = TextRange(0, fieldValue.text.length))
                } else if (wasFocused && !isFocused) {
                    replaceOnNextInput = false
                    val normalized = fieldValue.text.ifBlank { "0" }.padStart(2, '0')
                    fieldValue = TextFieldValue(normalized, TextRange(normalized.length))
                    onValueChange(normalized)
                }
            },
        textStyle = textStyle.copy(color = textColor, textAlign = TextAlign.Center),
        singleLine = true,
        cursorBrush = SolidColor(Color.Transparent),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        interactionSource = interactionSource,
        decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.Center) { innerTextField() }
        },
    )
}

fun sanitizeTimeSegment(raw: String, current: String, maxValue: Int): String {
    val digits = raw.filter(Char::isDigit).take(2)
    if (digits.isEmpty()) return ""
    return if ((digits.toIntOrNull() ?: return current) <= maxValue) digits else current
}

internal fun replaceOnFirstInput(previous: String, changed: String): String {
    val previousDigits = previous.filter(Char::isDigit)
    val changedDigits = changed.filter(Char::isDigit)
    if (changedDigits.length <= previousDigits.length) return changedDigits.take(2)

    val insertedIndex = changedDigits.indices.firstOrNull { index ->
        changedDigits.removeRange(index, index + 1) == previousDigits
    }
    return insertedIndex?.let { changedDigits[it].toString() } ?: changedDigits.takeLast(1)
}
