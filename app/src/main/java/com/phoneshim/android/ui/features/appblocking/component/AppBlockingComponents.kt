package com.phoneshim.android.ui.features.appblocking.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.annotation.DrawableRes
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.phoneshim.android.R
import com.phoneshim.android.ui.common.PrimaryButton
import com.phoneshim.android.ui.common.PhoneShimButtonSize
import com.phoneshim.android.ui.theme.PhoneShimPalette
import com.phoneshim.android.ui.theme.PhoneShimType
import com.phoneshim.android.ui.theme.PhoneShimDimens

@Composable
fun BlockingOverlay(
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.Center,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PhoneShimPalette.Gray100)
            .systemBarsPadding()
            .padding(PhoneShimDimens.screenHorizontalPadding),
        contentAlignment = contentAlignment,
    ) { content() }
}

@Composable
fun BlockingDialog(
    title: String,
    descriptions: List<String>,
    buttonText: String,
    onConfirm: () -> Unit,
    @DrawableRes illustrationRes: Int? = null,
    illustrationContentDescription: String? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(PhoneShimPalette.SoftCream, MaterialTheme.shapes.medium)
            .padding(PhoneShimDimens.spacing24),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing24),
    ) {
        illustrationRes?.let { drawableRes ->
            BlockingIllustration(
                illustrationRes = drawableRes,
                contentDescription = illustrationContentDescription,
                modifier = Modifier.size(PhoneShimDimens.blockingDialogIllustrationSize),
            )
        }
        BlockingTitle(title)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing4),
        ) {
            descriptions.forEach { description ->
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = PhoneShimPalette.Gray700,
                    textAlign = TextAlign.Center,
                )
            }
        }
        PrimaryButton(
            modifier = Modifier.padding(top = PhoneShimDimens.spacing12),
            text = buttonText,
            onClick = onConfirm,
            size = PhoneShimButtonSize.Small,
        )
    }
}

@Composable
fun BlockingTitle(title: String) {
    Text(
        text = title,
        style = PhoneShimType.KorH1,
        color = PhoneShimPalette.Gray700,
        textAlign = TextAlign.Center,
    )
}

@Composable
fun BlockingIllustration(
    @DrawableRes illustrationRes: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    Image(
        painter = painterResource(illustrationRes),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Fit,
    )
}

@Composable
fun EmergencyAction(
    label: String,
    @DrawableRes icon: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(PhoneShimDimens.blockingActionSize)
            .background(PhoneShimPalette.Primary400, MaterialTheme.shapes.medium)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing12),
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(PhoneShimDimens.blockingActionIconSize),
                tint = Color.Unspecified,
            )
            Text(label, style = MaterialTheme.typography.bodyMedium, color = PhoneShimPalette.SoftCream)
        }
    }
}

@Preview(name = "Blocking dialog", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun BlockingDialogPreview() {
    com.phoneshim.android.ui.theme.PhoneShimTheme {
        BlockingOverlay {
            BlockingDialog(
                title = "목표 달성!",
                descriptions = listOf(
                    "오늘 목표 시간을 모두 채웠어요.",
                    "조금만 휴대폰을 내려두고",
                    "잠시 쉬어볼까요?",
                ),
                buttonText = "좋아요",
                onConfirm = {},
                illustrationRes = R.drawable.appblocking_mascot_daily_goal,
            )
        }
    }
}

@Preview(name = "Blocking illustration", showBackground = true, backgroundColor = 0xFFFFFDF7)
@Composable
private fun BlockingIllustrationPreview() {
    com.phoneshim.android.ui.theme.PhoneShimTheme {
        BlockingIllustration(
            illustrationRes = R.drawable.appblocking_mascot_daily_goal,
            contentDescription = null,
            modifier = Modifier.size(PhoneShimDimens.blockingDialogIllustrationSize),
        )
    }
}

@Preview(name = "Emergency actions", showBackground = true, backgroundColor = 0xFFF4F8F1)
@Composable
private fun EmergencyActionPreview() {
    com.phoneshim.android.ui.theme.PhoneShimTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing16)) {
            EmergencyAction("전화", R.drawable.ic_phone, onClick = {})
            EmergencyAction("메시지", R.drawable.ic_message, onClick = {})
        }
    }
}
