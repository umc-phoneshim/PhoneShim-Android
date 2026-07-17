package com.phoneshim.android.ui.features.pref.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.phoneshim.android.R
import com.phoneshim.android.ui.features.pref.viewmodel.TimeEditorState
import com.phoneshim.android.ui.features.pref.viewmodel.TimeEditTarget
import com.phoneshim.android.ui.features.pref.viewmodel.TimeInputError
import com.phoneshim.android.ui.theme.PhoneShimDimens
import com.phoneshim.android.ui.theme.PhoneShimTheme

private object PrefDialogDefaults {
    val dialogWidth = 328.dp
    val contentPadding = 24.dp
    val descriptionInputHeight = 56.dp
    val saveButtonWidth = 120.dp
    val saveButtonHeight = 36.dp
}

@Composable
fun GoalTimeDialog(
    state: TimeEditorState,
    onHoursChanged: (String) -> Unit,
    onMinutesChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Column(
            modifier = Modifier
                .width(PrefDialogDefaults.dialogWidth)
                .background(PhoneShimTheme.colors.surface, MaterialTheme.shapes.large)
                .padding(PrefDialogDefaults.contentPadding),
        ) {
            Text(
                text = when (state.target) {
                    TimeEditTarget.TotalGoal -> stringResource(R.string.pref_total_time_dialog_title)
                    is TimeEditTarget.AppGoal -> stringResource(R.string.pref_app_time_dialog_title)
                },
                style = MaterialTheme.typography.titleLarge,
                color = PhoneShimTheme.colors.textPrimary,
            )
            Spacer(Modifier.height(PhoneShimDimens.spacing20))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing12),
            ) {
                TimeInput(
                    value = state.hoursInput,
                    label = stringResource(R.string.pref_hours),
                    onValueChange = onHoursChanged,
                    modifier = Modifier.weight(1f),
                )
                TimeInput(
                    value = state.minutesInput,
                    label = stringResource(R.string.pref_minutes),
                    onValueChange = onMinutesChanged,
                    modifier = Modifier.weight(1f),
                )
            }
            state.error?.let { error ->
                Spacer(Modifier.height(PhoneShimDimens.spacing8))
                Text(
                    text = when (error) {
                        TimeInputError.INVALID_MINUTE_RANGE ->
                            stringResource(R.string.pref_invalid_minute_range)
                        TimeInputError.BELOW_MINIMUM ->
                            stringResource(R.string.pref_minimum_time_error)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = PhoneShimTheme.colors.error,
                )
            }
            Spacer(Modifier.height(PhoneShimDimens.spacing16))
            Row(
                modifier = Modifier.align(Alignment.End),
                horizontalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing8),
            ) {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = stringResource(R.string.pref_cancel),
                        style = MaterialTheme.typography.bodyMedium,
                        color = PhoneShimTheme.colors.textSecondary,
                    )
                }
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PhoneShimTheme.colors.brand,
                        contentColor = PhoneShimTheme.colors.onBrand,
                    ),
                    shape = MaterialTheme.shapes.small,
                ) {
                    Text(
                        text = stringResource(R.string.pref_confirm),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun TimeInput(
    value: String,
    label: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = { Text(label, style = MaterialTheme.typography.bodySmall) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        textStyle = MaterialTheme.typography.bodyLarge,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PhoneShimTheme.colors.brand,
            unfocusedBorderColor = PhoneShimTheme.colors.border,
            focusedTextColor = PhoneShimTheme.colors.textPrimary,
            unfocusedTextColor = PhoneShimTheme.colors.textPrimary,
            focusedLabelColor = PhoneShimTheme.colors.brand,
            unfocusedLabelColor = PhoneShimTheme.colors.textTertiary,
        ),
        shape = MaterialTheme.shapes.small,
    )
}

@Composable
fun AppGoalDescriptionDialog(
    description: String,
    onDescriptionChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
        ),
    ) {
        Column(
            modifier = Modifier
                .width(PrefDialogDefaults.dialogWidth)
                .background(PhoneShimTheme.colors.surface, MaterialTheme.shapes.small)
                .padding(PrefDialogDefaults.contentPadding),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.pref_app_goal_dialog_title),
                    style = MaterialTheme.typography.bodySmall,
                    color = PhoneShimTheme.colors.textPrimary,
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = stringResource(R.string.pref_close),
                        tint = PhoneShimTheme.colors.textPrimary,
                    )
                }
            }
            Spacer(Modifier.height(PhoneShimDimens.spacing12))
            TextField(
                value = description,
                onValueChange = onDescriptionChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(PrefDialogDefaults.descriptionInputHeight),
                textStyle = MaterialTheme.typography.bodySmall,
                minLines = 2,
                maxLines = 3,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = PhoneShimTheme.colors.brandSubtle,
                    unfocusedContainerColor = PhoneShimTheme.colors.brandSubtle,
                    focusedTextColor = PhoneShimTheme.colors.textSecondary,
                    unfocusedTextColor = PhoneShimTheme.colors.textSecondary,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                shape = MaterialTheme.shapes.small,
            )
            Spacer(Modifier.height(PhoneShimDimens.spacing24))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                Button(
                    onClick = onSave,
                    modifier = Modifier
                        .width(PrefDialogDefaults.saveButtonWidth)
                        .height(PrefDialogDefaults.saveButtonHeight),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PhoneShimTheme.colors.brand,
                        contentColor = PhoneShimTheme.colors.onBrand,
                    ),
                    shape = MaterialTheme.shapes.small,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                ) {
                    Text(
                        text = stringResource(R.string.pref_save),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}
