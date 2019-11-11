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
object OkHttp3Client {

    private lateinit var client: OkHttpClient
    private var customClient: OkHttp3CustomClient? = null
    private var initialised: Boolean = false

    /**
     * Get the OkHttpClient with Approov included, but without any customization.
     *
     * @return The OkHttpClient instance.
     */
    fun getOkHttpClient(): OkHttpClient {
        return this.buildOkHttpClient()
    }

    /**
     * Get the OkHttpClient with Approov included, and with the custom client customization.
     *
     * @return The OkHttpClient instance.
     */
    fun getOkHttpClient(customClient: OkHttp3CustomClient): OkHttpClient {
        this.customClient = customClient
        return this.buildOkHttpClient()
    }

    /**
     * Rebuilds the OkHttpClient Builder.
     *
     * This is called when we want to ensure that the persisted instance for the client gets the
     *  new certificates pins, that where present in the last Approov Token fetch.
     *
     *  @return OkHttp3Client
.    */
    fun rebuild(): OkHttpClient {
        this.initialised = false
        return this.buildOkHttpClient()
    }

    /**
     * On first call it will build an OkHttpClient with:
     *
     * This instance is persisted in a private member for reuse in subsequent calls to this method.
     *
     * We can rebuild the persisted instance by calling `this.rebuild()`.
     *
     * @return OkHttpClient
     */
    private fun buildOkHttpClient(): OkHttpClient {

        if (this.initialised) {
            return this.client
        }

        if (this.customClient != null) {
            return this.buildWithApproov(this.customClient!!.customize(this.baseClient()))
        }

        return this.buildWithApproov(this.baseClient())
    }

    /**
     * It will build an OkHttpClient with:
     *  - The certificate pins retrieved from Approov.
     *  - The Approov interceptor to add the Approov Token header, and to trigger a rebuild of the
     *    OkHttp2Client when its detected an SSLPeerUnverifiedException.
     */
    private fun buildWithApproov(clientBuilder: OkHttpClient.Builder): OkHttpClient {

        this.client = clientBuilder
                .certificatePinner(this.buildCertificatePinner())
                .addInterceptor(OkHttp3RequestInterceptor())
                .build()

        this.initialised = true

        return this.client
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
