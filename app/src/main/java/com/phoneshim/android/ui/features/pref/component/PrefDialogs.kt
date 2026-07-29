package com.phoneshim.android.ui.features.pref.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.phoneshim.android.ui.common.TextInputDialog
import com.phoneshim.android.ui.common.PhoneShimDialog
import com.phoneshim.android.ui.common.Toggle
import com.phoneshim.android.ui.features.pref.viewmodel.TimeEditorState
import com.phoneshim.android.ui.features.pref.viewmodel.TimeInputError
import com.phoneshim.android.ui.theme.PhoneShimDimens
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimType

private object PrefDialogDefaults {
    val dialogWidth = 328.dp
    val contentPadding = 24.dp
    val totalTimeRowHeight = 56.dp
    val timeNumberWidth = 56.dp
    val confirmButtonWidth = 72.dp
    val confirmButtonHeight = 30.dp
}

@Composable
fun GoalTimeDialog(
    title: String,
    state: TimeEditorState,
    onHoursChanged: (String) -> Unit,
    onMinutesChanged: (String) -> Unit,
    onLimitToggled: () -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    PhoneShimDialog(
        onDismissRequest = onDismiss,
        width = PrefDialogDefaults.dialogWidth,
        shape = MaterialTheme.shapes.medium,
        contentPadding = PaddingValues(PrefDialogDefaults.contentPadding),
        properties = DialogProperties(usePlatformDefaultWidth = false),
        verticalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing12),
    ) {
        Text(
            text = title,
            style = PhoneShimType.KorBodyM,
            color = PhoneShimTheme.colors.textPrimary,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(PrefDialogDefaults.totalTimeRowHeight),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TimeValueInput(
                value = state.hoursInput,
                unit = "시간",
                onValueChange = onHoursChanged,
            )
            Spacer(Modifier.width(PhoneShimDimens.spacing4))
            TimeValueInput(
                value = state.minutesInput,
                unit = "분",
                onValueChange = onMinutesChanged,
            )
        }

        state.error?.let { error ->
            Text(
                text = when (error) {
                    TimeInputError.INVALID_MINUTE_RANGE -> "분은 0부터 59까지 입력해 주세요."
                    TimeInputError.BELOW_MINIMUM -> "목표 사용 시간은 10분 이상 입력해 주세요."
                },
                modifier = Modifier.fillMaxWidth(),
                style = PhoneShimType.KorCaption,
                color = PhoneShimTheme.colors.error,
                textAlign = TextAlign.Center,
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "목표 시간 이후 폰 금지",
                style = PhoneShimType.KorCaption,
                color = PhoneShimTheme.colors.error,
            )
            Spacer(Modifier.width(PhoneShimDimens.spacing12))
            Toggle(
                checked = state.isLimitEnabled,
                onCheckedChange = { onLimitToggled() },
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = PhoneShimDimens.spacing12),
            contentAlignment = Alignment.CenterEnd,
        ) {
            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .width(PrefDialogDefaults.confirmButtonWidth)
                    .height(PrefDialogDefaults.confirmButtonHeight),
                shape = MaterialTheme.shapes.extraSmall,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PhoneShimTheme.colors.brand,
                    contentColor = PhoneShimTheme.colors.onBrand,
                ),
                contentPadding = PaddingValues(0.dp),
            ) {
                Text(text = "확인", style = PhoneShimType.KorCaption)
            }
        }
    }
}

@Composable
private fun TimeValueInput(
    value: String,
    unit: String,
    onValueChange: (String) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.width(PrefDialogDefaults.timeNumberWidth),
            textStyle = PhoneShimType.EngDisplay.copy(
                color = PhoneShimTheme.colors.brand,
                textAlign = TextAlign.Center,
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
        Text(
            text = unit,
            style = PhoneShimType.KorH2,
            color = PhoneShimTheme.colors.brand,
        )
    }
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
