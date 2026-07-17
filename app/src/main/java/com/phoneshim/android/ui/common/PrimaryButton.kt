package com.phoneshim.android.ui.common

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.phoneshim.android.ui.theme.PhoneShimPalette
import com.phoneshim.android.ui.theme.PhoneShimTheme

enum class PrimaryButtonSize(
    val height: Dp,
) {
    Large(56.dp),
    Small(36.dp),
}

@Preview(showBackground = true)
@Composable
private fun PrimaryButtonPreview() {
    PhoneShimTheme { PrimaryButton(text = "로그인", onClick = {}) }
}

/** Primary action button used throughout the app. */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: PrimaryButtonSize = PrimaryButtonSize.Large,
    fullWidth: Boolean = true,
    containerColor: Color = PhoneShimPalette.Primary500,
    pressedContainerColor: Color = PhoneShimPalette.Primary600,
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
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = currentContainerColor,
            contentColor = PhoneShimPalette.White,
        ),
        contentPadding = contentPadding ?: ButtonDefaults.ContentPadding,
        interactionSource = interactionSource,
    ) {
        Text(
            text = text,
            style = when (size) {
                PrimaryButtonSize.Large -> MaterialTheme.typography.titleLarge
                PrimaryButtonSize.Small -> MaterialTheme.typography.bodySmall
            },
        )
    }
}
