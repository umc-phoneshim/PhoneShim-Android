package com.phoneshim.android.blocking.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.WindowManager
import androidx.activity.setViewTreeOnBackPressedDispatcherOwner
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.phoneshim.android.blocking.policy.BlockDecision
import com.phoneshim.android.ui.features.appblocking.BlockOverlayContent

/**
 * WindowManager 로 전체화면 ComposeView 를 붙였다 뗐다 한다..
 */
class BlockOverlayManager(
    private val context: Context,
    private val onAction: (OverlayAction) -> Unit,
) {
    private val windowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private var composeView: ComposeView? = null
    private var lifecycleOwner: OverlayLifecycleOwner? = null
    private val decisionState = mutableStateOf<BlockDecision>(BlockDecision.Allow)

    val isShowing: Boolean get() = composeView != null

    fun show(decision: BlockDecision) {
        decisionState.value = decision
        if (composeView != null) return
        attach()
    }

    fun hide() {
        val view = composeView ?: return
        runCatching { windowManager.removeView(view) }
        lifecycleOwner?.onDestroy()
        composeView = null
        lifecycleOwner = null
        decisionState.value = BlockDecision.Allow
    }

    private fun attach() {
        val owner = OverlayLifecycleOwner().apply { onCreate() }
        val view = ComposeView(context).apply {
            setViewTreeLifecycleOwner(owner)
            setViewTreeViewModelStoreOwner(owner)
            setViewTreeSavedStateRegistryOwner(owner)
            setViewTreeOnBackPressedDispatcherOwner(owner)
            setContent {
                BlockOverlayContent(
                    decision = decisionState.value,
                    onAction = onAction,
                )
            }
        }
        runCatching { windowManager.addView(view, layoutParams()) }
            .onSuccess {
                composeView = view
                lifecycleOwner = owner
            }
            .onFailure { owner.onDestroy() }
    }

    private fun layoutParams(): WindowManager.LayoutParams {
        val type =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }
        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            type,
            // 차단 화면은 뒤 앱 입력을 완전히 가로채야 한다.
            //  - FLAG_NOT_TOUCH_MODAL 없음 + 전체화면 → 바깥 터치가 뒤로 새지 않음
            //  - focusable(=NOT_FOCUSABLE 안 줌) → 키 이벤트 수신. back 은 Compose BackHandler 가 소비
            //
            // FLAG_KEEP_SCREEN_ON 은 쓰지 않는다. 쿼터 차단은 자정까지 안 풀리므로,
            // 사용자가 확인을 누르지 않고 폰을 내려놓으면 화면이 계속 켜진 채로 남는다.
            // BlockerService 는 SCREEN_OFF 에서 이 오버레이를 내리고 폴링을 멈추도록
            // 짜여 있는데, 화면이 안 꺼지면 그 경로가 돌지 않는다.
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.OPAQUE,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }
    }
}
