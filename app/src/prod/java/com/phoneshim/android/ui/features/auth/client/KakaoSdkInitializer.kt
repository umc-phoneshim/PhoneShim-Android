package com.phoneshim.android.ui.features.auth.client

import android.app.Application
import com.kakao.sdk.common.KakaoSdk
import com.phoneshim.android.BuildConfig
import javax.inject.Inject

class KakaoSdkInitializer @Inject constructor() : SocialSdkInitializer {
    override fun initialize(application: Application) {
        if (BuildConfig.KAKAO_NATIVE_APP_KEY.isNotBlank()) {
            KakaoSdk.init(application, BuildConfig.KAKAO_NATIVE_APP_KEY)
        }
    }
}
