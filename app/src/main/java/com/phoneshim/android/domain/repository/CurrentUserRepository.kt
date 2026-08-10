package com.phoneshim.android.domain.repository

import com.phoneshim.android.domain.model.User
import kotlinx.coroutines.flow.StateFlow

interface CurrentUserRepository {
    val user: StateFlow<User?>
    fun update(user: User)
    fun clear()
}
