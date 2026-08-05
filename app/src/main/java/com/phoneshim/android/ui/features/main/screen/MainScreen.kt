package com.phoneshim.android.ui.features.main.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.phoneshim.android.R
import com.phoneshim.android.domain.model.DashboardSummary
import com.phoneshim.android.domain.model.UsageStatus
import com.phoneshim.android.ui.common.BottomBar
import com.phoneshim.android.ui.common.BottomBarTab
import com.phoneshim.android.ui.common.BottomBarDefaults
import com.phoneshim.android.ui.common.TopAppBar
import com.phoneshim.android.ui.common.DurationDisplay
import com.phoneshim.android.ui.common.TodoRow
import com.phoneshim.android.ui.common.TodoRowVariant
import com.phoneshim.android.ui.common.SectionHeader
import com.phoneshim.android.ui.features.main.viewmodel.MainViewModel
import com.phoneshim.android.ui.features.setgoal.component.AppIcon
import com.phoneshim.android.ui.theme.PhoneShimPalette
import androidx.compose.material3.Text
import com.phoneshim.android.ui.theme.PhoneShimType
import com.phoneshim.android.ui.theme.PhoneShimTheme

/* ============================================================
 * 1. DESIGN TOKENS - COLORS
 * ============================================================ */
// TODO: 클론 디자인 시스템(PhoneShimColors)에 #FAF7F0(로컬 프로젝트의 BackgroundCream)에
// 대응하는 시맨틱 토큰이 없습니다(가장 가까운 PhoneShimTheme.colors.background는 #FFFDF7).
// 디자인 시스템 담당자 확인 후 Color.kt/Theme.kt에 정식 토큰으로 추가되면 이 로컬 상수는 제거하세요.
private val BackgroundCream = Color(0xFFFAF7F0)

// 섹션 타이틀: 피그마 시안 기준 KorBodyM 크기 + SemiBold 웨이트
private val SectionTitleStyle = PhoneShimType.KorBodyM.copy(fontWeight = FontWeight.SemiBold)

/* ============================================================
 * 2. DATA MODEL
 * ============================================================ */
data class MainCautionAppItem(
    val packageName: String,
    val usedTime: String,
    val progress: Float,
    val entryCount: String
)

// 리마인더 도메인(타로 담당, 이번 범위 밖) 더미 카드 표시용. 건드리지 않음.
data class MainTodoItem(
    val title: String,
    val timeRange: String
)

/* ============================================================
 * 3. FORMAT / MAPPING HELPERS
 * ============================================================ */
// "Xh Ym" 형태로 분 단위 시간을 표시. 남은 시간 캡션과 주의 앱 사용 시간에서 공용으로 사용.
private fun formatDuration(totalMinutes: Int): String {
    val safeMinutes = totalMinutes.coerceAtLeast(0)
    val hours = safeMinutes / 60
    val minutes = safeMinutes % 60
    return "${hours}h ${minutes}m"
}

// targetMinutes가 없거나 0 이하면 진행률 없음(0f)으로 처리.
private fun calculateProgress(usedMinutes: Int, targetMinutes: Int?): Float =
    if (targetMinutes != null && targetMinutes > 0) {
        (usedMinutes.toFloat() / targetMinutes).coerceIn(0f, 1f)
    } else {
        0f
    }

private fun UsageStatus.toCautionAppItem(): MainCautionAppItem = MainCautionAppItem(
    packageName = packageName,
    usedTime = formatDuration(usedMinutes),
    progress = calculateProgress(usedMinutes, targetMinutes),
    entryCount = "${entryCount}회",
)

/* ============================================================
 * 4. SCREEN
 * ============================================================ */
