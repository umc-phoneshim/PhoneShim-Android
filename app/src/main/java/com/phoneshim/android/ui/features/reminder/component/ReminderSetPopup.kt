package com.phoneshim.android.ui.features.reminder.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.phoneshim.android.R
import com.phoneshim.android.ui.features.reminder.viewmodel.ReminderDraft
import com.phoneshim.android.ui.features.reminder.viewmodel.RestrictionMode
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimType
import java.time.LocalDate

private val PopupWidth = 328.dp
private val PopupBeforeHeight = 260.dp
private val PopupAfterHeight = 285.dp
private val PopupPadding = 16.dp
private val PopupCornerRadius = 12.dp
private val PopupItemSpacing = 16.dp
private val PopupButtonHeight = 36.dp
private val PopupHorizontalItemPadding = 16.dp
private val PopupIconSize = 24.dp

@Composable
fun ReminderSetPopup(
    selectedDate: LocalDate,
    draft: ReminderDraft,
    onDismiss: () -> Unit,
    onTitleChange: (String) -> Unit,
    onStartTimeChange: (String) -> Unit,
    onEndTimeChange: (String) -> Unit,
    onRestrictionModeChange: (RestrictionMode) -> Unit,
    onToggleApp: (String) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
) {
    val isConfigured = draft.startTimeText.isNotBlank() && draft.endTimeText.isNotBlank()
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            modifier = Modifier
                .width(PopupWidth)
                .height(if (isConfigured) PopupAfterHeight else PopupBeforeHeight)
                .background(PhoneShimTheme.colors.surface, RoundedCornerShape(PopupCornerRadius))
                .padding(PopupPadding),
            verticalArrangement = Arrangement.spacedBy(PopupItemSpacing),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PopupHeader(
                title = if (isConfigured) "해야 할 일" else "오늘 할 일",
                onDismiss = onDismiss,
            )
            PopupTimeSection(
                selectedDate = selectedDate,
                isConfigured = isConfigured,
                startTime = draft.startTimeText,
                endTime = draft.endTimeText,
                onClick = {
                    if (isConfigured) {
                        onStartTimeChange("")
                        onEndTimeChange("")
                    } else {
                        onStartTimeChange("10:00")
                        onEndTimeChange("11:00")
                    }
                },
            )
            PopupSelectionRow(
                label = "이름",
                labelColor = PhoneShimTheme.colors.textSecondary,
                onClick = { onTitleChange(if (draft.title == "과제") "과제하기" else "과제") },
            ) {
                Text(draft.title.ifBlank { "과제" }, style = PhoneShimType.KorCaption, color = PhoneShimTheme.colors.textPrimary)
                Spacer(Modifier.width(4.dp))
                Icon(painterResource(R.drawable.ic_chevron_right_small), null, tint = Color.Unspecified, modifier = Modifier.size(16.dp))
            }
            PopupSelectionRow(
                label = "제한 선택",
                labelColor = PhoneShimTheme.colors.error,
                onClick = {
                    val next = if (draft.restrictionMode == RestrictionMode.SPECIFIC_APPS) RestrictionMode.FULL_PHONE else RestrictionMode.SPECIFIC_APPS
                    onRestrictionModeChange(next)
                    if (next == RestrictionMode.SPECIFIC_APPS && "kakao" !in draft.restrictedAppIds) onToggleApp("kakao")
                },
            ) {
                Icon(painterResource(R.drawable.ic_target_app), null, tint = Color.Unspecified, modifier = Modifier.size(PopupIconSize))
                Spacer(Modifier.width(8.dp))
                Icon(painterResource(R.drawable.ic_reminder_app_kakao), null, tint = Color.Unspecified, modifier = Modifier.size(PopupIconSize))
            }
            PopupActions(
                isEditing = draft.editingTaskId != null,
                onCancel = onDismiss,
                onDelete = onDelete,
                onSave = onSave,
            )
        }
    }
}

