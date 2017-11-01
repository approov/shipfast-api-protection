/*****************************************************************************
 * Project:     ShipFast API Protection (App)
 * File:        ApproovRequestInterceptor.kt
 * Original:    Created on 30 Oct 2017 by Simon Rigg
 * Copyright(c) 2002 - 2017 by CriticalBlue Ltd.
 *
 * An HTTP request Interceptor used to add Approov tokens to requests.
 *****************************************************************************/

package com.criticalblue.shipfast

import com.criticalblue.attestationlibrary.ApproovAttestation
import okhttp3.Interceptor
import okhttp3.Response

/**
 * The ApproovRequestInterceptor class is responsible for intercepting HTTP
 * requests and adding the Approov token to the request in the form of a new
 * HTTP header named 'Approov-Token'.
 */
class ApproovRequestInterceptor : Interceptor {

    /**
     * Intercept the given request chain to add the Approov token to an 'Approov-Token' header.
     *
     * @param chain the request chain to modify
     * @return the modified response, authenticated by Approov
     */
    override fun intercept(chain: Interceptor.Chain): Response {

        val originalRequest = chain.request()
        val approovToken = ApproovAttestation.shared().fetchApproovTokenAndWait(originalRequest.url().toString()).token
        val approovRequest = originalRequest.newBuilder().addHeader("Approov-Token", approovToken).build()
        return chain.proceed(approovRequest)
    }
}