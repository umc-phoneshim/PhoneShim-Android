package com.phoneshim.android.blocking.policy

import com.phoneshim.android.blocking.demo.DemoBlockTrigger
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class BlockPolicyEngineDemoTest {
    @Test
    fun `armed demo app is blocked before regular policy evaluation`() = runTest {
        val trigger = FakeDemoBlockTrigger("com.example.video", "동영상")
        val engine = BlockPolicyEngine(EmptyGoalProvider, EmptyScheduleProvider, trigger)

        val decision = engine.decide("com.example.video", 0, 0, reasonAlreadyAsked = false)

        assertEquals(BlockDecision.AppBlocked("com.example.video", "동영상"), decision)
    }

    @Test
    fun `phoneshim remains allowed even if demo trigger targets it`() = runTest {
        val trigger = FakeDemoBlockTrigger("com.phoneshim.android", "폰쉼")
        val engine = BlockPolicyEngine(EmptyGoalProvider, EmptyScheduleProvider, trigger)

        val decision = engine.decide("com.phoneshim.android", 999, 999, reasonAlreadyAsked = false)

        assertEquals(BlockDecision.Allow, decision)
    }
}

private object EmptyGoalProvider : BlockingPolicyProvider {
    override suspend fun phoneGoal(): PhoneGoalPolicy? = null
    override suspend fun watchedApps(): List<AppBlockingPolicy> = emptyList()
}

private object EmptyScheduleProvider : SchedulePolicyProvider {
    override suspend fun activeScheduleBlock(): ScheduleBlock = ScheduleBlock.None
}

private class FakeDemoBlockTrigger(
    private var packageName: String?,
    private val label: String,
) : DemoBlockTrigger {
    override fun arm(packageName: String, appLabel: String) {
        this.packageName = packageName
    }

    override fun decisionFor(foregroundPackage: String): BlockDecision? =
        packageName?.takeIf { it == foregroundPackage }?.let { BlockDecision.AppBlocked(it, label) }

    override fun consume(packageName: String) {
        if (this.packageName == packageName) this.packageName = null
    }

    override fun reset() {
        packageName = null
    }
}
