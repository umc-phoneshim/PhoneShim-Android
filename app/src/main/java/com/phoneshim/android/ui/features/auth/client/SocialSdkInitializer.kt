package com.phoneshim.android.ui.features.auth.client

import android.app.Application

interface SocialSdkInitializer {
    fun initialize(application: Application)
}
