package com.phoneshim.android.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.phoneshim.android.ui.theme.PhoneShimDimens
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimType

enum class SelectableChipVariant {
    Outlined,
    Filled,
}

@Composable
fun SelectableChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: SelectableChipVariant = SelectableChipVariant.Outlined,
) {
    val selectedContainer = PhoneShimTheme.colors.brand
    val container = when {
        !enabled -> PhoneShimTheme.colors.divider
        selected || variant == SelectableChipVariant.Filled -> selectedContainer
        else -> PhoneShimTheme.colors.surface
    }
    val content = when {
        !enabled -> PhoneShimTheme.colors.textTertiary
        selected || variant == SelectableChipVariant.Filled -> PhoneShimTheme.colors.onBrand
        else -> PhoneShimTheme.colors.textPrimary
    }
    val border = when {
        !enabled -> PhoneShimTheme.colors.divider
        selected || variant == SelectableChipVariant.Filled -> selectedContainer
        else -> PhoneShimTheme.colors.border
    }

    Box(
        modifier = modifier
            .height(28.dp)
            .clip(MaterialTheme.shapes.small)
            .background(container)
            .border(1.dp, border, MaterialTheme.shapes.small)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = PhoneShimDimens.spacing8),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, style = PhoneShimType.KorLabel, color = content)
    }
}

@Composable
fun SelectionField(
    value: String?,
    placeholder: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val container = if (enabled) PhoneShimTheme.colors.brand else PhoneShimTheme.colors.divider
    val content = if (enabled) PhoneShimTheme.colors.onBrand else PhoneShimTheme.colors.textTertiary
    Row(
        modifier = modifier
            .height(28.dp)
            .clip(CircleShape)
            .background(container)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = PhoneShimDimens.spacing16),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = value ?: placeholder,
            style = PhoneShimType.KorLabel,
            color = content,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = Icons.Filled.KeyboardArrowDown,
            contentDescription = null,
            tint = content,
            modifier = Modifier.size(12.dp),
        )
    }
}

@Composable
fun <T> SelectionDropdown(
    expanded: Boolean,
    options: List<T>,
    optionLabel: (T) -> String,
    onSelected: (T) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    selected: T? = null,
    optionTrailingContent: (@Composable (T, Boolean) -> Unit)? = null,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = modifier.background(PhoneShimTheme.colors.surface),
    ) {
        options.forEach { option ->
            DropdownMenuItem(
                text = {
                    Text(
                        text = optionLabel(option),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (option == selected) {
                            PhoneShimTheme.colors.brand
                        } else {
                            PhoneShimTheme.colors.textSecondary
                        },
                    )
                },
                trailingIcon = optionTrailingContent?.let { content ->
                    { content(option, option == selected) }
                },
                onClick = { onSelected(option) },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SelectionComponentsPreview() {
    PhoneShimTheme {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SelectableChip(text = "여성", selected = true, onClick = {})
            SelectableChip(text = "남성", selected = false, onClick = {}, enabled = false)
        }
    }
}
