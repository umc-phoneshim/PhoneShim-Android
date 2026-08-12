package com.phoneshim.android.ui.features.reminder.component

import android.graphics.Color
import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.phoneshim.android.R
import com.phoneshim.android.ui.features.reminder.viewmodel.ReminderDraft
import com.phoneshim.android.ui.features.reminder.viewmodel.RestrictionMode
import com.phoneshim.android.ui.theme.PhoneShimPalette
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimType

private data class ReminderAppOption(val id: String, val label: String)

private val reminderAppOptions = listOf(
    ReminderAppOption("com.kakao.talk", "카카오톡"),
    ReminderAppOption("com.facebook.katana", "페이스북"),
    ReminderAppOption("com.google.android.youtube", "유튜브"),
    ReminderAppOption("phoneshim-self", "폰쉴"),
    ReminderAppOption("com.musinsa.store", "무신사"),
)

@Composable
internal fun ReminderRestrictionPopup(
    draft: ReminderDraft,
    onModeChange: (RestrictionMode) -> Unit,
    onToggleApp: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    Popup(
        alignment = Alignment.CenterEnd,
        offset = IntOffset(x = -16, y = 72),
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true),
    ) {
        Column(
            modifier = Modifier
                .width(139.dp)
                .background(PhoneShimTheme.colors.brandSubtle, RoundedCornerShape(8.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            RestrictionOptionRow("폰 사용 전체 제한", draft.restrictionMode == RestrictionMode.FULL_PHONE) {
                onModeChange(RestrictionMode.FULL_PHONE)
            }
            RestrictionOptionRow("제한 없음", draft.restrictionMode == RestrictionMode.NONE) {
                onModeChange(RestrictionMode.NONE)
            }
            reminderAppOptions.forEach { app ->
                RestrictionOptionRow(app.label, app.id in draft.restrictedAppIds) {
                    if (draft.restrictionMode != RestrictionMode.SPECIFIC_APPS) {
                        onModeChange(RestrictionMode.SPECIFIC_APPS)
                    }
                    onToggleApp(app.id)
                }
            }
        }
    }
}

@Composable
private fun RestrictionOptionRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = PhoneShimType.KorCaption, color = PhoneShimTheme.colors.textSecondary)
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(if (selected) PhoneShimTheme.colors.brand else PhoneShimPalette.White, RoundedCornerShape(4.dp))
                .border(1.dp, if (selected) PhoneShimTheme.colors.brand else PhoneShimPalette.Gray100, RoundedCornerShape(4.dp)),
        )
    }
}

@Composable
internal fun ReminderAppIcon(appId: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val packageName = if (appId == "phoneshim-self") context.packageName else appId
    AndroidView(
        modifier = modifier,
        factory = { imageContext ->
            ImageView(imageContext).apply {
                scaleType = ImageView.ScaleType.FIT_CENTER
                setBackgroundColor(Color.TRANSPARENT)
            }
        },
        update = { imageView ->
            val icon = runCatching { context.packageManager.getApplicationIcon(packageName) }.getOrNull()
            if (icon != null) {
                imageView.setImageDrawable(icon)
            } else {
                imageView.setImageResource(
                    if (appId == "com.kakao.talk") R.drawable.ic_reminder_app_kakao else R.drawable.ic_phone,
                )
            }
        },
    )
}
