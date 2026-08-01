package com.phoneshim.android

import android.app.Application
import com.phoneshim.android.ui.features.auth.client.SocialSdkInitializer
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class PhoneShimApplication : Application() {
    @Inject
    lateinit var socialSdkInitializers: Set<@JvmSuppressWildcards SocialSdkInitializer>

    override fun onCreate() {
        super.onCreate()
        socialSdkInitializers.forEach { initializer -> initializer.initialize(this) }
    }
}
