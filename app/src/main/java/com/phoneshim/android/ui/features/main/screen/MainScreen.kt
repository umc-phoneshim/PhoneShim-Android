package com.phoneshim.android.ui.features.main.screen

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.phoneshim.android.R
import com.phoneshim.android.ui.common.BottomBar
import com.phoneshim.android.ui.common.BottomBarTab
import com.phoneshim.android.ui.common.SectionHeader
import com.phoneshim.android.ui.common.TodoRow
import com.phoneshim.android.ui.common.TodoRowVariant
import com.phoneshim.android.ui.common.TopAppBar
import com.phoneshim.android.ui.features.main.viewmodel.MainViewModel
import com.phoneshim.android.ui.theme.PhoneShimPalette
import com.phoneshim.android.ui.theme.PhoneShimType
import com.phoneshim.android.ui.theme.PhoneShimTheme

/* ============================================================
 * 1. DESIGN TOKENS - COLORS
 * ============================================================ */
// 카카오 브랜드 컬러, 디자인 시스템 토큰 아님
private val KakaoYellow = Color(0xFFF7E600)

// TODO: 클론 디자인 시스템(PhoneShimColors)에 #FAF7F0(로컬 프로젝트의 BackgroundCream)에
// 대응하는 시맨틱 토큰이 없습니다(가장 가까운 PhoneShimTheme.colors.background는 #FFFDF7).
// 디자인 시스템 담당자 확인 후 Color.kt/Theme.kt에 정식 토큰으로 추가되면 이 로컬 상수는 제거하세요.
private val BackgroundCream = Color(0xFFFAF7F0)

// 섹션 타이틀: 피그마 시안 기준 KorBodyL 크기 + SemiBold 웨이트
private val SectionTitleStyle = PhoneShimType.KorBodyL.copy(fontWeight = FontWeight.SemiBold)

/* ============================================================
 * 2. SVG PATH DATA (Figma export 원본)
 * ============================================================ */
private const val PATH_KAKAO =
    "M8 14.0644C12.418 14.0644 16 10.916 16 7.03222C16 3.14842 12.418 0 8 0C3.582 0 0 3.14842 0 " +
            "7.03222C0 8.80031 0.743 10.4177 1.97 11.6534C1.873 12.6741 1.553 13.7932 1.199 14.633C1.12 " +
            "14.8199 1.273 15.0288 1.472 14.9967C3.728 14.625 5.069 14.0544 5.652 13.757C6.41777 13.9629 " +
            "7.20727 14.0663 8 14.0644Z"

private const val PATH_MENU_LINE =
    "M16.6667 15.0002C16.8791 15.0004 17.0834 15.0817 17.2378 15.2275C17.3923 15.3733 17.4852 15.5726 " +
            "17.4976 15.7847C17.5101 15.9967 17.4411 16.2055 17.3048 16.3684C17.1685 16.5312 16.9751 16.6359 " +
            "16.7642 16.661L16.6667 16.6668H3.33333C3.12093 16.6666 2.91664 16.5853 2.76219 16.4395C2.60775 " +
            "16.2936 2.5148 16.0944 2.50236 15.8823C2.48991 15.6703 2.5589 15.4615 2.69522 15.2986C2.83155 " +
            "15.1358 3.02492 15.0311 3.23583 15.006L3.33333 15.0002H16.6667ZM16.6667 9.16683C16.8877 9.16683 " +
            "17.0996 9.25463 17.2559 9.41091C17.4122 9.56719 17.5 9.77915 17.5 10.0002C17.5 10.2212 17.4122 " +
            "10.4331 17.2559 10.5894C17.0996 10.7457 16.8877 10.8335 16.6667 10.8335H3.33333C3.11232 10.8335 " +
            "2.90036 10.7457 2.74408 10.5894C2.5878 10.4331 2.5 10.2212 2.5 10.0002C2.5 9.77915 2.5878 " +
            "9.56719 2.74408 9.41091C2.90036 9.25463 3.11232 9.16683 3.33333 9.16683H16.6667ZM16.6667 " +
            "3.3335C16.8877 3.3335 17.0996 3.42129 17.2559 3.57757C17.4122 3.73385 17.5 3.94582 17.5 " +
            "4.16683C17.5 4.38784 17.4122 4.5998 17.2559 4.75608C17.0996 4.91237 16.8877 5.00016 16.6667 " +
            "5.00016H3.33333C3.11232 5.00016 2.90036 4.91237 2.74408 4.75608C2.5878 4.5998 2.5 4.38784 2.5 " +
            "4.16683C2.5 3.94582 2.5878 3.73385 2.74408 3.57757C2.90036 3.42129 3.11232 3.3335 3.33333 " +
            "3.3335H16.6667Z"

