package com.phoneshim.android.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 리마인더 일정 제한 서버 데이터의 로컬 캐시.
 *
 * [원본] 서버 Reminder API (/api/reminders). restrictMode/restrictedAppIds 포함.
 * [역할] 오프라인에서도 엔진이 "지금 시간대에 제한 걸린 일정 있나"를 읽게 미러링.
 *   API 명세의 "제한 실행은 클라이언트" 계약을 로컬에서 수행하기 위한 캐시.
 *
 * [필드가 서버와 다른 이유]
 *   서버 restrictedAppIds 는 monitoredAppId(uuid) 목록이지만, 엔진은 패키지명이 필요.
 *   그래서 캐시에는 uuid 를 패키지명으로 해석한 결과 restrictedPackages를 저장.
 *   (명세: "클라이언트가 주의앱 API 조회해 패키지명 해석")
 *   restrictionMode: 서버 restrictMode 와 동일 문자열.
 *
 */
@Entity(tableName = "reminder_restriction_cache")
data class ReminderRestrictionEntity(
    @PrimaryKey val taskId: String,          // 서버 reminder id
    val date: Long,                          // epochDay
    val startMinutes: Int,
    val endMinutes: Int,
    val restrictionMode: String,             // 서버 restrictMode: NONE/FULL_PHONE/SPECIFIC_APP
    val restrictedPackages: String,          // uuid→패키지명 해석 결과
)
