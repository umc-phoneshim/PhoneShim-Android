package com.phoneshim.android.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.phoneshim.android.ui.theme.PhoneShimTheme

@Composable
fun Toggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val colors = PhoneShimTheme.colors
    Box(
        modifier = modifier
            .size(width = 32.dp, height = 16.dp)
            .clip(CircleShape)
            .background(if (checked) colors.error else colors.border)
            .semantics { stateDescription = if (checked) "선택됨" else "선택 안 됨" }
            .clickable(
                enabled = enabled,
                role = Role.Switch,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { onCheckedChange(!checked) }
            .padding(2.dp),
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(colors.surface, CircleShape),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TogglePreview() {
    PhoneShimTheme {
        androidx.compose.foundation.layout.Row {
            Toggle(checked = false, onCheckedChange = {})
            Toggle(checked = true, onCheckedChange = {}, modifier = Modifier.offset(x = 8.dp))
        }
    }
}
