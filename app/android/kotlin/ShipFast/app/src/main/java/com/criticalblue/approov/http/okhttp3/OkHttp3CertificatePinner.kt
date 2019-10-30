package com.criticalblue.approov.http.okhttp3

import android.util.Log
import com.criticalblue.approov.ApproovSdk
import com.criticalblue.approov.ApproovSdk.TAG
import okhttp3.CertificatePinner

/**
 * A class to build the certificate pinner to use when building the OkHttp3 client.
 */
class OkHttp3CertificatePinner {

    private var pinBuilder: CertificatePinner.Builder = CertificatePinner.Builder()

    /**
     * Builds the `CertificatePinner` with all the pins returned from the Approov SDK.
     * Currently the pins returned are for all the API domains registered in the Approov account.
     *
     * @return CertificatePinner
     */
    fun build(): CertificatePinner {
        val pins = ApproovSdk.getCertificatePins()

        for ((key, value) in pins) {
            for (pin in value) {
                this.pinBuilder.add(key, "sha256/$pin")
                Log.i(TAG, "Adding to OkHttp the pin $key:sha256/$pin")
            }
        }

        return this.pinBuilder.build()
    }
}