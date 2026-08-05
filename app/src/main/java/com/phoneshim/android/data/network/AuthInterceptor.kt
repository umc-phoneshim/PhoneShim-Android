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
        // 로그인 API가 붙이는 내부 표식이며 실제 서버 요청에는 노출하지 않는다.
        val requiresAuthentication = originalRequest.header(NO_AUTH_HEADER) == null
        val requestBuilder = originalRequest.newBuilder().removeHeader(NO_AUTH_HEADER)

        if (requiresAuthentication) {
            // interceptor는 동기 API이므로 DataStore를 직접 읽지 않고 복원된 메모리 토큰만 사용한다.
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
