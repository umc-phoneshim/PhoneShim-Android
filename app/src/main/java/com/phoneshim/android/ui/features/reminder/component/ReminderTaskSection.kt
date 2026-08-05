package com.phoneshim.android.ui.features.reminder.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
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
import kotlin.math.abs

private val TaskRowHeight = 56.dp
private val TaskItemSpacing = 12.dp
private val TaskCardPadding = 12.dp
private val TaskCardHeight = TaskRowHeight + TaskCardPadding * 2

@Composable
internal fun ReminderTaskSection(
    tasks: List<ReminderTaskUiModel>,
    isLoading: Boolean,
    errorMessage: String?,
    warningMessage: String?,
    isReadOnly: Boolean,
    onAddTask: () -> Unit,
    onEditTask: (ReminderTaskUiModel) -> Unit,
    onMoveTask: (Int, Int) -> Unit,
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
                    itemsIndexed(tasks, key = { _, task -> task.id }) { index, task ->
                        Box(
                            modifier = Modifier
                                .animateItem(
                                    fadeInSpec = null,
                                    placementSpec = spring(
                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                        stiffness = Spring.StiffnessMediumLow,
                                    ),
                                    fadeOutSpec = null,
                                )
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(PhoneShimTheme.colors.surface)
                                .border(1.dp, PhoneShimPalette.Primary300, RoundedCornerShape(12.dp))
                                .padding(TaskCardPadding),
                        ) {
                            ReminderTaskItem(
                                task = task,
                                index = index,
                                lastIndex = tasks.lastIndex,
                                isReadOnly = isReadOnly,
                                onEditTask = onEditTask,
                                onMoveTask = onMoveTask,
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
    index: Int,
    lastIndex: Int,
    isReadOnly: Boolean,
    onEditTask: (ReminderTaskUiModel) -> Unit,
    onMoveTask: (Int, Int) -> Unit,
) {
    val moveThreshold = with(androidx.compose.ui.platform.LocalDensity.current) {
        ((TaskRowHeight + TaskCardPadding * 2 + TaskItemSpacing) / 2).toPx()
    }
    val latestIndex by rememberUpdatedState(index)
    var accumulatedDrag by remember(task.id) { mutableFloatStateOf(0f) }
    var currentIndex by remember(task.id) { mutableFloatStateOf(index.toFloat()) }

    TodoRow(
        title = task.title,
        timeRange = "${formatMinutes(task.startMinutes)} ~ ${formatMinutes(task.endMinutes)}",
        modifier = Modifier.fillMaxWidth().height(TaskRowHeight),
        leadingContent = {
            Icon(
                painter = painterResource(R.drawable.ic_reminder_drag_handle),
                contentDescription = "할 일 순서 변경",
                tint = Color.Unspecified,
                modifier = Modifier
                    .size(20.dp)
                    .pointerInput(task.id, lastIndex, isReadOnly) {
                        if (isReadOnly) return@pointerInput
                        detectDragGestures(
                            onDragStart = {
                                accumulatedDrag = 0f
                                currentIndex = latestIndex.toFloat()
                            },
                            onDragEnd = { accumulatedDrag = 0f },
                            onDragCancel = { accumulatedDrag = 0f },
                        ) { change, dragAmount ->
                            change.consume()
                            accumulatedDrag += dragAmount.y
                            if (abs(accumulatedDrag) >= moveThreshold) {
                                val from = currentIndex.toInt()
                                val to = (from + if (accumulatedDrag > 0) 1 else -1).coerceIn(0, lastIndex)
                                if (from != to) {
                                    onMoveTask(from, to)
                                    currentIndex = to.toFloat()
                                }
                                accumulatedDrag = 0f
                            }
                        }
                    },
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
