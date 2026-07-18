package com.phoneshim.android.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimPalette

/** Secondary action button used throughout the app. */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: PhoneShimButtonSize = PhoneShimButtonSize.Large,
    fullWidth: Boolean = true,
    containerColor: Color = PhoneShimPalette.SoftCream,
    accentColor: Color = PhoneShimPalette.Primary500,
    pressedAccentColor: Color = PhoneShimPalette.Primary600,
    disabledContainerColor: Color = containerColor,
    disabledAccentColor: Color = PhoneShimPalette.Gray300,
    shape: Shape = MaterialTheme.shapes.medium,
    labelStyle: TextStyle? = null,
    contentPadding: PaddingValues? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val currentAccentColor = if (isPressed) {
        pressedAccentColor
    } else {
        accentColor
    }

    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
            .height(size.height),
        enabled = enabled,
        shape = shape,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = containerColor,
            contentColor = currentAccentColor,
            disabledContainerColor = disabledContainerColor,
            disabledContentColor = disabledAccentColor,
        ),
        border = BorderStroke(1.dp, if (enabled) currentAccentColor else disabledAccentColor),
        contentPadding = contentPadding ?: ButtonDefaults.ContentPadding,
        interactionSource = interactionSource,
    ) {
        Text(
            text = text,
            style = labelStyle ?: defaultSecondaryButtonLabelStyle(size),
        )
    }
}

@Composable
private fun defaultSecondaryButtonLabelStyle(size: PhoneShimButtonSize): TextStyle = when (size) {
    PhoneShimButtonSize.Large -> MaterialTheme.typography.titleLarge
    PhoneShimButtonSize.Medium -> MaterialTheme.typography.bodyMedium
    PhoneShimButtonSize.Small -> MaterialTheme.typography.bodySmall
}

@Preview(showBackground = true)
@Composable
private fun SecondaryButtonPreview() {
    PhoneShimTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PhoneShimButtonSize.entries.forEach { size ->
                SecondaryButton(text = size.name, onClick = {}, size = size)
            }
            SecondaryButton(text = "Disabled", onClick = {}, enabled = false)
        }
    }
}
