package com.phoneshim.android.blocking

import android.content.Context
import android.content.Intent
import com.phoneshim.android.blocking.service.BlockerService
import com.phoneshim.android.domain.model.AuthSessionState
import com.phoneshim.android.domain.repository.AuthSessionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/** 인증 세션 상태를 차단 엔진의 실행 상태에 반영합니다. */
@Singleton
class BlockingSessionCoordinator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authSessionRepository: AuthSessionRepository,
) {
    fun observe(scope: CoroutineScope): Job = scope.launch {
        authSessionRepository.sessionState
            .collect { state ->
                when (state) {
                    AuthSessionState.AUTHENTICATED -> enable()
                    AuthSessionState.UNAUTHENTICATED -> disable()
                    AuthSessionState.RESTORING -> Unit
                }
            }
    }

    private fun enable() {
        // 로그인 세션이 확인된 뒤 플래그를 먼저 열어 BlockingStarter의 시작 조건을 만족시킵니다.
        BlockingSessionGate.setEnabled(context, true)
        BlockingStarter.startIfPermitted(context)
    }

    private fun disable() {
        // 로그아웃 시 수신기가 동시에 서비스를 깨우더라도 닫힌 게이트를 먼저 보도록 합니다.
        // stopService()가 BlockerService.onDestroy()를 호출하면서 표시 중인 오버레이도 제거합니다.
        BlockingSessionGate.setEnabled(context, false)
        context.stopService(Intent(context, BlockerService::class.java))
    }
}
