package com.phoneshim.android.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.phoneshim.android.ui.features.setgoal.viewmodel.SetGoalViewModel
import com.phoneshim.android.ui.features.auth.screen.LoginRoute
import com.phoneshim.android.ui.features.auth.viewmodel.AuthSessionEffect
import com.phoneshim.android.ui.features.auth.viewmodel.AuthSessionViewModel
import com.phoneshim.android.ui.features.auth.screen.SplashRoute
import com.phoneshim.android.ui.common.BottomBarTab
import com.phoneshim.android.ui.features.main.screen.MainScreen
import com.phoneshim.android.ui.features.mypage.screen.MyRoute
import com.phoneshim.android.ui.features.mypage.screen.MySideMenuRoute
import com.phoneshim.android.ui.features.pref.screen.PrefRoute
import com.phoneshim.android.ui.features.reminder.screen.ReminderRoute
import com.phoneshim.android.ui.features.report.screen.ReportSummaryRoute
import com.phoneshim.android.ui.features.report.screen.RestSuggestionRoute
import com.phoneshim.android.ui.features.report.screen.TimetableRoute
import com.phoneshim.android.ui.features.report.screen.UsageReasonInputRoute
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.phoneshim.android.ui.features.setgoal.screen.AccessGoalSetScreen
import com.phoneshim.android.ui.features.setgoal.screen.AppSelectScreen
import com.phoneshim.android.ui.features.setgoal.screen.GenderAgeSelectScreen
import com.phoneshim.android.ui.features.setgoal.screen.SetGoalCompleteScreen
import com.phoneshim.android.ui.features.setgoal.screen.SetGoalConfirmScreen
import com.phoneshim.android.ui.features.setgoal.screen.SetGoalStartScreen
import com.phoneshim.android.ui.features.setgoal.screen.UsageTimeSetScreen

