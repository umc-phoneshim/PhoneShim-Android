package com.phoneshim.android.blocking.permission

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import com.phoneshim.android.blocking.BlockingStarter
import com.phoneshim.android.blocking.detection.BlockingPermissions

/**
 * 차단 엔진에 필요한 두 특수 권한을 순서대로 안내하는 헬퍼. UI 는 없다(화면 담당이 소유).
 *
 *   1) 사용정보 접근(PACKAGE_USAGE_STATS) — 포그라운드 앱 감지에 필요
 *   2) 다른 앱 위에 표시(SYSTEM_ALERT_WINDOW) — 차단 화면 표시에 필요
 *
 * 둘 다 런타임 다이얼로그가 없고 사용자가 '설정 앱'에서 직접 켜야 하는 권한이라,
 * 설정 화면을 띄우고 돌아왔을 때 다시 확인하는 왕복이 필요하다. 그 왕복을 여기서 처리한다.
 *
 * 알림 권한(POST_NOTIFICATIONS)은 여기서 다루지 않는다:
 *   리마인더 알림과 공용이고, 없어도 엔진은 정상 동작한다(상시 알림만 안 보임).
 *   요청 주체는 팀에서 정할 사안.
 *
 * 사용 예 (권한 동의 팝업의 '모두 허용하기' 등):
 * ```
 * val permissionRequest = rememberBlockingPermissionRequest { granted ->
 *     // granted == true 면 엔진이 이미 시작된 상태
 * }
 * PrimaryButton(text = "모두 허용하기", onClick = { permissionRequest.launch() })
 * ```
 */
class BlockingPermissionRequest internal constructor(
    private val context: Context,
    /** (요청 인텐트, 실패 시 대체 인텐트) — 대체가 없으면 두 번째는 null. */
    private val openSettings: (Intent, Intent?) -> Unit,
) {
    private enum class Step { USAGE_ACCESS, OVERLAY }

    /** 방금 설정 화면으로 보낸 단계. 돌아왔을 때 허용됐는지 확인하는 기준. */
    private var pending: Step? = null

    internal var onFinished: (Boolean) -> Unit = {}

    /** 이미 다 허용돼 있으면 즉시 완료 처리된다. */
    fun launch() = advance()

    /** 설정 화면에서 돌아왔을 때. */
    internal fun onReturnedFromSettings() {
        val grantedThisStep = when (pending) {
            Step.USAGE_ACCESS -> BlockingPermissions.hasUsageAccess(context)
            Step.OVERLAY -> BlockingPermissions.hasOverlay(context)
            null -> true
        }
        // 사용자가 허용하지 않고 돌아온 경우 같은 설정 화면을 다시 띄우지 않는다(무한 반복 방지).
        if (!grantedThisStep) {
            pending = null
            onFinished(false)
            return
        }
        advance()
    }

    private fun advance() {
        when {
            !BlockingPermissions.hasUsageAccess(context) -> {
                pending = Step.USAGE_ACCESS
                openSettings(
                    BlockingPermissions.usageAccessIntent(context),
                    BlockingPermissions.usageAccessIntentFallback(),
                )
            }
            !BlockingPermissions.hasOverlay(context) -> {
                pending = Step.OVERLAY
                openSettings(BlockingPermissions.overlayIntent(context), null)
            }
            else -> {
                pending = null
                BlockingStarter.startIfPermitted(context)
                onFinished(true)
            }
        }
    }
}

/**
 * @param onFinished 흐름이 끝났을 때 호출. true 면 두 권한이 모두 허용되어 엔진이 시작된 상태,
 *                   false 면 사용자가 중간에 허용하지 않고 돌아온 상태.
 */
@Composable
fun rememberBlockingPermissionRequest(
    onFinished: (granted: Boolean) -> Unit = {},
): BlockingPermissionRequest {
    val context = LocalContext.current
    val latestOnFinished by rememberUpdatedState(onFinished)

    // 런처 콜백은 request 를, request 는 런처를 필요로 해 서로를 참조한다.
    // 홀더를 한 단계 두어 순환을 끊는다(컴포지션 범위 안에 두어야 화면마다 독립).
    val holder = remember { mutableStateOf<BlockingPermissionRequest?>(null) }

    // 설정 화면은 결과를 돌려주지 않으므로 '돌아온 시점'을 신호로 삼아 다시 확인한다.
    val settingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { holder.value?.onReturnedFromSettings() }

    val request = remember(context) {
        BlockingPermissionRequest(
            context = context,
            openSettings = { intent, fallback ->
                // 기기가 앱별 딥링크를 처리하지 못하면 목록 화면으로 대체한다(크래시 방지).
                runCatching { settingsLauncher.launch(intent) }
                    .onFailure { fallback?.let { alt -> settingsLauncher.launch(alt) } }
            },
        )
    }
    holder.value = request
    request.onFinished = { latestOnFinished(it) }
    return request
}