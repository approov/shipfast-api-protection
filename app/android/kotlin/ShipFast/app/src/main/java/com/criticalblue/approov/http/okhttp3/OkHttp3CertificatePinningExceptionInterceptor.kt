/*****************************************************************************
 * Project:     Approov Kotlin Framework
 * Copyright(c) 2019 by CriticalBlue Ltd.
 *****************************************************************************/

package com.criticalblue.approov.http.okhttp3

import com.criticalblue.approov.exceptions.ApproovIOTransientException
import okhttp3.Interceptor
import okhttp3.Response
import javax.net.ssl.SSLPeerUnverifiedException

class OkHttp3CertificatePinningExceptionInterceptor : Interceptor {

    /**
     * Intercepts requests that throw a certificate pin exception, rebuilds the OkHttp3ClientBuilder,
     *  and then throws an ApproovIOTransientException.
     *
     * @throws ApproovIOTransientException When it catches a SSLPeerUnverifiedException.
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        try {
            return chain.proceed(chain.request())
        } catch (exception: SSLPeerUnverifiedException) {
            // We will rebuild the OkHttp3Client with the new certificate pins, that will be fetched
            //  from the Approov dynamic config.
            OkHttp3ClientBuilder.rebuild()

            throw ApproovIOTransientException("Transient Error: Certificate pin mismatch detected.")
        }
    }
}
