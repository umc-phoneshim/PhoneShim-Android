package com.phoneshim.android.ui.features.pref.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.phoneshim.android.R
import com.phoneshim.android.ui.common.BottomBar
import com.phoneshim.android.ui.common.BottomBarTab
import com.phoneshim.android.ui.common.BottomBarDefaults
import com.phoneshim.android.ui.common.PhoneShimButtonSize
import com.phoneshim.android.ui.common.PrimaryButton
import com.phoneshim.android.ui.common.SecondaryButton
import com.phoneshim.android.ui.features.pref.component.AppGoalDescriptionDialog
import com.phoneshim.android.ui.features.pref.component.GoalTimeDialog
import com.phoneshim.android.ui.features.pref.component.PrefGoalSection
import com.phoneshim.android.ui.features.pref.component.PrefUserInfoSection
import com.phoneshim.android.ui.features.pref.viewmodel.AgeGroup
import com.phoneshim.android.ui.features.pref.viewmodel.Gender
import com.phoneshim.android.ui.features.pref.viewmodel.PrefMockData
import com.phoneshim.android.ui.features.pref.viewmodel.PrefUiState
import com.phoneshim.android.ui.features.pref.viewmodel.PrefValidationResult
import com.phoneshim.android.ui.features.pref.viewmodel.SelectionPopup
import com.phoneshim.android.ui.features.pref.viewmodel.TimeEditorState
import com.phoneshim.android.ui.features.pref.viewmodel.TimeEditTarget
import com.phoneshim.android.ui.theme.PhoneShimDimens
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimType
import kotlinx.coroutines.delay

private object PrefScreenDefaults {
    val topBarHeight = 56.dp
    val topBarStartPadding = 4.dp
    val userToGoalSpacing = 20.dp
    val contentBottomPadding = 32.dp
    val actionToBottomBarSpacing = 8.dp
    const val restrictionTooltipDurationMillis = 4_000L
}

@Suppress("UNUSED_PARAMETER")
@Composable
fun PrefScreen(
    uiState: PrefUiState,
    selectedBottomTab: BottomBarTab,
    onBottomNavSelected: (BottomBarTab) -> Unit,
    onBack: () -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit,
    onGenderClick: () -> Unit,
    onAgeGroupClick: () -> Unit,
    onGenderSelected: (Gender) -> Unit,
    onAgeGroupSelected: (AgeGroup) -> Unit,
    onSelectionDismissed: () -> Unit,
    onTotalGoalClick: () -> Unit,
    onHoursChanged: (String) -> Unit,
    onMinutesChanged: (String) -> Unit,
    onTimeEditorLimitToggled: () -> Unit,
    onTimeEditorDismissed: () -> Unit,
    onTimeEditorConfirmed: () -> Unit,
    onEditAppTime: (String) -> Unit,
    onToggleLimit: (String) -> Unit,
    onAppDescriptionChanged: (String) -> Unit,
    onAppGoalEditorDismissed: () -> Unit,
    onAppGoalSaved: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showRestrictionTooltip by rememberSaveable { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(PrefScreenDefaults.restrictionTooltipDurationMillis)
        showRestrictionTooltip = false
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = PhoneShimTheme.colors.background,
            topBar = { PrefTopBar(onBack = onBack) },
        ) { innerPadding ->
            when {
                uiState.isLoading -> Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = PhoneShimTheme.colors.brand)
                }
                !uiState.hasGoalData -> Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = uiState.errorMessage ?: "목표 설정을 불러오지 못했습니다.",
                        style = PhoneShimType.KorCaption,
                        color = PhoneShimTheme.colors.textSecondary,
                    )
                }
                else -> PrefContent(
                    uiState = uiState,
                    contentPadding = innerPadding,
                    onGenderClick = onGenderClick,
                    onAgeGroupClick = onAgeGroupClick,
                    onGenderSelected = onGenderSelected,
                    onAgeGroupSelected = onAgeGroupSelected,
                    onSelectionDismissed = onSelectionDismissed,
                    onTotalGoalClick = onTotalGoalClick,
                    onEditAppTime = onEditAppTime,
                    onToggleLimit = {
                        showRestrictionTooltip = false
                        onToggleLimit(it)
                    },
                    showRestrictionTooltip = showRestrictionTooltip,
                )
            }
        }
        if (uiState.hasGoalData && uiState.hasUnsavedChanges) {
            PrefActionButtons(
                onCancel = onCancel,
                onSave = onSave,
                isSaving = uiState.isSaving,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(
                        start = PhoneShimDimens.screenHorizontalPadding,
                        end = PhoneShimDimens.screenHorizontalPadding,
                        bottom = BottomBarDefaults.ContentBottomPadding +
                            PrefScreenDefaults.actionToBottomBarSpacing,
                    ),
            )
        }
        BottomBar(
            selectedTab = selectedBottomTab,
            onTabSelected = onBottomNavSelected,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }

    uiState.timeEditor?.let { editor ->
        GoalTimeDialog(
            title = when (val target = editor.target) {
                TimeEditTarget.TotalGoal -> "하루 폰 목표 사용 시간"
                is TimeEditTarget.AppGoal -> {
                    val appName = uiState.draftSettings.appGoals
                        .firstOrNull { it.id == target.appId }
                        ?.appName
                        ?: "앱"
                    "$appName 목표 사용 시간"
                }
            },
            state = editor,
            onHoursChanged = onHoursChanged,
            onMinutesChanged = onMinutesChanged,
            onLimitToggled = onTimeEditorLimitToggled,
            onDismiss = onTimeEditorDismissed,
            onConfirm = onTimeEditorConfirmed,
        )
    }

    if (uiState.editingAppId != null) {
        AppGoalDescriptionDialog(
            description = uiState.appDescriptionInput,
            onDescriptionChanged = onAppDescriptionChanged,
            onDismiss = onAppGoalEditorDismissed,
            onSave = onAppGoalSaved,
        )
    }
}

