package com.criticalblue.approov

import android.util.Log
import com.criticalblue.approovsdk.Approov
import com.criticalblue.approov.ApproovSdk.TAG
import com.criticalblue.approov.dto.ApproovToken
import com.criticalblue.approovsdk.Approov.TokenFetchStatus.BAD_URL
import com.criticalblue.approovsdk.Approov.TokenFetchStatus.INTERNAL_ERROR
import com.criticalblue.approovsdk.Approov.TokenFetchStatus.MISSING_LIB_DEPENDENCY
import com.criticalblue.approovsdk.Approov.TokenFetchStatus.MITM_DETECTED
import com.criticalblue.approovsdk.Approov.TokenFetchStatus.NO_APPROOV_SERVICE
import com.criticalblue.approovsdk.Approov.TokenFetchStatus.NO_NETWORK
import com.criticalblue.approovsdk.Approov.TokenFetchStatus.NO_NETWORK_PERMISSION
import com.criticalblue.approovsdk.Approov.TokenFetchStatus.POOR_NETWORK
import com.criticalblue.approovsdk.Approov.TokenFetchStatus.SUCCESS
import com.criticalblue.approovsdk.Approov.TokenFetchStatus.UNKNOWN_URL
import com.criticalblue.approovsdk.Approov.TokenFetchStatus.UNPROTECTED_URL
import com.criticalblue.approov.exceptions.ApproovSdkFatalException
import com.criticalblue.approov.exceptions.ApproovSdkTemporaryException

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
        } catch (e: ApproovSdkTemporaryException) {
            Log.i(TAG, "ApproovSdkTemporaryException | " + e.message)
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
     *     type ApproovSdkTemporaryException or ApproovSdkFatalException.
     *  - returns the Approov token if we have a valid status.
     *  Alternatively use `this.parse()` to not have to deal with the exceptions.
     *
     * @throws ApproovSdkTemporaryException When is possible to recover from the error.
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

        // An ApproovSdkTemporaryException or ApproovSdkFatalException will be thrown, depending if
        //  the status is for a recoverable error or not.
        this.validateStatus(approovResult.status)

        return ApproovToken(approovResult.token, approovResult.isConfigChanged)
    }

    /**
     * Validates the status for the given Approov token fetch result.
     * When the status is not `SUCCESS` an exception of ApproovSdkTemporaryException or
     *  ApproovSdkFatalException will be thrown, depending if the error is recoverable or not.
     *
     * @throws ApproovSdkTemporaryException When is possible to recover from the error.
     * @throws ApproovSdkFatalException     When the error is not recoverable.
     *
     * @param status The Approov token fetch status, as per defined by Approov.TokenFetchStatus.
     */
    private fun validateStatus(status: Approov.TokenFetchStatus) {

        Log.i(TAG, "Approov Token Fetch Status: " + status.toString())

        when (status) {
            SUCCESS -> {
                return
            }
            NO_APPROOV_SERVICE -> {
                // We may want to add retry logic, once this should be a temporary error.
                throw ApproovSdkTemporaryException("Temporary error: Please retry!")
            }
            NO_NETWORK -> {
                // We may want to add retry logic, once this should be a temporary error.
                throw ApproovSdkTemporaryException("Temporary error: No Network available. Please retry when you got network signal again!")
            }
            POOR_NETWORK -> {
                // We may want to add retry logic, once this should be a temporary error.
                throw ApproovSdkTemporaryException("Temporary error: Poor Network signal. Please retry when you got a better network signal!")
            }
            MITM_DETECTED -> {
                // We may want to add retry logic, once this could be a temporary error.
                throw ApproovSdkTemporaryException("Temporary error: Man in the Middle Attack detected. If in a public wifi, disconnect and retry again with mobile data!")
            }
            BAD_URL -> {
                throw ApproovSdkFatalException("Fatal error: " + BAD_URL.toString())
            }
            UNKNOWN_URL -> {
                throw ApproovSdkFatalException("Fatal error: " + UNKNOWN_URL.toString())
            }
            UNPROTECTED_URL -> {
                throw ApproovSdkFatalException("Fatal error: " + UNPROTECTED_URL.toString())
            }
            NO_NETWORK_PERMISSION -> {
                throw ApproovSdkFatalException("Fatal error: " + NO_NETWORK_PERMISSION.toString())
            }
            MISSING_LIB_DEPENDENCY -> {
                throw ApproovSdkFatalException("Fatal error: " + MISSING_LIB_DEPENDENCY.toString())
            }
            INTERNAL_ERROR -> {
                throw ApproovSdkFatalException("Fatal error: " + INTERNAL_ERROR.toString())
            }
            else ->
                // There has been some error event that should be reported
                Log.i(TAG,"UNKNOWN ERROR OCCURRED FOR APPROOV RESULT STATUS: " + status.toString())
        }
    }
}
