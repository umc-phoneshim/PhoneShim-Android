package com.phoneshim.android.ui.features.mypage.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.phoneshim.android.domain.model.User
import com.phoneshim.android.domain.model.UserStatus
import com.phoneshim.android.ui.common.BottomBar
import com.phoneshim.android.ui.common.BottomBarTab
import com.phoneshim.android.ui.common.BottomBarDefaults
import com.phoneshim.android.ui.common.PhoneShimIcon
import com.phoneshim.android.ui.common.PhoneShimIconType
import com.phoneshim.android.ui.features.mypage.viewmodel.MyPageUiEffect
import com.phoneshim.android.ui.features.mypage.viewmodel.MyPageUiEvent
import com.phoneshim.android.ui.features.mypage.viewmodel.MyPageUiState
import com.phoneshim.android.ui.features.mypage.viewmodel.MyPageViewModel
import com.phoneshim.android.ui.theme.PhoneShimDimens
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimType

/**
 * 08. 마이페이지 진입점. ViewModel 을 주입받아 상태 수집과 이펙트 처리를 담당하고,
 * 실제 UI 는 상태를 인자로만 받는 [MyScreen] 이 그립니다.
 */
@Composable
fun MyRoute(
    onNavigateToSideMenu: () -> Unit,
    modifier: Modifier = Modifier,
    selectedBottomTab: BottomBarTab = BottomBarTab.REPORT,
    onNavigateToMain: () -> Unit = {},
    onNavigateToReminder: () -> Unit = {},
    onNavigateToReport: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    viewModel: MyPageViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.onEvent(MyPageUiEvent.ScreenEntered)
    }
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                MyPageUiEffect.NavigateToSideMenu -> onNavigateToSideMenu()
                MyPageUiEffect.NavigateToLogin -> onNavigateToLogin()
                is MyPageUiEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
                // 마이페이지 본체에서는 발생하지 않는 이펙트입니다. (사이드 메뉴 전용)
                MyPageUiEffect.NavigateToWithdraw -> Unit
                MyPageUiEffect.OpenContactSupport -> Unit
            }
        }
    }

    MyScreen(
        state = state,
        modifier = modifier,
        snackbarHostState = snackbarHostState,
        selectedBottomTab = selectedBottomTab,
        onMenuClick = { viewModel.onEvent(MyPageUiEvent.SideMenuClicked) },
        onEditClick = { viewModel.onEvent(MyPageUiEvent.EditClicked) },
        onEditCancel = { viewModel.onEvent(MyPageUiEvent.EditCancelled) },
        onNameChange = { viewModel.onEvent(MyPageUiEvent.NameChanged(it)) },
        onMotivationChange = { viewModel.onEvent(MyPageUiEvent.MotivationChanged(it)) },
        onSaveClick = { viewModel.onEvent(MyPageUiEvent.SaveClicked) },
        onBottomNavSelected = { tab ->
            when (tab) {
                BottomBarTab.MAIN -> onNavigateToMain()
                BottomBarTab.REMINDER -> onNavigateToReminder()
                BottomBarTab.REPORT -> onNavigateToReport()
            }
        },
    )
}

@Composable
fun MyScreen(
    state: MyPageUiState,
    onMenuClick: () -> Unit,
    onBottomNavSelected: (BottomBarTab) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    selectedBottomTab: BottomBarTab = BottomBarTab.REPORT,
    onEditClick: () -> Unit = {},
    onEditCancel: () -> Unit = {},
    onNameChange: (String) -> Unit = {},
    onMotivationChange: (String) -> Unit = {},
    onSaveClick: () -> Unit = {},
) {
    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = PhoneShimTheme.colors.background,
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = BottomBarDefaults.ContentBottomPadding),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = PhoneShimDimens.screenHorizontalPadding,
                            vertical = PhoneShimDimens.spacing12,
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "MY",
                        style = PhoneShimType.KorH3,
                        color = PhoneShimTheme.colors.textPrimary,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = if (state.isEditing) "취소" else "수정",
                        style = PhoneShimType.KorBodyM,
                        color = PhoneShimTheme.colors.brandStrong,
                        modifier = Modifier
                            .clickable(onClick = if (state.isEditing) onEditCancel else onEditClick)
                            .padding(horizontal = PhoneShimDimens.spacing8),
                    )
                    PhoneShimIcon(
                        type = PhoneShimIconType.ChevronRight,
                        contentDescription = "사이드 메뉴",
                        modifier = Modifier.clickable(onClick = onMenuClick),
                    )
                }

                state.withdrawalNoticeText?.let { notice ->
                    WithdrawalNotice(text = notice)
                }

                Spacer(modifier = Modifier.height(PhoneShimDimens.spacing24))

                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .background(PhoneShimTheme.colors.brand, CircleShape),
                    )
                }

                Spacer(modifier = Modifier.height(PhoneShimDimens.spacing32))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = PhoneShimDimens.screenHorizontalPadding),
                    verticalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing24),
                ) {
                    // TODO: state.isLoading 구간엔 공통 LoadingIndicator 를 노출하세요.
                    MyInfoField(
                        label = "이름",
                        value = if (state.isEditing) state.nameDraft else state.name,
                        editable = state.isEditing,
                        onValueChange = onNameChange,
                        errorText = state.nameError,
                    )
                    MyInfoField(label = "이메일", value = state.email)
                    MyInfoField(
                        label = "다짐 문구",
                        value = if (state.isEditing) state.motivationDraft else state.motivation,
                        editable = state.isEditing,
                        onValueChange = onMotivationChange,
                        errorText = state.motivationError,
                        minHeight = 88.dp,
                    )
                    MyGoalField()

                    if (state.isEditing) {
                        SaveButton(enabled = state.canSave, isSaving = state.isSaving, onClick = onSaveClick)
                    }
                }

                Spacer(modifier = Modifier.height(PhoneShimDimens.spacing24))
            }
        }
        BottomBar(
            selectedTab = selectedBottomTab,
            onTabSelected = onBottomNavSelected,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun WithdrawalNotice(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = PhoneShimDimens.screenHorizontalPadding)
            .background(PhoneShimTheme.colors.brandSubtle, RoundedCornerShape(12.dp))
            .padding(PhoneShimDimens.spacing12),
    ) {
        Text(text = text, style = PhoneShimType.KorCaption, color = PhoneShimTheme.colors.error)
    }
}

