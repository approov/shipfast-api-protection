package com.criticalblue.approov

import android.util.Log
import com.criticalblue.approovsdk.Approov
import com.criticalblue.approov.ApproovSdk.TAG
import com.criticalblue.approov.dto.ApproovToken
import com.criticalblue.approovsdk.Approov.TokenFetchStatus.MITM_DETECTED
import com.criticalblue.approovsdk.Approov.TokenFetchStatus.NO_APPROOV_SERVICE
import com.criticalblue.approovsdk.Approov.TokenFetchStatus.NO_NETWORK
import com.criticalblue.approovsdk.Approov.TokenFetchStatus.POOR_NETWORK
import com.criticalblue.approovsdk.Approov.TokenFetchStatus.SUCCESS
import com.criticalblue.approov.exceptions.ApproovSdkFatalException
import com.criticalblue.approov.exceptions.ApproovSdkTransientException

/**
 * This class is responsible to extract the Approov token from the Approov token fetch result object,
 *  and when the Approov dynamic configuration on it is marked as changed triggers an update.
 */
internal class ApproovTokenFetchResult(
    private val approovConfig: ApproovDynamicConfig
) {

    /**
     * Get the Approov token from the fetched result.
     * This calls `this.handle()` and returns an empty string when one of the two possible possible
     *  exceptions is thrown.
     * Alternatively use `this.handle()` to control what to do when an exceptions is thrown.
     *
     * @param approovTokenFetchResult The result object returned from the Approov SDK.
     *
     * @return The data object for the Approov Token.
     */
    fun parse(approovTokenFetchResult: Approov.TokenFetchResult): ApproovToken {

        try {
            return this.handle(approovTokenFetchResult)
        } catch (e: ApproovSdkTransientException) {
            Log.i(TAG, "ApproovSdkTransientException | " + e.message)
        } catch (e: ApproovSdkFatalException) {
            Log.i(TAG, "ApproovSdkFatalException | " + e.message)
        }

        return ApproovToken("", approovTokenFetchResult.isConfigChanged)
    }

    /**
     * Handles the Approov token fetch result:
     *  - Updates the Approov dynamic configuration when is marked as changed.
     *  - Validates the Approov token fetch result status, and throws an exception when the status
     *     represents a temporary or a fatal error. To distinguish them the exception can be of the
     *     type ApproovSdkTransientException or ApproovSdkFatalException.
     *  - returns the Approov token if we have a valid status.
     *  Alternatively use `this.parse()` to not have to deal with the exceptions.
     *
     * @throws ApproovSdkTransientException When is possible to recover from the error.
     * @throws ApproovSdkFatalException     When the error is not recoverable.
     *
     * @param approovTokenFetchResult The result object returned from the Approov SDK.
     *
     * @return The data object for the Approov Token.
     */
    fun handle(approovResult: Approov.TokenFetchResult): ApproovToken {

        // To support Over the Air(OTA) updates to the Approov dynamic configuration, on each
        //  Approov token fetch result we MUST check if we have a new one, and if so we need to
        //  persist it.
        if (approovResult.isConfigChanged) {
            approovConfig.saveApproovConfigUpdate()
        }

        // An ApproovSdkTransientException or ApproovSdkFatalException will be thrown, depending if
        //  the status is for a recoverable error or not.
        this.validateStatus(approovResult.status)

        return ApproovToken(approovResult.token, approovResult.isConfigChanged)
    }

    /**
     * Validates the status for the given Approov token fetch result.
     * When the status is not `SUCCESS` an exception of ApproovSdkTransientException or
     *  ApproovSdkFatalException will be thrown, depending if the error is recoverable or not.
     *
     * @throws ApproovSdkTransientException When is possible to recover from the error.
     * @throws ApproovSdkFatalException     When the error is not recoverable.
     *
     * @param status The Approov token fetch status, as per defined by Approov.TokenFetchStatus.
     */
    private fun validateStatus(status: Approov.TokenFetchStatus) {

        Log.i(TAG, "Approov Token Fetch Status: $status")

        when (status) {
            SUCCESS -> {
                return
            }
            NO_APPROOV_SERVICE -> {
                // Check why we consider it here as a valid status at https://approov.io/docs/v2.0/approov-usage-documentation/#token-fetch-errors
                return
            }
            NO_NETWORK, POOR_NETWORK, MITM_DETECTED -> {
                // We may want to add retry logic, once this should be a transient error.
                throw ApproovSdkTransientException("Transient error: $status")
            }

            else ->
                // There has been some fatal error event that should be reported
                throw ApproovSdkFatalException("Fatal Error: $status")
        }
    }
}