@Composable
fun MainScreen(
    onNavigateToSetGoal: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToMyPage: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToReminder: () -> Unit = {},
    onNavigateToReport: () -> Unit = {},
    viewModel: MainViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    // TODO: 실제 사용자 이름 연동 필요. Auth/MyPage 도메인 담당이라 이번 범위(UsageLog/Dashboard/
    // DeviceUsage) 밖이라 임시 고정값을 씁니다.
    val userName = "유리"

    // 리마인더 도메인(타로 담당, 이번 범위 밖) 더미 데이터. 손대지 않음.
    val todayTodos = remember {
        List(4) { MainTodoItem("과제하기", "10:00 ~ 11:00") }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundCream)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // NOTE: 피그마엔 KOR/H3(Pretendard)로 찍혀 있으나 영문이므로 디자인 시스템 규칙(ENG=Inter)을 따름.
            TopAppBar(
                title = "MAIN",
                titleStyle = PhoneShimType.EngH3,
                leadingAction = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_topbar_goal),
                            contentDescription = "설정",
                            tint = PhoneShimTheme.colors.textPrimary,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                },
                trailingAction = {
                    IconButton(onClick = onNavigateToMyPage) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_my),
                            contentDescription = "마이페이지",
                            tint = PhoneShimTheme.colors.textPrimary,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = BottomBarDefaults.ContentBottomPadding,
                ),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                if (!state.isGoalSet) {
                    item { GreetingCard(userName = userName, isSetupCompleted = state.isGoalSet) }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            SectionTitle(title = "하루 사용 시간")
                            EmptySetupCard(onSettingsClick = onNavigateToSetGoal)
                        }
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            SectionTitle(title = "오늘 할 일")
                            EmptySetupCard(onSettingsClick = onNavigateToSetGoal)
                        }
                    }
                } else {
                    item { GreetingCard(userName = userName, isSetupCompleted = state.isGoalSet) }
                    item { DailyUsageSection(dashboardSummary = state.dashboardSummary) }
                    item {
                        CautionAppSection(apps = state.usageStatus.map { it.toCautionAppItem() })
                    }
                    item { TodoSection(todos = todayTodos) }
                }
            }
        }

        BottomBar(
            selectedTab = BottomBarTab.MAIN,
            onTabSelected = { tab ->
                when (tab) {
                    BottomBarTab.MAIN -> Unit
                    BottomBarTab.REMINDER -> onNavigateToReminder()
                    BottomBarTab.REPORT -> onNavigateToReport()
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

/* ============================================================
 * 5. SECTION TITLE (▶ 8x10 + SectionTitle style)
 * ============================================================ */
@Composable
fun SectionTitle(title: String) {
    SectionHeader(title = title, titleStyle = SectionTitleStyle)
}

/* ============================================================
 * 6. GREETING CARD
 * ============================================================ */
@Composable
private fun GreetingCard(userName: String, isSetupCompleted: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PhoneShimTheme.colors.brandSubtle)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "${userName}님, 오늘 하루 힘차게 시작 해봐요!",
                style = PhoneShimType.KorBodyM,
                color = PhoneShimTheme.colors.brandStrong
            )
            Text(
                text = if (isSetupCompleted) {
                    "오늘도 좋은 습관 만들어봐요!"
                } else {
                    "아직 초기 설정이 완료되지 않았어요!"
                },
                style = PhoneShimType.KorH2,
                color = PhoneShimTheme.colors.textPrimary
            )
        }

        // phoneshim_mascot.png 원본 156x164px 비율(약 0.95:1) 유지
        Image(
            painter = painterResource(id = R.drawable.phoneshim_mascot),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .height(72.dp)
                .aspectRatio(156f / 164f)
        )
    }
}

/* ============================================================
 * 6-1. 초기 설정 전 빈 상태 카드
 * ============================================================ */
@Composable
fun EmptySetupCard(onSettingsClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 170.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(PhoneShimTheme.colors.surface)
            // TODO: 클론 PhoneShimColors엔 Primary300(#DCE7D4) 시맨틱 별칭이 없어
            // PhoneShimPalette.Primary300을 직접 참조합니다. border=Gray300과는 다른 색이니
            // 디자인 시스템에 별칭 추가를 검토해주세요.
            .border(1.dp, PhoneShimPalette.Primary300, RoundedCornerShape(12.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
        ) {
            // TODO: 피그마에 실제 들어갈 아이콘/일러스트 에셋 확인 필요 (현재 빈 박스로 표시)
            Box(modifier = Modifier.size(72.dp))

        Text(
                text = "아직 설정되지 않았어요",
                style = PhoneShimType.KorCaption,
                // NOTE: 피그마 순정값 #000. 디자인 시스템 textPrimary(#262626)와 다른 값이라
                // 디자이너 확인 필요, 확인 전까지 피그마 값 그대로 반영
                color = Color(0xFF000000)
            )

            Box(
                modifier = Modifier
                    .width(156.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(PhoneShimTheme.colors.divider)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onSettingsClick
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "설정하러 가기",
                    style = PhoneShimType.KorLabel,
                    color = PhoneShimTheme.colors.textSecondary
                )
            }
        }
    }
}

/* ============================================================
 * 7. 하루 사용 시간
 * ============================================================ */
