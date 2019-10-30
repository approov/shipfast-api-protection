package com.criticalblue.approov.http.okhttp3

import com.criticalblue.approov.dto.ApproovToken
import java.util.concurrent.TimeUnit
import okhttp3.*

/**
 * This object is responsible to build and rebuild an `OkHttpClient` instance with or without Appoov.
 */
object OkHttp3ClientBuilder {

    private lateinit var okHttp3BaseClient: OkHttpClient
    private lateinit var okHttp3ApproovClient: OkHttpClient
    private var initialisedWithApproov: Boolean = false
    private var initialisedWithoutApproov: Boolean = false

    /**
     * Build a default `OkHttpClient` to use for API requests.
     *
     * @return OkHttpClient
     */
     fun buildWithoutApproov(): OkHttpClient {

        if (this.initialisedWithoutApproov) {
            return this.okHttp3BaseClient
        }

        this.okHttp3BaseClient = baseClient().build()

        this.initialisedWithoutApproov = true

        return this.okHttp3BaseClient
    }

    /**
     * On first call it will build an OkHttpClient with the certificate pins retrieved from the
     *  Approov dynamic configuration.
     * This instance is persisted in a private member for reuse in subsequent calls to this method.
     * We can rebuild the persisted instance by calling `this.rebuildWithApproovWhenConfigChanges(approovToken: ApproovToken)`
     *
     * @return OkHttpClient
     */
    fun buildWithApproov(): OkHttpClient {

        if (this.initialisedWithApproov) {
            return this.okHttp3ApproovClient
        }

        // now we can construct the OkHttpClient with the correct pins preset
        this.okHttp3ApproovClient = baseClient()
                .certificatePinner(OkHttp3CertificatePinner().build())
                .addInterceptor(OkHttp3RequestInterceptor())
                .build()

        this.initialisedWithApproov = true

        return this.okHttp3ApproovClient
    }

    /**
     * Rebuilds the persisted instance with Approov, but just if the Approov dynamic configuration
     *  have changed in the given `ApproovToken` data object.
     *
     *  @param  approovToken The data object for the `ApproovToken`.
     *
     *  @return OkHttp3Client
.    */
    fun rebuildWithApproovWhenConfigChanges(approovToken: ApproovToken): OkHttp3Client {

        if (approovToken.hasNewConfig) {
            this.initialisedWithApproov = false
            return OkHttp3Client(this.buildWithApproov(), true)
        }

        return OkHttp3Client(this.okHttp3ApproovClient, false)
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
}
