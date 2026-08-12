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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import com.phoneshim.android.R
import com.phoneshim.android.blocking.detection.BlockingPermissions
import com.phoneshim.android.blocking.permission.rememberBlockingPermissionRequest
import com.phoneshim.android.domain.model.DashboardSummary
import com.phoneshim.android.domain.model.Reminder
import com.phoneshim.android.domain.model.UsageStatus
import com.phoneshim.android.ui.common.BottomBar
import com.phoneshim.android.ui.common.BottomBarTab
import com.phoneshim.android.ui.common.BottomBarDefaults
import com.phoneshim.android.ui.common.PhoneShimButtonSize
import com.phoneshim.android.ui.common.SecondaryButton
import com.phoneshim.android.ui.common.TopAppBar
import com.phoneshim.android.ui.common.DurationDisplay
import com.phoneshim.android.ui.common.TodoRow
import com.phoneshim.android.ui.common.TodoRowVariant
import com.phoneshim.android.ui.common.SectionHeader
import com.phoneshim.android.ui.features.main.viewmodel.MainUiEvent
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

// "OO시간 OO분" 형태(2자리 패딩)로 표시. "하루 사용 시간" 카드의 목표 총량 캡션("OO시간 OO분 중")에서 사용.
private fun formatKoreanDuration(totalMinutes: Int): String {
    val safeMinutes = totalMinutes.coerceAtLeast(0)
    val hours = safeMinutes / 60
    val minutes = safeMinutes % 60
    return String.format("%02d시간 %02d분", hours, minutes)
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

private val KOREA_ZONE_ID = ZoneId.of("Asia/Seoul")
private val TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm").withZone(KOREA_ZONE_ID)

private fun Reminder.toMainTodoItem(): MainTodoItem = MainTodoItem(
    title = title,
    timeRange = "${TIME_FORMATTER.format(startTime)} ~ ${TIME_FORMATTER.format(endTime)}",
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

    // usageStatus/dashboardSummary는 로컬 캐시가 없어 리마인더처럼 Flow로 바꿀 수 없다(#74 너울
    // 리뷰). 메인 탭을 벗어났다 돌아와도 MainViewModel은 살아있어 재조회가 안 되던 문제라,
    // 재진입(ON_RESUME)마다 다시 불러온다. "최초 진입인지" 판단은 여기(컴포저블 지역 변수)에
    // 두지 않는다 — 탭 전환으로 이 Composable 자체가 dispose/재생성되면 MainViewModel은
    // 살아있어도 플래그는 매번 리셋돼 오분류된다. 대신 MainViewModel.refreshUsageAndDashboard()의
    // isLoading 가드가 init 직후의 동기 catch-up ON_RESUME만 자연스럽게 무시한다.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onEvent(MainUiEvent.RefreshUsageAndDashboard)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // 앱 재설치 시 사용정보 접근/오버레이 권한이 초기화되는데, 지금 동의 UI는 온보딩에만
    // 있어 재설치 후 다시 요청할 경로가 없다는 문제 대응. 설정 화면 쪽 UI가 준비되기 전까지
    // 메인 화면에 최소 구현으로 배너를 둔다. isPreview는 SetGoalStartScreen.kt와 동일하게
    // ActivityResult 런처를 만들 수 없는 프리뷰에서 권한 요청 훅을 건너뛰기 위함.
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current
    var hasAllPermissions by remember(context, isPreview) {
        mutableStateOf(isPreview || BlockingPermissions.hasAll(context))
    }

    // 설정 화면 왕복 후 돌아왔을 때(ON_RESUME) 배너가 바로 갱신되도록 재확인한다.
    DisposableEffect(lifecycleOwner, context, isPreview) {
        if (isPreview) return@DisposableEffect onDispose {}
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasAllPermissions = BlockingPermissions.hasAll(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val permissionRequest = if (isPreview) {
        null
    } else {
        rememberBlockingPermissionRequest { hasAllPermissions = BlockingPermissions.hasAll(context) }
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
                    item { GreetingCard(userName = state.userName, isSetupCompleted = state.isGoalSet) }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            SectionTitle(title = "하루 사용 시간")
                            EmptySetupCard(
                                mascotRes = R.drawable.usage_time_character,
                                mascotContentDescription = "시계를 든 사용 시간 마스코트",
                                onSettingsClick = onNavigateToSetGoal,
                            )
                        }
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            SectionTitle(title = "오늘 할 일")
                            EmptySetupCard(
                                mascotRes = R.drawable.todo_character,
                                mascotContentDescription = "달력을 든 할 일 마스코트",
                                onSettingsClick = onNavigateToSetGoal,
                            )
                        }
                    }
                } else {
                    // 설정 완료 후 화면에서만 배너를 보여준다 — 설정 전(온보딩 흐름)에는 넣지 않는다.
                    if (!hasAllPermissions) {
                        item {
                            PermissionRecheckBanner(onRecheckClick = { permissionRequest?.launch() })
                        }
                    }
                    item { GreetingCard(userName = state.userName, isSetupCompleted = state.isGoalSet) }
                    item { DailyUsageSection(dashboardSummary = state.dashboardSummary) }
                    item {
                        CautionAppSection(apps = state.usageStatus.map { it.toCautionAppItem() })
                    }
                    item { TodoSection(todos = state.todayReminders.map { it.toMainTodoItem() }) }
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
    SectionHeader(
        title = title,
        titleStyle = SectionTitleStyle,
        leadingContent = {
            Icon(
                painter = painterResource(R.drawable.ic_disclosure_triangle),
                contentDescription = null,
                tint = PhoneShimTheme.colors.textPrimary,
                modifier = Modifier.size(16.dp),
            )
        },
    )
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
                    // v2: 설정 후 인사 배너 문구 확정값
                    "휴대폰 보는 시간을 조금이라도 줄이자!"
                } else {
                    "아직 초기 설정이 완료되지 않았어요!"
                },
                style = PhoneShimType.KorH2,
                color = PhoneShimTheme.colors.textPrimary
            )
        }

        // 설정 후(하트)와 설정 전(슬픔)은 같은 캐릭터의 다른 포즈 export라 원본 픽셀 비율이
        // 조금씩 다름 — welcome_character 80x75(mdpi), welcome_character_heart 79x72(mdpi).
        val (mascotRes, mascotAspectRatio, mascotDescription) = if (isSetupCompleted) {
            Triple(R.drawable.welcome_character_heart, 79f / 72f, "웃으며 손을 흔드는 마스코트")
        } else {
            Triple(R.drawable.welcome_character, 80f / 75f, "슬퍼하는 마스코트")
        }
        Image(
            painter = painterResource(id = mascotRes),
            contentDescription = mascotDescription,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .height(88.dp)
                .aspectRatio(mascotAspectRatio)
        )
    }
}

