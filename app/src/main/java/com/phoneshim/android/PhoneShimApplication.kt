package com.phoneshim.android

import android.app.Application
import com.phoneshim.android.ui.features.auth.client.SocialSdkInitializer
import com.phoneshim.android.data.realtime.ReminderSocketSessionCoordinator
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class PhoneShimApplication : Application() {
    @Inject
    lateinit var socialSdkInitializers: Set<@JvmSuppressWildcards SocialSdkInitializer>
    @Inject
    lateinit var reminderSocketSessionCoordinator: ReminderSocketSessionCoordinator

    override fun onCreate() {
        super.onCreate()
        // main은 특정 SDK를 알지 않고 prod flavor가 등록한 initializer만 실행한다.
        socialSdkInitializers.forEach { initializer -> initializer.initialize(this) }
        reminderSocketSessionCoordinator.start()
    }
}
