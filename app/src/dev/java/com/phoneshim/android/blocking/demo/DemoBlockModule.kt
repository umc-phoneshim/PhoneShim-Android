package com.phoneshim.android.blocking.demo

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DemoBlockModule {
    @Binds @Singleton abstract fun bindDemoBlockTrigger(impl: NoOpDemoBlockTrigger): DemoBlockTrigger
}
