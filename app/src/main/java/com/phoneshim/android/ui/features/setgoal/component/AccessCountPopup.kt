package com.phoneshim.android.ui.features.setgoal.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.phoneshim.android.ui.common.PhoneShimButtonSize
import com.phoneshim.android.ui.common.PrimaryButton
import com.phoneshim.android.ui.theme.PhoneShimDimens
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimType

// 목표 앱의 하루 접근 허용 횟수를 입력받는 팝업 (Figma 04-4. 어플 접근 횟수 설정 팝업)
@Composable
fun AccessCountPopup(
    onDismiss: () -> Unit,
    onConfirm: (accessCount: Int) -> Unit,
    initialCount: Int = 0,
) {
    var text by remember {
        mutableStateOf(if (initialCount > 0) initialCount.toString() else "")
    }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(PhoneShimTheme.colors.surface)
                .padding(PhoneShimDimens.spacing16),
            verticalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing16),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "어플 접근 횟수 설정",
                    style = PhoneShimType.KorBodyM,
                    color = PhoneShimTheme.colors.textPrimary,
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "닫기",
                    tint = PhoneShimTheme.colors.textPrimary,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable(onClick = onDismiss),
                )
            }

            Text(
                text = "하루 동안 이 어플에 접근할 수 있는 횟수를 정해주세요.",
                style = PhoneShimType.KorCaption,
                color = PhoneShimTheme.colors.textTertiary,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing8),
            ) {
                BasicTextField(
                    value = text,
                    onValueChange = { new -> text = new.filter(Char::isDigit).take(3) },
                    textStyle = PhoneShimType.KorBodyM.copy(
                        color = PhoneShimTheme.colors.textPrimary,
                        textAlign = TextAlign.Center,
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.small)
                                .background(PhoneShimTheme.colors.brandSubtle)
                                .padding(PhoneShimDimens.spacing12),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (text.isEmpty()) {
                                Text(
                                    text = "0",
                                    style = PhoneShimType.KorBodyM,
                                    color = PhoneShimTheme.colors.textTertiary,
                                )
                            }
                            innerTextField()
                        }
                    },
                )
                Text(
                    text = "회",
                    style = PhoneShimType.KorBodyM,
                    color = PhoneShimTheme.colors.textPrimary,
                )
            }

            PrimaryButton(
                text = "저장",
                onClick = { onConfirm(text.toIntOrNull() ?: 0) },
                modifier = Modifier
                    .align(Alignment.End)
                    .width(100.dp),
                size = PhoneShimButtonSize.Small,
                fullWidth = false,
                shape = MaterialTheme.shapes.small,
                labelStyle = PhoneShimType.KorBodyM,
            )
        }
    }
}

@Preview
@Composable
private fun AccessCountPopupPreview() {
    PhoneShimTheme {
        AccessCountPopup(onDismiss = {}, onConfirm = {}, initialCount = 5)
    }
}
