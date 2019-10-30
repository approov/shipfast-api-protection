/*****************************************************************************
 * Project:     ShipFast API Protection (App)
 * File:        OkHttp3RequestInterceptor.kt
 * Original:    Created on 30 Oct 2017 by Simon Rigg
 * Copyright(c) 2002 - 2017 by CriticalBlue Ltd.
 *
 * An HTTP request Interceptor used to add Approov tokens to requests.
 *****************************************************************************/

package com.criticalblue.approov.http.okhttp3

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import com.criticalblue.approov.ApproovSdk
import com.criticalblue.approov.ApproovSdk.TAG

/**
 * The OkHttp3RequestInterceptor class is responsible for intercepting HTTP
 *  requests and adding the Approov token, and if necessary to cancel the request chain and execute
 *  a new OkHttpClient call.
 */
class OkHttp3RequestInterceptor : Interceptor {

    /**
     * Intercept the given request chain to add the Approov token to an 'Approov-Token' header.
     * When the Approv token has a new Approov dynamic configuration we interrupt the current
     *  request chain and execute a new call with the new OkHttpClient, that was automatically
     *  rebuilt after the Approov token fetch.
     *
     * @param  chain    The request chain to intercept and modify.
     *
     * @return Response The modified response, authenticated by Approov.
     */
    override fun intercept(chain: Interceptor.Chain): Response {

        val originalRequest = chain.request()

        val url = originalRequest.url().toString()
        Log.i(TAG, "API REQUEST URL: $url")

        val approovToken = ApproovSdk.fetchApproovTokenAndWait(url)
        Log.i(TAG, "APPROOV TOKEN: $approovToken")

        val approovRequest = originalRequest.newBuilder()
                .addHeader("Approov-Token", approovToken.token)
                .build()

        if (approovToken.hasNewConfig) {
            // When the Approov dynamic config changes the OkHttp3Client is automatically rebuilt,
            //  because we may have a new certificate pin.
            val okHttp3Client = OkHttp3ClientBuilder.buildWithApproov()
            Log.i(TAG, "Approov dynamic config change detected. Request will be executed with a new OkHttp3Client call.")

            // Now that we have a new instance of the OkHttp3Client we cannot proceed with the
            //  current request chain. Instead we will execute a new call with the Approov token
            //  injected in the header.
            return okHttp3Client.newCall(approovRequest).execute()
        }

        // Continues the original request chain, but now with the Approov token injected in the header.
        return chain.proceed(approovRequest)
    }
}