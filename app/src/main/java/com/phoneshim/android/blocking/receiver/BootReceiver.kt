package com.phoneshim.android.blocking.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.phoneshim.android.blocking.BlockingSessionGate
import com.phoneshim.android.blocking.detection.BlockingPermissions
import com.phoneshim.android.blocking.schedule.ReminderAlarmScheduler
import com.phoneshim.android.blocking.service.BlockerService
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 재부팅 후 복구.
 *  - 권한이 살아 있으면 서비스 재기동.
 *  - AlarmManager 예약은 리부트 때 전부 날아가므로 오늘 리마인더를 다시 예약한다.
 */
class BootReceiver : BroadcastReceiver() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface BootEntryPoint {
        fun reminderAlarmScheduler(): ReminderAlarmScheduler
    }

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        // 로그아웃 전에 받은 권한은 재부팅 후에도 남으므로 세션 게이트를 별도로 확인합니다.
        if (!BlockingSessionGate.isEnabled(context)) return
        if (!BlockingPermissions.hasAll(context)) return

        ContextCompat.startForegroundService(
            context,
            Intent(context, BlockerService::class.java),
        )

        // 리부트로 날아간 알람 재예약.
        val pending = goAsync()
        val scheduler = EntryPointAccessors
            .fromApplication(context.applicationContext, BootEntryPoint::class.java)
            .reminderAlarmScheduler()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                scheduler.rescheduleToday()
            } catch (t: Throwable) {
                /*
                 * 예약 실패가 부팅 복구 전체를 죽이지 않게 한다.
                 * launch 밖으로 빠져나온 예외는 핸들러가 없으면 프로세스를 종료시킨다.
                 *
                 * 실제 경로: USE_EXACT_ALARM 이 심사에서 반려되면 SCHEDULE_EXACT_ALARM 만 남긴다
                 *
                 * 알람이 없어도 화면이 켜져 있으면 폴링이 일정 차단을 잡으므로,
                 * 위의 서비스 기동만 살아 있으면 복구는 성립한다.
                 */
            } finally {
                pending.finish()
            }
        }
    }
}
