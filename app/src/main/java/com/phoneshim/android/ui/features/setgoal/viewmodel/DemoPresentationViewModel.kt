package com.phoneshim.android.ui.features.setgoal.viewmodel

import androidx.lifecycle.ViewModel
import com.phoneshim.android.blocking.demo.DemoBlockTrigger
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DemoPresentationViewModel @Inject constructor(
    private val trigger: DemoBlockTrigger,
) : ViewModel() {
    fun arm(packageName: String, appLabel: String) = trigger.arm(packageName, appLabel)
}
