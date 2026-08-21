package com.phoneshim.android.blocking

import android.content.Context
import android.content.Intent
import com.phoneshim.android.blocking.service.BlockerService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** 세션 코디네이터가 차단 엔진의 실행 상태를 변경할 때 사용하는 Android 경계입니다. */
interface BlockingSessionActions {
    fun setEnabled(enabled: Boolean)
    fun startIfPermitted()
    fun stop()
}

@Singleton
class AndroidBlockingSessionActions @Inject constructor(
    @ApplicationContext private val context: Context,
) : BlockingSessionActions {
    override fun setEnabled(enabled: Boolean) {
        BlockingSessionGate.setEnabled(context, enabled)
    }

    override fun startIfPermitted() {
        BlockingStarter.startIfPermitted(context)
    }

    override fun stop() {
        context.stopService(Intent(context, BlockerService::class.java))
    }
}
