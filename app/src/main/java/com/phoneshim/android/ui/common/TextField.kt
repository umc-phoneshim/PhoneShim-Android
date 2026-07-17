package com.phoneshim.android.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimType

@Composable
fun TextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    errorMessage: String? = null,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true,
) {
    val colors = PhoneShimTheme.colors
    val shape = RoundedCornerShape(12.dp)
    val borderColor = if (errorMessage != null) colors.error else colors.border
    val containerColor = if (enabled) colors.surface else colors.divider

    Column(modifier = modifier.wrapContentHeight()) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .background(containerColor, shape)
                .then(
                    if (enabled) Modifier.border(1.dp, borderColor, shape) else Modifier,
                )
                .padding(16.dp),
            enabled = enabled,
            textStyle = PhoneShimType.KorBodyM.copy(color = colors.textSecondary),
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            singleLine = singleLine,
            cursorBrush = SolidColor(colors.brand),
            decorationBox = { innerTextField ->
                Box {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = PhoneShimType.KorBodyM,
                            color = if (errorMessage != null) colors.border else colors.textSecondary,
                        )
                    }
                    innerTextField()
                }
            },
        )
        errorMessage?.let {
            Text(
                text = it,
                style = PhoneShimType.KorCaption,
                color = colors.error,
                modifier = Modifier.padding(top = 9.dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TextFieldDefaultPreview() {
    PhoneShimTheme { TextField(value = "", onValueChange = {}, placeholder = "이메일") }
}

@Preview(showBackground = true)
@Composable
private fun TextFieldErrorPreview() {
    PhoneShimTheme {
        TextField(
            value = "",
            onValueChange = {},
            placeholder = "이메일",
            errorMessage = "이메일을 입력하세요.",
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TextFieldDisabledPreview() {
    PhoneShimTheme {
        TextField(value = "", onValueChange = {}, placeholder = "이메일", enabled = false)
    }
}
