package com.phoneshim.android.domain.repository

/** OkHttp interceptor에서 suspend 호출 없이 현재 JWT를 읽기 위한 계약입니다. */
interface TokenProvider {
    fun getAccessToken(): String?
}
