/*****************************************************************************
 * Project:     Approov Kotlin Framework
 * Copyright(c) 2019 by CriticalBlue Ltd.
 *****************************************************************************/

package com.criticalblue.approov.http.okhttp3

import okhttp3.Interceptor
import okhttp3.Response
import com.criticalblue.approovsdk.Approov
import com.criticalblue.approovsdk.Approov.TokenFetchStatus.*
import com.criticalblue.approov.ApproovFramework
import com.criticalblue.approov.exceptions.ApproovIOFatalException
import com.criticalblue.approov.exceptions.ApproovIOTransientException

/**
 * The OkHttp3RequestInterceptor class is responsible for intercepting HTTP requests and to add the
 *  Approov token header, and if a new dynamic config is present in the Approov token fetch result,
 *  it also triggers an update to the persisted one.
 */
class OkHttp3RequestInterceptor : Interceptor {

    /**
     * Intercept the given request chain to add the Approov token to an 'Approov-Token' header.
     *
     * When the Approv token has a new Approov dynamic configuration we save the new configuration.
     *
     * Based on the result status of fetching an Approov token we will add or not the Approov token
     *  header, or throw an ApproovIOTransientException or an ApproovIOFatalException.
     *
     * It will add the Approov Token header:
     *  - SUCCESS
     *  - NO_APPROOV_SERVICE
     *
     * It will NOT add the Approov Token header:
     *  - BAD_URL
     *  - UNKNOWN_URL
     *
     * @throws ApproovIOTransientException For NO_NETWORK, POOR_NETWORK or MITM_DETECTED status.
     * @throws ApproovIOFatalException     For any other status.
     *
     * @param  chain    The request chain to intercept and modify.
     *
     * @return Response The response authenticated by Approov.
     */
    override fun intercept(chain: Interceptor.Chain): Response {

        var request = chain.request()

        val tokenBindingHeader: String? = ApproovFramework.getTokenBindingHeader()

        if (tokenBindingHeader != null) {

            if (! request.headers().names().contains(tokenBindingHeader)) {
                throw ApproovIOFatalException("Fatal Error: Token binding header not found: $tokenBindingHeader")
            }

            val tokenBinding: String = request.header(tokenBindingHeader).toString()

            Approov.setDataHashInToken(tokenBinding)
        }

        val approovResult = Approov.fetchApproovTokenAndWait(request.url().toString())

        if (approovResult.isConfigChanged) {
            ApproovFramework.saveDynamicConfig()

            // We will clearClient the OkHttp3Client with the new certificate pins, that will be fetched
            //  from the Approov dynamic config.
            OkHttp3Client.clearClient()
        }

        when (approovResult.status) {
            SUCCESS, NO_APPROOV_SERVICE -> {
                // Check why we consider it here as a valid status at https://approov.io/docs/v2.0/approov-usage-documentation/#token-fetch-errors
                request = request.newBuilder().addHeader("Approov-Token", approovResult.token).build()
            }
            BAD_URL, UNKNOWN_URL -> {
                // Continues the original request chain, but without the Approov token injected in the header.
            }
            NO_NETWORK, POOR_NETWORK, MITM_DETECTED -> {
                // The developer needs to catch this transient error in order to retry the request.
                throw ApproovIOTransientException("Transient Error: ${approovResult.status}")
            }
            else -> {
                // There has been some fatal error event that should be reported
                throw ApproovIOFatalException("Fatal Error: ${approovResult.status}")
            }
        }

        return chain.proceed(request)
    }
}