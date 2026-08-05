package com.phoneshim.android.ui.features.reminder.component

import androidx.compose.foundation.background
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.phoneshim.android.R
import com.phoneshim.android.ui.common.PhoneShimButtonSize
import com.phoneshim.android.ui.common.PrimaryButton
import com.phoneshim.android.ui.common.SecondaryButton
import com.phoneshim.android.ui.common.InteractiveTimeSegmentInput
import com.phoneshim.android.ui.features.reminder.viewmodel.ReminderDraft
import com.phoneshim.android.ui.features.reminder.viewmodel.RestrictionMode
import com.phoneshim.android.ui.features.reminder.viewmodel.DUPLICATE_SCHEDULE_MESSAGE
import com.phoneshim.android.ui.theme.PhoneShimPalette
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimType
import java.time.LocalDate

private val PopupWidth = 328.dp
private val PopupHeight = 285.dp
private val PopupPadding = 16.dp
private val PopupCornerRadius = 12.dp
private val PopupItemSpacing = 16.dp
private val PopupHorizontalItemPadding = 16.dp
private val PopupIconSize = 24.dp

@Composable
fun ReminderSetPopup(
    selectedDate: LocalDate,
    todayDate: LocalDate,
    draft: ReminderDraft,
    onDismiss: () -> Unit,
    onTitleChange: (String) -> Unit,
    onStartTimeChange: (String) -> Unit,
    onEndTimeChange: (String) -> Unit,
    onRestrictionModeChange: (RestrictionMode) -> Unit,
    onToggleApp: (String) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    isSubmitting: Boolean = false,
) {
    var isNameInputVisible by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf(draft.title) }
    var isRestrictionPopupVisible by remember { mutableStateOf(false) }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(modifier = Modifier.width(PopupWidth).height(PopupHeight)) {
            Column(
                modifier = Modifier
                    .matchParentSize()
                    .background(PhoneShimTheme.colors.surface, RoundedCornerShape(PopupCornerRadius))
                    .padding(PopupPadding),
                verticalArrangement = Arrangement.spacedBy(PopupItemSpacing),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
            PopupHeader(onDismiss = onDismiss)
            PopupTimeSection(
                selectedDate = selectedDate,
                todayDate = todayDate,
                startTime = draft.startTimeText,
                endTime = draft.endTimeText,
                onStartTimeChange = onStartTimeChange,
                onEndTimeChange = onEndTimeChange,
            )
            PopupSelectionRow(
                label = "이름",
                labelColor = PhoneShimTheme.colors.textSecondary,
                onClick = {
                    nameInput = draft.title
                    isNameInputVisible = true
                },
            ) {
                if (draft.title.isNotBlank()) {
                    Text(draft.title, style = PhoneShimType.KorCaption, color = PhoneShimTheme.colors.textPrimary)
                    Spacer(Modifier.width(4.dp))
                }
                Icon(painterResource(R.drawable.ic_chevron_right_small), null, tint = Color.Unspecified, modifier = Modifier.size(16.dp))
            }
            PopupSelectionRow(
                label = "제한 선택",
                labelColor = PhoneShimTheme.colors.error,
                onClick = { isRestrictionPopupVisible = true },
            ) {
                RestrictionSelection(draft)
            }
            PopupActions(
                isEditing = draft.editingTaskId != null,
                onCancel = onDismiss,
                onDelete = onDelete,
                onSave = onSave,
                enabled = !isSubmitting,
            )
            }
            if (draft.timeError == DUPLICATE_SCHEDULE_MESSAGE) {
                DuplicateScheduleErrorBanner(modifier = Modifier.align(Alignment.Center))
            }
            if (isRestrictionPopupVisible) {
                ReminderRestrictionPopup(
                    draft = draft,
                    onModeChange = onRestrictionModeChange,
                    onToggleApp = onToggleApp,
                    onDismiss = { isRestrictionPopupVisible = false },
                )
            }
        }
    }
    if (isNameInputVisible) {
        ReminderNameInputDialog(
            value = nameInput,
            onValueChange = { nameInput = it },
            onConfirm = {
                onTitleChange(nameInput)
                isNameInputVisible = false
            },
            onDismiss = { isNameInputVisible = false },
        )
    }
}