@Composable
private fun VectorPath(
    pathData: String,
    viewportWidth: Float,
    viewportHeight: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    val path = remember(pathData) { PathParser().parsePathString(pathData).toPath() }
    Canvas(modifier = modifier) {
        scale(
            scaleX = size.width / viewportWidth,
            scaleY = size.height / viewportHeight,
            pivot = Offset.Zero
        ) {
            drawPath(path = path, color = color)
        }
    }
}

/* ============================================================
 * 3. DATA MODEL
 *
 * TODO: 로컬 프로젝트에서 그대로 이식한 더미 UI 상태입니다. 실제로는
 * MainViewModel의 MainUiState(isGoalSet/todayUsage/isLoading)와 합쳐져야 하며,
 * userName/usedHour/usedMinute/remainingTime/totalTimeProgress/cautionApps/todayTodos를
 * todayUsage: List<AppUsage> 등 실제 도메인 모델로부터 매핑하는 작업이 필요합니다.
 * ============================================================ */
data class MainUiState(
    val userName: String = "유리",
    val usedHour: String = "01",
    val usedMinute: String = "30",
    val remainingTime: String = "01h 30m",
    val totalTimeProgress: Float = 0.5f,
    val isSetupCompleted: Boolean = true,
    // 가로 슬라이드 확인용 더미 6개 (임시 테스트 데이터, 추후 실제 API 데이터로 교체)
    val cautionApps: List<MainCautionAppItem> = List(6) {
        MainCautionAppItem("카카오톡", "1h 30m", 0.5f, "3회")
    },
    // 세로 슬라이드 확인용 더미 4개
    val todayTodos: List<MainTodoItem> = List(4) {
        MainTodoItem("과제하기", "10:00 ~ 11:00")
    }
)

data class MainCautionAppItem(
    val name: String,
    val usedTime: String,
    val progress: Float,
    val entryCount: String
)

