package com.criticalblue.approov.http.okhttp3

import okhttp3.OkHttpClient

interface OkHttp3ClientCustomizer {
   fun customize(builder: OkHttpClient.Builder): OkHttpClient.Builder
}