@Composable
private fun PrefTopBar(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(PrefScreenDefaults.topBarHeight)
            .background(PhoneShimTheme.colors.background),
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = PrefScreenDefaults.topBarStartPadding),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_back),
                contentDescription = "뒤로가기",
                tint = PhoneShimTheme.colors.textPrimary,
            )
        }
        Text(
            text = "설정",
            style = MaterialTheme.typography.bodyLarge,
            color = PhoneShimTheme.colors.textPrimary,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

@Composable
private fun PrefContent(
    uiState: PrefUiState,
    contentPadding: PaddingValues,
    onGenderClick: () -> Unit,
    onAgeGroupClick: () -> Unit,
    onGenderSelected: (Gender) -> Unit,
    onAgeGroupSelected: (AgeGroup) -> Unit,
    onSelectionDismissed: () -> Unit,
    onTotalGoalClick: () -> Unit,
    onEditAppTime: (String) -> Unit,
    onToggleLimit: (String) -> Unit,
    showRestrictionTooltip: Boolean,
) {
    val actionButtonsReservedSpace = if (uiState.hasUnsavedChanges) {
        PhoneShimButtonSize.Medium.height + PrefScreenDefaults.actionToBottomBarSpacing
    } else {
        0.dp
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(
                start = PhoneShimDimens.screenHorizontalPadding,
                end = PhoneShimDimens.screenHorizontalPadding,
                bottom = PrefScreenDefaults.contentBottomPadding +
                    BottomBarDefaults.ContentBottomPadding +
                    actionButtonsReservedSpace,
            ),
    ) {
        uiState.errorMessage?.let { message ->
            Text(
                text = message,
                style = PhoneShimType.KorCaption,
                color = PhoneShimTheme.colors.error,
                modifier = Modifier.padding(bottom = PhoneShimDimens.spacing8),
            )
        }
        PrefUserInfoSection(
            gender = uiState.draftSettings.gender,
            ageGroup = uiState.draftSettings.ageGroup,
            selectionPopup = uiState.selectionPopup,
            onGenderClick = onGenderClick,
            onAgeGroupClick = onAgeGroupClick,
            onGenderSelected = onGenderSelected,
            onAgeGroupSelected = onAgeGroupSelected,
            onDismissPopup = onSelectionDismissed,
        )
        Spacer(Modifier.height(PrefScreenDefaults.userToGoalSpacing))
        PrefGoalSection(
            totalGoalMinutes = uiState.draftSettings.totalGoalMinutes,
            appGoals = uiState.draftSettings.appGoals,
            validation = uiState.validation,
            onTotalGoalClick = onTotalGoalClick,
            onEditAppTime = onEditAppTime,
            onToggleLimit = onToggleLimit,
            showRestrictionTooltip = showRestrictionTooltip,
        )
    }
}

@Composable
private fun PrefActionButtons(
    onCancel: () -> Unit,
    onSave: () -> Unit,
    isSaving: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing8),
    ) {
        SecondaryButton(
            text = "취소",
            onClick = onCancel,
            modifier = Modifier.weight(1f),
            size = PhoneShimButtonSize.Medium,
            fullWidth = false,
            labelStyle = PhoneShimType.KorCaption,
            enabled = !isSaving,
        )
        PrimaryButton(
            text = if (isSaving) "저장 중" else "확인",
            onClick = onSave,
            modifier = Modifier.weight(1f),
            size = PhoneShimButtonSize.Medium,
            fullWidth = false,
            labelStyle = PhoneShimType.KorCaption,
            enabled = !isSaving,
        )
    }
}

