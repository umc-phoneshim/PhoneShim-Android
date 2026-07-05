package com.phoneshim.android.navigation

object Routes {
    // 01~03. auth
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val SIGN_UP = "sign_up"

    // 04. setgoal
    const val SET_GOAL_START = "set_goal_start"
    const val APP_SELECT = "app_select"
    const val USAGE_TIME_SET = "usage_time_set"
    const val ACCESS_GOAL_SET = "access_goal_set"
    const val SET_GOAL_CONFIRM = "set_goal_confirm"
    const val SET_GOAL_COMPLETE = "set_goal_complete"

    // 05. main
    const val MAIN = "main"

    // 06. reminder
    const val REMINDER = "reminder"

    // 07. report
    const val TIMETABLE = "timetable"
    const val USAGE_REASON_INPUT = "usage_reason_input/{entryId}"
    const val REPORT_AI_SUGGEST = "report_ai_suggest"
    const val REPORT_SUMMARY = "report_summary"

    // 08. mypage
    const val MY_PAGE = "my_page"
    const val MY_SIDE_MENU = "my_side_menu"

    fun usageReasonInput(entryId: String) = "usage_reason_input/$entryId"
}
