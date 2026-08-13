package com.phoneshim.android.blocking.detection

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings

/**
 * 차단 엔진에 필요한 두 특수 권한 확인/요청.
 *  - PACKAGE_USAGE_STATS : 포그라운드 앱 감지 (설정 화면에서 사용자가 직접 허용)
 *  - SYSTEM_ALERT_WINDOW : 오버레이로 덮기
 */
object BlockingPermissions {

    fun hasUsageAccess(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        // unsafeCheckOpNoThrow 는 API 29 에 추가됐다. minSdk 가 26 이라 그 아래 기기에서
        // 그대로 부르면 NoSuchMethodError 로 죽는다. 이 함수는 앱 진입과 부팅 복구 경로에서 불리므로 진입 크래시가 된다.
        // 29 미만에서는 같은 의미의 deprecated checkOpNoThrow 를 쓴다.
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName,
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName,
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun hasOverlay(context: Context): Boolean = Settings.canDrawOverlays(context)

    fun hasAll(context: Context): Boolean = hasUsageAccess(context) && hasOverlay(context)

    /**
     * 사용정보 접근 설정 화면.
     *
     * 오버레이(ACTION_MANAGE_OVERLAY_PERMISSION)와 달리 '앱별 화면으로 바로 가기'가
     * 공식 지원되지 않는다. package: URI 를 붙이면 일부 기기에서 해당 앱 항목으로 바로
     * 이동하지만, 지원하지 않는 기기에서는 무시되거나 인텐트를 처리할 액티비티가 없어
     * 실패한다. 그래서 [usageAccessIntentFallback] 을 함께 두고 실패 시 목록 화면으로 연다.
     *
     * → 목록 화면이 뜨는 기기에서는 사용자가 직접 앱을 찾아야 하므로,
     *   호출하는 화면에서 안내 문구가 필요하다.
     */
    fun usageAccessIntent(context: Context): Intent =
        Intent(
            Settings.ACTION_USAGE_ACCESS_SETTINGS,
            Uri.parse("package:${context.packageName}"),
        )

    /** 앱별 이동이 안 되는 기기용. 사용정보 접근 '목록' 화면. */
    fun usageAccessIntentFallback(): Intent =
        Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)

    fun overlayIntent(context: Context): Intent =
        Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}"),
        )
}