@Composable
private fun PopupHeader(title: String, onDismiss: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(title, style = PhoneShimType.KorCaption, color = PhoneShimTheme.colors.textPrimary)
        Box(Modifier.size(16.dp).clickable(onClick = onDismiss), contentAlignment = Alignment.Center) {
            Icon(painterResource(R.drawable.ic_reminder_close), null, tint = Color.Unspecified, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun PopupTimeSection(
    selectedDate: LocalDate,
    isConfigured: Boolean,
    startTime: String,
    endTime: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (isConfigured) {
            Box(Modifier.width(50.dp).height(21.dp).background(PhoneShimTheme.colors.brandStrong, CircleShape), contentAlignment = Alignment.Center) {
                Text("${selectedDate.monthValue}.${selectedDate.dayOfMonth}", style = PhoneShimType.EngLabel, color = PhoneShimTheme.colors.onBrand)
            }
        }
        Row(Modifier.fillMaxWidth().height(36.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            PopupClock(if (isConfigured) startTime else "00:00", isConfigured)
            Text("-", modifier = Modifier.padding(horizontal = 8.dp), style = PhoneShimType.EngH2, color = PhoneShimTheme.colors.textPrimary)
            PopupClock(if (isConfigured) endTime else "00:00", isConfigured)
        }
    }
}

@Composable
private fun PopupClock(value: String, isConfigured: Boolean) {
    val color = if (isConfigured) PhoneShimTheme.colors.textPrimary else Color(0xFFB2C69D)
    val parts = value.split(':').let { if (it.size == 2) it else listOf("00", "00") }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(parts[0], modifier = Modifier.width(27.dp), style = PhoneShimType.EngH2, color = color, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Text(":", modifier = Modifier.height(32.dp), style = PhoneShimType.EngH2, color = color)
        Text(parts[1], modifier = Modifier.width(27.dp), style = PhoneShimType.EngH2, color = color, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

@Composable
private fun PopupSelectionRow(
    label: String,
    labelColor: Color,
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = PopupHorizontalItemPadding, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = PhoneShimType.KorCaption, color = labelColor)
        Row(verticalAlignment = Alignment.CenterVertically, content = content)
    }
}

@Composable
private fun PopupActions(isEditing: Boolean, onCancel: () -> Unit, onDelete: () -> Unit, onSave: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier.width(76.dp).height(PopupButtonHeight)
                .background(PhoneShimTheme.colors.background, RoundedCornerShape(8.dp))
                .border(1.dp, PhoneShimTheme.colors.brand, RoundedCornerShape(8.dp))
                .clickable(onClick = if (isEditing) onDelete else onCancel),
            contentAlignment = Alignment.Center,
        ) {
            Text(if (isEditing) "삭제" else "취소", style = PhoneShimType.KorCaption, color = PhoneShimTheme.colors.brand)
        }
        Box(
            modifier = Modifier.weight(1f).height(PopupButtonHeight).background(PhoneShimTheme.colors.brand, RoundedCornerShape(8.dp)).clickable(onClick = onSave),
            contentAlignment = Alignment.Center,
        ) { Text("할 일 저장", style = PhoneShimType.KorCaption, color = PhoneShimTheme.colors.onBrand) }
    }
}

@Preview(name = "할 일 팝업 세팅 전", widthDp = 360, heightDp = 800, showBackground = true)
@Composable private fun BeforePopupPreview() = PhoneShimTheme { ReminderSetPopup(LocalDate.of(2026, 7, 17), ReminderDraft(title = "과제"), {}, {}, {}, {}, {}, {}, {}, {}) }

@Preview(name = "할 일 팝업 세팅 후", widthDp = 360, heightDp = 800, showBackground = true)
@Composable private fun AfterPopupPreview() = PhoneShimTheme { ReminderSetPopup(LocalDate.of(2026, 7, 17), ReminderDraft(title = "과제", startTimeText = "10:00", endTimeText = "11:00", restrictionMode = RestrictionMode.SPECIFIC_APPS, restrictedAppIds = setOf("kakao")), {}, {}, {}, {}, {}, {}, {}, {}) }

@Preview(name = "기존 일정 수정 팝업", widthDp = 360, heightDp = 800, showBackground = true)
@Composable private fun EditPopupPreview() = PhoneShimTheme { ReminderSetPopup(LocalDate.of(2026, 7, 17), ReminderDraft("1", "과제하기", "10:00", "11:00"), {}, {}, {}, {}, {}, {}, {}, {}) }

@Preview(name = "시간 중복 오류 상태", widthDp = 360, heightDp = 800, showBackground = true)
@Composable private fun OverlapPreview() = PhoneShimTheme { ReminderSetPopup(LocalDate.of(2026, 7, 17), ReminderDraft(title = "과제", startTimeText = "10:30", endTimeText = "11:30", timeError = "이미 해당 시간에 등록된 할 일이 있습니다"), {}, {}, {}, {}, {}, {}, {}, {}) }
