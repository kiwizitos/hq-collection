package com.kiwizitos.collection.data.remote

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

class GuiaAccessInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        val challenge = response.header("cf-mitigated").equals("challenge", ignoreCase = true)
        if (!response.isSuccessful || challenge) {
            Log.w("GuiaHttp", "HTTP ${response.code} ${response.request.url.encodedPath}; " +
                "challenge=$challenge; ray=${response.header("cf-ray").orEmpty()}")
        }
        if (challenge) {
            response.close()
            throw GuiaAccessChallengeException()
        }
        return response
    }
}