// 앱 전체 화면 이동 경로(그래프)를 정의하는 네비게이션 호스트
@Composable
fun PhoneShimNavHost(navController: NavHostController) {
    val authSessionViewModel: AuthSessionViewModel = hiltViewModel()
    val authNoticeMessage by authSessionViewModel.noticeMessage.collectAsState()

    LaunchedEffect(authSessionViewModel, navController) {
        authSessionViewModel.effect.collect { effect ->
            when (effect) {
                AuthSessionEffect.NavigateToLogin -> navController.navigate(Routes.LOGIN) {
                    popUpTo(navController.graph.id) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    NavHost(navController = navController, startDestination = Routes.SPLASH) {

        // 인증(스플래시/로그인/회원가입) 화면
        composable(Routes.SPLASH) {
            SplashRoute(
                onAuthenticated = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onUnauthenticated = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.LOGIN) {
            LoginRoute(
                onAuthExpired = authSessionViewModel::onAuthExpired,
                noticeMessage = authNoticeMessage,
                // 로그인 직후 목표 설정 시작 화면으로 진입 (접근 권한 동의 팝업이 그 위에 표시됨)
                onNavigateToGoalSetup = {
                    authSessionViewModel.onSessionStarted()
                    navController.navigate(Routes.SET_GOAL_GRAPH) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToMain = {
                    authSessionViewModel.onSessionStarted()
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
            )
        }

        // 목표 설정 온보딩 플로우 — 그래프 범위로 SetGoalViewModel 하나를 공유해
        // 화면 간 선택 값(성별/나이, 앱, 시간, 제한/목표)이 이어집니다.
        navigation(startDestination = Routes.SET_GOAL_START, route = Routes.SET_GOAL_GRAPH) {
            composable(Routes.SET_GOAL_START) {
                SetGoalStartScreen(
                    onStart = { navController.navigate(Routes.GENDER_AGE_SELECT) },
                    onSkip = {
                        navController.navigate(Routes.MAIN) {
                            popUpTo(Routes.SET_GOAL_GRAPH) { inclusive = true }
                        }
                    },
                )
            }
            composable(Routes.GENDER_AGE_SELECT) { entry ->
                GenderAgeSelectScreen(
                    onNext = { navController.navigate(Routes.USAGE_TIME_SET) },
                    onBack = { navController.popBackStack() },
                    viewModel = navController.sharedSetGoalViewModel(entry),
                )
            }
            composable(Routes.APP_SELECT) { entry ->
                AppSelectScreen(
                    onNext = { navController.navigate(Routes.ACCESS_GOAL_SET) },
                    onBack = { navController.popBackStack() },
                    viewModel = navController.sharedSetGoalViewModel(entry),
                )
            }
            composable(Routes.USAGE_TIME_SET) { entry ->
                UsageTimeSetScreen(
                    onNext = { navController.navigate(Routes.APP_SELECT) },
                    onBack = { navController.popBackStack() },
                    viewModel = navController.sharedSetGoalViewModel(entry),
                )
            }
            composable(Routes.ACCESS_GOAL_SET) { entry ->
                AccessGoalSetScreen(
                    onNext = { navController.navigate(Routes.SET_GOAL_CONFIRM) },
                    onBack = { navController.popBackStack() },
                    viewModel = navController.sharedSetGoalViewModel(entry),
                )
            }
            composable(Routes.SET_GOAL_CONFIRM) { entry ->
                SetGoalConfirmScreen(
                    onConfirm = { navController.navigate(Routes.SET_GOAL_COMPLETE) },
                    onBack = { navController.popBackStack() },
                    viewModel = navController.sharedSetGoalViewModel(entry),
                )
            }
            composable(Routes.SET_GOAL_COMPLETE) { entry ->
                SetGoalCompleteScreen(
                    onFinish = {
                        navController.navigate(Routes.MAIN) {
                            popUpTo(Routes.SET_GOAL_GRAPH) { inclusive = true }
                        }
                    },
                    viewModel = navController.sharedSetGoalViewModel(entry),
                )
            }
        }

        // 메인 대시보드 화면
        composable(Routes.MAIN) {
            MainScreen(
                onNavigateToSetGoal = { navController.navigate(Routes.SET_GOAL_GRAPH) },
                onNavigateToSettings = { navController.navigate(Routes.PREF) },
                onNavigateToMyPage = { navController.navigate(Routes.MY_PAGE) },
                onNavigateToReminder = { navController.navigateToTopLevel(Routes.REMINDER) },
                onNavigateToReport = { navController.navigateToTopLevel(Routes.TIMETABLE) },
            )
        }

        composable(Routes.PREF) {
            val sourceTab = navController.previousBackStackEntry?.destination?.route.toBottomBarTab()
            PrefRoute(
                onBack = { navController.popBackStack() },
                onCancel = { navController.popBackStack() },
                onSave = { navController.popBackStack() },
                selectedBottomTab = sourceTab,
                onNavigateToMain = { navController.navigateFromTransientToTopLevel(Routes.MAIN) },
                onNavigateToReminder = { navController.navigateFromTransientToTopLevel(Routes.REMINDER) },
                onNavigateToReport = { navController.navigateFromTransientToTopLevel(Routes.TIMETABLE) },
            )
        }

        // 리마인더 화면
        composable(Routes.REMINDER) {
            ReminderRoute(
                onNavigateToSettings = { navController.navigate(Routes.PREF) },
                onNavigateToMyPage = { navController.navigate(Routes.MY_PAGE) },
                onNavigateToMain = { navController.navigateToTopLevel(Routes.MAIN) },
                onNavigateToReport = { navController.navigateToTopLevel(Routes.TIMETABLE) },
                onAuthExpired = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                },
            )
        }

        // 리포트(타임테이블/사용이유/AI제안/요약) 화면
        composable(Routes.TIMETABLE) {
            TimetableRoute(
                onAuthExpired = authSessionViewModel::onAuthExpired,
                onEntryClick = { target ->
                    navController.navigate(
                        Routes.usageReasonInput(
                            monitoredAppId = target.monitoredAppId,
                            date = target.date,
                            start = target.timeRangeStart,
                            end = target.timeRangeEnd,
                        ),
                    )
                },
                onNavigateToAiSuggestion = { navController.navigate(Routes.REPORT_AI_SUGGEST) },
                onNavigateToSettings = { navController.navigate(Routes.PREF) },
                onNavigateToSummary = { navController.navigate(Routes.REPORT_SUMMARY) },
                onNavigateToMain = { navController.navigateToTopLevel(Routes.MAIN) },
                onNavigateToReminder = { navController.navigateToTopLevel(Routes.REMINDER) },
                onNavigateToMyPage = { navController.navigate(Routes.MY_PAGE) },
            )
        }
        composable(
            route = Routes.USAGE_REASON_INPUT,
            arguments = listOf(
                navArgument("monitoredAppId") { type = NavType.StringType },
                navArgument("date") { type = NavType.StringType; defaultValue = "" },
                navArgument("start") { type = NavType.StringType; defaultValue = "" },
                navArgument("end") { type = NavType.StringType; defaultValue = "" },
            ),
        ) { backStackEntry ->
            // 선택한 사용 구간의 주의 앱과 시간 범위를 그대로 전달합니다.
            val args = backStackEntry.arguments
            val start = args?.getString("start").orEmpty()
            val end = args?.getString("end").orEmpty()
            UsageReasonInputRoute(
                onAuthExpired = authSessionViewModel::onAuthExpired,
                monitoredAppId = args?.getString("monitoredAppId").orEmpty(),
                date = args?.getString("date").orEmpty(),
                timeRangeStart = start,
                timeRangeEnd = end,
                timeRangeLabel = formatTimeRangeLabel(start, end),
                onSubmitted = { navController.popBackStack() },
            )
        }
        composable(Routes.REPORT_AI_SUGGEST) {
            RestSuggestionRoute(
                onNavigateToSummary = { navController.navigate(Routes.REPORT_SUMMARY) },
                onAuthExpired = authSessionViewModel::onAuthExpired,
            )
        }
        composable(Routes.REPORT_SUMMARY) {
            ReportSummaryRoute(
                onAuthExpired = authSessionViewModel::onAuthExpired,
                onNavigateToSettings = { navController.navigate(Routes.PREF) },
                onNavigateToTimetable = { navController.popBackStack(Routes.TIMETABLE, inclusive = false) },
                onNavigateToMain = { navController.navigateToTopLevel(Routes.MAIN) },
                onNavigateToReminder = { navController.navigateToTopLevel(Routes.REMINDER) },
                onNavigateToMyPage = { navController.navigate(Routes.MY_PAGE) },
            )
        }

        // 마이페이지 화면
        composable(Routes.MY_PAGE) {
            val sourceTab = navController.previousBackStackEntry?.destination?.route.toBottomBarTab()
            MyRoute(
                onAuthExpired = authSessionViewModel::onAuthExpired,
                onNavigateToSideMenu = { navController.navigate(Routes.MY_SIDE_MENU) },
                selectedBottomTab = sourceTab,
                onNavigateToMain = { navController.navigateFromTransientToTopLevel(Routes.MAIN) },
                onNavigateToReminder = { navController.navigateFromTransientToTopLevel(Routes.REMINDER) },
                onNavigateToReport = { navController.navigateFromTransientToTopLevel(Routes.TIMETABLE) },
                // TODO: 로그아웃 API 연동 후 Routes.LOGIN 으로 이동하도록 연결하세요.
                onNavigateToLogin = authSessionViewModel::onSessionEnded,
            )
        }
        composable(Routes.MY_SIDE_MENU) {
            MySideMenuRoute(
                onAuthExpired = authSessionViewModel::onAuthExpired,
                onNavigateToLogin = authSessionViewModel::onSessionEnded,
                onDismiss = { navController.popBackStack() },
            )
        }
    }
}

/** ISO 8601 시간 문자열 두 개를 "22:00 ~ 22:35" 형태로 만듭니다. */
private fun formatTimeRangeLabel(start: String, end: String): String {
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    val from = start.toLocalDateTimeOrNull()?.format(formatter) ?: return ""
    val to = end.toLocalDateTimeOrNull()?.format(formatter) ?: return ""
    return "$from ~ $to"
}

private fun String.toLocalDateTimeOrNull(): LocalDateTime? =
    runCatching { LocalDateTime.parse(this) }.getOrNull()

private fun String?.toBottomBarTab(): BottomBarTab = when (this) {
    Routes.MAIN -> BottomBarTab.MAIN
    Routes.REMINDER -> BottomBarTab.REMINDER
    Routes.TIMETABLE,
    Routes.USAGE_REASON_INPUT,
    Routes.REPORT_AI_SUGGEST,
    Routes.REPORT_SUMMARY,
    -> BottomBarTab.REPORT
    else -> BottomBarTab.MAIN
}

private fun NavHostController.navigateToTopLevel(route: String) {
    navigate(route) {
        popUpTo(Routes.MAIN) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

private fun NavHostController.navigateFromTransientToTopLevel(route: String) {
    popBackStack()
    navigateToTopLevel(route)
}

// setgoal 그래프 범위로 스코프된 SetGoalViewModel을 가져옵니다.
// 그래프 안 어느 화면에서 요청해도 같은 인스턴스가 반환됩니다.
@Composable
private fun NavHostController.sharedSetGoalViewModel(entry: NavBackStackEntry): SetGoalViewModel {
    val parentEntry = remember(entry) { getBackStackEntry(Routes.SET_GOAL_GRAPH) }
    return hiltViewModel(parentEntry)
}
