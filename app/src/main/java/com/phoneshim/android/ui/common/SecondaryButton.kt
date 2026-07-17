package com.phoneshim.android.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.phoneshim.android.ui.theme.PhoneShimDimens
import com.phoneshim.android.ui.theme.PhoneShimPalette
import com.phoneshim.android.ui.theme.PhoneShimTheme

/** Secondary action button used throughout the app. */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val accentColor = if (isPressed) {
        PhoneShimPalette.Primary600
    } else {
        PhoneShimPalette.Primary500
    }

    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(PhoneShimDimens.primaryButtonHeight),
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = PhoneShimPalette.SoftCream,
            contentColor = accentColor,
        ),
        border = BorderStroke(1.dp, accentColor),
        contentPadding = ButtonDefaults.ContentPadding,
        interactionSource = interactionSource,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SecondaryButtonPreview() {
    PhoneShimTheme { SecondaryButton(text = "나중에", onClick = {}) }
}
