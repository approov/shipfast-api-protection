package com.criticalblue.approov.http.okhttp3

import okhttp3.OkHttpClient

/**
 * The data class for the `OkHttp3Client`.
 */
data class OkHttp3Client(
    val client: OkHttpClient,
    val hasNewInstance: Boolean
)