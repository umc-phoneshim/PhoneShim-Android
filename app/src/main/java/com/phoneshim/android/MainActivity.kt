package com.phoneshim.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.phoneshim.android.blocking.BlockingStarter
import com.phoneshim.android.navigation.PhoneShimNavHost
import com.phoneshim.android.ui.theme.PhoneShimTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 권한이 이미 허용돼 있으면 차단 엔진 복구
        // 권한이 없으면 아무 일도 하지 않는다.
        BlockingStarter.startIfPermitted(this)
        setContent {
            PhoneShimTheme {
                PhoneShimNavHost(navController = rememberNavController())
            }
        }
    }
}
