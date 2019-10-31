package com.criticalblue.approov

import android.content.Context
import android.util.Log
import com.criticalblue.approov.dto.ApproovToken
import com.criticalblue.approovsdk.Approov
import com.criticalblue.approov.exceptions.ApproovSdkFatalException
import com.criticalblue.approov.http.okhttp3.OkHttp3ClientBuilder

/**
 * This singleton to wrap the Approov SDK, for initialization, getting certificate pins and fetch
 *  Approov tokens.
 */
object ApproovSdk {

    val TAG = "APPROOV_PACKAGE"
    private var initialised: Boolean = false
    private var config: ApproovDynamicConfig? = null

    /**
     * Initializes the Approov SDK with the given context, but only in the first time is called.
     *
     * @throws ApproovSdkFatalException When the error is not recoverable.
     *
     * @param context The application context.
     */
    fun initialize(context: Context) {

        // The Approov SDK cannot be initialize more than once.
        if (this.initialised) {
            return
        }

        if (this.config == null) {
            this.config = ApproovDynamicConfig(context.assets, context.openFileOutput("approov-dynamic.config", Context.MODE_PRIVATE))
        }

        try {
            Approov.initialize(context.applicationContext, this.config?.readInitialConfig(), this.config?.readDynamicConfig(), null)
            this.initialised = true
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Approov SDK initialization failed: " + e.message)

            // This is fatal, because if the Approov SDK cannot be initialized, all subsequent
            // attempts to use the it will fail.
            throw ApproovSdkFatalException("Approov SDK initialization failed.")
        }
    }

    /**
     * Get the certificate pins from the Approov SDK, and currently this means the pins for all API
     *  domains registered in the Approov account.
     * Aborts with an exception if the Approov SDK is not yet initialized.
     *
     * @throws ApproovSdkFatalException When the error is not recoverable.
     *
     * @return A list of all certificate pins for the API domains registered in the Approov account
     */
    fun getCertificatePins(): Map<String, List<String>> {
        this.abortWhenApproovSdkNotInitialized()
        return Approov.getPins("public-key-sha256")
    }

    /**
     * Fetch the Approov token synchronously.
     * Aborts with an exception if the Approov SDK is not yet initialized.
     *
     * @throws ApproovSdkFatalException When the error is not recoverable.
     *
     * @param url The url to get the Approov token for.
     *
     * @return The data object for the Approov Token.
     */
    fun fetchApproovTokenAndWait(url: String): ApproovToken {

        this.abortWhenApproovSdkNotInitialized()

        if (this.config == null) {
            throw ApproovSdkFatalException("Unexpected null object for the Approov dynamic configuration.")
        }

        val approovTokenFetchResult = Approov.fetchApproovTokenAndWait(url)
        val approovToken = ApproovTokenFetchResult(this.config!!).parse(approovTokenFetchResult)

        // When the Approov dynamic config changes, we may have new certificate pins, thus we need
        //  to trigger a rebuild of the OkHttp3Client.
        OkHttp3ClientBuilder.rebuildWithApproovWhenConfigChanges(approovToken)

        return approovToken
    }

    /**
     * Used internally to abort with an exception if the Approov SDK is not yet initialized.
     *
     * @throws ApproovSdkFatalException When the error is not recoverable.
     */
    private fun abortWhenApproovSdkNotInitialized() {
        if (!this.initialised) {
            throw ApproovSdkFatalException("Approov SDK is not initialized.")
        }
    }
}
