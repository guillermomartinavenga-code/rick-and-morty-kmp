package com.example.rickandmorty.data.remote

import android.util.Log
import com.example.rickandmorty.BuildConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.engine.ProxyBuilder
import io.ktor.http.Url
import okhttp3.Authenticator
import okhttp3.Credentials

private val USE_PROXY = BuildConfig.DEBUG

actual fun httpClientEngine(): HttpClientEngine = OkHttp.create {
    if (USE_PROXY) {
        proxy = ProxyBuilder.http(Url(
            urlString = "http://${BuildConfig.PROXY_HOST}:${BuildConfig.PROXY_PORT}")
        )

        config {
            proxyAuthenticator { _, response ->
                val credential = Credentials.basic(
                    username = BuildConfig.PROXY_USER,
                    password = BuildConfig.PROXY_PASSWORD
                )
                response.request.newBuilder()
                    .header("Proxy-Authorization", credential)
                    .build()
            }
        }
    }
}