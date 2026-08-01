package com.phoneshim.android.data.demo

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoOpDemoScenarioResetter @Inject constructor() : DemoScenarioResetter {
    override suspend fun reset() = Unit
}
