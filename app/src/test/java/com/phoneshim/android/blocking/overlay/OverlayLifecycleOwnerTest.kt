package com.phoneshim.android.blocking.overlay

import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class OverlayLifecycleOwnerTest {

    @Before
    fun setUp() {
        ArchTaskExecutor.getInstance().setDelegate(
            object : TaskExecutor() {
                override fun executeOnDiskIO(runnable: Runnable) = runnable.run()

                override fun postToMainThread(runnable: Runnable) = runnable.run()

                override fun isMainThread(): Boolean = true
            },
        )
    }

    @After
    fun tearDown() {
        ArchTaskExecutor.getInstance().setDelegate(null)
    }

    @Test
    fun `오버레이 종료 시 ViewModelStore를 비운다`() {
        val owner = OverlayLifecycleOwner()
        val viewModel = TrackingViewModel()
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = viewModel as T
        }

        owner.onCreate()
        val stored = ViewModelProvider(owner, factory)[TrackingViewModel::class.java]

        assertFalse(stored.wasCleared)
        owner.onDestroy()
        assertTrue(stored.wasCleared)
    }

    private class TrackingViewModel : ViewModel() {
        var wasCleared = false

        override fun onCleared() {
            wasCleared = true
        }
    }
}
