package com.phoneshim.android.blocking.policy

import com.phoneshim.android.data.database.dao.ReminderRestrictionDao
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * reminder_restrictions 테이블에서 지금 시간대에 걸린 일정을 찾아 판정.
 * 저장이 아직 안 붙었으면 테이블이 비어 None 을 반환.
 */
@Singleton
class ReminderSchedulePolicyProvider @Inject constructor(
    private val dao: ReminderRestrictionDao,
) : SchedulePolicyProvider {

    override suspend fun activeScheduleBlock(): ScheduleBlock {
        val today = LocalDate.now()
        val nowMinutes = LocalTime.now().let { it.hour * 60 + it.minute }

        // 리마인더는 자정 넘김을 막고,
        // 오늘 등록된 일정만 당일 범위로 판정한다.
        // 만약 자정 넘김이 허용되도록 스펙이 바뀌면, 어제 일정 조회 + wrap 판정을 추가해야 한다.
        val active = dao.getForDate(today.toEpochDay())
            .filter { nowMinutes in it.startMinutes until it.endMinutes }

        if (active.any { it.restrictionMode == MODE_FULL_PHONE }) {
            return ScheduleBlock.FullPhone
        }

        val packages = active
            .filter { it.restrictionMode == MODE_SPECIFIC_APPS }
            .flatMap { it.restrictedPackages.split(",") }
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()

        return if (packages.isEmpty()) ScheduleBlock.None
        else ScheduleBlock.SpecificApps(packages)
    }

    private companion object {
        const val MODE_FULL_PHONE = "FULL_PHONE"
        const val MODE_SPECIFIC_APPS = "SPECIFIC_APP" // API 명세 값
    }
}
