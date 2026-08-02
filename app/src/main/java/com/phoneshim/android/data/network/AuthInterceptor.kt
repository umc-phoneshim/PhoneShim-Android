package com.phoneshim.android.data.network

import com.phoneshim.android.data.local.TokenProvider
import javax.inject.Inject
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor @Inject constructor(
    private val tokenProvider: TokenProvider,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requiresAuthentication = originalRequest.header(NO_AUTH_HEADER) == null
        val requestBuilder = originalRequest.newBuilder().removeHeader(NO_AUTH_HEADER)

        if (requiresAuthentication) {
            tokenProvider.getAccessToken()
                ?.takeIf(String::isNotBlank)
                ?.let { token -> requestBuilder.header(AUTHORIZATION, "Bearer $token") }
        }

        return chain.proceed(requestBuilder.build())
    }

    companion object {
        const val NO_AUTH_HEADER = "X-No-Authentication"
        const val AUTHORIZATION = "Authorization"
    }
}
