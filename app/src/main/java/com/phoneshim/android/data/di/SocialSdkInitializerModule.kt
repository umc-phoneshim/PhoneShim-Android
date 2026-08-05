package com.phoneshim.android.data.di

import com.phoneshim.android.ui.features.auth.client.SocialSdkInitializer
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.Multibinds

@Module
@InstallIn(SingletonComponent::class)
abstract class SocialSdkInitializerModule {
    @Multibinds
    abstract fun socialSdkInitializers(): Set<SocialSdkInitializer>
}
