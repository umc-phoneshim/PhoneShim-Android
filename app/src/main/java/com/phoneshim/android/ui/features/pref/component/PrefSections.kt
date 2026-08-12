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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.phoneshim.android.R
import com.phoneshim.android.ui.common.GoalTimeCard
import com.phoneshim.android.ui.common.PhoneShimTooltip
import com.phoneshim.android.ui.common.SelectableChip
import com.phoneshim.android.ui.common.SelectableChipVariant
import com.phoneshim.android.ui.common.TooltipTailAlignment
import com.phoneshim.android.ui.features.pref.viewmodel.AgeGroup
import com.phoneshim.android.ui.features.pref.viewmodel.AppGoal
import com.phoneshim.android.ui.features.pref.viewmodel.Gender
import com.phoneshim.android.ui.features.pref.viewmodel.PrefValidationResult
import com.phoneshim.android.ui.features.pref.viewmodel.SelectionPopup
import com.phoneshim.android.ui.theme.PhoneShimDimens
import com.phoneshim.android.ui.theme.PhoneShimPalette
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
    val appRowHeight = 28.dp
    val appIconSize = 24.dp
    val actionIconSize = 20.dp
    val listDividerThickness = 1.dp
    val selectionPopupWidth = 96.dp
    val selectionPopupOffset = 4.dp
    val selectionPopupPadding = 12.dp
    val selectionOptionSpacing = 10.dp
    val selectionIndicatorSize = 12.dp
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
    if (!expanded) return

    val density = LocalDensity.current
    val positionProvider = remember(density) {
        BelowAnchorPositionProvider(
            gapPx = with(density) { PrefSectionDefaults.selectionPopupOffset.roundToPx() },
        )
    }
    Popup(
        popupPositionProvider = positionProvider,
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true),
    ) {
        Column(
            modifier = Modifier
                .width(PrefSectionDefaults.selectionPopupWidth)
                .clip(RoundedCornerShape(8.dp))
                .background(PhoneShimTheme.colors.brandSubtle)
                .padding(PrefSectionDefaults.selectionPopupPadding),
            verticalArrangement = Arrangement.spacedBy(PrefSectionDefaults.selectionOptionSpacing),
        ) {
            options.forEach { option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(role = Role.RadioButton) { onSelected(option) },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = label(option),
                        style = PhoneShimType.KorLabel,
                        color = PhoneShimTheme.colors.textSecondary,
                    )
                    SquareSelectionIndicator(selected = option == selected)
                }
            }
        }
    }
}

@Composable
private fun SquareSelectionIndicator(selected: Boolean) {
    val color = if (selected) PhoneShimTheme.colors.brand else PhoneShimTheme.colors.divider
    Box(
        modifier = Modifier
            .size(PrefSectionDefaults.selectionIndicatorSize)
            .clip(RoundedCornerShape(4.dp))
            .border(1.dp, color, RoundedCornerShape(4.dp))
            .background(
                color = if (selected) color else PhoneShimPalette.White,
                shape = RoundedCornerShape(4.dp),
            ),
    )
}

private class BelowAnchorPositionProvider(
    private val gapPx: Int,
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val preferredX = when (layoutDirection) {
            LayoutDirection.Ltr -> anchorBounds.left
            LayoutDirection.Rtl -> anchorBounds.right - popupContentSize.width
        }
        val x = preferredX.coerceIn(
            minimumValue = 0,
            maximumValue = (windowSize.width - popupContentSize.width).coerceAtLeast(0),
        )
        val below = anchorBounds.bottom + gapPx
        val y = if (below + popupContentSize.height <= windowSize.height) {
            below
        } else {
            (anchorBounds.top - gapPx - popupContentSize.height).coerceAtLeast(0)
        }
        return IntOffset(x, y)
    }
}

