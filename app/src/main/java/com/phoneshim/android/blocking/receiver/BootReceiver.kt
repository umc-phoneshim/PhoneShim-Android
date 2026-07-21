package com.phoneshim.android.blocking.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
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
            } finally {
                pending.finish()
            }
        }
    }
}
