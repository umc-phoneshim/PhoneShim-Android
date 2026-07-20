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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.phoneshim.android.ui.common.BottomBar
import com.phoneshim.android.ui.common.BottomBarTab
import com.phoneshim.android.ui.common.BottomBarDefaults
import com.phoneshim.android.ui.common.PhoneShimIcon
import com.phoneshim.android.ui.common.PhoneShimIconType
import com.phoneshim.android.ui.features.mypage.viewmodel.MyPageViewModel
import com.phoneshim.android.ui.theme.PhoneShimDimens
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimType

/** 08. 마이페이지 본체. 상단 우측 아이콘으로 [MySideMenuScreen] 을 엽니다. */
@Composable
fun MyScreen(
    onNavigateToSideMenu: () -> Unit,
    modifier: Modifier = Modifier,
    selectedBottomTab: BottomBarTab = BottomBarTab.REPORT,
    onNavigateToMain: () -> Unit = {},
    onNavigateToReminder: () -> Unit = {},
    onNavigateToReport: () -> Unit = {},
    viewModel: MyPageViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    // TODO: uiState.user 가 null 인 초기 로딩 구간엔 LoadingIndicator 를 보여주고,
    //  "목표" 영역은 SetGoal 도메인 쪽 GetGoal 유스케이스가 추가되면 실제 목표 데이터로 채우세요.
    MyContent(
        modifier = modifier,
        nickname = uiState.user?.nickname.orEmpty(),
        email = uiState.user?.email.orEmpty(),
        onMenuClick = onNavigateToSideMenu,
        selectedBottomTab = selectedBottomTab,
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
private fun MyContent(
    nickname: String,
    email: String,
    onMenuClick: () -> Unit,
    onBottomNavSelected: (BottomBarTab) -> Unit,
    modifier: Modifier = Modifier,
    selectedBottomTab: BottomBarTab = BottomBarTab.REPORT,
) {
    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = PhoneShimTheme.colors.background,
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
                MyInfoField(label = "이름", value = nickname)
                MyInfoField(label = "이메일", value = email)
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
        // TODO: 설정된 목표 목록/요약으로 교체하세요.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(PhoneShimTheme.colors.brandSubtle, RoundedCornerShape(12.dp)),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MyContentPreview() {
    PhoneShimTheme {
        MyContent(
            nickname = "유리",
            email = "abcde123@gmail.com",
            onMenuClick = {},
            onBottomNavSelected = {},
        )
    }
}
