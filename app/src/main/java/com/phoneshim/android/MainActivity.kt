package com.phoneshim.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.phoneshim.android.navigation.PhoneShimNavHost
import com.phoneshim.android.ui.theme.PhoneShimTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PhoneShimTheme {
                PhoneShimNavHost(navController = rememberNavController())
            }
        }
    }
}
