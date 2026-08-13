package com.phoneshim.android.blocking.policy

import android.util.Log
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

        val specificApps = active.filter { it.restrictionMode == MODE_SPECIFIC_APPS }
        val packages = specificApps
            .flatMap { it.restrictedPackages.split(",") }
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()

        // 사용자가 앱을 골라 저장했으니 목록이 비어 있을 수 없다. 비었다면 캐시에 잘못 들어간 것이다.
        // 흔한 원인은 monitoredAppId -> packageName 변환 실패
        // 여기서 막을 방법은 없으므로 판정은 그대로 두고, "일정을 걸었는데 안 막힌다" 는
        // 증상의 원인을 로그로 남긴다.
        if (specificApps.isNotEmpty() && packages.isEmpty()) {
            Log.w(TAG, "특정 앱 제한 일정이 활성인데 대상 패키지가 비었다. 변환 실패 가능성. " +
                    "taskIds=${specificApps.map { it.taskId }}")
        }

        return if (packages.isEmpty()) ScheduleBlock.None
        else ScheduleBlock.SpecificApps(packages)
    }

    private companion object {
        const val TAG = "ReminderSchedule"
        const val MODE_FULL_PHONE = "FULL_PHONE"
        const val MODE_SPECIFIC_APPS = "SPECIFIC_APP" // API 명세 값
    }
}