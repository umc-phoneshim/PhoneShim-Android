package com.phoneshim.android.ui.features.appblocking.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.phoneshim.android.domain.repository.UsageReasonRepository

class UsageReasonViewModelFactory(
    private val repository: UsageReasonRepository,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UsageReasonViewModel::class.java)) {
            return UsageReasonViewModel(repository) as T
        }
        throw IllegalArgumentException("지원하지 않는 ViewModel입니다: ${modelClass.name}")
    }
}