data class MainTodoItem(
    val title: String,
    val timeRange: String
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
    // TODO: viewModel.uiState 연동 필요. 지금은 로컬 프로젝트에서 그대로 가져온
    // 더미 상태로 UI 쉘만 확인합니다 (viewModel은 아직 사용하지 않음).
    val uiState by remember { mutableStateOf(MainUiState()) }

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
                    Icon(
                        painter = painterResource(id = R.drawable.mynaui_target_solid),
                        contentDescription = "설정",
                        tint = PhoneShimTheme.colors.textPrimary,
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onNavigateToSettings
                        )
                    )
                },
                trailingAction = {
                    Icon(
                        painter = painterResource(id = R.drawable.my),
                        contentDescription = "마이페이지",
                        tint = PhoneShimTheme.colors.textPrimary,
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onNavigateToMyPage
                        )
                    )
                }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                if (!uiState.isSetupCompleted) {
                    GreetingCard(userName = uiState.userName, isSetupCompleted = uiState.isSetupCompleted)

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionTitle(title = "하루 사용 시간")
                        EmptySetupCard(onSettingsClick = onNavigateToSetGoal)
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionTitle(title = "오늘 할 일")
                        EmptySetupCard(onSettingsClick = onNavigateToSetGoal)
                    }
                } else {
                    GreetingCard(userName = uiState.userName, isSetupCompleted = uiState.isSetupCompleted)
                    DailyUsageSection(uiState = uiState)
                    CautionAppSection(apps = uiState.cautionApps)
                    TodoSection(todos = uiState.todayTodos)
                }

                // 플로팅 내비게이션 영역 확보 (bar 56 + bottom 24 + 여백 24)
                Spacer(modifier = Modifier.height(104.dp))
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
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 16.dp, end = 16.dp, bottom = 24.dp)
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
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "${userName}님, 오늘 하루 힘차게 시작 해봐요!",
                style = PhoneShimType.KorCaption,
                color = PhoneShimTheme.colors.brandStrong
            )
            Text(
                text = if (isSetupCompleted) {
                    "오늘도 좋은 습관 만들어\n봐요!"
                } else {
                    "아직 초기 설정이 완료되\n지 않았어요!"
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
private fun DailyUsageSection(uiState: MainUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle(title = "하루 사용 시간")

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(PhoneShimTheme.colors.surface)
                .border(1.dp, PhoneShimPalette.Primary300, RoundedCornerShape(12.dp))
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            // "01 시간 30 분" : 숫자 Display / 단위 BodyM, 베이스라인 정렬
            Row(verticalAlignment = Alignment.Bottom) {
                Text(text = uiState.usedHour, style = PhoneShimType.EngDisplay, color = PhoneShimTheme.colors.textPrimary)
                Text(
                    text = " 시간 ",
                    style = PhoneShimType.KorBodyM,
                    color = PhoneShimTheme.colors.textSecondary,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
                Text(text = uiState.usedMinute, style = PhoneShimType.EngDisplay, color = PhoneShimTheme.colors.textPrimary)
                Text(
                    text = " 분",
                    style = PhoneShimType.KorBodyM,
                    color = PhoneShimTheme.colors.textSecondary,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "남은 시간", style = PhoneShimType.KorLabel, color = PhoneShimTheme.colors.textTertiary)
                Text(text = uiState.remainingTime, style = PhoneShimType.EngLabel, color = PhoneShimTheme.colors.textSecondary)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(PhoneShimTheme.colors.brandSubtle)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(uiState.totalTimeProgress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(100.dp))
                        .background(PhoneShimTheme.colors.brandStrong)
                        .padding(horizontal = 6.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = "${(uiState.totalTimeProgress * 100).toInt()}%",
                        style = PhoneShimType.EngMicro,
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
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Icon Box : 40x40 / radius 100 / surfaceCream
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(PhoneShimTheme.colors.surfaceCream),
            contentAlignment = Alignment.Center
        ) {
            VectorPath(
                pathData = PATH_KAKAO,
                viewportWidth = 16f,
                viewportHeight = 15f,
                color = KakaoYellow,          // 카카오 말풍선은 KakaoYellow
                modifier = Modifier.size(width = 16.dp, height = 15.dp)
            )
        }

        Text(
            text = app.usedTime,
            style = PhoneShimType.EngLabel,
            color = PhoneShimTheme.colors.textSecondary
        )

        Row(
            modifier = Modifier.padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
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
// 할 일 카드 1개 높이 = 12(padding) + 22.4(title) + 2(gap) + 18(time) + 12(padding) ≈ 67dp
// 3개 + 여백까지 노출하고 그 이상은 목록 자체가 세로 슬라이드
private val TodoListMaxHeight = 226.dp

@Composable
private fun TodoSection(todos: List<MainTodoItem>) {
    val todoScrollState = rememberScrollState()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle(title = "오늘 할 일")

        Column(
            modifier = Modifier
                .fillMaxWidth()
                // 부모(화면)가 verticalScroll 이라 높이 제약이 Infinity 로 내려옴.
                // heightIn(max) 로 상한을 줘야 내부 verticalScroll 이 정상 동작함.
                .heightIn(max = TodoListMaxHeight)
                .verticalScroll(todoScrollState),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            todos.forEach { todo ->
                TodoCard(todo = todo)
            }
        }
    }
}

@Composable
private fun TodoCard(todo: MainTodoItem) {
    TodoRow(
        title = todo.title,
        timeRange = todo.timeRange,
        modifier = Modifier.fillMaxWidth(),
        variant = TodoRowVariant.Card,
        leadingContent = {
            // 좌측 드래그 핸들 (mingcute menu line, 20x20)
            VectorPath(
                pathData = PATH_MENU_LINE,
                viewportWidth = 20f,
                viewportHeight = 20f,
                color = PhoneShimTheme.colors.brandStrong,
                modifier = Modifier.size(20.dp)
            )
        },
    )
}
