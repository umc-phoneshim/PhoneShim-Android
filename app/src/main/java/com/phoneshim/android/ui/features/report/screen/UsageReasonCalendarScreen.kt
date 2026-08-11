package com.phoneshim.android.ui.features.report.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.phoneshim.android.ui.common.CalendarGrid
import com.phoneshim.android.ui.common.DateNavigator
import com.phoneshim.android.ui.common.TopAppBar
import com.phoneshim.android.ui.common.base.CollectCommonEffect
import com.phoneshim.android.ui.features.report.component.ReportCard
import com.phoneshim.android.ui.features.report.component.ReportColorGreen
import com.phoneshim.android.ui.features.report.viewmodel.UsageReasonCalendarUiEffect
import com.phoneshim.android.ui.features.report.viewmodel.UsageReasonCalendarUiEvent
import com.phoneshim.android.ui.features.report.viewmodel.UsageReasonCalendarUiState
import com.phoneshim.android.ui.features.report.viewmodel.UsageReasonCalendarViewModel
import com.phoneshim.android.ui.theme.PhoneShimDimens
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimType
import java.time.LocalDate

/**
 * 07. 목표 달성 달력. GET /api/usage-logs/calendar
 *
 * 그 달에 전체 목표를 지킨 날짜를 보여주고, 날짜를 누르면 그날 리포트로 이동합니다.
 * (사용 이유 입력 여부를 조회하는 엔드포인트는 서버에 없습니다)
 * TODO: 아직 네비게이션에 연결돼 있지 않습니다. 진입점이 정해지면 라우트를 추가하세요.
 */
@Composable
fun UsageReasonCalendarRoute(
    onNavigateToReport: (LocalDate) -> Unit,
    onAuthExpired: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: UsageReasonCalendarViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    CollectCommonEffect(viewModel, onAuthExpired)

    LaunchedEffect(viewModel) {
        viewModel.onEvent(UsageReasonCalendarUiEvent.ScreenEntered)
    }
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is UsageReasonCalendarUiEffect.NavigateToReport -> onNavigateToReport(effect.date)
                is UsageReasonCalendarUiEffect.ShowMessage ->
                    snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    UsageReasonCalendarScreen(
        state = state,
        modifier = modifier,
        snackbarHostState = snackbarHostState,
        onPreviousMonth = { viewModel.onEvent(UsageReasonCalendarUiEvent.MonthMoved(-1)) },
        onNextMonth = { viewModel.onEvent(UsageReasonCalendarUiEvent.MonthMoved(1)) },
        onDateSelected = { viewModel.onEvent(UsageReasonCalendarUiEvent.DateSelected(it)) },
    )
}

@Composable
fun UsageReasonCalendarScreen(
    state: UsageReasonCalendarUiState,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onPreviousMonth: () -> Unit = {},
    onNextMonth: () -> Unit = {},
    onDateSelected: (LocalDate) -> Unit = {},
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = PhoneShimTheme.colors.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { TopAppBar(title = "목표 달성 기록", titleStyle = PhoneShimType.KorH3) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(PhoneShimDimens.screenHorizontalPadding),
            verticalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing16),
        ) {
            ReportCard {
                DateNavigator(
                    label = state.monthLabel,
                    onPrevious = onPreviousMonth,
                    onNext = onNextMonth,
                    nextEnabled = state.canGoNextMonth,
                    modifier = Modifier.fillMaxWidth(),
                    labelStyle = PhoneShimType.KorH3,
                    labelColor = PhoneShimTheme.colors.brandStrong,
                )
                Spacer(modifier = Modifier.height(PhoneShimDimens.spacing8))
                CalendarGrid(
                    visibleMonth = state.visibleMonth,
                    selectedDate = state.selectedDate,
                    todayDate = state.today,
                    onDateSelected = onDateSelected,
                    enabled = !state.isLoading,
                )
            }

            // TODO: CalendarGrid 는 공통 컴포넌트라 날짜별 표시를 지원하지 않습니다.
            //  달성한 날을 달력 안에 직접 표시하려면 공통 컴포넌트에 슬롯 추가가 필요해
            //  지금은 아래 요약으로 대신합니다.
            ReportCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(ReportColorGreen, CircleShape),
                    )
                    Spacer(modifier = Modifier.size(PhoneShimDimens.spacing8))
                    Text(
                        text = when {
                            state.isLoading -> "불러오는 중..."
                            state.emptyMessage != null -> state.emptyMessage
                            state.achievedCount == 0 -> "이번 달에는 아직 적은 기록이 없어요."
                            else -> "이번 달 ${state.achievedCount}일 기록했어요."
                        },
                        style = PhoneShimType.KorBodyM,
                        color = PhoneShimTheme.colors.textSecondary,
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PhoneShimTheme.colors.brandSubtle, RoundedCornerShape(12.dp))
                    .padding(PhoneShimDimens.spacing12),
            ) {
                Text(
                    text = "기록하지 않은 날짜를 누르면 사용 이유를 적을 수 있어요.",
                    style = PhoneShimType.KorCaption,
                    color = PhoneShimTheme.colors.brandStrong,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UsageReasonCalendarPreview() {
    PhoneShimTheme {
        UsageReasonCalendarScreen(
            state = UsageReasonCalendarUiState(
                achievedDates = setOf(LocalDate.now(), LocalDate.now().minusDays(2)),
            ),
        )
    }
}

@Preview(name = "서버 미연동", showBackground = true)
@Composable
private fun UsageReasonCalendarEmptyPreview() {
    PhoneShimTheme {
        UsageReasonCalendarScreen(
            state = UsageReasonCalendarUiState(emptyMessage = "아직 기록을 불러올 수 없어요."),
        )
    }
}
