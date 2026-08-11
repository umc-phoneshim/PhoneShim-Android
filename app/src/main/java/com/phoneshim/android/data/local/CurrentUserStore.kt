package com.phoneshim.android.data.local

import com.phoneshim.android.domain.model.User
import com.phoneshim.android.domain.repository.CurrentUserRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class CurrentUserStore @Inject constructor() : CurrentUserRepository {
    private val _user = MutableStateFlow<User?>(null)
    override val user: StateFlow<User?> = _user.asStateFlow()

    override fun update(user: User) {
        _user.value = user
    }

    override fun clear() {
        _user.value = null
    }
}
