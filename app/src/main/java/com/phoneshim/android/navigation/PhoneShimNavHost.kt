package com.phoneshim.android.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
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
import com.phoneshim.android.ui.features.setgoal.screen.SetGoalCompleteScreen
import com.phoneshim.android.ui.features.setgoal.screen.SetGoalConfirmScreen
import com.phoneshim.android.ui.features.setgoal.screen.SetGoalStartScreen
import com.phoneshim.android.ui.features.setgoal.screen.UsageTimeSetScreen

@Composable
fun PhoneShimNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Routes.SPLASH) {

        composable(Routes.SPLASH) {
            SplashScreen(onSplashFinished = { navController.navigate(Routes.LOGIN) })
        }
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = { navController.navigate(Routes.MAIN) },
                onNavigateToSignUp = { navController.navigate(Routes.SIGN_UP) },
            )
        }
        composable(Routes.SIGN_UP) {
            SignUpScreen(
                onSignUpSuccess = { navController.navigate(Routes.MAIN) },
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(Routes.SET_GOAL_START) {
            SetGoalStartScreen(onStart = { navController.navigate(Routes.APP_SELECT) })
        }
        composable(Routes.APP_SELECT) {
            AppSelectScreen(onNext = { navController.navigate(Routes.USAGE_TIME_SET) })
        }
        composable(Routes.USAGE_TIME_SET) {
            UsageTimeSetScreen(onNext = { navController.navigate(Routes.ACCESS_GOAL_SET) })
        }
        composable(Routes.ACCESS_GOAL_SET) {
            AccessGoalSetScreen(onNext = { navController.navigate(Routes.SET_GOAL_CONFIRM) })
        }
        composable(Routes.SET_GOAL_CONFIRM) {
            SetGoalConfirmScreen(onConfirm = { navController.navigate(Routes.SET_GOAL_COMPLETE) })
        }
        composable(Routes.SET_GOAL_COMPLETE) {
            SetGoalCompleteScreen(onFinish = { navController.navigate(Routes.MAIN) })
        }

        composable(Routes.MAIN) {
            MainScreen(
                onNavigateToSetGoal = { navController.navigate(Routes.SET_GOAL_START) },
                onNavigateToMyPage = { navController.navigate(Routes.MY_PAGE) },
            )
        }

        composable(Routes.REMINDER) {
            ReminderScreen(onAddReminder = { })
        }

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
            val entryId = backStackEntry.arguments?.getString("entryId").orEmpty()
            UsageReasonInputScreen(entryId = entryId, onSubmitted = { navController.popBackStack() })
        }
        composable(Routes.REPORT_AI_SUGGEST) {
            ReportAiSuggestScreen(onNavigateToSummary = { navController.navigate(Routes.REPORT_SUMMARY) })
        }
        composable(Routes.REPORT_SUMMARY) {
            ReportSummaryScreen()
        }

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
