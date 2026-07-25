package com.phoneshim.android.ui.features.appblocking

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.phoneshim.android.blocking.overlay.OverlayAction
import com.phoneshim.android.blocking.policy.BlockDecision
import com.phoneshim.android.ui.features.appblocking.screen.AppGoalAchievedDialogScreen
import com.phoneshim.android.ui.features.appblocking.screen.AppLimitReachedScreen
import com.phoneshim.android.ui.features.appblocking.screen.DailyGoalAchievedDialogScreen
import com.phoneshim.android.ui.features.appblocking.screen.DailyLimitReachedScreen
import com.phoneshim.android.ui.features.appblocking.screen.GoalAchievedScreen
import com.phoneshim.android.ui.features.appblocking.screen.UsageReasonSelectionScreen
import com.phoneshim.android.ui.theme.PhoneShimTheme

/**
 * BlockDecision → 차단 화면 배선.
 *
 *   - "어떤 판정에 어떤 화면을 그리는가" + "화면 안 상태"는 UI 일이라 여기 둡니다.
 *   - 엔진 패키지가 화면 6개를 import 하면 화면 이름만 바뀌어도 엔진 빌드가 깨지므로,
 *     그 커플링을 끊으려고 이쪽으로 옮겼습니다.
 */
@Composable
fun BlockOverlayContent(
    decision: BlockDecision,
    onAction: (OverlayAction) -> Unit,
) {
    PhoneShimTheme {
        // 차단이 떠 있는 동안 back 으로 뒤 앱에 빠져나가지 못하게 소비한다
        BackHandler(enabled = decision != BlockDecision.Allow) { /* 소비 */ }

        when (decision) {
            BlockDecision.Allow -> Unit

            BlockDecision.PhoneBlocked -> {
                var noticeConfirmed by remember(decision) { mutableStateOf(false) }
                if (!noticeConfirmed) {
                    DailyLimitReachedScreen(onConfirm = { noticeConfirmed = true })
                } else {
                    GoalAchievedScreen(
                        onCall = { onAction(OverlayAction.Call) },
                        onMessage = { onAction(OverlayAction.Message) },
                        onOpenPhoneShim = { onAction(OverlayAction.OpenPhoneShim) },
                    )
                }
            }

            BlockDecision.PhoneGoalReached ->
                DailyGoalAchievedDialogScreen(onConfirm = { onAction(OverlayAction.Dismiss) })

            is BlockDecision.AppBlocked ->
                AppLimitReachedScreen(
                    appName = decision.appLabel,
                    onConfirm = { onAction(OverlayAction.Dismiss) },
                )

            is BlockDecision.AppGoalReached ->
                AppGoalAchievedDialogScreen(
                    appName = decision.appLabel,
                    onConfirm = { onAction(OverlayAction.Dismiss) },
                )

            is BlockDecision.UsageReasonPrompt -> {
                var selectedReason by remember(decision) { mutableStateOf<String?>(null) }
                UsageReasonSelectionScreen(
                    appName = decision.appLabel,
                    selectedReason = selectedReason,
                    onReasonSelected = { selectedReason = it },
                    onComplete = {
                        onAction(
                            OverlayAction.ReasonSubmitted(
                                packageName = decision.packageName,
                                reason = selectedReason ?: "기타",
                            ),
                        )
                    },
                )
            }
        }
    }
}
