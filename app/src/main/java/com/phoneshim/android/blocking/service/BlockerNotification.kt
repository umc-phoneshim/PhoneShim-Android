package com.phoneshim.android.blocking.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.phoneshim.android.R

/** 포그라운드 서비스 상시 알림. */
object BlockerNotification {
    const val ID = 4101
    private const val CHANNEL_ID = "phoneshim_blocking"

    fun build(context: Context): Notification {
        ensureChannel(context)
        return Notification.Builder(context, CHANNEL_ID)
            .setContentTitle("폰쉼 실행 중")
            .setContentText("앱 사용 목표를 지키고 있어요")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .build()
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService(NotificationManager::class.java)
        if (nm.getNotificationChannel(CHANNEL_ID) == null) {
            nm.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "폰쉼 차단",
                    NotificationManager.IMPORTANCE_LOW,
                ),
            )
        }
    }
}
