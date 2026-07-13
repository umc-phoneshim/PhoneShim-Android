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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.phoneshim.android.R
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
            painter = painterResource(R.drawable.ic_section_indicator),
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
        PrefSectionTitle(title = stringResource(R.string.pref_user_info))
        Spacer(Modifier.height(PrefSectionDefaults.selectorTopSpacing))
        Row(
            modifier = Modifier.padding(horizontal = PhoneShimDimens.spacing12),
            horizontalArrangement = Arrangement.spacedBy(PrefSectionDefaults.selectorSpacing),
        ) {
            Box {
                SelectionChip(
                    text = genderLabel(gender),
                    width = PrefSectionDefaults.genderChipWidth,
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
                SelectionChip(
                    text = ageGroupLabel(ageGroup),
                    width = PrefSectionDefaults.ageChipWidth,
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
private fun SelectionChip(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .width(width)
            .height(PrefSectionDefaults.selectorChipHeight)
            .clip(MaterialTheme.shapes.extraLarge)
            .background(PhoneShimTheme.colors.brand)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = PhoneShimType.KorLabel,
            color = PhoneShimTheme.colors.onBrand,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun <T> SingleSelectionMenu(
    expanded: Boolean,
    options: List<T>,
    selected: T,
    label: @Composable (T) -> String,
    onSelected: (T) -> Unit,
    onDismiss: () -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier.background(PhoneShimTheme.colors.surface),
    ) {
        options.forEach { option ->
            DropdownMenuItem(
                text = {
                    Text(
                        text = label(option),
                        style = MaterialTheme.typography.bodySmall,
                        color = PhoneShimTheme.colors.textSecondary,
                    )
                },
                trailingIcon = {
                    SquareSelectionIndicator(selected = option == selected)
                },
                onClick = { onSelected(option) },
                modifier = Modifier.height(36.dp),
            )
        }
    }
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
        PrefSectionTitle(title = stringResource(R.string.pref_goal_settings))
        Spacer(Modifier.height(PhoneShimDimens.spacing24))
        TotalGoalCard(
            totalMinutes = totalGoalMinutes,
            isError = validation.isTotalGoalInvalid,
            onClick = onTotalGoalClick,
        )
        Spacer(Modifier.height(PhoneShimDimens.spacing24))
        Text(
            text = stringResource(R.string.pref_app_goal_time_title),
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
                    text = stringResource(R.string.pref_total_goal_time),
                    style = MaterialTheme.typography.bodySmall,
                    color = PhoneShimTheme.colors.brand,
                )
                Spacer(Modifier.height(PhoneShimDimens.spacing8))
                Text(
                    text = stringResource(
                        R.string.pref_total_time_format,
                        totalMinutes / 60,
                        totalMinutes % 60,
                    ),
                    style = PhoneShimType.EngH1,
                    color = PhoneShimTheme.colors.textPrimary,
                )
            }
        }
        if (isError) {
            Text(
                text = stringResource(R.string.pref_minimum_time_error),
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
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clickable(onClick = onTimeClick)
                .padding(start = PhoneShimDimens.spacing12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(PrefSectionDefaults.appIconSize)
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(PhoneShimTheme.colors.divider),
            )
            Spacer(Modifier.width(PhoneShimDimens.spacing12))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appGoal.appName,
                    style = MaterialTheme.typography.bodySmall,
                    color = PhoneShimTheme.colors.textSecondary,
                )
                if (isError) {
                    Text(
                        text = stringResource(R.string.pref_invalid_app_time),
                        style = PhoneShimType.KorLabel,
                        color = PhoneShimTheme.colors.error,
                    )
                }
            }
            Text(
                text = stringResource(
                    R.string.pref_app_time_format,
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
        }
        IconButton(
            onClick = onToggleLimit,
            modifier = Modifier.size(PrefSectionDefaults.actionTouchSize),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_limit_remove),
                contentDescription = if (appGoal.isLimitEnabled) {
                    stringResource(R.string.pref_disable_limit, appGoal.appName)
                } else {
                    stringResource(R.string.pref_enable_limit, appGoal.appName)
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
                painter = painterResource(R.drawable.ic_edit),
                contentDescription = stringResource(R.string.pref_edit_app_goal, appGoal.appName),
                modifier = Modifier.size(PrefSectionDefaults.actionIconSize),
            )
        }
    }
}

@Composable
fun genderLabel(gender: Gender): String = when (gender) {
    Gender.MALE -> stringResource(R.string.pref_gender_male)
    Gender.FEMALE -> stringResource(R.string.pref_gender_female)
}

@Composable
fun ageGroupLabel(ageGroup: AgeGroup): String = when (ageGroup) {
    AgeGroup.TEENS -> stringResource(R.string.pref_age_teens)
    AgeGroup.TWENTIES -> stringResource(R.string.pref_age_twenties)
    AgeGroup.THIRTIES -> stringResource(R.string.pref_age_thirties)
    AgeGroup.FORTIES -> stringResource(R.string.pref_age_forties)
    AgeGroup.FIFTIES_OR_MORE -> stringResource(R.string.pref_age_fifties_or_more)
}
