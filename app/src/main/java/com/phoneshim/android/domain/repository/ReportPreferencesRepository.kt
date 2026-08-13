package com.phoneshim.android.domain.repository

/**
 * 데일리 리포트 화면의 기기 로컬 설정.
 *
 * 서버가 관리하지 않는 UI 설정만 다룹니다. AlertSetting은 계정별 서버 데이터이므로
 * 이 저장소에 중복 저장하지 않습니다.
 */
interface ReportPreferencesRepository {

    /** 달력 버튼 안내 툴팁을 이미 닫았는지. */
    suspend fun isCalendarTooltipDismissed(): Boolean

    suspend fun dismissCalendarTooltip()
}
