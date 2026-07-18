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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.phoneshim.android.R
import com.phoneshim.android.ui.common.PhoneShimButtonSize
import com.phoneshim.android.ui.common.PrimaryButton
import com.phoneshim.android.ui.common.TextInputDialog
import com.phoneshim.android.ui.common.PhoneShimDialog
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
}

@Composable
fun GoalTimeDialog(
    state: TimeEditorState,
    onHoursChanged: (String) -> Unit,
    onMinutesChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    PhoneShimDialog(
        onDismissRequest = onDismiss,
        width = PrefDialogDefaults.dialogWidth,
        shape = MaterialTheme.shapes.large,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(PrefDialogDefaults.contentPadding),
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
            Text(
                text = when (state.target) {
                    TimeEditTarget.TotalGoal -> "전체 폰 목표 시간 설정"
                    is TimeEditTarget.AppGoal -> "앱별 목표 시간 설정"
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
                    label = "시간",
                    onValueChange = onHoursChanged,
                    modifier = Modifier.weight(1f),
                )
                TimeInput(
                    value = state.minutesInput,
                    label = "분",
                    onValueChange = onMinutesChanged,
                    modifier = Modifier.weight(1f),
                )
            }
            state.error?.let { error ->
                Spacer(Modifier.height(PhoneShimDimens.spacing8))
                Text(
                    text = when (error) {
                        TimeInputError.INVALID_MINUTE_RANGE ->
                            "분은 0부터 59까지 입력해 주세요."
                        TimeInputError.BELOW_MINIMUM ->
                            "목표 사용 시간은 10분 이상 입력해 주세요."
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
                        text = "취소",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PhoneShimTheme.colors.textSecondary,
                    )
                }
                PrimaryButton(
                    text = "확인",
                    onClick = onConfirm,
                    size = PhoneShimButtonSize.Small,
                    fullWidth = false,
                    shape = MaterialTheme.shapes.small,
                    labelStyle = MaterialTheme.typography.bodyMedium,
                )
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
) = TextInputDialog(
    title = "어플 목표 설정",
    value = description,
    onValueChange = onDescriptionChanged,
    onConfirm = onSave,
    onDismiss = onDismiss,
)
