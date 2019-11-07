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
     * @param  chain    The request chain to intercept and modify.
     *
     * @return Response The response authenticated by Approov.
     */
    override fun intercept(chain: Interceptor.Chain): Response {

        val originalRequest = chain.request()
        val url = originalRequest.url().toString()
        val approovResult = Approov.fetchApproovTokenAndWait(url)
        val approovStatus = approovResult.status
        val approovToken = approovResult.token

        if (approovResult.isConfigChanged) {
            ApproovFramework.saveDynamicConfig()
        }

        if (this.isToAddApproovHeader(approovStatus)) {
            return chain.proceed(originalRequest.newBuilder()
                    .addHeader("Approov-Token", approovToken)
                    .build())
        }

        // Continues the original request chain, but without the Approov token injected in the header.
        return chain.proceed(originalRequest)
    }

    /**
     * Based on the result status of fetching an Approov token we will return true, false or throw
     *  a ApproovIOTransientException or a ApproovIOFatalException.
     *
     * It will return true:
     *  - SUCCESS
     *  - NO_APPROOV_SERVICE
     *
     * It will return false:
     *  - BAD_URL
     *  - UNKNOWN_URL
     *
     * @throws ApproovIOTransientException For NO_NETWORK, POOR_NETWORK or MITM_DETECTED status.
     * @throws ApproovIOFatalException     For any other status.
     *
     * return true or false depending on the Approov token fetch status.
     */
    private fun isToAddApproovHeader(status: Approov.TokenFetchStatus): Boolean {

        when (status) {
            SUCCESS -> {
                return true
            }
            NO_APPROOV_SERVICE -> {
                // Check why we consider it here as a valid status at https://approov.io/docs/v2.0/approov-usage-documentation/#token-fetch-errors
                return true
            }
            BAD_URL, UNKNOWN_URL -> {
                // This means that the given url is not registered as an API domain to be protected
                //  by Approov, therefore we will not consider valid, and as consequence the Approov
                //  token header will not be added into the request.
                return false
            }
            NO_NETWORK, POOR_NETWORK, MITM_DETECTED -> {
                // The developer needs to catch this transient error in order to retry the request.
                throw ApproovIOTransientException("Transient Error: $status")
            }
            else -> {
                // There has been some fatal error event that should be reported
                throw ApproovIOFatalException("Fatal Error: $status")
            }
        }
    }
}