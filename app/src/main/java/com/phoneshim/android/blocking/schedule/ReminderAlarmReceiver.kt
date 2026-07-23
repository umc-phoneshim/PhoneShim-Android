package com.phoneshim.android.blocking.schedule

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.phoneshim.android.blocking.service.BlockerService

/**
 * 리마인더 시작/종료 시각에 AlarmManager 가 깨우는 수신기.
 *
 * 서비스가 죽어 있어도 여기서 다시 살림 시작 edge 면 서비스를 띄워 차단을 켜고,
 * 종료 edge 면 서비스에 해당 일정 종료를 알려 차단을 내림.
 * 이 수신기는 "지금 판정을 다시 하라"는 트리거 역할만 한다.
 */
class ReminderAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val edge = intent?.getStringExtra(ReminderAlarmScheduler.EXTRA_EDGE) ?: return

        // 시작이든 종료든, 서비스를 깨워 재판정하게 한다.
        val serviceIntent = Intent(context, BlockerService::class.java).apply {
            action = BlockerService.ACTION_REEVALUATE
            putExtra(ReminderAlarmScheduler.EXTRA_EDGE, edge)
        }
        ContextCompat.startForegroundService(context, serviceIntent)
    }
}
