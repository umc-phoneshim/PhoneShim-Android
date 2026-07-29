package com.phoneshim.android.ui.features.appblocking

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.phoneshim.android.blocking.overlay.OverlayAction
import com.phoneshim.android.blocking.policy.BlockDecision
import com.phoneshim.android.ui.features.appblocking.di.UsageReasonEntryPoint
import com.phoneshim.android.ui.features.appblocking.screen.AppGoalAchievedDialogScreen
import com.phoneshim.android.ui.features.appblocking.screen.AppLimitReachedScreen
import com.phoneshim.android.ui.features.appblocking.screen.DailyGoalAchievedDialogScreen
import com.phoneshim.android.ui.features.appblocking.screen.DailyLimitReachedScreen
import com.phoneshim.android.ui.features.appblocking.screen.GoalAchievedScreen
import com.phoneshim.android.ui.features.appblocking.screen.UsageReasonSelectionScreen
import com.phoneshim.android.ui.features.appblocking.viewmodel.UsageReasonUiEffect
import com.phoneshim.android.ui.features.appblocking.viewmodel.UsageReasonViewModel
import com.phoneshim.android.ui.features.appblocking.viewmodel.UsageReasonViewModelFactory
import com.phoneshim.android.ui.theme.PhoneShimTheme
import dagger.hilt.android.EntryPointAccessors

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
    val applicationContext = LocalContext.current.applicationContext
    val repository = remember(applicationContext) {
        /*
         * 오버레이 ComposeView는 Activity의 Hilt ViewModel 생성 경로 밖에 있다.
         * ApplicationContext에서 SingletonComponent EntryPoint를 조회하면 Service나
         * OverlayManager에 Repository/Factory 파라미터를 추가하지 않고도 동일한
         * 애플리케이션 범위 Repository를 안전하게 얻을 수 있다.
         */
        EntryPointAccessors.fromApplication(
            applicationContext,
            UsageReasonEntryPoint::class.java,
        ).usageReasonRepository()
    }
    val viewModelFactory = remember(repository) {
        /*
         * recomposition마다 새 Factory 객체를 만들 필요가 없으며, 같은 Repository를
         * 사용하는 동안 Factory의 정체성을 유지하면 ViewModel 생성 경계도 명확해진다.
         */
        UsageReasonViewModelFactory(repository)
    }

    /*
     * viewModel()은 별도 owner를 지정하지 않으면 ComposeView의 ViewTree에서
     * ViewModelStoreOwner를 찾는다. BlockOverlayManager가 setContent 전에 심어 둔
     * OverlayLifecycleOwner가 선택되므로 Activity가 아니라 오버레이와 생명주기를 같이한다.
     */
    val usageReasonViewModel = viewModel<UsageReasonViewModel>(
        factory = viewModelFactory,
    )
    val usageReasonState by usageReasonViewModel.uiState.collectAsState()

    LaunchedEffect(decision) {
        if (decision is BlockDecision.UsageReasonPrompt) {
            usageReasonViewModel.startSession(
                sessionId = decision.usageReasonSessionId(),
                packageName = decision.packageName,
                appName = decision.appLabel,
            )
        }
    }

    LaunchedEffect(usageReasonViewModel, decision) {
        usageReasonViewModel.effect.collect { effect ->
            when (effect) {
                is UsageReasonUiEffect.ReasonSubmitted -> {
                    val activePrompt = decision as? BlockDecision.UsageReasonPrompt
                    if (
                        activePrompt != null &&
                        effect.sessionId == activePrompt.usageReasonSessionId()
                    ) {
                        onAction(
                            OverlayAction.ReasonSubmitted(
                                packageName = effect.submission.packageName,
                                reason = effect.submission.reason,
                            ),
                        )
                    }
                }
            }
        }
    }

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
                val isCurrentSession =
                    usageReasonState.sessionId == decision.usageReasonSessionId()
                UsageReasonSelectionScreen(
                    appName = decision.appLabel,
                    selectedReason = usageReasonState.selectedReason.takeIf { isCurrentSession },
                    onReasonSelected = usageReasonViewModel::selectReason,
                    onComplete = usageReasonViewModel::submitReason,
                    reasons = usageReasonState.reasons,
                    isSaving = isCurrentSession && usageReasonState.isSaving,
                    errorMessage = usageReasonState.errorMessage.takeIf { isCurrentSession },
                )
            }
        }
    }
}

private fun BlockDecision.UsageReasonPrompt.usageReasonSessionId(): String =
    "usage-reason:$packageName"