@Composable
private fun PopupHeader(onDismiss: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text("해야 할 일", style = PhoneShimType.KorCaption, color = PhoneShimTheme.colors.textPrimary)
        Box(Modifier.size(16.dp).clickable(onClick = onDismiss), contentAlignment = Alignment.Center) {
            Icon(painterResource(R.drawable.ic_reminder_close), null, tint = Color.Unspecified, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun PopupTimeSection(
    selectedDate: LocalDate,
    todayDate: LocalDate,
    startTime: String,
    endTime: String,
    onStartTimeChange: (String) -> Unit,
    onEndTimeChange: (String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            Modifier.width(50.dp).height(21.dp).background(PhoneShimTheme.colors.brandStrong, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                if (selectedDate == todayDate) "Today" else "${selectedDate.monthValue}.${selectedDate.dayOfMonth}",
                style = PhoneShimType.EngLabel,
                color = PhoneShimTheme.colors.onBrand,
            )
        }
        Row(Modifier.fillMaxWidth().height(44.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            EditablePopupClock(startTime.ifBlank { "00:00" }, onStartTimeChange)
            Text("-", modifier = Modifier.padding(horizontal = 8.dp), style = PhoneShimType.EngH2, color = PhoneShimTheme.colors.textPrimary)
            EditablePopupClock(endTime.ifBlank { "00:00" }, onEndTimeChange)
        }
    }
}

@Composable
private fun RestrictionSelection(draft: ReminderDraft) {
    when (draft.restrictionMode) {
        RestrictionMode.NONE -> {
            Box(
                modifier = Modifier
                    .width(56.dp)
                    .height(24.dp)
                    .background(PhoneShimPalette.Gray500, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "제한 없음",
                    modifier = Modifier.fillMaxWidth(),
                    style = PhoneShimType.EngLabel,
                    color = PhoneShimPalette.White,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }

        RestrictionMode.FULL_PHONE -> {
            Icon(
                painterResource(R.drawable.ic_target_app),
                contentDescription = "전체 폰 제한",
                tint = Color.Unspecified,
                modifier = Modifier.size(PopupIconSize),
            )
        }

        RestrictionMode.SPECIFIC_APPS -> {
            Icon(
                painterResource(R.drawable.ic_target_app),
                contentDescription = "앱 제한",
                tint = Color.Unspecified,
                modifier = Modifier.size(PopupIconSize),
            )
            draft.restrictedAppIds.forEach { appId ->
                Spacer(Modifier.width(8.dp))
                ReminderAppIcon(appId, Modifier.size(PopupIconSize))
            }
        }
    }
}

@Composable
private fun EditablePopupClock(value: String, onValueChange: (String) -> Unit) {
    val parts = value.split(':').let { if (it.size == 2) it else listOf("00", "00") }
    Row(verticalAlignment = Alignment.CenterVertically) {
        EditableTimeCell(parts[0], 23) { hour -> onValueChange("$hour:${parts[1]}") }
        Text(":", modifier = Modifier.height(32.dp), style = PhoneShimType.EngH2, color = PhoneShimPalette.Primary400)
        EditableTimeCell(parts[1], 59) { minute -> onValueChange("${parts[0]}:$minute") }
    }
}

@Composable
private fun EditableTimeCell(value: String, maxValue: Int, onValueChange: (String) -> Unit) {
    InteractiveTimeSegmentInput(
        value = value,
        maxValue = maxValue,
        onValueChange = onValueChange,
        textStyle = PhoneShimType.EngH2,
        restingWidth = 28.dp,
        activeWidth = 44.dp,
        restingHeight = 32.dp,
        activeHeight = 44.dp,
    )
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
private fun PopupActions(
    isEditing: Boolean,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
    onSave: () -> Unit,
    enabled: Boolean,
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SecondaryButton(
            text = if (isEditing) "삭제" else "취소",
            onClick = if (isEditing) onDelete else onCancel,
            modifier = Modifier.width(76.dp),
            size = PhoneShimButtonSize.Small,
            fullWidth = false,
            enabled = enabled,
            shape = RoundedCornerShape(8.dp),
        )
        PrimaryButton(
            text = "할 일 저장",
            onClick = onSave,
            modifier = Modifier.weight(1f),
            size = PhoneShimButtonSize.Small,
            fullWidth = false,
            enabled = enabled,
            shape = RoundedCornerShape(8.dp),
        )
    }
}

@Preview(name = "할 일 팝업 세팅 전", widthDp = 360, heightDp = 800, showBackground = true)
@Composable private fun BeforePopupPreview() = PhoneShimTheme { ReminderSetPopup(LocalDate.of(2026, 7, 17), LocalDate.of(2026, 7, 11), ReminderDraft(), {}, {}, {}, {}, {}, {}, {}, {}) }

@Preview(name = "할 일 팝업 세팅 후", widthDp = 360, heightDp = 800, showBackground = true)
@Composable private fun AfterPopupPreview() = PhoneShimTheme { ReminderSetPopup(LocalDate.of(2026, 7, 17), LocalDate.of(2026, 7, 11), ReminderDraft(title = "과제", startTimeText = "10:00", endTimeText = "11:00", restrictionMode = RestrictionMode.SPECIFIC_APPS, restrictedAppIds = setOf("kakao")), {}, {}, {}, {}, {}, {}, {}, {}) }

@Preview(name = "기존 일정 수정 팝업", widthDp = 360, heightDp = 800, showBackground = true)
@Composable private fun EditPopupPreview() = PhoneShimTheme { ReminderSetPopup(LocalDate.of(2026, 7, 17), LocalDate.of(2026, 7, 11), ReminderDraft("1", "과제하기", "10:00", "11:00"), {}, {}, {}, {}, {}, {}, {}, {}) }

@Preview(name = "시간 중복 오류 상태", widthDp = 360, heightDp = 800, showBackground = true)
@Composable private fun OverlapPreview() = PhoneShimTheme { ReminderSetPopup(LocalDate.of(2026, 7, 17), LocalDate.of(2026, 7, 11), ReminderDraft(title = "과제", startTimeText = "10:30", endTimeText = "11:30", timeError = "이미 해당 시간에 등록된 할 일이 있습니다"), {}, {}, {}, {}, {}, {}, {}, {}) }
