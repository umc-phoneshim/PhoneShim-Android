package com.phoneshim.android.blocking.demo

import android.content.Context
import com.phoneshim.android.blocking.policy.BlockDecision
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PersistentDemoBlockTrigger @Inject constructor(
    @ApplicationContext context: Context,
) : DemoBlockTrigger {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun arm(packageName: String, appLabel: String) {
        preferences.edit().putString(KEY_PACKAGE, packageName).putString(KEY_LABEL, appLabel).apply()
    }

    override fun decisionFor(foregroundPackage: String): BlockDecision? {
        val target = preferences.getString(KEY_PACKAGE, null) ?: return null
        if (foregroundPackage != target) return null
        val label = preferences.getString(KEY_LABEL, foregroundPackage).orEmpty()
        return BlockDecision.AppBlocked(target, label)
    }

    override fun consume(packageName: String) {
        if (preferences.getString(KEY_PACKAGE, null) == packageName) reset()
    }

    override fun reset() = preferences.edit().clear().apply()

    private companion object {
        const val PREFERENCES_NAME = "phoneshim_demo_block_trigger"
        const val KEY_PACKAGE = "package"
        const val KEY_LABEL = "label"
    }
}
