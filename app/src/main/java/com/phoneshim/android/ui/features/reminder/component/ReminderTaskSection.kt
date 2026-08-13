package com.phoneshim.android.ui.features.reminder.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.phoneshim.android.R
import com.phoneshim.android.ui.common.TodoRow
import com.phoneshim.android.ui.features.reminder.viewmodel.ReminderTaskUiModel
import com.phoneshim.android.ui.features.reminder.viewmodel.formatMinutes
import com.phoneshim.android.ui.theme.PhoneShimPalette
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimType

private val TaskRowHeight = 56.dp
private val TaskItemSpacing = 12.dp
private val TaskCardHeight = TaskRowHeight

@Composable
internal fun ReminderTaskSection(
    tasks: List<ReminderTaskUiModel>,
    isLoading: Boolean,
    errorMessage: String?,
    warningMessage: String?,
    isReadOnly: Boolean,
    onAddTask: () -> Unit,
    onEditTask: (ReminderTaskUiModel) -> Unit,
    onRetry: () -> Unit,
) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(painterResource(R.drawable.ic_goal), null, tint = Color.Unspecified, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(4.dp))
            Text(
                "오늘 할 일 설정",
                style = PhoneShimType.KorBodyM.copy(fontWeight = FontWeight.SemiBold),
                color = PhoneShimTheme.colors.textPrimary,
            )
        }
        when {
            isLoading && tasks.isEmpty() -> {
                Box(Modifier.fillMaxWidth().height(96.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PhoneShimTheme.colors.brand)
                }
            }
            errorMessage != null && tasks.isEmpty() -> {
                ReminderLoadError(errorMessage, onRetry)
            }
            tasks.isNotEmpty() -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(TaskCardHeight * tasks.size + TaskItemSpacing * (tasks.size - 1)),
                    verticalArrangement = Arrangement.spacedBy(TaskItemSpacing),
                    userScrollEnabled = false,
                ) {
                    items(tasks, key = { task -> task.id }) { task ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(),
                        ) {
                            ReminderTaskItem(
                                task = task,
                                isReadOnly = isReadOnly,
                                onEditTask = onEditTask,
                            )
                        }
                    }
                }
            }
        }
        if (errorMessage != null && tasks.isNotEmpty()) {
            ReminderLoadError(errorMessage, onRetry)
        }
        if (warningMessage != null) ReminderLoadError(warningMessage, onRetry)
        if (!isLoading && errorMessage == null && !isReadOnly) ReminderEmptyTaskCard(onAddTask)
    }
}

@Composable
private fun ReminderLoadError(errorMessage: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(errorMessage, style = PhoneShimType.KorCaption, color = PhoneShimTheme.colors.textSecondary)
        TextButton(onClick = onRetry) {
            Text("다시 시도", color = PhoneShimTheme.colors.brand)
        }
    }
}

@Composable
private fun ReminderTaskItem(
    task: ReminderTaskUiModel,
    isReadOnly: Boolean,
    onEditTask: (ReminderTaskUiModel) -> Unit,
) {
    TodoRow(
        title = task.title,
        timeRange = "${formatMinutes(task.startMinutes)} ~ ${formatMinutes(task.endMinutes)}",
        modifier = Modifier.fillMaxWidth().height(TaskRowHeight),
        variant = com.phoneshim.android.ui.common.TodoRowVariant.Card,
        leadingContent = {
            Icon(
                painter = painterResource(R.drawable.ic_reminder_drag_handle),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(20.dp),
            )
        },
        trailingContent = {
            Box(
                Modifier.size(40.dp).clickable(enabled = !isReadOnly) { onEditTask(task) },
                contentAlignment = Alignment.Center,
            ) {
                Icon(painterResource(R.drawable.ic_modify), "할 일 수정", tint = Color.Unspecified, modifier = Modifier.size(20.dp))
            }
        },
    )
}

@Composable
private fun ReminderEmptyTaskCard(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(PhoneShimTheme.colors.brandSubtle)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(painterResource(R.drawable.ic_plus), null, tint = Color.Unspecified, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Text("할 일을 추가하세요", style = PhoneShimType.KorCaption, color = PhoneShimTheme.colors.brand)
    }
}
