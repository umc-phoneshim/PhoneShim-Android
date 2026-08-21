package com.phoneshim.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.phoneshim.android.blocking.BlockingSessionCoordinator
import com.phoneshim.android.navigation.PhoneShimNavHost
import com.phoneshim.android.ui.features.auth.client.ForegroundActivityProvider
import com.phoneshim.android.data.realtime.ReminderSocketSessionCoordinator
import com.phoneshim.android.ui.theme.PhoneShimTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var foregroundActivityProvider: ForegroundActivityProvider
    @Inject
    lateinit var reminderSocketSessionCoordinator: ReminderSocketSessionCoordinator
    @Inject
    lateinit var blockingSessionCoordinator: BlockingSessionCoordinator

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightNavigationBars = true
        // 인증 세션이 복원된 뒤에만 차단 엔진을 시작하고,
        // 로그아웃·세션 만료 시에는 실행 중인 서비스와 오버레이를 즉시 내립니다.
        blockingSessionCoordinator.observe(lifecycleScope)
        setContent {
            PhoneShimTheme {
                PhoneShimNavHost(
                    navController = rememberNavController(),
                    reminderSocketSessionCoordinator = reminderSocketSessionCoordinator,
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        foregroundActivityProvider.attach(this)
        reminderSocketSessionCoordinator.onAppForegrounded()
    }

    override fun onStop() {
        foregroundActivityProvider.detach(this)
        super.onStop()
    }
}