@Composable
fun PrefGoalSection(
    totalGoalMinutes: Int,
    appGoals: List<AppGoal>,
    validation: PrefValidationResult,
    onTotalGoalClick: () -> Unit,
    onEditAppTime: (String) -> Unit,
    onToggleLimit: (String) -> Unit,
    showRestrictionTooltip: Boolean,
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
        Box(modifier = Modifier.fillMaxWidth()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = PhoneShimTheme.colors.surface),
                border = BorderStroke(1.dp, PhoneShimPalette.Primary300),
            ) {
                Column(
                    modifier = Modifier.padding(PhoneShimDimens.spacing12),
                    verticalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing12),
                ) {
                    appGoals.forEachIndexed { index, goal ->
                        AppGoalItem(
                            appGoal = goal,
                            isError = goal.id in validation.invalidAppGoalIds,
                            onTimeClick = { onEditAppTime(goal.id) },
                            onToggleLimit = { onToggleLimit(goal.id) },
                        )
                        if (index != appGoals.lastIndex) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(PrefSectionDefaults.listDividerThickness)
                                    .background(PhoneShimTheme.colors.divider),
                            )
                        }
                    }
                }
            }
            if (showRestrictionTooltip) {
                PhoneShimTooltip(
                    text = "목표 시간 이후 어플 제한을 표시해줍니다.",
                    tailAlignment = TooltipTailAlignment.End,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(y = 33.dp)
                        .zIndex(1f),
                )
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
    Column {
        GoalTimeCard(
            label = "전체 폰 목표 시간",
            totalMinutes = totalMinutes,
            isError = isError,
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(PrefSectionDefaults.totalCardHeight),
        )
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
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(PrefSectionDefaults.appRowHeight)
                .clickable(
                    role = Role.Button,
                    onClickLabel = "${appGoal.appName} 목표 시간 수정",
                    onClick = onTimeClick,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppGoalIcon(appGoal = appGoal)
            Spacer(Modifier.width(PhoneShimDimens.spacing12))
            Text(
                text = appGoal.appName,
                modifier = Modifier.weight(1f),
                style = PhoneShimType.KorCaption,
                color = PhoneShimTheme.colors.textPrimary,
                maxLines = 1,
            )
            GoalTimeText(
                totalMinutes = appGoal.goalMinutes,
                enabled = appGoal.isLimitEnabled,
            )
            Spacer(Modifier.width(PhoneShimDimens.spacing12))
            Image(
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
                modifier = Modifier
                    .size(PrefSectionDefaults.actionIconSize)
                    .clickable(
                        role = Role.Switch,
                        onClick = onToggleLimit,
                    ),
            )
        }
        if (isError) {
            Text(
                text = "목표 시간이 10분 미만입니다.",
                modifier = Modifier.padding(top = PhoneShimDimens.spacing4),
                style = PhoneShimType.KorLabel,
                color = PhoneShimTheme.colors.error,
            )
        }
    }
}

@Composable
private fun GoalTimeText(
    totalMinutes: Int,
    enabled: Boolean,
) {
    val numberColor = if (enabled) {
        PhoneShimTheme.colors.textPrimary
    } else {
        PhoneShimTheme.colors.textTertiary
    }
    val unitColor = if (enabled) {
        PhoneShimTheme.colors.textSecondary
    } else {
        PhoneShimTheme.colors.textTertiary
    }
    Row(
        horizontalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing4)) {
            Text(
                text = "%02d".format(totalMinutes / 60),
                style = PhoneShimType.EngLabel,
                color = numberColor,
            )
            Text(text = "시간", style = PhoneShimType.KorLabel, color = unitColor)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing4)) {
            Text(
                text = "%02d".format(totalMinutes % 60),
                style = PhoneShimType.EngLabel,
                color = numberColor,
            )
            Text(text = "분", style = PhoneShimType.KorLabel, color = unitColor)
        }
    }
}

@Composable
private fun AppGoalIcon(appGoal: AppGoal) {
    val iconRes = when (appGoal.id) {
        "kakao", "com.kakao.talk" -> R.drawable.pref_app_kakao
        "facebook", "com.facebook.katana" -> R.drawable.pref_app_facebook
        "tiktok", "com.zhiliaoapp.musically" -> R.drawable.pref_app_tiktok
        else -> null
    }
    if (iconRes != null) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier
                .size(PrefSectionDefaults.appIconSize)
                .clip(MaterialTheme.shapes.extraSmall),
            contentScale = ContentScale.Crop,
        )
    } else {
        Box(
            modifier = Modifier
                .size(PrefSectionDefaults.appIconSize)
                .clip(MaterialTheme.shapes.extraSmall)
                .background(PhoneShimTheme.colors.divider),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = appGoal.appName.take(1),
                style = PhoneShimType.KorLabel,
                color = PhoneShimTheme.colors.textSecondary,
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