@Composable
private fun MyInfoField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    editable: Boolean = false,
    onValueChange: (String) -> Unit = {},
    errorText: String? = null,
    minHeight: Dp = PhoneShimDimens.textFieldHeight,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = label, style = PhoneShimType.KorBodyM, color = PhoneShimTheme.colors.textSecondary)
        Spacer(modifier = Modifier.height(PhoneShimDimens.spacing8))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = minHeight)
                .background(PhoneShimTheme.colors.surfaceCream, RoundedCornerShape(12.dp))
                .padding(horizontal = PhoneShimDimens.spacing16, vertical = PhoneShimDimens.spacing12),
            contentAlignment = Alignment.CenterStart,
        ) {
            if (editable) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = PhoneShimType.KorBodyM.copy(color = PhoneShimTheme.colors.textPrimary),
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                Text(
                    text = value,
                    style = PhoneShimType.KorBodyM,
                    color = PhoneShimTheme.colors.textPrimary,
                )
            }
        }
        if (errorText != null) {
            Spacer(modifier = Modifier.height(PhoneShimDimens.spacing4))
            Text(text = errorText, style = PhoneShimType.KorCaption, color = PhoneShimTheme.colors.error)
        }
    }
}

@Composable
private fun SaveButton(
    enabled: Boolean,
    isSaving: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(PhoneShimDimens.textFieldHeight)
            .background(
                color = if (enabled) PhoneShimTheme.colors.brand else PhoneShimTheme.colors.divider,
                shape = RoundedCornerShape(12.dp),
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (isSaving) "저장 중..." else "저장",
            style = PhoneShimType.KorBodyM,
            color = PhoneShimTheme.colors.onBrand,
        )
    }
}

@Composable
private fun MyGoalField(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = "목표", style = PhoneShimType.KorBodyM, color = PhoneShimTheme.colors.textSecondary)
        Spacer(modifier = Modifier.height(PhoneShimDimens.spacing8))
        // TODO: GetGoalUseCase 를 MyPageViewModel 에 주입해 실제 목표 요약으로 교체하세요.
        //  전체 목표 API(GET /api/total-goals)가 아직 서버 "예정" 상태입니다.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(PhoneShimTheme.colors.brandSubtle, RoundedCornerShape(12.dp)),
        )
    }
}

private fun previewUser(status: UserStatus = UserStatus.ACTIVE) = User(
    id = "1",
    email = "abcde123@gmail.com",
    name = "유리",
    motivation = "오늘은 필요한 앱만 보기",
    status = status,
)

@Preview(showBackground = true)
@Composable
private fun MyScreenPreview() {
    PhoneShimTheme {
        MyScreen(state = MyPageUiState(user = previewUser()), onMenuClick = {}, onBottomNavSelected = {})
    }
}

@Preview(name = "편집 모드", showBackground = true)
@Composable
private fun MyScreenEditingPreview() {
    PhoneShimTheme {
        MyScreen(
            state = MyPageUiState(
                user = previewUser(),
                isEditing = true,
                nameDraft = "유리",
                motivationDraft = "오늘은 필요한 앱만 보기",
            ),
            onMenuClick = {},
            onBottomNavSelected = {},
        )
    }
}

@Preview(name = "탈퇴 유예 상태", showBackground = true)
@Composable
private fun MyScreenWithdrawalPendingPreview() {
    PhoneShimTheme {
        MyScreen(
            state = MyPageUiState(user = previewUser(UserStatus.WITHDRAWAL_PENDING)),
            onMenuClick = {},
            onBottomNavSelected = {},
        )
    }
}
