package com.phoneshim.android.blocking.schedule

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.phoneshim.android.data.database.dao.ReminderRestrictionDao
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 리마인더 일정 제한을 AlarmManager 로 예약.
 *
 * 폴링 대신 예약을 쓰는 이유:
 *  - 서비스가 죽어도 시각이 되면 시스템이 깨워 차단을 발동할 수 있음.
 *  - 명세의 실행 모델과 일치.
 *  - 상시 폴링 부하 제거.
 *
 * 리마인더 CRUD 시(저장/수정/삭제) 호출해 예약을 재구성.
 */
@Singleton
class ReminderAlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: ReminderRestrictionDao,
) {
    private val alarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * 오늘 일정 전체를 다시 예약.
     *
     * 한계: 오늘치만 armed 된다. 자정을 넘기거나 앱을 안 열면 내일 리마인더는
     * 예약되지 않음. 데모가 당일 테스트면 무방. 상시 운영하려면 자정 롤오버 시다시 호출.
     */
    suspend fun rescheduleToday() {
        val today = LocalDate.now()
        val items = dao.getForDate(today.toEpochDay())
            .filter { it.restrictionMode != MODE_NONE }

        items.forEach { r ->
            val startMs = epochMillisFor(today, r.startMinutes)
            val endMs = epochMillisFor(today, r.endMinutes)
            val now = System.currentTimeMillis()

            // 이미 지난 시각은 예약하지 않음
            if (startMs > now) setExact(startMs, r.taskId, edge = EDGE_START)
            if (endMs > now) setExact(endMs, r.taskId, edge = EDGE_END)
        }
    }

    fun cancel(taskId: String) {
        alarmManager.cancel(pendingIntent(taskId, EDGE_START))
        alarmManager.cancel(pendingIntent(taskId, EDGE_END))
    }

    private fun setExact(triggerAtMs: Long, taskId: String, edge: String) {
        val pi = pendingIntent(taskId, edge)
        // 정시 발동이 중요하므로 exact.
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMs,
            pi,
        )
    }

    private fun pendingIntent(taskId: String, edge: String): PendingIntent {
        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            action = "$ACTION_PREFIX.$edge"
            data = android.net.Uri.parse("phoneshim://reminder/$taskId/$edge")
            putExtra(EXTRA_TASK_ID, taskId)
            putExtra(EXTRA_EDGE, edge)
        }
        return PendingIntent.getBroadcast(
            context,
            (taskId + edge).hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun epochMillisFor(date: LocalDate, minutesOfDay: Int): Long =
        date.atStartOfDay(ZoneId.systemDefault())
            .plusMinutes(minutesOfDay.toLong())
            .toInstant()
            .toEpochMilli()

    companion object {
        const val ACTION_PREFIX = "com.phoneshim.android.REMINDER_ALARM"
        const val EXTRA_TASK_ID = "taskId"
        const val EXTRA_EDGE = "edge"
        const val EDGE_START = "START"
        const val EDGE_END = "END"
        private const val MODE_NONE = "NONE"
    }
}