@Composable
private fun DailyUsageSection(dashboardSummary: DashboardSummary?) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle(title = "하루 사용 시간")

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(PhoneShimTheme.colors.surface)
                .border(1.dp, PhoneShimPalette.Primary300, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            // dashboardSummary가 아직 없으면(로딩/실패) 전부 0으로 안전하게 폴백.
            val usedMinutes = dashboardSummary?.usedMinutes ?: 0
            val targetMinutes = dashboardSummary?.targetMinutes
            val remainingMinutes = dashboardSummary?.remainingMinutes ?: 0
            val totalTimeProgress = calculateProgress(usedMinutes, targetMinutes)

            DurationDisplay(
                totalMinutes = usedMinutes,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "남은 시간", style = PhoneShimType.KorCaption.copy(fontWeight = FontWeight.Medium), color = PhoneShimTheme.colors.textTertiary)
                Text(text = formatDuration(remainingMinutes), style = PhoneShimType.EngCaption.copy(fontWeight = FontWeight.Medium), color = PhoneShimTheme.colors.textSecondary)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(PhoneShimTheme.colors.brandSubtle)
            ) {
                // 실제 채워짐 표시 (시각적 진행률, 정확한 값 그대로 유지)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(totalTimeProgress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(100.dp))
                        .background(PhoneShimTheme.colors.brandStrong)
                )

                // 퍼센트 라벨: 채워진 영역의 오른쪽 끝을 따라가되, 96%로 상한을 둬서
                // 100%에 가까워져도 둥근 모서리에 끼지 않도록 함
                Box(
                    modifier = Modifier
                        .fillMaxWidth(totalTimeProgress.coerceAtMost(0.96f))
                        .fillMaxHeight()
                        .padding(horizontal = 6.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = "${(totalTimeProgress * 100).toInt()}%",
                        style = PhoneShimType.EngLabel,
                        color = PhoneShimTheme.colors.surface
                    )
                }
            }
        }
    }
}

/* ============================================================
 * 8. 주의 어플 사용 시간
 *  - 바깥 Small Card 1개 + 내부 가로 스크롤 Row
 *  - 아이템 사이 1dp x 72dp Divider
 * ============================================================ */
@Composable
private fun CautionAppSection(apps: List<MainCautionAppItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle(title = "주의 어플 사용 시간")

        // 바깥 Small Card 는 고정, 내부만 가로로 슬라이드
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))   // 스크롤 콘텐츠가 라운드 밖으로 삐져나오지 않도록 clip
                .background(PhoneShimTheme.colors.surface)
                .border(1.dp, PhoneShimPalette.Primary300, RoundedCornerShape(12.dp))
        ) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(12.dp),   // Small Card Base padding: 12px
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                itemsIndexed(apps) { index, app ->
                    // 아이템 + 구분선을 한 item 으로 묶어야 spacedBy(12) 가 양쪽에 균등하게 걸림
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CautionAppItem(app = app)
                        if (index < apps.lastIndex) {
                            Box(
                                modifier = Modifier
                                    // 피그마 스펙은 0.5px hairline이지만, Compose에서 서브픽셀 폭은
                                    // 기기별 안티앨리어싱 편차로 진하기가 들쭉날쭉하거나 아예 안 보여 1dp로 조정.
                                    .width(1.dp)
                                    .height(72.dp)
                                    .background(PhoneShimTheme.colors.divider)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CautionAppItem(app: MainCautionAppItem) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AppIcon(packageName = app.packageName, size = 40.dp)

        Text(
            text = app.usedTime,
            style = PhoneShimType.EngCaption.copy(fontWeight = FontWeight.Medium),
            color = PhoneShimTheme.colors.textSecondary
        )

        Row(
            modifier = Modifier.padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Badge(
                text = "${(app.progress * 100).toInt()}%",
                backgroundColor = PhoneShimTheme.colors.brandSubtle,
                textColor = PhoneShimTheme.colors.brandStrong
            )
            Badge(
                text = app.entryCount,
                backgroundColor = BackgroundCream,
                textColor = PhoneShimTheme.colors.textSecondary
            )
        }
    }
}

@Composable
private fun Badge(text: String, backgroundColor: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, style = PhoneShimType.KorMicro, color = textColor)
    }
}

/* ============================================================
 * 9. 오늘 할 일
 *  - 피그마 기준: [≡ 핸들] + [제목 / 시간 세로 스택]
 * ============================================================ */
@Composable
private fun TodoSection(todos: List<MainTodoItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle(title = "오늘 할 일")

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            todos.forEach { todo ->
                TodoCard(todo = todo)
            }
        }
    }
}

// TODO: Main은 TodoRowVariant.Card(카드형, 배경+테두리)를 쓰고 Reminder는
// TodoRowVariant.Plain + 수정 아이콘 trailingContent를 쓴다. 두 화면이 서로 다른
// 스타일인데 어느 쪽이 최신 피그마 스펙에 맞는지 디자이너 확인 필요.
@Composable
private fun TodoCard(todo: MainTodoItem) {
    TodoRow(
        title = todo.title,
        timeRange = todo.timeRange,
        modifier = Modifier.fillMaxWidth(),
        variant = TodoRowVariant.Card,
        leadingContent = {
            Icon(
                painter = painterResource(R.drawable.ic_reminder_drag_handle),
                contentDescription = null,
                tint = PhoneShimTheme.colors.brandStrong,
                modifier = Modifier.size(20.dp),
            )
        },
    )
}
