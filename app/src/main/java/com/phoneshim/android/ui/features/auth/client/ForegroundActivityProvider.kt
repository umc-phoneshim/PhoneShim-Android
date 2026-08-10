package com.phoneshim.android.ui.features.auth.client

import android.app.Activity
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ForegroundActivityProvider @Inject constructor() {
    // SDK 인증에는 Activity가 필요하지만 Singleton이 화면 인스턴스를 강하게 참조해 누수시키면 안 된다.
    private var activityReference: WeakReference<Activity>? = null

    fun attach(activity: Activity) {
        activityReference = WeakReference(activity)
    }

    fun detach(activity: Activity) {
        if (activityReference?.get() === activity) activityReference = null
    }

    fun requireActivity(): Activity = activityReference?.get()
        ?: throw IllegalStateException("소셜 인증을 실행할 foreground Activity가 없습니다.")
}
