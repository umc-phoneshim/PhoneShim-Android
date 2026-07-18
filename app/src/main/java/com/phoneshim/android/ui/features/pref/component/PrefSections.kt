package com.phoneshim.android.ui.features.pref.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.phoneshim.android.R
import com.phoneshim.android.ui.common.SelectableChip
import com.phoneshim.android.ui.common.AppInfoRow
import com.phoneshim.android.ui.common.DurationDisplay
import com.phoneshim.android.ui.common.DurationDisplayVariant
import com.phoneshim.android.ui.common.SelectableChipVariant
import com.phoneshim.android.ui.common.SelectionDropdown
import com.phoneshim.android.ui.features.pref.viewmodel.AgeGroup
import com.phoneshim.android.ui.features.pref.viewmodel.AppGoal
import com.phoneshim.android.ui.features.pref.viewmodel.Gender
import com.phoneshim.android.ui.features.pref.viewmodel.PrefValidationResult
import com.phoneshim.android.ui.features.pref.viewmodel.SelectionPopup
import com.phoneshim.android.ui.theme.PhoneShimDimens
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimType

private object PrefSectionDefaults {
    val sectionIconSize = 24.dp
    val selectorTopSpacing = 24.dp
    val genderChipWidth = 36.dp
    val ageChipWidth = 96.dp
    val selectorChipHeight = 28.dp
    val selectorSpacing = 12.dp
    val totalCardHeight = 90.dp
    val appRowHeight = 52.dp
    val appErrorRowHeight = 62.dp
    val appIconSize = 24.dp
    val actionTouchSize = 40.dp
    val actionIconSize = 20.dp
    val listDividerThickness = 1.dp
}

@Composable
fun PrefSectionTitle(
    title: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_goal),
            contentDescription = null,
            modifier = Modifier.size(PrefSectionDefaults.sectionIconSize),
            tint = PhoneShimTheme.colors.textPrimary,
        )
        Spacer(Modifier.width(PhoneShimDimens.spacing4))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = PhoneShimTheme.colors.textPrimary,
        )
    }
}

@Composable
fun PrefUserInfoSection(
    gender: Gender,
    ageGroup: AgeGroup,
    selectionPopup: SelectionPopup?,
    onGenderClick: () -> Unit,
    onAgeGroupClick: () -> Unit,
    onGenderSelected: (Gender) -> Unit,
    onAgeGroupSelected: (AgeGroup) -> Unit,
    onDismissPopup: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        PrefSectionTitle(title = "사용자 정보")
        Spacer(Modifier.height(PrefSectionDefaults.selectorTopSpacing))
        Row(
            modifier = Modifier.padding(horizontal = PhoneShimDimens.spacing12),
            horizontalArrangement = Arrangement.spacedBy(PrefSectionDefaults.selectorSpacing),
        ) {
            Box {
                SelectableChip(
                    text = genderLabel(gender),
                    selected = true,
                    variant = SelectableChipVariant.Filled,
                    modifier = Modifier.width(PrefSectionDefaults.genderChipWidth),
                    onClick = onGenderClick,
                )
                SingleSelectionMenu(
                    expanded = selectionPopup == SelectionPopup.GENDER,
                    options = Gender.entries,
                    selected = gender,
                    label = { genderLabel(it) },
                    onSelected = onGenderSelected,
                    onDismiss = onDismissPopup,
                )
            }
            Box {
                SelectableChip(
                    text = ageGroupLabel(ageGroup),
                    selected = true,
                    variant = SelectableChipVariant.Filled,
                    modifier = Modifier.width(PrefSectionDefaults.ageChipWidth),
                    onClick = onAgeGroupClick,
                )
                SingleSelectionMenu(
                    expanded = selectionPopup == SelectionPopup.AGE_GROUP,
                    options = AgeGroup.entries,
                    selected = ageGroup,
                    label = { ageGroupLabel(it) },
                    onSelected = onAgeGroupSelected,
                    onDismiss = onDismissPopup,
                )
            }
        }
    }
}

@Composable
private fun <T> SingleSelectionMenu(
    expanded: Boolean,
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelected: (T) -> Unit,
    onDismiss: () -> Unit,
) {
    SelectionDropdown(
        expanded = expanded,
        options = options,
        selected = selected,
        optionLabel = label,
        onSelected = onSelected,
        onDismiss = onDismiss,
        optionTrailingContent = { _, isSelected ->
            SquareSelectionIndicator(selected = isSelected)
        },
    )
}

@Composable
private fun SquareSelectionIndicator(selected: Boolean) {
    val color = if (selected) PhoneShimTheme.colors.brand else PhoneShimTheme.colors.divider
    Box(
        modifier = Modifier
            .size(11.dp)
            .clip(MaterialTheme.shapes.extraSmall)
            .border(1.dp, color, MaterialTheme.shapes.extraSmall)
            .then(
                if (selected) Modifier.background(color, MaterialTheme.shapes.extraSmall)
                else Modifier
            ),
    )
}

