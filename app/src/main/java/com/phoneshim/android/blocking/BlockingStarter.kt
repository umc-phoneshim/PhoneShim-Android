package com.phoneshim.android.blocking

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.phoneshim.android.blocking.detection.BlockingPermissions
import com.phoneshim.android.blocking.service.BlockerService

/**
 * 차단 엔진 시작 API.
 *
 * 권한이 이미 허용돼 있을 때만 서비스를 킴.
 * (사용정보 접근·오버레이는 사용자가 설정 앱에서 직접 켜야 하는 특수 권한이라
 *  앱이 코드로 부여할 수 없다. 요청 안내는 BlockingPermissionRequest 참고.)
 *
 * 호출해도 안전한 지점:
 *   - 앱 진입 시: 권한이 있으면 엔진이 살아나고, 없으면 아무 일도 안 일어남
 *   - 권한 동의 플로우 완료 직후
 * 이미 실행 중이면 중복 실행되지 않음
 *
 * @return 실제로 시작을 요청했으면 true, 권한이 없어 아무것도 안 했으면 false
 */
object BlockingStarter {

    fun startIfPermitted(context: Context): Boolean {
        // 권한이 남아 있어도 로그아웃 상태에서는 차단 서비스를 다시 시작하지 않습니다.
        if (!BlockingSessionGate.isEnabled(context)) return false
        if (!BlockingPermissions.hasAll(context)) return false
        ContextCompat.startForegroundService(
            context,
            Intent(context, BlockerService::class.java),
        )
        return true
    }
}
