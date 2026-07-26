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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.phoneshim.android.domain.model.User
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
                // 마이페이지 본체에서는 발생하지 않는 이펙트입니다. (사이드 메뉴 전용)
                MyPageUiEffect.NavigateToWithdraw -> Unit
                MyPageUiEffect.OpenContactSupport -> Unit
                is MyPageUiEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    MyScreen(
        state = state,
        modifier = modifier,
        snackbarHostState = snackbarHostState,
        selectedBottomTab = selectedBottomTab,
        onMenuClick = { viewModel.onEvent(MyPageUiEvent.SideMenuClicked) },
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
                        .padding(horizontal = PhoneShimDimens.screenHorizontalPadding, vertical = PhoneShimDimens.spacing12),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "MY",
                        style = PhoneShimType.KorH3,
                        color = PhoneShimTheme.colors.textPrimary,
                        modifier = Modifier.weight(1f),
                    )
                    PhoneShimIcon(
                        type = PhoneShimIconType.ChevronRight,
                        contentDescription = "사이드 메뉴",
                        modifier = Modifier.clickable(onClick = onMenuClick),
                    )
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
                    // TODO: state.isLoading 구간엔 LoadingIndicator 를 노출하세요.
                    MyInfoField(label = "이름", value = state.nickname)
                    MyInfoField(label = "이메일", value = state.email)
                    MyGoalField()
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
private fun MyInfoField(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = label, style = PhoneShimType.KorBodyM, color = PhoneShimTheme.colors.textSecondary)
        Spacer(modifier = Modifier.height(PhoneShimDimens.spacing8))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(PhoneShimDimens.textFieldHeight)
                .background(PhoneShimTheme.colors.surfaceCream, RoundedCornerShape(12.dp))
                .padding(horizontal = PhoneShimDimens.spacing16),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(text = value, style = PhoneShimType.KorBodyM, color = PhoneShimTheme.colors.textPrimary)
        }
    }
}

@Composable
private fun MyGoalField(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = "목표", style = PhoneShimType.KorBodyM, color = PhoneShimTheme.colors.textSecondary)
        Spacer(modifier = Modifier.height(PhoneShimDimens.spacing8))
        // TODO: SetGoal 도메인의 GetGoal 유스케이스가 추가되면 MyPageUiState 에 목표 요약을 담아 교체하세요.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(PhoneShimTheme.colors.brandSubtle, RoundedCornerShape(12.dp)),
        )
    }
}

private fun previewState(user: User? = User(id = "1", email = "abcde123@gmail.com", nickname = "유리")) =
    MyPageUiState(user = user)

@Preview(showBackground = true)
@Composable
private fun MyScreenPreview() {
    PhoneShimTheme {
        MyScreen(state = previewState(), onMenuClick = {}, onBottomNavSelected = {})
    }
}

@Preview(name = "프로필 로딩 전", showBackground = true)
@Composable
private fun MyScreenEmptyPreview() {
    PhoneShimTheme {
        MyScreen(state = previewState(user = null), onMenuClick = {}, onBottomNavSelected = {})
    }
}
