package com.phoneshim.android.ui.features.appblocking.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.phoneshim.android.R
import com.phoneshim.android.ui.features.appblocking.component.BlockingDialog
import com.phoneshim.android.ui.features.appblocking.component.BlockingOverlay
import com.phoneshim.android.ui.theme.PhoneShimTheme

@Composable
fun DailyLimitReachedScreen(onConfirm: () -> Unit, modifier: Modifier = Modifier) {
    BlockingDialogScreen(modifier) {
        BlockingDialog(
            title = "오늘도 수고했어요!",
            descriptions = listOf("설정한 목표 시간이 끝났어요.", "이제 잠시 휴식을 가져볼까요?", "내일 다시 사용할 수 있어요."),
            buttonText = "확인",
            onConfirm = onConfirm,
        )
    }
}

@Composable
fun DailyGoalAchievedDialogScreen(onConfirm: () -> Unit, modifier: Modifier = Modifier) {
    BlockingDialogScreen(modifier) {
        BlockingDialog(
            title = "목표 달성!",
            descriptions = listOf("오늘 목표 시간을 모두 채웠어요.", "조금만 휴대폰을 내려두고", "잠시 쉬어볼까요?"),
            buttonText = "좋아요",
            onConfirm = onConfirm,
            illustrationRes = R.drawable.appblocking_mascot_daily_goal,
            illustrationContentDescription = "목표 달성을 기뻐하는 폰쉼 마스코트",
        )
    }
}

@Composable
fun AppGoalAchievedDialogScreen(appName: String, onConfirm: () -> Unit, modifier: Modifier = Modifier) {
    BlockingDialogScreen(modifier) {
        BlockingDialog(
            title = "목표 달성!",
            descriptions = listOf("오늘 $appName 사용 시간을 모두 채웠어요.", "조금만 휴대폰을 내려두고", "잠시 쉬어볼까요?"),
            buttonText = "좋아요",
            onConfirm = onConfirm,
            illustrationRes = R.drawable.appblocking_mascot_app_goal,
            illustrationContentDescription = "앱 목표 달성을 축하하는 폰쉼 마스코트",
        )
    }
}

@Composable
fun AppLimitReachedScreen(appName: String, onConfirm: () -> Unit, modifier: Modifier = Modifier) {
    BlockingDialogScreen(modifier) {
        BlockingDialog(
            title = "$appName 사용 제한",
            descriptions = listOf("설정한 ${appName}의 목표 시간이 끝났어요.", "내일 다시 사용할 수 있어요!"),
            buttonText = "확인",
            onConfirm = onConfirm,
        )
    }
}

@Composable
private fun BlockingDialogScreen(
    modifier: Modifier,
    content: @Composable () -> Unit,
) {
    BlockingOverlay(modifier = modifier, content = content)
}

@Preview(name = "폰 전체 제한", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun DailyLimitReachedScreenPreview() {
    PhoneShimTheme { DailyLimitReachedScreen(onConfirm = {}) }
}

@Preview(name = "폰 목표 시간 알림", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun DailyGoalAchievedDialogScreenPreview() {
    PhoneShimTheme { DailyGoalAchievedDialogScreen(onConfirm = {}) }
}

@Preview(name = "주의 앱 목표 시간 알림", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun AppGoalAchievedDialogScreenPreview() {
    PhoneShimTheme {
        AppGoalAchievedDialogScreen(appName = "인스타그램", onConfirm = {})
    }
}

@Preview(name = "주의 앱 사용 제한", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun AppLimitReachedScreenPreview() {
    PhoneShimTheme {
        AppLimitReachedScreen(appName = "인스타그램", onConfirm = {})
    }
}
