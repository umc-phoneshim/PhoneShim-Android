package com.phoneshim.android.blocking.policy

/** 리마인더 일정 기반 차단 판정 결과. */
sealed interface ScheduleBlock {
    data object None : ScheduleBlock
    /** 현재 시간대에 전체 폰 제한 일정이 활성. */
    data object FullPhone : ScheduleBlock
    /** 현재 시간대에 특정 앱 제한 일정이 활성. */
    data class SpecificApps(val packages: Set<String>) : ScheduleBlock
}

interface SchedulePolicyProvider {
    /** 지금 이 순간 활성인 일정 제한을 반환. */
    suspend fun activeScheduleBlock(): ScheduleBlock
}
