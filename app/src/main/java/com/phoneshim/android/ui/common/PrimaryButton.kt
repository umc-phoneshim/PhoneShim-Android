package com.phoneshim.android.ui.common

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
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

/** Primary action button used throughout the app. */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: PhoneShimButtonSize = PhoneShimButtonSize.Large,
    fullWidth: Boolean = true,
    containerColor: Color = PhoneShimPalette.Primary500,
    pressedContainerColor: Color = PhoneShimPalette.Primary600,
    contentColor: Color = PhoneShimPalette.White,
    disabledContainerColor: Color = PhoneShimPalette.Primary300,
    disabledContentColor: Color = PhoneShimPalette.White,
    shape: Shape = MaterialTheme.shapes.medium,
    labelStyle: TextStyle? = null,
    contentPadding: PaddingValues? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val currentContainerColor = if (isPressed) {
        pressedContainerColor
    } else {
        containerColor
    }

    Button(
        onClick = onClick,
        modifier = modifier
            .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
            .height(size.height),
        enabled = enabled,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = currentContainerColor,
            contentColor = contentColor,
            disabledContainerColor = disabledContainerColor,
            disabledContentColor = disabledContentColor,
        ),
        contentPadding = contentPadding ?: ButtonDefaults.ContentPadding,
        interactionSource = interactionSource,
    ) {
        Text(
            text = text,
            style = labelStyle ?: defaultButtonLabelStyle(size),
        )
    }
}

@Composable
private fun defaultButtonLabelStyle(size: PhoneShimButtonSize): TextStyle = when (size) {
    PhoneShimButtonSize.Large -> MaterialTheme.typography.titleLarge
    PhoneShimButtonSize.Medium -> MaterialTheme.typography.bodyMedium
    PhoneShimButtonSize.Small -> MaterialTheme.typography.bodySmall
}

@Preview(showBackground = true)
@Composable
private fun PrimaryButtonPreview() {
    PhoneShimTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PhoneShimButtonSize.entries.forEach { size ->
                PrimaryButton(text = size.name, onClick = {}, size = size)
            }
            PrimaryButton(text = "Disabled", onClick = {}, enabled = false)
        }
    }
}
