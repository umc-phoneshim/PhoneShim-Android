package com.phoneshim.android.blocking.demo

import com.phoneshim.android.blocking.policy.BlockDecision

interface DemoBlockTrigger {
    fun arm(packageName: String, appLabel: String)
    fun decisionFor(foregroundPackage: String): BlockDecision?
    fun consume(packageName: String)
    fun reset()
}
