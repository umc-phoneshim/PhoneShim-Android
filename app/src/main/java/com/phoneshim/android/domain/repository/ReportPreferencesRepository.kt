package com.phoneshim.android.domain.repository

import com.phoneshim.android.domain.model.DailyReportAlarm

/**
 * 데일리 리포트 화면의 기기 로컬 설정.
 *
 * 서버가 관리하지 않는 값만 다룹니다. 계정이 아니라 기기에 붙는 설정이라
 * 로그아웃해도 남고, 다른 기기로 옮겨가지 않습니다.
 */
interface ReportPreferencesRepository {

    /** 달력 버튼 안내 툴팁을 이미 닫았는지. */
    suspend fun isCalendarTooltipDismissed(): Boolean

    suspend fun dismissCalendarTooltip()

    /** 설정된 알림 시각. 아직 설정한 적 없으면 null. */
    suspend fun getDailyReportAlarm(): DailyReportAlarm?

    suspend fun saveDailyReportAlarm(alarm: DailyReportAlarm)
}
