package com.phoneshim.android.blocking.overlay


import androidx.activity.OnBackPressedDispatcher
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner

/**
 * WindowManager 로 붙인 ComposeView 는 Activity 밖이라
 * ViewTreeLifecycleOwner / SavedStateRegistry / OnBackPressedDispatcher 가 없어 크래시.
 * 이 owner 를 ComposeView 에 심어 해결한다.
 *
 */
class OverlayLifecycleOwner :
    LifecycleOwner,
    ViewModelStoreOwner,
    SavedStateRegistryOwner,
    OnBackPressedDispatcherOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateController = SavedStateRegistryController.create(this)
    override val viewModelStore = ViewModelStore()

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateController.savedStateRegistry

    // BackHandler 가 소비할 dispatcher. 오버레이 안에서 back 을 여기로 받는다.
    override val onBackPressedDispatcher = OnBackPressedDispatcher()

    fun onCreate() {
        savedStateController.performRestore(null)
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    fun onDestroy() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        /*
         * WindowManager 오버레이는 Activity의 onDestroy를 거치지 않으므로 owner가 직접
         * ViewModelStore를 비워야 한다. 이 호출로 UsageReasonViewModel.onCleared()와
         * viewModelScope 취소가 보장되어 닫힌 오버레이의 저장 작업이 남지 않는다.
         */
        viewModelStore.clear()
    }
}