@Composable
fun PrefGoalSection(
    totalGoalMinutes: Int,
    appGoals: List<AppGoal>,
    validation: PrefValidationResult,
    onTotalGoalClick: () -> Unit,
    onEditAppTime: (String) -> Unit,
    onToggleLimit: (String) -> Unit,
    onEditAppGoal: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        PrefSectionTitle(title = "목표 설정")
        Spacer(Modifier.height(PhoneShimDimens.spacing24))
        TotalGoalCard(
            totalMinutes = totalGoalMinutes,
            isError = validation.isTotalGoalInvalid,
            onClick = onTotalGoalClick,
        )
        Spacer(Modifier.height(PhoneShimDimens.spacing24))
        Text(
            text = "어플 별 목표 시간",
            style = MaterialTheme.typography.bodySmall,
            color = PhoneShimTheme.colors.brand,
        )
        Spacer(Modifier.height(PhoneShimDimens.spacing8))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = PhoneShimTheme.colors.surface),
            border = BorderStroke(1.dp, PhoneShimTheme.colors.divider),
        ) {
            appGoals.forEachIndexed { index, goal ->
                AppGoalItem(
                    appGoal = goal,
                    isError = goal.id in validation.invalidAppGoalIds,
                    onTimeClick = { onEditAppTime(goal.id) },
                    onToggleLimit = { onToggleLimit(goal.id) },
                    onEdit = { onEditAppGoal(goal.id) },
                )
                if (index != appGoals.lastIndex) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = PhoneShimDimens.spacing12)
                            .height(PrefSectionDefaults.listDividerThickness)
                            .background(PhoneShimTheme.colors.divider),
                    )
                }
            }
        }
    }
}

@Composable
private fun TotalGoalCard(
    totalMinutes: Int,
    isError: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = if (isError) PhoneShimTheme.colors.error else PhoneShimTheme.colors.brand
    Column {
        Card(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(PrefSectionDefaults.totalCardHeight),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = PhoneShimTheme.colors.brandSubtle),
            border = BorderStroke(1.dp, borderColor),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(PhoneShimDimens.spacing16),
            ) {
                Text(
                    text = "전체 폰 목표 시간",
                    style = MaterialTheme.typography.bodySmall,
                    color = PhoneShimTheme.colors.brand,
                )
                Spacer(Modifier.height(PhoneShimDimens.spacing8))
                DurationDisplay(
                    totalMinutes = totalMinutes,
                    variant = DurationDisplayVariant.Compact,
                )
            }
        }
        if (isError) {
            Text(
                text = "목표 사용 시간은 10분 이상 입력해 주세요.",
                style = MaterialTheme.typography.bodySmall,
                color = PhoneShimTheme.colors.error,
                modifier = Modifier.padding(top = PhoneShimDimens.spacing4),
            )
        }
    }
}

@Composable
private fun AppGoalItem(
    appGoal: AppGoal,
    isError: Boolean,
    onTimeClick: () -> Unit,
    onToggleLimit: () -> Unit,
    onEdit: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(
                if (isError) PrefSectionDefaults.appErrorRowHeight
                else PrefSectionDefaults.appRowHeight
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppInfoRow(
            appName = appGoal.appName,
            appNameStyle = MaterialTheme.typography.bodySmall,
            appNameColor = PhoneShimTheme.colors.textSecondary,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clickable(onClick = onTimeClick)
                .padding(start = PhoneShimDimens.spacing12),
            supportingContent = {
                if (isError) {
                    Text(
                        text = "목표 시간이 10분 미만입니다.",
                        style = PhoneShimType.KorLabel,
                        color = PhoneShimTheme.colors.error,
                    )
                }
            },
            trailingContent = {
                Text(
                    text = "%02d 시간  %02d 분".format(
                        appGoal.goalMinutes / 60,
                        appGoal.goalMinutes % 60,
                    ),
                    style = PhoneShimType.EngLabel,
                    color = if (appGoal.isLimitEnabled) {
                        PhoneShimTheme.colors.textPrimary
                    } else {
                        PhoneShimTheme.colors.textTertiary
                    },
                )
            },
        )
        IconButton(
            onClick = onToggleLimit,
            modifier = Modifier.size(PrefSectionDefaults.actionTouchSize),
        ) {
            Icon(
                painter = painterResource(
                    if (appGoal.isLimitEnabled) {
                        R.drawable.ic_access_restriction
                    } else {
                        R.drawable.ic_access_restriction_disabled
                    },
                ),
                contentDescription = if (appGoal.isLimitEnabled) {
                    "${appGoal.appName} 사용 제한 비활성화"
                } else {
                    "${appGoal.appName} 사용 제한 활성화"
                },
                modifier = Modifier.size(PrefSectionDefaults.actionIconSize),
                tint = if (appGoal.isLimitEnabled) {
                    PhoneShimTheme.colors.error
                } else {
                    PhoneShimTheme.colors.textTertiary
                },
            )
        }
        IconButton(
            onClick = onEdit,
            modifier = Modifier.size(PrefSectionDefaults.actionTouchSize),
        ) {
            Image(
                painter = painterResource(R.drawable.ic_modify),
                contentDescription = "${appGoal.appName} 목표 문구 편집",
                modifier = Modifier.size(PrefSectionDefaults.actionIconSize),
            )
        }
    }
}

fun genderLabel(gender: Gender): String = when (gender) {
    Gender.MALE -> "남"
    Gender.FEMALE -> "여"
}

fun ageGroupLabel(ageGroup: AgeGroup): String = when (ageGroup) {
    AgeGroup.TEENS -> "10대"
    AgeGroup.TWENTIES -> "20대"
    AgeGroup.THIRTIES -> "30대"
    AgeGroup.FORTIES -> "40대"
    AgeGroup.FIFTIES_OR_MORE -> "50대 이상"
}
