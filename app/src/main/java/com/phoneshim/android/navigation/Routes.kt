package com.phoneshim.android.navigation

// 네비게이션에서 사용하는 화면별 경로(route) 상수 모음
object Routes {
    // 01~02. auth
    const val SPLASH = "splash"
    const val LOGIN = "login"

    // 04. setgoal
    const val SET_GOAL_GRAPH = "set_goal_graph"
    const val SET_GOAL_START = "set_goal_start"
    const val GENDER_AGE_SELECT = "gender_age_select"
    const val APP_SELECT = "app_select"
    const val USAGE_TIME_SET = "usage_time_set"
    const val ACCESS_GOAL_SET = "access_goal_set"
    const val SET_GOAL_CONFIRM = "set_goal_confirm"
    const val SET_GOAL_COMPLETE = "set_goal_complete"

    // 05. main
    const val MAIN = "main"

    // 환경 설정
    const val PREF = "pref"

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

    // entryId를 채워 넣은 사용 이유 입력 화면 경로 생성
    fun usageReasonInput(entryId: String) = "usage_reason_input/$entryId"
}