/* ============================================================
 * 6-1. 초기 설정 전 빈 상태 카드
 * ============================================================ */
@Composable
fun EmptySetupCard(
    mascotRes: Int,
    mascotContentDescription: String,
    onSettingsClick: () -> Unit,
) {
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
            // usage_time_character/todo_character 원본 export가 72x72(mdpi)라 72dp로 표시해야
            // 밀도별 비트맵의 실제 해상도와 맞아 확대 블러가 생기지 않음.
            Image(
                painter = painterResource(id = mascotRes),
                contentDescription = mascotContentDescription,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(72.dp),
            )

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
 * 6-2. 권한 재확인 배너
 *  - 앱 재설치로 특수 권한(사용정보 접근/오버레이)이 초기화됐을 때, 온보딩 밖에서
 *    다시 요청할 경로가 없어 설정 완료 후 메인 화면에 임시로 배치.
 *  - TODO: 디자인팀 설정 화면 UI 확정 전 임시 구현. 배너 스타일(테두리 색/버튼)은
 *    기존 카드·SecondaryButton을 그대로 재사용한 것이라 확정 디자인이 나오면
 *    디자이너 리뷰 후 교체 필요.
 * ============================================================ */
@Composable
private fun PermissionRecheckBanner(onRecheckClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PhoneShimTheme.colors.surface)
            .border(1.dp, PhoneShimTheme.colors.warning, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "일부 권한이 꺼져 있어 차단 기능이 동작하지 않아요",
            style = PhoneShimType.KorCaption,
            color = PhoneShimTheme.colors.textPrimary,
        )
        SecondaryButton(
            text = "권한 다시 설정하기",
            onClick = onRecheckClick,
            size = PhoneShimButtonSize.Small,
            fullWidth = false,
            accentColor = PhoneShimTheme.colors.warning,
            pressedAccentColor = PhoneShimTheme.colors.warning,
        )
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

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(100.dp))
                        .background(PhoneShimTheme.colors.brandSubtle),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_target_time),
                        contentDescription = null,
                        tint = PhoneShimTheme.colors.brandStrong,
                        modifier = Modifier.size(16.dp),
                    )
                }

                DurationDisplay(totalMinutes = usedMinutes)

                Text(
                    text = "사용",
                    style = PhoneShimType.KorBodyM,
                    color = PhoneShimTheme.colors.textPrimary,
                )

                Spacer(modifier = Modifier.weight(1f))

                // 목표 총량 — targetMinutes가 없으면(로딩/미설정) 표시 자체를 생략
                if (targetMinutes != null) {
                    Text(
                        text = "${formatKoreanDuration(targetMinutes)} 중",
                        style = PhoneShimType.KorLabel,
                        color = PhoneShimTheme.colors.textTertiary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(PhoneShimPalette.Primary300),
            )
            Spacer(modifier = Modifier.height(12.dp))

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

        // TODO: 오늘 리마인더가 0건이면 지금은 빈 Column만 나옴. 빈 상태 문구/일러스트가
        // 필요한지 타로/디자이너 확인 필요.
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

/* ============================================================
 * 10. PREVIEW
 *  - MainScreen 자체는 hiltViewModel()을 직접 물고 있어 미리보기가 안 되므로,
 *    isGoalSet 분기별 섹션 조합을 그대로 재현해서 마스코트/레이아웃만 확인.
 * ============================================================ */
@Preview(name = "초기 설정 전", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun MainScreenEmptyPreview() {
    PhoneShimTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundCream)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            GreetingCard(userName = "유리", isSetupCompleted = false)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle(title = "하루 사용 시간")
                EmptySetupCard(
                    mascotRes = R.drawable.usage_time_character,
                    mascotContentDescription = "시계를 든 사용 시간 마스코트",
                    onSettingsClick = {},
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle(title = "오늘 할 일")
                EmptySetupCard(
                    mascotRes = R.drawable.todo_character,
                    mascotContentDescription = "달력을 든 할 일 마스코트",
                    onSettingsClick = {},
                )
            }
        }
    }
}

