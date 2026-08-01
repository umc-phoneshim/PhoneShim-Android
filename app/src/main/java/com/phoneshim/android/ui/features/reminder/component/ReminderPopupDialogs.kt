package com.phoneshim.android.ui.features.reminder.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.phoneshim.android.ui.theme.PhoneShimPalette
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimType
import com.phoneshim.android.ui.features.reminder.viewmodel.DUPLICATE_SCHEDULE_MESSAGE

@Composable
internal fun ReminderNameInputDialog(
    value: String,
    onValueChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Column(
            modifier = Modifier
                .width(328.dp)
                .background(PhoneShimTheme.colors.brandSubtle, RoundedCornerShape(12.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "해야 할 일 이름 입력",
                style = PhoneShimType.KorCaption,
                color = PhoneShimTheme.colors.textPrimary,
            )
            BasicTextField(
                value = value,
                onValueChange = { onValueChange(it.take(20)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .background(PhoneShimPalette.White, RoundedCornerShape(8.dp))
                    .border(1.dp, PhoneShimPalette.Primary400, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp),
                textStyle = PhoneShimType.KorCaption.copy(color = PhoneShimTheme.colors.textPrimary),
                singleLine = true,
                cursorBrush = SolidColor(PhoneShimTheme.colors.brandStrong),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { if (value.isNotBlank()) onConfirm() }),
                decorationBox = { innerTextField ->
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                        innerTextField()
                    }
                },
            )
            CompactConfirmButton(enabled = value.isNotBlank(), onClick = onConfirm)
        }
    }
}

@Composable
private fun CompactConfirmButton(enabled: Boolean, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Box(
            modifier = Modifier
                .width(72.dp)
                .height(24.dp)
                .background(
                    if (enabled) PhoneShimPalette.Primary400 else PhoneShimPalette.Primary300,
                    RoundedCornerShape(4.dp),
                )
                .clickable(enabled = enabled, onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Text("확인", style = PhoneShimType.KorLabel, color = PhoneShimPalette.White)
        }
    }
}

@Composable
internal fun DuplicateScheduleErrorBanner(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(328.dp)
            .height(43.dp)
            .background(PhoneShimPalette.Gray100, RoundedCornerShape(8.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = DUPLICATE_SCHEDULE_MESSAGE,
            style = PhoneShimType.KorCaption,
            color = PhoneShimTheme.colors.error,
        )
    }
}
