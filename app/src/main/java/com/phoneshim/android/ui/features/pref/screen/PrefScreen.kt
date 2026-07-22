package com.phoneshim.android.ui.features.pref.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.phoneshim.android.R
import com.phoneshim.android.ui.common.BottomBar
import com.phoneshim.android.ui.common.BottomBarTab
import com.phoneshim.android.ui.common.BottomBarDefaults
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

private object PrefScreenDefaults {
    val topBarHeight = 56.dp
    val topBarStartPadding = 4.dp
    val userToGoalSpacing = 20.dp
    val contentBottomPadding = 32.dp
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
    onTimeEditorDismissed: () -> Unit,
    onTimeEditorConfirmed: () -> Unit,
    onEditAppTime: (String) -> Unit,
    onToggleLimit: (String) -> Unit,
    onEditAppGoal: (String) -> Unit,
    onAppDescriptionChanged: (String) -> Unit,
    onAppGoalEditorDismissed: () -> Unit,
    onAppGoalSaved: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = PhoneShimTheme.colors.background,
            topBar = { PrefTopBar(onBack = onBack) },
        ) { innerPadding ->
            PrefContent(
                uiState = uiState,
                contentPadding = innerPadding,
                onGenderClick = onGenderClick,
                onAgeGroupClick = onAgeGroupClick,
                onGenderSelected = onGenderSelected,
                onAgeGroupSelected = onAgeGroupSelected,
                onSelectionDismissed = onSelectionDismissed,
                onTotalGoalClick = onTotalGoalClick,
                onEditAppTime = onEditAppTime,
                onToggleLimit = onToggleLimit,
                onEditAppGoal = onEditAppGoal,
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
            state = editor,
            onHoursChanged = onHoursChanged,
            onMinutesChanged = onMinutesChanged,
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
    onEditAppGoal: (String) -> Unit,
) {
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
                    BottomBarDefaults.ContentBottomPadding,
            ),
    ) {
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
            onEditAppGoal = onEditAppGoal,
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
            uiState = uiState,
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
            onTimeEditorDismissed = noOp,
            onTimeEditorConfirmed = noOp,
            onEditAppTime = noOpString,
            onToggleLimit = noOpString,
            onEditAppGoal = noOpString,
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