private val noOp: () -> Unit = {}
private val noOpString: (String) -> Unit = {}
private val noOpGender: (Gender) -> Unit = {}
private val noOpAgeGroup: (AgeGroup) -> Unit = {}

@Composable
private fun PreviewPrefScreen(uiState: PrefUiState) {
    PhoneShimTheme {
        PrefScreen(
            uiState = uiState.copy(isLoading = false, hasGoalData = true),
            selectedBottomTab = BottomBarTab.MAIN,
            onBottomNavSelected = {},
            onBack = noOp,
            onCancel = noOp,
            onSave = noOp,
            onGenderClick = noOp,
            onAgeGroupClick = noOp,
            onGenderSelected = noOpGender,
            onAgeGroupSelected = noOpAgeGroup,
            onSelectionDismissed = noOp,
            onTotalGoalClick = noOp,
            onHoursChanged = noOpString,
            onMinutesChanged = noOpString,
            onTimeEditorLimitToggled = noOp,
            onTimeEditorDismissed = noOp,
            onTimeEditorConfirmed = noOp,
            onEditAppTime = noOpString,
            onToggleLimit = noOpString,
            onAppDescriptionChanged = noOpString,
            onAppGoalEditorDismissed = noOp,
            onAppGoalSaved = noOp,
        )
    }
}

@Preview(name = "PREF - 기본 화면", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun PrefDefaultPreview() = PreviewPrefScreen(PrefUiState())

@Preview(name = "PREF - 성별 선택", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun PrefGenderSelectionPreview() = PreviewPrefScreen(
    PrefUiState(selectionPopup = SelectionPopup.GENDER),
)

@Preview(name = "PREF - 연령대 선택", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun PrefAgeSelectionPreview() = PreviewPrefScreen(
    PrefUiState(selectionPopup = SelectionPopup.AGE_GROUP),
)

@Preview(name = "PREF - 전체 목표 시간 수정", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun PrefTotalTimeEditorPreview() = PreviewPrefScreen(
    PrefUiState(
        timeEditor = TimeEditorState(
            target = TimeEditTarget.TotalGoal,
            hoursInput = "3",
            minutesInput = "30",
        ),
    ),
)

@Preview(name = "PREF - 앱별 목표 시간 수정", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun PrefAppTimeEditorPreview() = PreviewPrefScreen(
    PrefUiState(
        timeEditor = TimeEditorState(
            target = TimeEditTarget.AppGoal("facebook"),
            hoursInput = "1",
            minutesInput = "30",
        ),
    ),
)

@Preview(name = "PREF - 앱 목표 문구 작성", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun PrefAppGoalEditorPreview() = PreviewPrefScreen(
    PrefUiState(
        editingAppId = "kakao",
        appDescriptionInput = PrefMockData.DEFAULT_GOAL_DESCRIPTION,
    ),
)

@Preview(name = "PREF - 전체 앱 제한 활성화", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun PrefAllLimitsEnabledPreview() = PreviewPrefScreen(PrefUiState())

@Preview(name = "PREF - 앱 제한 비활성화", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun PrefLimitDisabledPreview() {
    val settings = PrefMockData.initialSettings.copy(
        appGoals = PrefMockData.initialSettings.appGoals.map { goal ->
            if (goal.id == "kakao") goal.copy(isLimitEnabled = false) else goal
        },
    )
    PreviewPrefScreen(PrefUiState(savedSettings = settings, draftSettings = settings))
}

@Preview(name = "PREF - 유효성 검사 오류", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun PrefValidationErrorPreview() {
    val invalidSettings = PrefMockData.initialSettings.copy(
        totalGoalMinutes = 5,
        appGoals = PrefMockData.initialSettings.appGoals.map { goal ->
            if (goal.id == "facebook") goal.copy(goalMinutes = 5) else goal
        },
    )
    PreviewPrefScreen(
        PrefUiState(
            draftSettings = invalidSettings,
            validation = PrefValidationResult(
                isValid = false,
                isTotalGoalInvalid = true,
                invalidAppGoalIds = setOf("facebook"),
            ),
        ),
    )
}
