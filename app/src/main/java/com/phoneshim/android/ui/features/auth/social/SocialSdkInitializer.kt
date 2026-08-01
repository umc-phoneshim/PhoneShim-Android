package com.phoneshim.android.ui.features.auth.social

import android.app.Application

interface SocialSdkInitializer {
    fun initialize(application: Application)
}
