package com.phoneshim.android.blocking.demo

import com.phoneshim.android.blocking.policy.BlockDecision
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoOpDemoBlockTrigger @Inject constructor() : DemoBlockTrigger {
    override fun arm(packageName: String, appLabel: String) = Unit
    override fun decisionFor(foregroundPackage: String): BlockDecision? = null
    override fun consume(packageName: String) = Unit
    override fun reset() = Unit
}
