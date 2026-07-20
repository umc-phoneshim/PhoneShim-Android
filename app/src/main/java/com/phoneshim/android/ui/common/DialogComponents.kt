package com.phoneshim.android.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.phoneshim.android.R
import com.phoneshim.android.ui.theme.PhoneShimDimens
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimType

@Composable
fun PhoneShimDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = 328.dp,
    shape: Shape = MaterialTheme.shapes.medium,
    contentPadding: PaddingValues = PaddingValues(PhoneShimDimens.spacing24),
    properties: DialogProperties = DialogProperties(usePlatformDefaultWidth = false),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(onDismissRequest = onDismissRequest, properties = properties) {
        Column(
            modifier = modifier
                .width(width)
                .background(PhoneShimTheme.colors.surface, shape)
                .padding(contentPadding),
            verticalArrangement = verticalArrangement,
            horizontalAlignment = horizontalAlignment,
            content = content,
        )
    }
}

@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    dismissText: String = "취소",
    destructive: Boolean = false,
) {
    PhoneShimDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        width = 280.dp,
        shape = MaterialTheme.shapes.large,
        contentPadding = PaddingValues(top = 24.dp, start = 20.dp, end = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, style = PhoneShimType.KorH3, color = PhoneShimTheme.colors.textPrimary)
        Spacer(Modifier.height(PhoneShimDimens.spacing8))
        Text(
            text = message,
            style = PhoneShimType.KorCaption,
            color = PhoneShimTheme.colors.textTertiary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(PhoneShimDimens.spacing20))
        Box(Modifier.fillMaxWidth().height(1.dp).background(PhoneShimTheme.colors.divider))
        Row(modifier = Modifier.fillMaxWidth()) {
            DialogTextAction(
                text = dismissText,
                color = PhoneShimTheme.colors.textSecondary,
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
            )
            Box(Modifier.width(1.dp).height(20.dp).background(PhoneShimTheme.colors.divider))
            DialogTextAction(
                text = confirmText,
                color = if (destructive) PhoneShimTheme.colors.error else PhoneShimTheme.colors.brand,
                onClick = onConfirm,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun DialogTextAction(
    text: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fontWeight: FontWeight? = null,
) {
    Text(
        text = text,
        style = PhoneShimType.KorBodyM,
        fontWeight = fontWeight,
        color = color,
        textAlign = TextAlign.Center,
        modifier = modifier.clickable(onClick = onClick).padding(vertical = PhoneShimDimens.spacing12),
    )
}

@Composable
fun TextInputDialog(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    confirmText: String = "저장",
    dismissible: Boolean = false,
) {
    val properties = DialogProperties(
        dismissOnBackPress = dismissible,
        dismissOnClickOutside = dismissible,
        usePlatformDefaultWidth = false,
    )
    PhoneShimDialog(
        onDismissRequest = if (dismissible) onDismiss else ({ }),
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        properties = properties,
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = PhoneShimTheme.colors.textPrimary,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                Icon(
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = "닫기",
                    tint = PhoneShimTheme.colors.textPrimary,
                )
            }
        }
        Spacer(Modifier.height(PhoneShimDimens.spacing12))
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            placeholder = { Text(placeholder, style = MaterialTheme.typography.bodySmall) },
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
            PrimaryButton(
                text = confirmText,
                onClick = onConfirm,
                modifier = Modifier.width(120.dp),
                size = PhoneShimButtonSize.Small,
                fullWidth = false,
                shape = MaterialTheme.shapes.small,
                contentPadding = PaddingValues(0.dp),
            )
        }
    }
}

@Composable
fun PermissionNoticeItem(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    contentSpacing: Dp = PhoneShimDimens.spacing12,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(contentSpacing)) {
        Text(title, style = PhoneShimType.KorBodyM, color = PhoneShimTheme.colors.textPrimary)
        Text(
            text = description,
            modifier = Modifier
                .fillMaxWidth()
                .background(PhoneShimTheme.colors.brandSubtle, MaterialTheme.shapes.small)
                .padding(horizontal = PhoneShimDimens.spacing12, vertical = 10.dp),
            color = PhoneShimTheme.colors.textPrimary,
            style = PhoneShimType.KorLabel,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ConfirmationDialogPreview() {
    PhoneShimTheme {
        ConfirmationDialog(
            title = "탈퇴하시겠습니까?",
            message = "탈퇴 후 2주까지만 복구 가능합니다.",
            confirmText = "탈퇴",
            destructive = true,
            onConfirm = {},
            onDismiss = {},
        )
    }
}
