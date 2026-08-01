package com.phoneshim.android.ui.features.auth.social

import android.app.Activity
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ForegroundActivityProvider @Inject constructor() {
    private var activityReference: WeakReference<Activity>? = null

    fun attach(activity: Activity) {
        activityReference = WeakReference(activity)
    }

    fun detach(activity: Activity) {
        if (activityReference?.get() === activity) {
            activityReference = null
        }
    }

    fun requireActivity(): Activity = activityReference?.get()
        ?: throw IllegalStateException("소셜 인증을 실행할 foreground Activity가 없습니다.")
}
