package com.phoneshim.android.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.phoneshim.android.ui.features.setgoal.viewmodel.SetGoalViewModel
import com.phoneshim.android.ui.features.auth.screen.LoginScreen
import com.phoneshim.android.ui.features.auth.screen.SignUpScreen
import com.phoneshim.android.ui.features.auth.screen.SplashScreen
import com.phoneshim.android.ui.features.main.screen.MainScreen
import com.phoneshim.android.ui.features.mypage.screen.MyScreen
import com.phoneshim.android.ui.features.mypage.screen.MySideMenuScreen
import com.phoneshim.android.ui.features.reminder.screen.ReminderScreen
import com.phoneshim.android.ui.features.report.screen.ReportAiSuggestScreen
import com.phoneshim.android.ui.features.report.screen.ReportSummaryScreen
import com.phoneshim.android.ui.features.report.screen.TimetableScreen
import com.phoneshim.android.ui.features.report.screen.UsageReasonInputScreen
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
    NavHost(navController = navController, startDestination = Routes.SPLASH) {

        // 인증(스플래시/로그인/회원가입) 화면
        composable(Routes.SPLASH) {
            SplashScreen(onSplashFinished = { navController.navigate(Routes.LOGIN) })
        }
        composable(Routes.LOGIN) {
            LoginScreen(
                // 로그인 직후 목표 설정 시작 화면으로 진입 (접근 권한 동의 팝업이 그 위에 표시됨)
                onLoginSuccess = { navController.navigate(Routes.SET_GOAL_START) },
                onNavigateToSignUp = { navController.navigate(Routes.SIGN_UP) },
            )
        }
        composable(Routes.SIGN_UP) {
            SignUpScreen(
                onSignUpSuccess = { navController.navigate(Routes.MAIN) },
                onNavigateBack = { navController.popBackStack() },
            )
        }

        // 목표 설정 온보딩 플로우 — 그래프 범위로 SetGoalViewModel 하나를 공유해
        // 화면 간 선택 값(성별/나이, 앱, 시간, 제한/목표)이 이어집니다.
        navigation(startDestination = Routes.SET_GOAL_START, route = Routes.SET_GOAL_GRAPH) {
            composable(Routes.SET_GOAL_START) {
                SetGoalStartScreen(
                    onStart = { navController.navigate(Routes.GENDER_AGE_SELECT) },
                    onSkip = { navController.navigate(Routes.MAIN) },
                )
            }
            composable(Routes.GENDER_AGE_SELECT) { entry ->
                GenderAgeSelectScreen(
                    onNext = { navController.navigate(Routes.APP_SELECT) },
                    onBack = { navController.popBackStack() },
                    viewModel = navController.sharedSetGoalViewModel(entry),
                )
            }
            composable(Routes.APP_SELECT) { entry ->
                AppSelectScreen(
                    onNext = { navController.navigate(Routes.USAGE_TIME_SET) },
                    onBack = { navController.popBackStack() },
                    viewModel = navController.sharedSetGoalViewModel(entry),
                )
            }
            composable(Routes.USAGE_TIME_SET) { entry ->
                UsageTimeSetScreen(
                    onNext = { navController.navigate(Routes.ACCESS_GOAL_SET) },
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
                    onFinish = { navController.navigate(Routes.MAIN) },
                    viewModel = navController.sharedSetGoalViewModel(entry),
                )
            }
        }

        // 메인 대시보드 화면
        composable(Routes.MAIN) {
            MainScreen(
                onNavigateToSetGoal = { navController.navigate(Routes.SET_GOAL_START) },
                onNavigateToMyPage = { navController.navigate(Routes.MY_PAGE) },
            )
        }

        // 리마인더 화면
        composable(Routes.REMINDER) {
            ReminderScreen(onAddReminder = { })
        }

        // 리포트(타임테이블/사용이유/AI제안/요약) 화면
        composable(Routes.TIMETABLE) {
            TimetableScreen(
                onEntryClick = { entryId -> navController.navigate(Routes.usageReasonInput(entryId)) },
                onNavigateToAiSuggestion = { navController.navigate(Routes.REPORT_AI_SUGGEST) },
            )
        }
        composable(
            route = Routes.USAGE_REASON_INPUT,
            arguments = listOf(navArgument("entryId") { type = NavType.StringType }),
        ) { backStackEntry ->
            // 경로 인자로 전달된 사용 기록 id를 꺼내 다음 화면에 전달
            val entryId = backStackEntry.arguments?.getString("entryId").orEmpty()
            UsageReasonInputScreen(entryId = entryId, onSubmitted = { navController.popBackStack() })
        }
        composable(Routes.REPORT_AI_SUGGEST) {
            ReportAiSuggestScreen(onNavigateToSummary = { navController.navigate(Routes.REPORT_SUMMARY) })
        }
        composable(Routes.REPORT_SUMMARY) {
            ReportSummaryScreen()
        }

        // 마이페이지 화면
        composable(Routes.MY_PAGE) {
            MyScreen(onNavigateToSideMenu = { navController.navigate(Routes.MY_SIDE_MENU) })
        }
        composable(Routes.MY_SIDE_MENU) {
            MySideMenuScreen(
                onNavigateToWithdraw = { },
                onDismiss = { navController.popBackStack() },
            )
        }
    }
}

// setgoal 그래프 범위로 스코프된 SetGoalViewModel을 가져옵니다.
// 그래프 안 어느 화면에서 요청해도 같은 인스턴스가 반환됩니다.
@Composable
private fun NavHostController.sharedSetGoalViewModel(entry: NavBackStackEntry): SetGoalViewModel {
    val parentEntry = remember(entry) { getBackStackEntry(Routes.SET_GOAL_GRAPH) }
    return hiltViewModel(parentEntry)
}