@Preview(name = "초기 설정 후", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun MainScreenFilledPreview() {
    val previewSummary = DashboardSummary(
        date = "2026-08-12",
        targetMinutes = 210,
        usedMinutes = 90,
        remainingMinutes = 120,
        isExceeded = false,
    )
    val previewApps = listOf(
        UsageStatus(
            monitoredAppId = "1",
            appName = "유튜브",
            packageName = "com.google.android.youtube",
            targetMinutes = 90,
            usedMinutes = 30,
            entryCount = 3,
        ),
        UsageStatus(
            monitoredAppId = "2",
            appName = "인스타그램",
            packageName = "com.instagram.android",
            targetMinutes = 60,
            usedMinutes = 45,
            entryCount = 5,
        ),
    )

    PhoneShimTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundCream)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            GreetingCard(userName = "유리", isSetupCompleted = true)
            DailyUsageSection(dashboardSummary = previewSummary)
            CautionAppSection(apps = previewApps.map { it.toCautionAppItem() })
            TodoSection(todos = List(2) { MainTodoItem("과제하기", "10:00 ~ 11:00") })
        }
    }
}

@Preview(name = "권한 재확인 배너 (임시 구현)", widthDp = 360, showBackground = true)
@Composable
private fun PermissionRecheckBannerPreview() {
    PhoneShimTheme {
        PermissionRecheckBanner(onRecheckClick = {})
    }
}
