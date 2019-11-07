/*****************************************************************************
 * Project:     Approov Kotlin Framework
 * Copyright(c) 2019 by CriticalBlue Ltd.
 *****************************************************************************/

package com.criticalblue.approov.http.okhttp3

import android.util.Log
import com.criticalblue.approov.ApproovFramework
import com.criticalblue.approovsdk.Approov
import java.util.concurrent.TimeUnit
import okhttp3.*

/**
 * This object is responsible to build and rebuild an `OkHttpClient` instance with a Certificate
 *  Pinner and Approov interceptors.
 */
object OkHttp3ClientBuilder {

    private lateinit var builder: OkHttpClient.Builder
    private var initialised: Boolean = false

    /**
     * On first call it will build an OkHttpClient.Builder with the certificate pins retrieved from
     *  Approov.
     *
     * This instance is persisted in a private member for reuse in subsequent calls to this method.
     *
     * We can rebuild the persisted instance by calling `this.rebuild()`.
     *
     * @return OkHttpClient
     */
    fun getOkHttpClientBuilder(): OkHttpClient.Builder {

        if (this.initialised) {
            return this.builder
        }

        // now we can construct the OkHttpClient with the correct pins preset
        this.builder = baseClient()
                .certificatePinner(this.buildCertificatePinner())
                .addInterceptor(OkHttp3RequestInterceptor())
                .addInterceptor(OkHttp3CertificatePinningExceptionInterceptor())

        this.initialised = true

        return this.builder
    }

    /**
     * Rebuilds the OkHttpClient Builder.
     *
     * This is called when we want to ensure that the persisted instance for the builder gets the
     *  new certificates pins, that where present in the last Approov Token fetch.
     *
     *  @return OkHttp3Client
.    */
    fun rebuild(): OkHttpClient.Builder {
        this.initialised = false
        return this.getOkHttpClientBuilder()
    }

    /**
     * Builds a base `OkHttpClient`.
     *
     * @return OkHttpClient.Builder
     */
    private fun baseClient(): OkHttpClient.Builder {
        return OkHttpClient.Builder()
                .connectTimeout(2, TimeUnit.SECONDS)
                .readTimeout(2, TimeUnit.SECONDS)
                .writeTimeout(2, TimeUnit.SECONDS)
    }

    /**
     * Builds the `CertificatePinner` with all the pins returned from the Approov SDK.
     * Currently the pins returned are for all the API domains registered in the Approov account.
     *
     * @return CertificatePinner
     */
    private fun buildCertificatePinner(): CertificatePinner {

        var pinBuilder: CertificatePinner.Builder = CertificatePinner.Builder()

        // Get the certificate pins from the Approov SDK, and currently this means the pins for all
        //  API domains registered in the Approov account.
        val pins: Map<String, List<String>> = Approov.getPins("public-key-sha256")

        for ((key, value) in pins) {
            for (pin in value) {
                pinBuilder.add(key, "sha256/$pin")
                Log.i(ApproovFramework.TAG, "Adding to OkHttp the pin $key:sha256/$pin")
            }
        }

        return pinBuilder.build()
    }
}
